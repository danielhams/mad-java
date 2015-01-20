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

package uk.co.modularaudio.mads.base.soundfile_player.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class SoundfilePlayerIOQueueBridge extends MadLocklessQueueBridge<SoundfilePlayerMadInstance>
{
	private static Log log = LogFactory.getLog( SoundfilePlayerIOQueueBridge.class.getName() );
	
	public static final int COMMAND_IN_ACTIVE = 1;
	public static final int COMMAND_IN_PLAYING_STATE = 2;
	public static final int COMMAND_IN_PLAY_SPEED = 3;
	public static final int COMMAND_IN_RESAMPLED_SAMPLE = 4;
	public static final int COMMAND_IN_SHUTTLE_REWIND_TO_START = 5;
	public static final int COMMAND_IN_SHUTTLE_FFWD_TO_END = 6;
	public static final int COMMAND_IN_SHUTTLE_REWIND_BEGIN = 7;
	public static final int COMMAND_IN_SHUTTLE_REWIND_END = 8;
	public static final int COMMAND_IN_SHUTTLE_FFWD_BEGIN = 9;
	public static final int COMMAND_IN_SHUTTLE_FFWD_END = 10;
	public static final int COMMAND_IN_SHUTTLE_SET_POSITION_END = 11;
	
	public static final int COMMAND_OUT_RECYCLE_SAMPLE = 16;
	
	public static final int COMMAND_OUT_STATE_CHANGE = 17;
	public static final int COMMAND_OUT_CURRENT_SAMPLE = 18;
	public static final int COMMAND_OUT_FRAME_POSITION_ABS = 19;
	public static final int COMMAND_OUT_FRAME_POSITION_DELTA = 20;
	
	public SoundfilePlayerIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( SoundfilePlayerMadInstance instance, ThreadSpecificTemporaryEventStorage tses, long periodTimestamp, IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				boolean active = ( queueEntry.value == 1 );
				instance.active = active;
				break;
			}
			case COMMAND_IN_PLAYING_STATE:
			{
				int value = (int)queueEntry.value;
				SoundfilePlayerMadInstance.PlayingState desiredState = SoundfilePlayerMadInstance.PlayingState.values()[ value ];
				instance.setDesiredState( desiredState );
				break;
			}
			case COMMAND_IN_PLAY_SPEED:
			{
				float value = Float.intBitsToFloat(((int)queueEntry.value));
				instance.setDesiredPlaySpeed( value );
				break;
			}
			case COMMAND_IN_RESAMPLED_SAMPLE:
			{
				BlockResamplingClient prevSample = instance.getResampledSample();
				BlockResamplingClient resampledSample = (BlockResamplingClient)queueEntry.object;
				instance.setResampledSample( resampledSample );
				if( prevSample != null )
				{
					// Get the UI to clean up the previously used one
					queueCommandEventToUi(tses, COMMAND_OUT_RECYCLE_SAMPLE, 0, prevSample);
				}
				queueCommandEventToUi(tses, COMMAND_OUT_CURRENT_SAMPLE, 0, resampledSample);
				queueCommandEventToUi(tses, COMMAND_OUT_FRAME_POSITION_ABS, 0, resampledSample);
				break;
			}
			case COMMAND_IN_SHUTTLE_REWIND_TO_START:
			{
				BlockResamplingClient curSample = instance.getResampledSample();
				if( curSample != null )
				{
					instance.resetFramePosition( 0 );
					instance.addJobForSampleCachingService();

					queueTemporalEventToUi(tses, periodTimestamp, COMMAND_OUT_FRAME_POSITION_ABS, 0, curSample );
				}
				break;
			}
			case COMMAND_IN_SHUTTLE_FFWD_TO_END:
			{
				BlockResamplingClient curSample = instance.getResampledSample();
				if( curSample != null )
				{
					long lastFrameNum = curSample.getTotalNumFrames() - 1;
					instance.resetFramePosition( lastFrameNum );
					instance.addJobForSampleCachingService();

					queueTemporalEventToUi(tses, periodTimestamp, COMMAND_OUT_FRAME_POSITION_ABS, lastFrameNum, curSample );
				}
				break;
			}
			default:
			{
				String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}
	}
}
