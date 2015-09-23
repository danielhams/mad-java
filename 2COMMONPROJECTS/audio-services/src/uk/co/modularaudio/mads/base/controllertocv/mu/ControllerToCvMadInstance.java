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

package uk.co.modularaudio.mads.base.controllertocv.mu;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvInterpolationChoiceUiJComponent;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvInterpolationChoiceUiJComponent.InterpolationChoice;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPassInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDLowPassInterpolator24;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDoubleInterpolator24;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPassInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPassInterpolator24;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.math.NormalisedValuesMapper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ControllerToCvMadInstance extends MadInstance<ControllerToCvMadDefinition,ControllerToCvMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvMadInstance.class.getName() );

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private ControllerEventMapping desiredMapping = ControllerEventMapping.LINEAR;
	private int desiredChannel = 0;
	private int desiredController = 0;

	private boolean isLearning;

	private final Map<InterpolationChoice, ControlValueInterpolator> freeInterpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

//	private final static float FIXED_INTERP_MILLIS = 5.3f;
	private final static float FIXED_INTERP_MILLIS = 9.8f;

	private int fixedInterpolatorsPeriodLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(
			sampleRate, FIXED_INTERP_MILLIS );

	private final Map<InterpolationChoice, ControlValueInterpolator> fixedInterpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

	private final Map<InterpolationChoice, ControlValueInterpolator> interpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

	private ControlValueInterpolator currentInterpolator;

	public ControllerToCvMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ControllerToCvMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		freeInterpolators.put( InterpolationChoice.NONE, new NoneInterpolator() );
		freeInterpolators.put( InterpolationChoice.SUM_OF_RATIOS, new SumOfRatiosInterpolator() );
		freeInterpolators.put( InterpolationChoice.LINEAR, new LinearInterpolator( 0.0f, 1.0f ) );
		freeInterpolators.put( InterpolationChoice.HALF_HANN, new HalfHannWindowInterpolator() );
		freeInterpolators.put( InterpolationChoice.SPRING_DAMPER, new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f ) );
		freeInterpolators.put( InterpolationChoice.LOW_PASS, new LowPassInterpolator() );
		freeInterpolators.put( InterpolationChoice.LOW_PASS24, new LowPassInterpolator24() );
		freeInterpolators.put( InterpolationChoice.CD_LOW_PASS, new CDLowPassInterpolator() );
		freeInterpolators.put( InterpolationChoice.CD_LOW_PASS_24, new CDLowPassInterpolator24() );
		freeInterpolators.put( InterpolationChoice.CD_SPRING_DAMPER, new CDSpringAndDamperDoubleInterpolator( 0.0f, 1.0f ) );
		freeInterpolators.put( InterpolationChoice.CD_SPRING_DAMPER24, new CDSpringAndDamperDoubleInterpolator24( 0.0f, 1.0f ) );

		fixedInterpolators.put( InterpolationChoice.SUM_OF_RATIOS_FIXED, new SumOfRatiosInterpolator() );
		fixedInterpolators.put( InterpolationChoice.LINEAR_FIXED, new LinearInterpolator( 0.0f, 1.0f ) );
		fixedInterpolators.put( InterpolationChoice.HALF_HANN_FIXED, new HalfHannWindowInterpolator() );

		for( final Map.Entry<InterpolationChoice, ControlValueInterpolator> e : fixedInterpolators.entrySet() )
		{
			interpolators.put( e.getKey(), e.getValue() );
		}

		for( final Map.Entry<InterpolationChoice, ControlValueInterpolator> e : freeInterpolators.entrySet() )
		{
			interpolators.put( e.getKey(), e.getValue() );
		}

		currentInterpolator = interpolators.get( ControllerToCvInterpolationChoiceUiJComponent.DEFAULT_INTERPOLATION );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			fixedInterpolatorsPeriodLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
					FIXED_INTERP_MILLIS );

			final int periodLengthFrames = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();

			if( log.isTraceEnabled() )
			{
				log.trace("Setting interpolator period length to " + periodLengthFrames );
				log.trace("Setting fixed interpolator period length to " + fixedInterpolatorsPeriodLength );
			}
			for( final ControlValueInterpolator cvi : freeInterpolators.values() )
			{
				cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames );
			}
			for( final ControlValueInterpolator cvi : fixedInterpolators.values() )
			{
				cvi.resetSampleRateAndPeriod( sampleRate, fixedInterpolatorsPeriodLength );
			}
		}
		catch (final Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		final MadChannelBuffer noteCb = channelBuffers[ ControllerToCvMadDefinition.CONSUMER_NOTE ];
		final MadChannelBuffer outCvCb = channelBuffers[ ControllerToCvMadDefinition.PRODUCER_CV_OUT ];

		final MadChannelNoteEvent[] noteEvents = noteCb.noteBuffer;
		final int numNotes = noteCb.numElementsInBuffer;

		final float[] outCvFlots = outCvCb.floatBuffer;

		if( isLearning )
		{
			int lastController = -1;
			int lastChannel = -1;
			boolean wasController = false;
			for( int n = 0 ; n < numNotes ; n++ )
			{
				final MadChannelNoteEvent ne = noteEvents[ n ];
				switch( ne.getEventType() )
				{
					case CONTROLLER:
					{
						lastChannel = ne.getChannel();
						lastController = ne.getParamOne();
						wasController = true;
						break;
					}
					default:
					{
						break;
					}
				}
			}
			if( wasController )
			{
				// Encode channel and controller in a message back
				// to the UI
				sendDiscoveredController( tempQueueEntryStorage, periodStartFrameTime, lastChannel, lastController );
				isLearning = false;
			}
		}

		int currentFrameOffset = frameOffset;
		int numFramesLeft = numFrames;

		int startFrameOffset = frameOffset;

		int currentNoteEvent = 0;

		while( numFramesLeft > 0 )
		{
			int numFramesThisRound = numFramesLeft;

			if( currentNoteEvent < numNotes )
			{
				// Get index of last note event for the next
				// sample index where it is a controller.

				// We do this by finding the next controller event
				// and then hunting for any further controller events
				// with the same sample index
				final int nextControllerEventIndex = findNextControllerEvent( noteEvents,
						numNotes,
						currentNoteEvent );

				if( nextControllerEventIndex == -1 )
				{
					// Didn't find any, just process the rest as is
					// by leaving numFramesThisRound alone
				}
				else
				{
					currentNoteEvent = nextControllerEventIndex;
					// This will return "currentNoteEvent" if there isn't any
					// following events with the same sample index
					currentNoteEvent = findLastControllerEventWithSampleIndex( noteEvents,
							numNotes,
							currentNoteEvent );

					final MadChannelNoteEvent ne = noteEvents[currentNoteEvent];
					int noteSampleIndex = ne.getEventSampleIndex();

					noteSampleIndex = (noteSampleIndex < 0
							?
							0
							:
							(noteSampleIndex > numFrames-1 ? numFrames-1 : noteSampleIndex)
					);

					numFramesThisRound = noteSampleIndex - currentFrameOffset;

					final float rawNoteValue = ne.getParamTwo() / 127.0f;
					final float mappedValueToUse = mapValue( desiredMapping, rawNoteValue );

					currentInterpolator.notifyOfNewValue( mappedValueToUse );
//					if( log.isTraceEnabled() )
//					{
//						log.trace( "Notifying interpolator to change to value " +
//								MathFormatter.fastFloatPrint( mappedValueToUse, 8, true ) );
//					}

					// Fall onto next (or end) note
					currentNoteEvent++;
				}
			}
			// else no note events to process

//			log.trace("Generating " + numFramesThisRound + " frames from offset " + startFrameOffset );
			if( numFramesThisRound < 0
					|| startFrameOffset > (frameOffset + numFrames + 1) )
			{
				// Until I have unified GUI and midi event handling, this is the best I can do
				log.error("Failed sanity check for interpolator call");
			}
			else
			{
				currentInterpolator.generateControlValues( outCvFlots, startFrameOffset, numFramesThisRound );
			}

			currentInterpolator.checkForDenormal();

			numFramesLeft -= numFramesThisRound;
			currentFrameOffset += numFramesThisRound;
			startFrameOffset += numFramesThisRound;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private int findNextControllerEvent( final MadChannelNoteEvent[] noteEvents,
			final int numNoteEvents,
			final int iCurrentNoteEvent )
	{
		int currentNoteEvent = iCurrentNoteEvent;
		do
		{
			final MadChannelNoteEvent ne = noteEvents[currentNoteEvent];
			if( ne.getEventType() == MadChannelNoteEventType.CONTROLLER &&
					ne.getChannel() == desiredChannel &&
					ne.getParamOne() == desiredController )
			{
				return currentNoteEvent;
			}
			currentNoteEvent++;
		}
		while( currentNoteEvent < numNoteEvents );

		return -1;
	}

	private int findLastControllerEventWithSampleIndex( final MadChannelNoteEvent[] noteEvents,
			final int numNoteEvents,
			final int iCurrentNoteEvent )
	{
		int currentNoteEvent = iCurrentNoteEvent;
		final int existingSampleIndex = noteEvents[iCurrentNoteEvent].getEventSampleIndex();

		int lastEventIndex = iCurrentNoteEvent;

		while( currentNoteEvent < (numNoteEvents - 1) )
		{
			final MadChannelNoteEvent nextEvent = noteEvents[currentNoteEvent+1];
			if( nextEvent.getEventSampleIndex() == existingSampleIndex )
			{
				if( nextEvent.getEventType() == MadChannelNoteEventType.CONTROLLER &&
						nextEvent.getChannel() == desiredChannel &&
						nextEvent.getParamOne() == desiredController )
				{
					lastEventIndex = currentNoteEvent+1;
				}
			}
			else
			{
				// We're done, don't use this one.
			}
			currentNoteEvent++;
		}

		return lastEventIndex;
	}

	public void beginLearn()
	{
		isLearning = true;
		log.trace("Beginning note learn");
	}

	public void setDesiredMapping( final ControllerEventMapping mapping )
	{
		this.desiredMapping = mapping;
	}

	public void setDesiredChannel( final int channelNumber )
	{
		this.desiredChannel = channelNumber;
	}

	public void setDesiredController( final int controllerNumber )
	{
		this.desiredController = controllerNumber;
	}

	private void sendDiscoveredController( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int lastChannel,
			final int lastController )
	{
		log.trace("Sending discovered channel " + lastChannel + " and controller " + lastController );
		final long value = (lastChannel << 32) | lastController;
		localBridge.queueTemporalEventToUi( tses,
				frameTime,
				ControllerToCvIOQueueBridge.COMMAND_OUT_LEARNT_CONTROLLER,
				value,
				null );

	}

	public void setDesiredInterpolation( final InterpolationChoice interpolation )
	{
		log.trace( "Would set interpolation to " + interpolation.toString() );
		currentInterpolator = interpolators.get( interpolation );
	}

	private float mapValue( final ControllerEventMapping mapping, final float valToMap )
	{
		switch( mapping )
		{
			case LINEAR:
			{
				return valToMap;
			}
			case LOG:
			{
				return NormalisedValuesMapper.logMapF( valToMap );
			}
			case LOG_FREQUENCY:
			{
				return NormalisedValuesMapper.logMinMaxMapF( valToMap, 0.0f, 22050.0f );
			}
			case EXP:
			{
				return NormalisedValuesMapper.expMapF( valToMap );
			}
			case EXP_FREQUENCY:
			{
				return NormalisedValuesMapper.expMinMaxMapF( valToMap, 0.0f, 22050.0f );
			}
			case CIRC_Q1:
			{
				return NormalisedValuesMapper.circleQuadOneF( valToMap );
			}
			case CIRC_Q2:
			{
				return NormalisedValuesMapper.circleQuadTwoF( valToMap );
			}
			case CIRC_Q3:
			{
				return NormalisedValuesMapper.circleQuadThreeF( valToMap );
			}
			case CIRC_Q4:
			{
				return NormalisedValuesMapper.circleQuadFourF( valToMap );
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown mapping: " + mapping.toString() );
				}
			}
		}
		return valToMap;
	}
}
