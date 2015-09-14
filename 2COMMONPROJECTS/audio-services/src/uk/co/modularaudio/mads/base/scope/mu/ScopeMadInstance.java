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
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ScopeMadInstance extends MadInstance<ScopeMadDefinition, ScopeMadInstance>
{
	private static Log log = LogFactory.getLog( ScopeMadInstance.class.getName() );

	private enum State
	{
		IDLE,
		TRIGGER_HUNT_PRE,
		TRIGGER_HUNT_POST,
		CAPTURING
	};

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private int maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
			LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );

	private MultiChannelBackendToFrontendDataRingBuffer backEndFrontEndBuffer;

	private float captureMillis = LogarithmicTimeMillis1To1000SliderModel.DEFAULT_MILLIS;
	private int desiredFramesToCapture = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
			captureMillis );
	// Little bit unrealistic. Rely on startup to fix it.
	private int framesPerFrontEndPeriod = desiredFramesToCapture;

	// Running state
	private boolean isActive = false;

	private State state = State.IDLE;
	private TriggerChoice desiredTrigger = ScopeTriggerChoiceUiJComponent.DEFAULT_TRIGGER_CHOICE;
	private TriggerChoice workingTrigger = desiredTrigger;
	private RepetitionChoice repetition = ScopeRepetitionsChoiceUiJComponent.DEFAULT_REPETITION_CHOICE;

	private int workingDesiredFramesToCapture = desiredFramesToCapture;
	private int workingFramesCaptured;
	private int workingFrontEndPeriodFramesCaptured;

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
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
				LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
		desiredFramesToCapture = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, captureMillis );
		workingDesiredFramesToCapture = desiredFramesToCapture;

		backEndFrontEndBuffer = new MultiChannelBackendToFrontendDataRingBuffer( ScopeMadDefinition.NUM_VIS_CHANNELS, maxRingBufferingInSamples );

		framesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();

		// Reset to idle
		workingFramesCaptured = 0;
		workingFrontEndPeriodFramesCaptured = 0;
		state = State.IDLE;
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tses,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		if( !isActive )
		{
			return RealtimeMethodReturnCodeEnum.SUCCESS;
		}

		int currentFrameOffset = 0;

		while( currentFrameOffset < numFrames )
		{
			final int numLeftThisRound = numFrames - currentFrameOffset;
			final int numFramesThisRound;

			switch( state )
			{
				case IDLE:
				{
					numFramesThisRound = doOneIdlePass( tses,
							periodStartFrameTime,
							frameOffset,
							currentFrameOffset,
							numLeftThisRound );
					break;
				}
				case TRIGGER_HUNT_PRE:
				{
					numFramesThisRound = doOneTriggerPrePass( frameOffset,
							currentFrameOffset,
							numLeftThisRound,
							channelBuffers );
					break;
				}
				case TRIGGER_HUNT_POST:
				{
					numFramesThisRound = doOneTriggerPostPass( tses,
							periodStartFrameTime,
							frameOffset,
							currentFrameOffset,
							numLeftThisRound,
							channelBuffers );
					break;
				}
				case CAPTURING:
				{
					numFramesThisRound = doOneCapturePass( tses,
							periodStartFrameTime,
							frameOffset,
							currentFrameOffset,
							numLeftThisRound,
							channelBuffers );
					break;
				}
				default:
				{
					log.error("Fell into non state handling of scope");
					numFramesThisRound = numLeftThisRound;
				}
			}

			currentFrameOffset += numFramesThisRound;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private int doOneIdlePass( final ThreadSpecificTemporaryEventStorage tses,
			final long periodStartFrameTime,
			final int frameOffset,
			final int currentFrameOffset,
			final int numLeftThisRound )
	{
		int numFramesThisRound;
		if( repetition == RepetitionChoice.CONTINUOUS )
		{
			numFramesThisRound = 0;
			final long eventFrameTime = periodStartFrameTime + frameOffset + currentFrameOffset;
			workingTrigger = desiredTrigger;
			if( desiredTrigger == TriggerChoice.NONE )
			{
				startCapture( tses, eventFrameTime );
			}
			else
			{
				startPreHunt();
			}
		}
		else
		{
			// Keep idling
			numFramesThisRound = numLeftThisRound;
		}
		return numFramesThisRound;
	}

	private int doOneTriggerPrePass( final int frameOffset,
			final int currentFrameOffset,
			final int numLeftThisRound,
			final MadChannelBuffer[] channelBuffers )
	{
		final float[] triggerFloats = channelBuffers[ ScopeMadDefinition.SCOPE_TRIGGER ].floatBuffer;

		// In case not found, skip over the frames we will check
		int numFramesThisRound = numLeftThisRound;

		TRIGGER_PRE_FOUND:
		for( int i = 0 ; i < numLeftThisRound ; ++i )
		{
			final float triggerValue = triggerFloats[frameOffset+currentFrameOffset+i];
			switch( workingTrigger )
			{
				case ON_RISE:
				{
					if( triggerValue <= 0.0f )
					{
						numFramesThisRound = i + 1;
						startPostHunt();
						break TRIGGER_PRE_FOUND;
					}
					break;
				}
				case ON_FALL:
				{
					if( triggerValue > 0.0f )
					{
						numFramesThisRound = i + 1;
						startPostHunt();
						break TRIGGER_PRE_FOUND;
					}
					break;
				}
				case NONE:
				{
					log.error("Fell into no trigger handling pre.");
					return numLeftThisRound;
				}
			}
		}
		return numFramesThisRound;
	}

	private int doOneTriggerPostPass( final ThreadSpecificTemporaryEventStorage tses,
			final long periodStartFrameTime,
			final int frameOffset,
			final int currentFrameOffset,
			final int numLeftThisRound,
			final MadChannelBuffer[] channelBuffers )
	{
		final float[] triggerFloats = channelBuffers[ ScopeMadDefinition.SCOPE_TRIGGER ].floatBuffer;

		// In case not found, skip over the frames we will check
		int numFramesThisRound = numLeftThisRound;

		TRIGGER_POST_FOUND:
		for( int i = 0 ; i < numLeftThisRound ; ++i )
		{
			final float triggerValue = triggerFloats[frameOffset+currentFrameOffset+i];
			switch( workingTrigger )
			{
				case ON_RISE:
				{
					if( triggerValue > 0.0f )
					{
						numFramesThisRound = i + 1;
						final long eventFrameTime = periodStartFrameTime + frameOffset + currentFrameOffset + i;
						startCapture( tses, eventFrameTime );
						break TRIGGER_POST_FOUND;
					}
					break;
				}
				case ON_FALL:
				{
					if( triggerValue <= 0.0f )
					{
						numFramesThisRound = i + 1;
						final long eventFrameTime = periodStartFrameTime + frameOffset + currentFrameOffset + i;
						startCapture( tses, eventFrameTime );
						break TRIGGER_POST_FOUND;
					}
					break;
				}
				case NONE:
				{
					log.error("Fell into no trigger handling post.");
					return numLeftThisRound;
				}
			}
		}
		return numFramesThisRound;
	}

	private int doOneCapturePass( final ThreadSpecificTemporaryEventStorage tses,
			final long periodStartFrameTime,
			final int frameOffset,
			final int currentFrameOffset,
			final int numLeftThisRound,
			final MadChannelBuffer[] channelBuffers )
	{
		final int numFramesLeftToCapture = workingDesiredFramesToCapture - workingFramesCaptured;
		final int numFramesToBufferEmit = framesPerFrontEndPeriod - workingFrontEndPeriodFramesCaptured;

		// Get a lower bound on how many to capture this time around
		int numFramesThisRound = (numLeftThisRound < numFramesLeftToCapture ? numLeftThisRound : numFramesLeftToCapture);
		numFramesThisRound = (numFramesThisRound < numFramesToBufferEmit ? numFramesThisRound : numFramesToBufferEmit);

		final float[][] sourceBuffers = new float[5][];
		sourceBuffers[0] = channelBuffers[ ScopeMadDefinition.SCOPE_TRIGGER ].floatBuffer;
		sourceBuffers[1] = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_0 ].floatBuffer;
		sourceBuffers[2] = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_1 ].floatBuffer;
		sourceBuffers[3] = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_2 ].floatBuffer;
		sourceBuffers[4] = channelBuffers[ ScopeMadDefinition.SCOPE_INPUT_3 ].floatBuffer;

		final int numWritten = backEndFrontEndBuffer.backEndWrite( sourceBuffers, frameOffset + currentFrameOffset, numFramesThisRound );

		if( numWritten != numFramesThisRound )
		{
			log.error("Failed to write frames to befe buffer - asked to write " + numFramesThisRound +
					" only wrote " + numWritten );
		}

		workingFramesCaptured += numFramesThisRound;
		workingFrontEndPeriodFramesCaptured += numFramesThisRound;

		if( workingFramesCaptured == workingDesiredFramesToCapture )
		{
			final long eventFrameTime = periodStartFrameTime + frameOffset + currentFrameOffset;
			// Completed capture
			emitWritePositionEvent( tses, eventFrameTime );

			// Now if we need to re-trigger, set the state accordingly
			// We check is active so we're only spamming one capture when
			// we're inactive (not visible on screen) as the front end
			// isn't picking up events when inactive.
			if( isActive && repetition == RepetitionChoice.CONTINUOUS )
			{
				if( desiredTrigger == TriggerChoice.NONE )
				{
					startCapture( tses, eventFrameTime );
				}
				else
				{
					workingTrigger = desiredTrigger;
					startPreHunt();
				}
			}
			else
			{
				startIdling();
			}
		}
		else if( workingFrontEndPeriodFramesCaptured == framesPerFrontEndPeriod )
		{
			final long eventFrameTime = periodStartFrameTime + frameOffset + currentFrameOffset;
			// mid capture, emit write position event
			emitWritePositionEvent( tses, eventFrameTime );
		}
		return numFramesThisRound;
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	public void setActive( final boolean active )
	{
		this.isActive = active;
	}

	public MultiChannelBackendToFrontendDataRingBuffer getBackendRingBuffer()
	{
		return backEndFrontEndBuffer;
	}

	public void setCaptureMillis( final float captureMillis )
	{
		this.captureMillis = captureMillis;
		this.desiredFramesToCapture = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
				captureMillis );
	}

	public void setTriggerChoice( final TriggerChoice trigger )
	{
		this.desiredTrigger = trigger;
		// If a trigger was previously set and the new trigger is "none"
		// we need to back out of the hunt state if it's active
		if( desiredTrigger == TriggerChoice.NONE )
		{
			switch( state )
			{
				case TRIGGER_HUNT_POST:
				case TRIGGER_HUNT_PRE:
				{
					state = State.IDLE;
					break;
				}
				default:
				{
					break;
				}
			}
		}
	}

	public void setRepetitionChoice( final RepetitionChoice repetition )
	{
		this.repetition = repetition;
	}

	private void emitWritePositionEvent( final ThreadSpecificTemporaryEventStorage tses,
			final long eventFrameTime )
	{
		final int writePosition = backEndFrontEndBuffer.getWritePosition();

		localBridge.queueTemporalEventToUi( tses,
				eventFrameTime,
				ScopeIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
				writePosition,
				null );
		workingFrontEndPeriodFramesCaptured = 0;
	}

	public void startPreHunt()
	{
		state = State.TRIGGER_HUNT_PRE;
	}

	private void startPostHunt()
	{
		state = State.TRIGGER_HUNT_POST;
	}

	private void startCapture( final ThreadSpecificTemporaryEventStorage tses,
			final long eventFrameTime )
	{
		state = State.CAPTURING;
		// We need a constant "how many to capture" per capture
		// so we set it here.
		workingDesiredFramesToCapture = desiredFramesToCapture;
		workingFramesCaptured = 0;
		workingFrontEndPeriodFramesCaptured = 0;

		localBridge.queueTemporalEventToUi( tses,
				eventFrameTime,
				ScopeIOQueueBridge.COMMAND_OUT_DATA_START,
				1,
				null );
	}

	private void startIdling()
	{
		state = State.IDLE;
	}
}
