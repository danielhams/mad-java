package uk.co.modularaudio.util.audio.mad.test;

import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public abstract class AbstractEventProcessor
{
	public void doProcess( final ThreadSpecificTemporaryEventStorage tempEventQueue,
			final MadTimingParameters timingParameters,
			final float[] buffer,
			final int numFrames,
			final long periodTimestamp ) throws MadProcessingException
	{
		final int numTemporalEvents = tempEventQueue.numTemporalEventsToInstance;
		if( numTemporalEvents > 0 )
		{
			int curEventIndex = 0;

			final int numLeft = numFrames;
			int curIndex = 0;

			while( curEventIndex < numTemporalEvents )
			{
				final IOQueueEvent comingEvent = tempEventQueue.temporalEventsToInstance[ curEventIndex ];

				final long frameTime = comingEvent.frameTime;

				final long numToNextEvent = (frameTime - periodTimestamp);
				final int numToNextEventInt = (int)numToNextEvent;
				if( numToNextEventInt != numToNextEvent )
				{
					throw new MadProcessingException( "Distance to event exceed ints!" );
				}

				final int numThisRound = (numToNextEventInt < numLeft ? numToNextEventInt : numLeft );

				process( buffer,
						curIndex,
						numThisRound );

				curIndex += numThisRound;
				curEventIndex++;

				processEvent( comingEvent );
			}

			if( curIndex < numFrames )
			{
				process( buffer,
						curIndex,
						numFrames - curIndex );
			}
		}
	}

	public abstract void processEvent( IOQueueEvent event );

	public abstract void process( final float[] buffer,
		final int index,
		final int length );
}
