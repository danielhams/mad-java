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

package test.uk.co.modularaudio.util.audio.mad.eventprocessingprototype;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public abstract class AbstractEventProcessor
{
	protected boolean hasEventProcessing;

	public void processWithEvents( final ThreadSpecificTemporaryEventStorage tempEventQueue,
			final MadTimingParameters timingParameters,
			int U_periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
		throws MadProcessingException
	{
		preProcess( tempEventQueue, timingParameters, numFrames );
		final int numTemporalEvents = tempEventQueue.numTemporalEventsToInstance;

		if( numTemporalEvents > 0 )
		{
			// Chop period up into chunks up to the next event
			// process the event and carry on.
			int curEventIndex = 0;

			final int numLeft = numFrames;
			int curIndex = 0;

			while( curEventIndex < numTemporalEvents )
			{
				IOQueueEvent comingEvent = tempEventQueue.temporalEventsToInstance[curEventIndex];

				int U_frameTime = comingEvent.U_frameTime;

				final int numToNextEvent = Integer.compareUnsigned( U_periodStartFrameTime, U_frameTime );

				final int numThisRound = (numToNextEvent < numLeft ? numToNextEvent : numLeft);

				process( tempEventQueue,
						timingParameters,
						U_periodStartFrameTime,
						channelConnectedFlags,
						channelBuffers,
						curIndex,
						numThisRound );

				curIndex += numThisRound;
				U_periodStartFrameTime += numThisRound;
				curEventIndex++;

				// Process any remaining events
				int framesLeft = Integer.compareUnsigned( U_frameTime, U_periodStartFrameTime );
				do
				{
					processEvent( comingEvent );
					comingEvent = tempEventQueue.temporalEventsToInstance[curEventIndex];
					U_frameTime = comingEvent.U_frameTime;
					framesLeft = Integer.compareUnsigned( U_frameTime, U_periodStartFrameTime );
				}
				while( curEventIndex < numTemporalEvents && framesLeft > 0 );
			}

			// Process any last chunk left over
			if( curIndex < numFrames )
			{
				process( tempEventQueue,
						timingParameters,
						U_periodStartFrameTime,
						channelConnectedFlags,
						channelBuffers,
						curIndex,
						numFrames - curIndex );
			}
		}
		else
		{
			// Can be processed as one big chunk
			process( tempEventQueue,
					timingParameters,
					U_periodStartFrameTime,
					channelConnectedFlags,
					channelBuffers,
					0,
					numFrames );
		}

		postProcess( tempEventQueue, timingParameters, U_periodStartFrameTime );
	}


	public void processNoEvents( final ThreadSpecificTemporaryEventStorage tempEventQueue,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
		throws MadProcessingException
	{
		// Can be processed as one big chunk
		process( tempEventQueue,
				timingParameters,
				periodStartFrameTime,
				channelConnectedFlags,
				channelBuffers,
				0,
				numFrames );
	}

	protected void preProcess( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
	}

	protected void postProcess( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime )
	{
	}

	public abstract void processEvent( IOQueueEvent event );

	public abstract void process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int frameOffset,
			int numFrames );
}
