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

package uk.co.modularaudio.mads.base.imixern.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNIOQueueBridge;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.lane.MeterValueReceiver;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.table.Span;

public class MixerNMadUiInstance<D extends MixerNMadDefinition<D,I>, I extends MixerNMadInstance<D,I>>
	extends AbstractNoNameChangeNonConfigurableMadUiInstance<D, I>
{
	private static Log log = LogFactory.getLog( MixerNMadUiInstance.class.getName() );

	private final MeterValueReceiver[] laneMeterReceiversMap;
	private MeterValueReceiver masterMeterReceiver;

	public MixerNMadUiInstance( final Span span,
			final I instance,
			final MadUiDefinition<D, I> componentUiDefinition )
	{
		super( span, instance, componentUiDefinition );

		laneMeterReceiversMap = new MeterValueReceiver[ instance.getDefinition().getMixerInstanceConfiguration().getNumMixerLanes() ];
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup( ratesAndLatency, timingParameters, frameTimeFactory );

		// Use the sample rate (i.e. one second between peak reset)
		final int framesBetweenPeakReset = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();

		for( int i = 0 ; i < laneMeterReceiversMap.length ; ++i )
		{
			laneMeterReceiversMap[i].setFramesBetweenPeakReset( framesBetweenPeakReset );
		}
		masterMeterReceiver.setFramesBetweenPeakReset( framesBetweenPeakReset );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		// Consume any incoming messages from the instance before we pass the tick onto any children
		localQueueBridge.receiveQueuedEventsToUi( guiEventStorage, instance, this );
//		log.debug("Consumed " + numMessagesConsumed + " messages");
//		debugTimestamp( "UI Mixer", currentGuiTime );

		super.doDisplayProcessing( guiEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void consumeQueueEntry( final I instance, final IOQueueEvent nextOutgoingEntry )
	{
//		log.debug("Consuming one");
		switch( nextOutgoingEntry.command )
		{
			case MixerNIOQueueBridge.COMMAND_OUT_LANE_METER:
			{
				// float
				final long value = nextOutgoingEntry.value;
				final int laneChanNum = (int)((value ) & 0xFFFFFFFF);
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );

				final int laneNum = laneChanNum / 2;
				final int channelNum = laneChanNum % 2;

				final MeterValueReceiver laneReceiver = laneMeterReceiversMap[ laneNum ];
				if( laneReceiver == null )
				{
					if( log.isWarnEnabled() )
					{
						log.warn( "Missing meter receiver for lane " + laneNum );
					}
				}
				else
				{
					laneReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, channelNum, ampValue );
				}
				break;
			}
			case MixerNIOQueueBridge.COMMAND_OUT_MASTER_METER:
			{
				final long value = nextOutgoingEntry.value;
				final int laneChanNum = (int)((value ) & 0xFFFFFFFF);
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );

				final int channelNum = laneChanNum % 2;

				if( masterMeterReceiver == null )
				{
					log.warn( "Missing master meter receiver");
				}
				else
				{
					masterMeterReceiver.receiveMeterReadingLevel( nextOutgoingEntry.frameTime, channelNum, ampValue );
				}
				break;
			}
			case MixerNIOQueueBridge.COMMAND_IN_LANE_AMP:
			case MixerNIOQueueBridge.COMMAND_IN_LANE_MUTE:
			case MixerNIOQueueBridge.COMMAND_IN_LANE_PAN:
			case MixerNIOQueueBridge.COMMAND_IN_LANE_SOLO:
			{
				// Ignore them.
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Unknown outgoing command: " + nextOutgoingEntry.command );
				}
				break;
			}
		}
	}

	public void registerLaneMeterReceiver( final int laneNum, final MeterValueReceiver meterReceiver )
	{
		laneMeterReceiversMap[ laneNum ] = meterReceiver;
	}

	public void registerMasterMeterReceiver( final MeterValueReceiver meterReceiver )
	{
		masterMeterReceiver = meterReceiver;
	}

	public void sendLaneMute( final int laneNumber, final boolean muteValue )
	{
		final long muteBits = (muteValue ? 1 : 0 );
		final long joinedParts = (muteBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_LANE_MUTE, joinedParts );
	}

	public void sendSoloValue( final int laneNumber, final boolean soloValue )
	{
		final long soloBits = ( soloValue ? 1 : 0 );
		final long joinedParts = ( soloBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_LANE_SOLO, joinedParts );
	}

	public void sendLaneAmp( final int laneNumber, final float newValue )
	{
		final long floatIntBits = Float.floatToIntBits( newValue );
		final long joinedParts = (floatIntBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_LANE_AMP, joinedParts );
	}

	public void sendMasterAmp( final float newValue )
	{
		final int floatIntBits = Float.floatToIntBits( newValue );
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_MASTER_AMP,  floatIntBits );
	}

	public void sendUiActive( final boolean active )
	{
		sendCommandValueToInstance( MixerNIOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0 ) );
		if( active )
		{
			// Reset meters to zero so they start as intended
			for( int i = 0 ; i < laneMeterReceiversMap.length ; ++i )
			{
				laneMeterReceiversMap[i].receiveMeterReadingLevel( 0, 0, 0.0f );
				laneMeterReceiversMap[i].receiveMeterReadingLevel( 0, 1, 0.0f );
			}
			masterMeterReceiver.receiveMeterReadingLevel( 0, 0, 0.0f );
			masterMeterReceiver.receiveMeterReadingLevel( 0, 1, 0.0f );
		}
	}

	public void sendLanePan( final int laneNumber, final float panValue )
	{
		final long floatIntBits = Float.floatToIntBits( panValue );
		final long joinedParts = (floatIntBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_LANE_PAN, joinedParts);
	}

	public void sendMasterPan( final float panValue )
	{
		final long floatIntBits = Float.floatToIntBits( panValue );
		sendTemporalValueToInstance( MixerNIOQueueBridge.COMMAND_IN_MASTER_PAN, floatIntBits);
	}

}
