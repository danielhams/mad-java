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

package uk.co.modularaudio.mads.base.soundfile_player2.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class SoundfilePlayer2IOQueueBridge extends MadLocklessQueueBridge<SoundfilePlayer2MadInstance>
{
	private static Log log = LogFactory.getLog( SoundfilePlayer2IOQueueBridge.class.getName() );

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

	public static final int COMMAND_IN_POSITION_JUMP = 12;

	public static final int COMMAND_IN_GAIN = 13;


	public static final int COMMAND_OUT_RECYCLE_SAMPLE = 16;

	public static final int COMMAND_OUT_STATE_CHANGE = 17;
	public static final int COMMAND_OUT_CURRENT_SAMPLE = 18;
	public static final int COMMAND_OUT_FRAME_POSITION_ABS = 19;
	public static final int COMMAND_OUT_FRAME_POSITION_DELTA = 20;
	public static final int COMMAND_OUT_FRAME_POSITION_ABS_WAIT_FOR_CACHE = 21;


	public SoundfilePlayer2IOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final SoundfilePlayer2MadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				instance.active = queueEntry.value == 1;
				break;
			}
			case COMMAND_IN_PLAYING_STATE:
			{
				final int value = (int)queueEntry.value;
				final SoundfilePlayer2MadInstance.PlayingState desiredState = SoundfilePlayer2MadInstance.PlayingState.values()[ value ];
				instance.setDesiredState( desiredState );
				break;
			}
			case COMMAND_IN_PLAY_SPEED:
			{
				final float value = Float.intBitsToFloat(((int)queueEntry.value));
				instance.setDesiredPlaySpeed( value );
				break;
			}
			case COMMAND_IN_GAIN:
			{
				final float value = Float.intBitsToFloat( ((int)queueEntry.value));
				instance.setDesiredGain( value );
				break;
			}
			case COMMAND_IN_RESAMPLED_SAMPLE:
			{
				final BlockResamplingClient prevSample = instance.getResampledSample();
				final BlockResamplingClient resampledSample = (BlockResamplingClient)queueEntry.object;
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
				final BlockResamplingClient curSample = instance.getResampledSample();
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
				final BlockResamplingClient curSample = instance.getResampledSample();
				if( curSample != null )
				{
					final long lastFrameNum = curSample.getTotalNumFrames() - 1;
					instance.resetFramePosition( lastFrameNum );
					instance.addJobForSampleCachingService();

					queueTemporalEventToUi(tses, periodTimestamp, COMMAND_OUT_FRAME_POSITION_ABS, lastFrameNum, curSample );
				}
				break;
			}
			case COMMAND_IN_POSITION_JUMP:
			{
				final long newPosition = queueEntry.value;
				final BlockResamplingClient curSample = instance.getResampledSample();
				if( curSample != null )
				{
					instance.resetFramePosition( newPosition );
					instance.addJobForSampleCachingService();

					queueTemporalEventToUi(tses, periodTimestamp, COMMAND_OUT_FRAME_POSITION_ABS_WAIT_FOR_CACHE, newPosition, curSample );
				}
				break;
			}
			default:
			{
				final String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}
	}
}
