package uk.co.modularaudio.mads.base.scope.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.scope.ui.ScopeRepetitionsChoiceUiJComponent;
import uk.co.modularaudio.mads.base.scope.ui.ScopeRepetitionsChoiceUiJComponent.RepetitionChoice;
import uk.co.modularaudio.mads.base.scope.ui.ScopeTriggerChoiceUiJComponent;
import uk.co.modularaudio.mads.base.scope.ui.ScopeTriggerChoiceUiJComponent.TriggerChoice;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.buffer.MultiChannelBackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To5000SliderModel;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ScopeMadInstance extends MadInstance<ScopeMadDefinition, ScopeMadInstance>
{
	private static Log log = LogFactory.getLog( ScopeMadInstance.class.getName() );

	private int maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.CD_QUALITY.getValue(),
			LogarithmicTimeMillis1To5000SliderModel.DEFAULT_MILLIS );

	private MultiChannelBackendToFrontendDataRingBuffer dataRingBuffer;

	private int numSamplePerFrontEndPeriod;

	private boolean isActive = false;

	private int captureSamples = maxRingBufferingInSamples;

	private TriggerChoice trigger = ScopeTriggerChoiceUiJComponent.DEFAULT_TRIGGER_CHOICE;
	private RepetitionChoice repetition = ScopeRepetitionsChoiceUiJComponent.DEFAULT_REPETITION_CHOICE;

	private enum ScopeState
	{
		IDLE,
		TRIGGER_HUNT,
		CAPTURING
	};

	private ScopeState state = ScopeState.IDLE;
	private int numSamplesCaptured = 0;
	private boolean foundTrigger0 = false;

	public ScopeMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ScopeMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
				LogarithmicTimeMillis1To5000SliderModel.MAX_MILLIS );

		dataRingBuffer = new MultiChannelBackendToFrontendDataRingBuffer( ScopeMadDefinition.NUM_VIS_CHANNELS, maxRingBufferingInSamples );

		numSamplePerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
	}

	private void startTriggerHunt()
	{
		state = ScopeState.TRIGGER_HUNT;
		foundTrigger0 = false;
		numSamplesCaptured = 0;
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		if( isActive )
		{
			if( state == ScopeState.IDLE &&
					repetition == RepetitionChoice.ONCE )
			{
				// Don't do anything.
				return RealtimeMethodReturnCodeEnum.SUCCESS;
			}

			int numFramesLeft = numFrames;
			int currentFrameIndex = 0;

			final float[] triggerFloats = channelBuffers[ ScopeMadDefinition.SCOPE_TRIGGER ].floatBuffer;

			final float[] input0Floats = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_0 ].floatBuffer;
			final float[] input1Floats = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_1 ].floatBuffer;
			final float[] input2Floats = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_2 ].floatBuffer;
			final float[] input3Floats = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_3 ].floatBuffer;

			while( numFramesLeft > 0 )
			{
				if( state == ScopeState.TRIGGER_HUNT )
				{
					currentFrameIndex = doTriggerSeek( tempQueueEntryStorage,
							periodStartFrameTime,
							frameOffset,
							numFrames,
							currentFrameIndex,
							triggerFloats );
				}

				numFramesLeft = numFrames - currentFrameIndex;

				// Capture to ui
				while( state == ScopeState.CAPTURING )
				{
//					log.trace("Capturing from frame " + currentFrameIndex);
					int numLeftToCapture = captureSamples - numSamplesCaptured;
					final int numLeftLowerBound = (numLeftToCapture < numFramesLeft ? numLeftToCapture : numFramesLeft);

					final int numToFrontEndPeriod = numSamplePerFrontEndPeriod - dataRingBuffer.backEndGetNumSamplesQueued();

					final int numThisRound = (numLeftLowerBound < numToFrontEndPeriod ? numLeftLowerBound : numToFrontEndPeriod);

					long timestampForIndexUpdate = periodStartFrameTime + frameOffset + currentFrameIndex;

					if( numToFrontEndPeriod <= 0 )
					{
//						final int numBackendFramesQueued = dataRingBuffer.backEndGetNumSamplesQueued();
//						log.debug("Queuing write index update of queue samples(" + numBackendFramesQueued + ")");
						queueWriteIndexUpdate( tempQueueEntryStorage,
							dataRingBuffer.getWritePosition(),
							timestampForIndexUpdate );
						dataRingBuffer.backEndClearNumSamplesQueued();
					}

//					log.trace("Have " + numLeftToCapture + " left to capture, doing " + numThisRound + " this round");

					if( numThisRound > 0 )
					{
						final float[][] outChannels = new float[ScopeMadDefinition.NUM_VIS_CHANNELS][];
						outChannels[0] = triggerFloats;
						outChannels[1] = input0Floats;
						outChannels[2] = input1Floats;
						outChannels[3] = input2Floats;
						outChannels[4] = input3Floats;
						final int numWritten = dataRingBuffer.backEndWrite( outChannels, frameOffset + currentFrameIndex, numThisRound );
						if( numWritten != numThisRound )
						{
							if( log.isErrorEnabled() )
							{
								log.error("Failed to write to back end ring buffer - attempted " +
										numThisRound + " but ring returned " + numWritten );
							}
						}
					}

					numFramesLeft -= numThisRound;
					currentFrameIndex += numThisRound;
					numLeftToCapture -= numThisRound;
					numSamplesCaptured += numThisRound;

					if( numLeftToCapture == 0 )
					{
						state = ScopeState.IDLE;
						numSamplesCaptured = 0;
//						log.trace( "Completed capture, switching to idle" );
						final int numBackendFramesQueued = dataRingBuffer.backEndGetNumSamplesQueued();
						if( numBackendFramesQueued > 0 )
						{
//							log.debug( "Still have queued samples(" + numBackendFramesQueued + ")");
							timestampForIndexUpdate = periodStartFrameTime + frameOffset + currentFrameIndex;
							queueWriteIndexUpdate( tempQueueEntryStorage,
									dataRingBuffer.getWritePosition(),
									timestampForIndexUpdate );
							dataRingBuffer.backEndClearNumSamplesQueued();
						}
					}

					if( numFramesLeft == 0 )
					{
						break;
					}
				}

				switch( state )
				{
					case IDLE:
					{
						if( repetition == RepetitionChoice.CONTINUOUS )
						{
//							log.trace( "In idle but continuous capture forcing trigger hunt" );
							startTriggerHunt();
						}
						else
						{
							// Finish
							currentFrameIndex = numFrames;
							numFramesLeft = 0;
						}
						break;
					}
					case TRIGGER_HUNT:
					{
						// Trigger hunting across periods
						break;
					}
					case CAPTURING:
					{
						// Still capturing across periods
						break;
					}
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private int doTriggerSeek( final ThreadSpecificTemporaryEventStorage tses,
			final long periodStartFrameTime,
			final int frameOffset,
			final int numFrames,
			final int iCurrentFrameIndex,
			final float[] triggerFloats )
	{
		int currentFrameIndex = iCurrentFrameIndex;
		// Non capturing seek in incoming data
		TRIGGER_LOOP:
		while( currentFrameIndex < numFrames )
		{
			final float triggerValue = triggerFloats[frameOffset + currentFrameIndex];

			if( !foundTrigger0 )
			{
				switch( trigger )
				{
					case NONE:
					{
//						log.trace( "Found no trigger prevalue at index " + currentFrameIndex );
						foundTrigger0 = true;
						break;
					}
					case ON_RISE:
					{
						if( triggerValue <= 0.0f )
						{
//							log.trace( "Found on rise pretrigger at index " + currentFrameIndex );
							foundTrigger0 = true;
						}
						break;
					}
					case ON_FALL:
					{
						if( triggerValue > 0.0f )
						{
//							log.trace( "Found on fall pretrigger at index " + currentFrameIndex );
							foundTrigger0 = true;
						}
						break;
					}
				}
			}
			else
			{
				switch( trigger )
				{
					case NONE:
					{
						state = ScopeState.CAPTURING;
						final long timestampForIndexUpdate = periodStartFrameTime + frameOffset + currentFrameIndex;
						queueTriggeredNotification( tses, timestampForIndexUpdate );
						foundTrigger0 = false;
//						log.trace("Found none trigger at index " + currentFrameIndex );
						break TRIGGER_LOOP;
					}
					case ON_RISE:
					{
						if( triggerValue > 0.0f )
						{
							state = ScopeState.CAPTURING;
							final long timestampForIndexUpdate = periodStartFrameTime + frameOffset + currentFrameIndex;
							queueTriggeredNotification( tses, timestampForIndexUpdate );
							foundTrigger0 = false;
//							log.trace("Found on rise trigger at index " + currentFrameIndex );
							break TRIGGER_LOOP;
						}
						break;
					}
					case ON_FALL:
					{
						if( triggerValue <= 0.0f )
						{
							state = ScopeState.CAPTURING;
							final long timestampForIndexUpdate = periodStartFrameTime + frameOffset + currentFrameIndex;
							queueTriggeredNotification( tses, timestampForIndexUpdate );
							foundTrigger0 = false;
//							log.trace("Found on fall trigger at index " + currentFrameIndex );
							break TRIGGER_LOOP;
						}
						break;
					}
				}
			}
			currentFrameIndex++;
		}

		return currentFrameIndex;
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	public void setActive( final boolean active )
	{
		this.isActive = active;
	}

	private void queueTriggeredNotification( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime )
	{
//		// Flush any remaining write position
//		localBridge.queueTemporalEventToUi( tses,
//				frameTime,
//				ScopeIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
//				dataRingBuffer.getWritePosition(),
//				null );
		dataRingBuffer.backEndClearNumSamplesQueued();
		localBridge.queueTemporalEventToUi( tses,
				frameTime,
				ScopeIOQueueBridge.COMMAND_OUT_DATA_START,
				0,
				null );
	}

	private void queueWriteIndexUpdate( final ThreadSpecificTemporaryEventStorage tses,
			final int writePosition,
			final long frameTime )
	{
//		log.trace( "Queued write index update with timestamp " + frameTime + " to " + writePosition );
		localBridge.queueTemporalEventToUi( tses,
			frameTime,
			ScopeIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
			writePosition,
			null );
	}

	public MultiChannelBackendToFrontendDataRingBuffer getBackendRingBuffer()
	{
		return dataRingBuffer;
	}

	public void setCaptureSamples( final int captureSamples )
	{
		this.captureSamples = captureSamples;
		this.numSamplesCaptured = 0;
		startTriggerHunt();
	}

	public void setTriggerChoice( final TriggerChoice trigger )
	{
		this.trigger  = trigger;
	}

	public void setRepetitionChoice( final RepetitionChoice repetition )
	{
		this.repetition = repetition;
	}

	public void doRecapture()
	{
		startTriggerHunt();
	}
}
