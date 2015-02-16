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

	public void doProcess( final ThreadSpecificTemporaryEventStorage tempEventQueue,
			final MadTimingParameters timingParameters,
			long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
		throws MadProcessingException
	{
		int numTemporalEvents = 0;
		if( hasEventProcessing )
		{
			preProcess( tempEventQueue, timingParameters, numFrames );
			numTemporalEvents = tempEventQueue.numTemporalEventsToInstance;
		}

		if( hasEventProcessing && numTemporalEvents > 0 )
		{
			// Chop period up into chunks up to the next event
			// process the event and carry on.
			int curEventIndex = 0;

			final int numLeft = numFrames;
			int curIndex = 0;

			while( curEventIndex < numTemporalEvents )
			{
				IOQueueEvent comingEvent = tempEventQueue.temporalEventsToInstance[curEventIndex];

				long frameTime = comingEvent.frameTime;

				final long numToNextEvent = (frameTime - periodStartFrameTime);
				final int numToNextEventInt = (int) numToNextEvent;
				if( numToNextEventInt != numToNextEvent )
				{
					throw new MadProcessingException( "Distance to event exceed ints!" );
				}

				final int numThisRound = (numToNextEventInt < numLeft ? numToNextEventInt : numLeft);

				process( tempEventQueue,
						timingParameters,
						periodStartFrameTime,
						channelConnectedFlags,
						channelBuffers,
						curIndex,
						numThisRound );

				curIndex += numThisRound;
				periodStartFrameTime += numThisRound;
				curEventIndex++;

				// Process any remaining events
				do
				{
					processEvent( comingEvent );
					comingEvent = tempEventQueue.temporalEventsToInstance[curEventIndex];
					frameTime = comingEvent.frameTime;
				}
				while( curEventIndex < numTemporalEvents && frameTime <= periodStartFrameTime );
			}

			// Process any last chunk left over
			if( curIndex < numFrames )
			{
				process( tempEventQueue,
						timingParameters,
						periodStartFrameTime,
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
					periodStartFrameTime,
					channelConnectedFlags,
					channelBuffers,
					0,
					numFrames );
		}

		if( hasEventProcessing )
		{
			postProcess( tempEventQueue, timingParameters, periodStartFrameTime );
		}
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
