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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvInterpolationChoiceUiJComponent;
import uk.co.modularaudio.mads.base.controllertocv.ui.ControllerToCvUseTimestampingUiJComponent;
import uk.co.modularaudio.mads.base.controllertocv.ui.InterpolationChoice;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
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

	public final static int NUM_INTERPOLATORS = InterpolationChoice.values().length;

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private ControllerEventMapping desiredMapping = ControllerEventMapping.LINEAR;
	private int desiredChannel = 0;
	private int desiredController = 0;

	private boolean isLearning;

//	private final static float FIXED_INTERP_MILLIS = 5.3f;
//	private final static float FIXED_INTERP_MILLIS = 9.8f;
	private final static float NOTS_INTERP_MILLIS = 8.2f;

	// For BCD EQ:
//	private static final float TS_MIN_CONTROLLER_PERIOD_MILLIS = 5.7f;
	private static final float TS_MIN_CONTROLLER_PERIOD_MILLIS = 4.1f;
//	private static final float TS_MIN_CONTROLLER_PERIOD_MILLIS = 2.0f;
	// For BCD Faders
//	private static final float TS_MIN_CONTROLLER_PERIOD_MILLIS = 1.0f;

	private final ControlValueInterpolator[] tsInterpolators = new ControlValueInterpolator[NUM_INTERPOLATORS];
	private final ControlValueInterpolator[] noTsInterpolators = new ControlValueInterpolator[NUM_INTERPOLATORS];

	private InterpolationChoice desiredInterpolator = ControllerToCvInterpolationChoiceUiJComponent.DEFAULT_INTERPOLATION;
	private boolean useTimestamps = ControllerToCvUseTimestampingUiJComponent.DEFAULT_STATE;
	private ControlValueInterpolator currentInterpolator = (useTimestamps
			?
			tsInterpolators[desiredInterpolator.ordinal()]
			:
			noTsInterpolators[desiredInterpolator.ordinal()] );

	public ControllerToCvMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ControllerToCvMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		try
		{
			int interpolatorNum = 0;
			for( final InterpolationChoice ic : InterpolationChoice.values() )
			{
				tsInterpolators[interpolatorNum] = ic.getInterpolatorClass().newInstance();
				noTsInterpolators[interpolatorNum] = ic.getInterpolatorClass().newInstance();
				interpolatorNum++;
			}

			currentInterpolator = tsInterpolators[0];
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught instantiating interpolators: " + e.toString();
			log.error( msg, e );
		}
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

			final int periodLengthFrames =
					hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();

			// When not using timestamps, use a default interpolation period that
			// should be long enough we cover a period
//			final int noTsInterpolatorsPeriodLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
//					NOTS_INTERP_MILLIS );
			final int noTsInterpolatorsPeriodLength = periodLengthFrames;

			// Quick hack to force the min length here to be double some controller period
			final int minInterpolatorLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
					TS_MIN_CONTROLLER_PERIOD_MILLIS * 2 );
			final int tsInterpolatorsPeriodLength = minInterpolatorLength;

			if( log.isTraceEnabled() )
			{
				log.trace("Period length is " + periodLengthFrames );
				log.trace("MinInterpolatorLength is " + minInterpolatorLength );
				log.trace("Setting timestamped interpolator period length to " + tsInterpolatorsPeriodLength );
				log.trace("Setting nots interpolator period length to " + noTsInterpolatorsPeriodLength );
			}
			for( final ControlValueInterpolator cvi : noTsInterpolators )
			{
				cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, noTsInterpolatorsPeriodLength );
			}
			for( final ControlValueInterpolator cvi : tsInterpolators )
			{
				cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, tsInterpolatorsPeriodLength );
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

		if( useTimestamps )
		{

			int currentFrameOffset = frameOffset;
			int numFramesLeft = numFrames;

			int startFrameOffset = frameOffset;

			int currentNoteEvent = 0;
//			if( numNotes > 0 )
//			{
//				log.trace("Beginning timestamped note processing");
//			}

			while( numFramesLeft > 0 )
			{
				int numFramesThisRound = numFramesLeft;

				if( currentNoteEvent < numNotes )
				{
//					log.trace( "Hunting for next event" );
					// Get index of last note event for the next
					// sample index where it is a controller.

					// We do this by finding the next controller event
					// and then hunting for any further controller events
					// with the same sample index
					currentNoteEvent = findNextControllerEvent( noteEvents,
							numNotes,
							currentNoteEvent );
//					log.trace( "Found next event at index " + currentNoteEvent );

					if( currentNoteEvent == numNotes )
					{
						// Didn't find any, just process the rest as is
						// by leaving numFramesThisRound alone
//						log.trace( "No further notes, generating " + numFramesThisRound + " from index " +
//								currentFrameOffset );
					}
					else
					{
						// This will return "currentNoteEvent" if there isn't any
						// following events with the same sample index
						currentNoteEvent = findLastControllerEventWithMatchingSampleIndex( noteEvents,
								numNotes,
								currentNoteEvent );
//						log.trace( "After attempting to find last, event to process at index " +
//								currentNoteEvent );

						final MadChannelNoteEvent ne = noteEvents[currentNoteEvent];
						int noteSampleIndex = ne.getEventSampleIndex();

						noteSampleIndex = (noteSampleIndex < 0
								?
								0
								:
								(noteSampleIndex > numFrames-1 ? numFrames-1 : noteSampleIndex)
						);

//						log.trace( "After bounds checking that is " + noteSampleIndex );

						if( noteSampleIndex < currentFrameOffset )
						{
							if( log.isErrorEnabled() )
							{
								log.error("Skipping CV generation - position goes backwards by "
										+ (currentFrameOffset - noteSampleIndex) );
							}
							numFramesThisRound = 0;
						}
						else
						{
							numFramesThisRound = noteSampleIndex - currentFrameOffset;
//							log.trace("Generating " + numFramesThisRound + " frames from offset " + startFrameOffset );
						}
					}
				}
				// else no note events to process

				if( numFramesThisRound < 0
						|| startFrameOffset > (frameOffset + numFrames + 1) )
				{
					// Until I have unified GUI and midi event handling, this is the best I can do
					log.error("Failed sanity check for interpolator call");
				}
				else if( numFramesThisRound > 0 )
				{
					currentInterpolator.generateControlValues( outCvFlots, startFrameOffset, numFramesThisRound );
				}

				currentInterpolator.checkForDenormal();

				if( currentNoteEvent < numNotes )
				{
					final MadChannelNoteEvent ne = noteEvents[currentNoteEvent];

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

				numFramesLeft -= numFramesThisRound;
				currentFrameOffset += numFramesThisRound;
				startFrameOffset += numFramesThisRound;
			}
		}
		else // Don't use timestamps
		{
			// Find value from last controller event
			// set it in the interpolator and generate a complete period
			// using that one value.
			final int lastControlEventIndex = findLastValidControllerEvent( noteEvents, numNotes );

			if( lastControlEventIndex != numNotes )
			{
				final MadChannelNoteEvent ne = noteEvents[lastControlEventIndex];
				final float rawNoteValue = ne.getParamTwo() / 127.0f;
				final float mappedValueToUse = mapValue( desiredMapping, rawNoteValue );
				currentInterpolator.notifyOfNewValue( mappedValueToUse );
			}

			currentInterpolator.generateControlValues( outCvFlots, frameOffset, numFrames );
			currentInterpolator.checkForDenormal();
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private int findLastValidControllerEvent( final MadChannelNoteEvent[] noteEvents,
			final int numNoteEvents )
	{
		int lastValidEventIndex = -1;

		for( int i = 0 ; i < numNoteEvents ; ++i )
		{
			final MadChannelNoteEvent ne = noteEvents[i];
			if( isValidControllerEvent( ne ) )
			{
				lastValidEventIndex = i;
			}
		}

		return( lastValidEventIndex == -1 ? numNoteEvents : lastValidEventIndex );
	}

	private boolean isValidControllerEvent( final MadChannelNoteEvent ne )
	{
		return ne.getEventType() == MadChannelNoteEventType.CONTROLLER &&
				ne.getChannel() == desiredChannel &&
				ne.getParamOne() == desiredController;
	}

	private int findNextControllerEvent( final MadChannelNoteEvent[] noteEvents,
			final int numNoteEvents,
			final int iCurrentNoteEvent )
	{
		int currentNoteEvent = iCurrentNoteEvent;
		do
		{
			final MadChannelNoteEvent ne = noteEvents[currentNoteEvent];
			if( isValidControllerEvent( ne ) )
			{
				return currentNoteEvent;
			}
			currentNoteEvent++;
		}
		while( currentNoteEvent < numNoteEvents );

		return numNoteEvents;
	}

	private int findLastControllerEventWithMatchingSampleIndex( final MadChannelNoteEvent[] noteEvents,
			final int numNoteEvents,
			final int iCurrentNoteEvent )
	{
		int currentNoteEvent = iCurrentNoteEvent;
		final int existingSampleIndex = noteEvents[iCurrentNoteEvent].getEventSampleIndex();

		int lastEventIndex = iCurrentNoteEvent;

		while( currentNoteEvent < (numNoteEvents - 1) )
		{
			final int nextEventIndex = currentNoteEvent + 1;
			final MadChannelNoteEvent nextEvent = noteEvents[nextEventIndex];
			if( nextEvent.getEventSampleIndex() == existingSampleIndex )
			{
				if( isValidControllerEvent( nextEvent ) )
				{
					lastEventIndex = nextEventIndex;
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
		if( log.isTraceEnabled() )
		{
			log.trace("Sending discovered channel " + lastChannel + " and controller " + lastController );
		}
		final long value = (lastChannel << 32) | lastController;
		localBridge.queueTemporalEventToUi( tses,
				frameTime,
				ControllerToCvIOQueueBridge.COMMAND_OUT_LEARNT_CONTROLLER,
				value,
				null );

	}

	public void setDesiredInterpolation( final InterpolationChoice interpolation )
	{
		if( log.isTraceEnabled() )
		{
			log.trace( "Would set interpolation to " + interpolation.toString() );
		}
		desiredInterpolator = interpolation;
		resetInterpolator();
	}

	private void resetInterpolator()
	{
		final float currentValue = currentInterpolator.getValue();
		currentInterpolator = ( useTimestamps
				?
				tsInterpolators[desiredInterpolator.ordinal()]
				:
				noTsInterpolators[desiredInterpolator.ordinal()] );
		currentInterpolator.hardSetValue( currentValue );
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

	public void setUseTimestamps( final boolean useTimestamps )
	{
		this.useTimestamps = useTimestamps;
		resetInterpolator();
	}
}
