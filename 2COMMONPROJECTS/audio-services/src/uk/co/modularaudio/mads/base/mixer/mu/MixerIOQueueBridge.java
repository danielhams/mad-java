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

package uk.co.modularaudio.mads.base.mixer.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class MixerIOQueueBridge extends
		MadLocklessQueueBridge<MixerMadInstance>
{
	private static Log log = LogFactory.getLog( MixerIOQueueBridge.class.getName() );
	
	public static final int COMMAND_IN_ACTIVE = 0;
	
	// Channel amp - lower 32 bits are channel num, top 32 is float amp
	public final static int COMMAND_IN_LANE_AMP = 1;
	public static final int COMMAND_IN_LANE_PAN = 2;
	public static final int COMMAND_IN_LANE_MUTE = 3;
	public static final int COMMAND_IN_LANE_SOLO = 4;
	
	// Just lower 32 bits float amp
	public final static int COMMAND_IN_MASTER_AMP = 5;
	public final static int COMMAND_IN_MASTER_PAN = 6;

	// Outgoing messages
	public static final int COMMAND_OUT_LANE_METER = 7;
	public static final int COMMAND_OUT_LANE_MUTE_SET = 8;
	public static final int COMMAND_OUT_LANE_SOLO_SET = 9;

	public static final int COMMAND_OUT_MASTER_METER = 10;

	// Extra capacity for messages
	private static final int CUSTOM_COMMAND_TO_UI_QUEUE_LENGTH = MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH * 2;

	public MixerIOQueueBridge()
	{
		super( MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH,
				MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH,
				CUSTOM_COMMAND_TO_UI_QUEUE_LENGTH,
				MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH );
	}

	@Override
	public void receiveQueuedEventsToInstance( MixerMadInstance instance,
			ThreadSpecificTemporaryEventStorage tses,
			long currentTimestamp,
			IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				boolean isActive = (queueEntry.value == 1 );
				instance.setActive( isActive );
				break;
			}
			case COMMAND_IN_LANE_AMP:
			{
				// float
				long value = queueEntry.value;
				int lower32Bits = (int)((value ) & 0xFFFFFFFF);
				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				float ampValue = Float.intBitsToFloat( upper32Bits );
//				log.debug("Received lane amp change " + lower32Bits + ", " + ampValue );
				instance.setLaneAmp( lower32Bits, ampValue );
				break;
			}
			case COMMAND_IN_LANE_PAN:
			{
				// float
				long value = queueEntry.value;
				int lower32Bits = (int)((value ) & 0xFFFFFFFF);
				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				float panValue = Float.intBitsToFloat( upper32Bits );
//				log.debug("Received lane amp change " + lower32Bits + ", " + ampValue );
				instance.setLanePan( lower32Bits, panValue );
				break;
			}
			case COMMAND_IN_LANE_MUTE:
			{
				long value = queueEntry.value;
				int laneNumber = (int)((value ) & 0xFFFFFFFF);
				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				boolean muteValue = ( upper32Bits != 0);
				instance.setLaneMute( tses, currentTimestamp, laneNumber, muteValue );
				break;
			}
			case COMMAND_IN_LANE_SOLO:
			{
				long value = queueEntry.value;
				int laneNumber = (int)((value ) & 0xFFFFFFFF);
				int upper32Bits = (int)((value >> 32 ) & 0xFFFFFFFF);
				boolean soloValue = ( upper32Bits != 0);
				instance.setLaneSolo( tses, currentTimestamp, laneNumber, soloValue );
				break;
			}
			case COMMAND_IN_MASTER_AMP:
			{
				// float
				long value = queueEntry.value;
				int truncVal = (int)value;
				float masterAmp = Float.intBitsToFloat( truncVal );
				instance.setMasterAmp( masterAmp );
//				log.debug("Received master amp change at " + currentTimestamp );
//				debugTimestamp("RecAm", currentTimestamp );
				break;
			}
			case COMMAND_IN_MASTER_PAN:
			{
				// float
				long value = queueEntry.value;
				float panValue = Float.intBitsToFloat( (int)value );
//				log.debug("Received lane amp change " + lower32Bits + ", " + ampValue );
				instance.setMasterPan( panValue );
				break;
			}
			default:
			{
				String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
			}
		}
	}
}
