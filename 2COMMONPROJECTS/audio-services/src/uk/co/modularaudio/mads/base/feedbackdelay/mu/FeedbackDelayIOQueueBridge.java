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

package uk.co.modularaudio.mads.base.feedbackdelay.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class FeedbackDelayIOQueueBridge extends MadLocklessQueueBridge<FeedbackDelayMadInstance>
{
	private static Log log = LogFactory.getLog( FeedbackDelayIOQueueBridge.class.getName() );

	public static final int COMMAND_DELAY = 0;
	public static final int COMMAND_FEEDBACK = 1;

	public FeedbackDelayIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final FeedbackDelayMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_DELAY:
			{
				// Is just a float
				final long value = queueEntry.value;
				final float desiredDelayMillis = Float.intBitsToFloat( (int)value );
				final int numFramesDelay = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( instance.sampleRate, desiredDelayMillis );
				instance.desiredDelayFrames = (numFramesDelay > 0 ? numFramesDelay : 0 );
				break;
			}
			case COMMAND_FEEDBACK:
			{
				// Is just a float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float f = Float.intBitsToFloat( truncVal );
				instance.desiredFeedback = f;
				break;
			}
			default:
			{
				final String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
			}
		}
	}
}
