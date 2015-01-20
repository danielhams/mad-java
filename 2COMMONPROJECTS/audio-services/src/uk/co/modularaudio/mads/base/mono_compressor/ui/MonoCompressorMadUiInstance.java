/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadInstance;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class MonoCompressorMadUiInstance
	extends AbstractNonConfigurableMadUiInstance<MonoCompressorMadDefinition, MonoCompressorMadInstance>
	implements IOQueueEventUiConsumer<MonoCompressorMadInstance>
{
	private static Log log = LogFactory.getLog( MonoCompressorMadUiInstance.class.getName() );
	
	// Reset every second
	private final static int MILLIS_BETWEEN_PEAK_RESET = 1000;
	
//	private FastSet<TimescaleChangeListener> timescaleChangeListeners = new FastSet<TimescaleChangeListener>();
	private List<GateListener> gateListeners = new ArrayList<GateListener>();

	public float guiDesiredAttackMillis = 0.0f;

	private MeterValueReceiver sourceSignalValueReceiver = null;
	private MeterValueReceiver outSignalValueReceiver = null;
	private ThresholdValueReceiver thresholdValueReceiver = null;
	private float lastThresholdDb = Float.NEGATIVE_INFINITY;
	private MeterValueReceiver envSignalValueReceiver = null;
	private MeterValueReceiver attenuationSignalValueReceiver = null;
	
	// One every second at 44.1
	// Should be reset on "startup"
	protected int framesBetweenPeakReset = 44100;

	public MonoCompressorMadUiInstance( MonoCompressorMadInstance instance,
			MonoCompressorMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}
	
	public void sendOneCurveAsFloat( int command,
			float guiDesiredValue )
	{
		long value = (long)(Float.floatToIntBits( guiDesiredValue ) );
		sendTemporalValueToInstance(command, value);
		propogateChange( command, guiDesiredValue );
	}
	
	private void propogateChange( int command, float value )
	{
		for( int i = 0; i < gateListeners.size() ; i++)
		{
			GateListener l = gateListeners.get( i );
			l.receiveChange( command, value );
		}
	}
	
	public void addGateListener( GateListener l )
	{
		gateListeners.add( l );
	}
	
	public void removeGateListener( GateListener l )
	{
		gateListeners.remove( l );
	}

	@Override
	public void doDisplayProcessing(
			ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		// Receive any events from the instance first
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );
		
		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( MonoCompressorMadInstance instance,
			IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case MonoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER:
			{
				long value = nextOutgoingEntry.value;
				float ampValue = Float.intBitsToFloat( (int)value );
				if( sourceSignalValueReceiver != null )
				{
					sourceSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, ampValue );
				}
				break;
			}
			case MonoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER:
			{
				long value = nextOutgoingEntry.value;
				float ampValue = Float.intBitsToFloat( (int)value );
				if( outSignalValueReceiver != null )
				{
					outSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, ampValue );
				}
				break;
			}
			case MonoCompressorIOQueueBridge.COMMAND_OUT_ENV_VALUE:
			{
				long value = nextOutgoingEntry.value;
				float ampValue = Float.intBitsToFloat( (int)value );
				if( envSignalValueReceiver != null )
				{
					envSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, ampValue );
				}
				break;
			}
			case MonoCompressorIOQueueBridge.COMMAND_OUT_ATTENUATION:
			{
				long value = nextOutgoingEntry.value;
				float ampValue = Float.intBitsToFloat( (int)value );
				if( attenuationSignalValueReceiver != null )
				{
					attenuationSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, ampValue );
				}
				break;
			}
			default:
			{
				String msg = "Unknown command receive for UI: " + nextOutgoingEntry.command;
				log.error( msg );
				break;
			}
		}
		
	}

	public void sendOneCurve( int command, float guiDesiredValue )
	{
		long value = (long)(Float.floatToIntBits( guiDesiredValue ) );
		sendTemporalValueToInstance(command, value);
	}

	public void registerSourceSignalMeterValueReceiver( MeterValueReceiver meterValueReceiver )
	{
		this.sourceSignalValueReceiver  = meterValueReceiver;
	}
	
	public void registerOutSignalMeterValueReceiver( MeterValueReceiver meterValueReceiver )
	{
		this.outSignalValueReceiver  = meterValueReceiver;
	}
	
	public void registerThresholdValueReceiver( ThresholdValueReceiver thresholdValueReceiver )
	{
		this.thresholdValueReceiver = thresholdValueReceiver;
		thresholdValueReceiver.receiveNewDbValue( lastThresholdDb );
	}
	
	public void registerEnvSignalMeterValueReceiver( MeterValueReceiver meterValueReceiver )
	{
		this.envSignalValueReceiver = meterValueReceiver;
	}

	public void registerAttenuationSignalMeterValueReceiver( MeterValueReceiver meterValueReceiver )
	{
		this.attenuationSignalValueReceiver = meterValueReceiver;
	}

	public void sendUiActive( boolean showing )
	{
		sendTemporalValueToInstance( MonoCompressorIOQueueBridge.COMMAND_IN_ACTIVE,
			( showing ? 1 : 0 ) );
	}

	public void emitThresholdChange( float newValue )
	{
		lastThresholdDb = newValue;
		if( thresholdValueReceiver != null )
		{
			thresholdValueReceiver.receiveNewDbValue( newValue );
//			log.debug("Passed threshold change to receiver: " + newValue );
		}
	}

	public void sendLookahead( boolean selected )
	{
		sendTemporalValueToInstance( MonoCompressorIOQueueBridge.COMMAND_IN_LOOKAHEAD,
				(selected ? 1 : 0 ) );
	}

	@Override
	public void receiveStartup(HardwareIOChannelSettings ratesAndLatency,
			MadTimingParameters timingParameters,
			MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		framesBetweenPeakReset = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( ratesAndLatency.getAudioChannelSetting().getDataRate().getValue(),
				MILLIS_BETWEEN_PEAK_RESET );
	}

	public void updateThresholdType( int thresholdType )
	{
		sendTemporalValueToInstance( MonoCompressorIOQueueBridge.COMMAND_IN_THRESHOLD_TYPE, thresholdType );
	}
}
