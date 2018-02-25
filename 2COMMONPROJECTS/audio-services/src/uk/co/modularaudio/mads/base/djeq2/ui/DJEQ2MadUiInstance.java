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

package uk.co.modularaudio.mads.base.djeq2.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadDefinition;
import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2MadInstance;
import uk.co.modularaudio.mads.base.djeq2.mu.DJEQ2IOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class DJEQ2MadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<DJEQ2MadDefinition, DJEQ2MadInstance>
	implements IOQueueEventUiConsumer<DJEQ2MadInstance>
{
	private static Log log = LogFactory.getLog( DJEQ2MadUiInstance.class.getName() );

	private long framesBetweenPeakReset;

	private float curHighAmp = 1.0f;
	private boolean curHighKilled = false;

	private float curLowAmp = 1.0f;
	private boolean curLowKilled = false;

	private DjEQ2LaneStereoAmpMeter meter;

	public DJEQ2MadUiInstance( final DJEQ2MadInstance instance,
			final DJEQ2MadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup( ratesAndLatency, timingParameters, frameTimeFactory );

		// Use the sample rate (i.e. one second between peak reset)
		framesBetweenPeakReset = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final int U_currentGuiTick,
			final int framesSinceLastTick )
	{
		// Process incoming queue messages before we let the controls have a chance to process;
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );

		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, U_currentGuiTick, framesSinceLastTick );
	}

	@Override
	public void consumeQueueEntry( final DJEQ2MadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case DJEQ2IOQueueBridge.COMMAND_OUT_METER_READINGS:
			{
				final int U_timestamp = nextOutgoingEntry.U_frameTime;
				final long val = nextOutgoingEntry.value;
				final int lower32 = (int)(val & 0xFFFFFFFF);
				final int upper32 = (int)((val >> 32) & 0xFFFFFFFF);
				final float rMeter = Float.intBitsToFloat( lower32 );
				final float lMeter = Float.intBitsToFloat( upper32 );
				final float lMeterDb = AudioMath.levelToDbF( lMeter );
				final float rMeterDb = AudioMath.levelToDbF( rMeter );
				meter.receiveMeterReadingInDb( U_timestamp, 0, lMeterDb );
				meter.receiveMeterReadingInDb( U_timestamp, 1, rMeterDb );
				break;
			}
			default:
			{
				final String msg = "Unknown command to guI: " + nextOutgoingEntry.command;
				log.error( msg );
			}
		}
	}

	public long getFramesBetweenPeakReset()
	{
		return framesBetweenPeakReset;
	}

	public void setHighAmp( final float actualValue )
	{
		if( actualValue != curHighAmp )
		{
			curHighAmp = actualValue;
			recalculateHigh();
		}
	}

	public void setHighKilled( final boolean killed )
	{
		if( curHighKilled != killed )
		{
			curHighKilled = killed;
			recalculateHigh();
		}
	}

	private void recalculateHigh()
	{
		final float ampToSend = ( curHighKilled ? 0.0f : curHighAmp );
		final long lValue = Float.floatToIntBits( ampToSend );
		sendTemporalValueToInstance( DJEQ2IOQueueBridge.COMMAND_IN_HP_AMP, lValue );
	}

	public void setLowAmp( final float actualValue )
	{
		if( actualValue != curLowAmp )
		{
			curLowAmp = actualValue;
			recalculateLow();
		}
	}

	public void setLowKilled( final boolean killed )
	{
		if( curLowKilled != killed )
		{
			curLowKilled = killed;
			recalculateLow();
		}
	}

	private void recalculateLow()
	{
		final float ampToSend = ( curLowKilled ? 0.0f : curLowAmp );
		final long lValue = Float.floatToIntBits( ampToSend );
		sendTemporalValueToInstance( DJEQ2IOQueueBridge.COMMAND_IN_LP_AMP, lValue );
	}

	public void setFaderAmp( final float faderAmp )
	{
		final long lValue = Float.floatToIntBits( faderAmp );
		sendTemporalValueToInstance( DJEQ2IOQueueBridge.COMMAND_IN_FADER_AMP, lValue );
	}

	public void setStereoAmpMeter( final DjEQ2LaneStereoAmpMeter meter )
	{
		this.meter = meter;
	}

	public void sendUiActive( final boolean active )
	{
		sendCommandValueToInstance( DJEQ2IOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0) );
	}
}
