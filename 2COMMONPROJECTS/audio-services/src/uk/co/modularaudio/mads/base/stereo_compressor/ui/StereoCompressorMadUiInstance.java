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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorIOQueueBridge;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class StereoCompressorMadUiInstance
	extends AbstractNoNameChangeNonConfigurableMadUiInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance>
	implements IOQueueEventUiConsumer<StereoCompressorMadInstance>
{
	private static Log log = LogFactory.getLog( StereoCompressorMadUiInstance.class.getName() );

	// Reset every second
	private final static int MILLIS_BETWEEN_PEAK_RESET = 1000;

//	public long audioIOLatencyNanos = 0;

//	private FastSet<TimescaleChangeListener> timescaleChangeListeners = new FastSet<TimescaleChangeListener>();
	private final List<GateListener> gateListeners = new ArrayList<GateListener>();

	public float guiDesiredAttackMillis = 0.0f;

	private MeterValueReceiver sourceSignalValueReceiver;
	private MeterValueReceiver outSignalValueReceiver;
	private ThresholdValueReceiver thresholdValueReceiver;
	private float lastThresholdDb = Float.NEGATIVE_INFINITY;
	private MeterValueReceiver envSignalValueReceiver;
	private MeterValueReceiver attenuationSignalValueReceiver;

	// One every second at 44.1
	// Should be reset on "startup"
	protected int framesBetweenPeakReset = 44100;

	public StereoCompressorMadUiInstance( final StereoCompressorMadInstance instance,
			final StereoCompressorMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendOneCurveAsFloat( final int command,
			final float guiDesiredValue )
	{
		final long value = (Float.floatToIntBits( guiDesiredValue ) );
		sendTemporalValueToInstance(command, value);
		propogateChange( command, guiDesiredValue );
	}

	private void propogateChange( final int command, final float value )
	{
		for( int i = 0; i < gateListeners.size() ; i++)
		{
			final GateListener l = gateListeners.get( i );
			l.receiveChange( command, value );
		}
	}

	public void addGateListener( final GateListener l )
	{
		gateListeners.add( l );
	}

	public void removeGateListener( final GateListener l )
	{
		gateListeners.remove( l );
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiFrameTime )
	{
		// Receive any events from the instance first
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );

		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiFrameTime );
	}

	@Override
	public void consumeQueueEntry(
			final StereoCompressorMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER:
			{
				final long value = nextOutgoingEntry.value;
				final int chanNum = (int)((value ) & 0xFFFFFFFF);
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );
				if( sourceSignalValueReceiver != null )
				{
					sourceSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, chanNum, ampValue );
				}
				break;
			}
			case StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER:
			{
				final long value = nextOutgoingEntry.value;
				final int chanNum = (int)((value ) & 0xFFFFFFFF);
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );
				if( outSignalValueReceiver != null )
				{
					outSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, chanNum, ampValue );
				}
				break;
			}
			case StereoCompressorIOQueueBridge.COMMAND_OUT_ENV_VALUE:
			{
				final long value = nextOutgoingEntry.value;
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );
				if( envSignalValueReceiver != null )
				{
					envSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, 0, ampValue );
				}
				break;
			}
			case StereoCompressorIOQueueBridge.COMMAND_OUT_ATTENUATION:
			{
				final long value = nextOutgoingEntry.value;
				final int upper32Bits = (int)(value);
				final float ampValue = Float.intBitsToFloat( upper32Bits );
				if( attenuationSignalValueReceiver != null )
				{
					attenuationSignalValueReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, 0, ampValue );
				}
				break;
			}
			default:
			{
				final String msg = "Unknown command receive for UI: " + nextOutgoingEntry.command;
				log.error( msg );
				break;
			}
		}

	}

	public void sendThreshold( final float desiredThreshold )
	{
		final long value = Float.floatToIntBits( desiredThreshold );
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_THRESHOLD, value );
		thresholdValueReceiver.receiveNewDbValue( desiredThreshold );
	}

	public void sendRatio( final float desiredRatio )
	{
		final long value = Float.floatToIntBits( desiredRatio );
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_RATIO, value );
	}

	public void sendAttack( final float desiredAttack )
	{
		final long value = Float.floatToIntBits( desiredAttack );
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_ATTACK_MILLIS, value );
	}

	public void sendRelease( final float desiredRelease )
	{
		final long value = Float.floatToIntBits( desiredRelease );
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_RELEASE_MILLIS, value );
	}

	public void sendGain( final float desiredGain )
	{
		final long value = Float.floatToIntBits( desiredGain );
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_MAKEUP_GAIN, value );
	}

	public void sendLookahead( final boolean selected )
	{
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_LOOKAHEAD,
			(selected ? 1 : 0 ) );
	}

	public void sendThresholdType( final int thresholdType )
	{
		sendTemporalValueToInstance(StereoCompressorIOQueueBridge.COMMAND_IN_THRESHOLD_TYPE, thresholdType);
	}

	public void registerSourceSignalMeterValueReceiver( final MeterValueReceiver meterValueReceiver )
	{
		this.sourceSignalValueReceiver  = meterValueReceiver;
	}

	public void registerOutSignalMeterValueReceiver( final MeterValueReceiver meterValueReceiver )
	{
		this.outSignalValueReceiver  = meterValueReceiver;
	}

	public void registerThresholdValueReceiver( final ThresholdValueReceiver thresholdValueReceiver )
	{
		this.thresholdValueReceiver = thresholdValueReceiver;
		thresholdValueReceiver.receiveNewDbValue( lastThresholdDb );
	}

	public void registerEnvSignalMeterValueReceiver( final MeterValueReceiver meterValueReceiver )
	{
		this.envSignalValueReceiver = meterValueReceiver;
	}

	public void registerAttenuationSignalMeterValueReceiver( final MeterValueReceiver meterValueReceiver )
	{
		this.attenuationSignalValueReceiver = meterValueReceiver;
	}

	public void sendUiActive( final boolean showing )
	{
		sendTemporalValueToInstance( StereoCompressorIOQueueBridge.COMMAND_IN_ACTIVE,
			( showing ? 1 : 0 ) );
	}

	public void emitThresholdChange( final float newValue )
	{
		lastThresholdDb = newValue;
		if( thresholdValueReceiver != null )
		{
			thresholdValueReceiver.receiveNewDbValue( newValue );
//			log.debug("Passed threshold change to receiver: " + newValue );
		}
	}

	@Override
	public void receiveStartup(final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		framesBetweenPeakReset = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( ratesAndLatency.getAudioChannelSetting().getDataRate().getValue(),
				MILLIS_BETWEEN_PEAK_RESET );
	}
}
