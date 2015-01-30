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

package uk.co.modularaudio.mads.base.mixer.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

import uk.co.modularaudio.mads.base.mixer.mu.MixerIOQueueBridge;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstance;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstanceConfiguration;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.table.Span;

public class MixerMadUiInstance extends AbstractNoNameChangeConfigurableMadUiInstance<MixerMadDefinition, MixerMadInstance>
	implements IOQueueEventUiConsumer<MixerMadInstance>
{
	private static Log log = LogFactory.getLog( MixerMadUiInstance.class.getName() );

	private final OpenIntObjectHashMap<MeterValueReceiver> laneMeterReceiversMap = new OpenIntObjectHashMap<MeterValueReceiver>();
	private MeterValueReceiver masterMeterReceiver;

//	private long audioIOLatencyNanos = 0;

	public MixerMadUiInstance( final MixerMadInstance instance,
			final MixerMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition, calculateSpanForInstanceConfiguration( instance.getInstanceConfiguration() ) );
	}

	private static Span calculateSpanForInstanceConfiguration( final MixerMadInstanceConfiguration instanceConfiguration )
	{
		final int numInputLanes = instanceConfiguration.getNumInputLanes();
//		int numChannelsPerLane = instanceConfiguration.getNumChannelsPerLane();
		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();
//		int numTotalChannels = instanceConfiguration.getNumTotalChannels();

		final int startXOffset = MixerMadUiDefinition.INPUT_LANES_START.x;

		final int numChannelsWidth = numInputLanes * MixerMadUiDefinition.LANE_TO_LANE_INCREMENT +
				MixerMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		final int outputChannelsWidth = numOutputChannels * MixerMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;

		final int totalWidth = startXOffset + numChannelsWidth + MixerMadUiDefinition.INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
				outputChannelsWidth;

		final int totalHeight = MixerMadUiDefinition.INPUT_LANES_START.y + 20;

		final int numCellsWide = (totalWidth / GuiRackPanel.FRONT_GRID_SIZE.width) + (totalWidth % GuiRackPanel.FRONT_GRID_SIZE.width > 0 ? 1 : 0 );
		final int numCellsHigh = (totalHeight / GuiRackPanel.FRONT_GRID_SIZE.height) + (totalHeight % GuiRackPanel.FRONT_GRID_SIZE.height > 0 ? 1 : 0 );

		return new Span( numCellsWide, numCellsHigh );
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
	public void consumeQueueEntry( final MixerMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
//		log.debug("Consuming one");
		switch( nextOutgoingEntry.command )
		{
			case MixerIOQueueBridge.COMMAND_OUT_LANE_METER:
			{
				// float
				final long value = nextOutgoingEntry.value;
				final int laneChanNum = (int)((value ) & 0xFFFFFFFF);
				final int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				final float ampValue = Float.intBitsToFloat( upper32Bits );

				final int laneNum = laneChanNum / 2;
				final int channelNum = laneChanNum % 2;

				final MeterValueReceiver laneReceiver = laneMeterReceiversMap.get( laneNum );
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
			case MixerIOQueueBridge.COMMAND_OUT_MASTER_METER:
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
			case MixerIOQueueBridge.COMMAND_OUT_LANE_MUTE_SET:
			{
//				long value = nextOutgoingEntry.value;
//				int laneChanNum = (int)((value ) & 0xFFFFFFFF);
//				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
//				boolean muted = (upper32Bits != 0);

//				laneMeterReceiversMap.get( laneChanNum ).receiveMuteSet( nextOutgoingEntry.frameTime, muted );
				break;
			}
			case MixerIOQueueBridge.COMMAND_OUT_LANE_SOLO_SET:
			{
//				long value = nextOutgoingEntry.value;
//				int laneChanNum = (int)((value ) & 0xFFFFFFFF);
//				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
//				boolean solod = (upper32Bits != 0);

//				laneMeterReceiversMap.get( laneChanNum ).receiveSoloSet( nextOutgoingEntry.frameTime, solod );
				break;
			}
			case MixerIOQueueBridge.COMMAND_IN_LANE_AMP:
			case MixerIOQueueBridge.COMMAND_IN_LANE_MUTE:
			case MixerIOQueueBridge.COMMAND_IN_LANE_PAN:
			case MixerIOQueueBridge.COMMAND_IN_LANE_SOLO:
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
		laneMeterReceiversMap.put( laneNum, meterReceiver );
	}

	public void registerMasterMeterReceiver( final MeterValueReceiver meterReceiver )
	{
		masterMeterReceiver = meterReceiver;
	}

	public void sendLaneMute( final int laneNumber, final boolean muteValue )
	{
		final long muteBits = (muteValue ? 1 : 0 );
		final long joinedParts = (muteBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_LANE_MUTE, joinedParts );
	}

	public void sendSoloValue( final int laneNumber, final boolean soloValue )
	{
		final long soloBits = ( soloValue ? 1 : 0 );
		final long joinedParts = ( soloBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_LANE_SOLO, joinedParts );
	}

	public void sendLaneAmp( final int laneNumber, final float newValue )
	{
		final long floatIntBits = Float.floatToIntBits( newValue );
		final long joinedParts = (floatIntBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_LANE_AMP, joinedParts );
	}

	public void sendMasterAmp( final float newValue )
	{
		final int floatIntBits = Float.floatToIntBits( newValue );
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_MASTER_AMP,  floatIntBits );
	}

	public void sendUiActive( final boolean active )
	{
		sendCommandValueToInstance( MixerIOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0 ) );
	}

	public void sendLanePan( final int laneNumber, final float panValue )
	{
		final long floatIntBits = Float.floatToIntBits( panValue );
		final long joinedParts = (floatIntBits << 32) | laneNumber;
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_LANE_PAN, joinedParts);
	}

	public void sendMasterPan( final float panValue )
	{
		final long floatIntBits = Float.floatToIntBits( panValue );
		sendTemporalValueToInstance( MixerIOQueueBridge.COMMAND_IN_MASTER_PAN, floatIntBits);
	}
}
