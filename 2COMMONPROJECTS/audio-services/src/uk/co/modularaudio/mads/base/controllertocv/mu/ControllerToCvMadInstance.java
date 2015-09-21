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
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class ControllerToCvMadInstance extends MadInstance<ControllerToCvMadDefinition,ControllerToCvMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvMadInstance.class.getName() );

	private int numFramesPerPeriod;
	private int notePeriodLength;

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private ControllerEventProcessor eventProcessor;

	private ControllerEventMapping desiredMapping = ControllerEventMapping.LINEAR;
	private int desiredChannel = 0;
	private int desiredController = 0;

	private boolean isLearning;

	private final Map<InterpolationChoice, ControlValueInterpolator> freeInterpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

	private final static float FIXED_INTERP_MILLIS = 5.3f;
//	private final static float FIXED_INTERP_MILLIS = 9.8f;

	private int fixedInterpolatorsPeriodLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(
			sampleRate, FIXED_INTERP_MILLIS );

	private final Map<InterpolationChoice, ControlValueInterpolator> fixedInterpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

	private final Map<InterpolationChoice, ControlValueInterpolator> interpolators =
			new HashMap<InterpolationChoice, ControlValueInterpolator>();

	private ControlValueInterpolator currentInterpolator;

	private long minSamplesBetweenNotes = 1000000000L; // A second

	public ControllerToCvMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ControllerToCvMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		freeInterpolators.put( InterpolationChoice.NONE, new NoneInterpolator() );
		freeInterpolators.put( InterpolationChoice.SUM_OF_RATIOS, new SumOfRatiosInterpolator() );
		freeInterpolators.put( InterpolationChoice.LINEAR, new LinearInterpolator() );
		freeInterpolators.put( InterpolationChoice.HALF_HANN, new HalfHannWindowInterpolator() );
		freeInterpolators.put( InterpolationChoice.SPRING_DAMPER, new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f ) );
		freeInterpolators.put( InterpolationChoice.LOW_PASS, new LowPassInterpolator() );
		freeInterpolators.put( InterpolationChoice.LOW_PASS24, new LowPassInterpolator24() );
		freeInterpolators.put( InterpolationChoice.CD_LOW_PASS, new CDLowPassInterpolator() );
		freeInterpolators.put( InterpolationChoice.CD_LOW_PASS_24, new CDLowPassInterpolator24() );
		freeInterpolators.put( InterpolationChoice.CD_SPRING_DAMPER, new CDSpringAndDamperDoubleInterpolator( 0.0f, 1.0f ) );
		freeInterpolators.put( InterpolationChoice.CD_SPRING_DAMPER24, new CDSpringAndDamperDoubleInterpolator24( 0.0f, 1.0f ) );

		fixedInterpolators.put( InterpolationChoice.SUM_OF_RATIOS_FIXED, new SumOfRatiosInterpolator() );
		fixedInterpolators.put( InterpolationChoice.LINEAR_FIXED, new LinearInterpolator() );
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
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			numFramesPerPeriod = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			fixedInterpolatorsPeriodLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
					FIXED_INTERP_MILLIS );

			eventProcessor = new ControllerEventProcessor( notePeriodLength );

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
			minSamplesBetweenNotes = 1000000000L; // A second
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
		final boolean noteConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.CONSUMER_NOTE );
		final MadChannelBuffer noteCb = channelBuffers[ ControllerToCvMadDefinition.CONSUMER_NOTE ];
		final boolean outCvConnected = channelConnectedFlags.get( ControllerToCvMadDefinition.PRODUCER_CV_OUT );
		final MadChannelBuffer outCvCb = channelBuffers[ ControllerToCvMadDefinition.PRODUCER_CV_OUT ];

		eventProcessor.setDesiredMapping( desiredMapping );

		if( noteConnected )
		{
			final MadChannelNoteEvent[] noteEvents = noteCb.noteBuffer;
			final int numNotes = noteCb.numElementsInBuffer;

			if( numNotes >= 2 )
			{
				final int firstNoteIndex = noteEvents[0].getEventSampleIndex();
				final int secondNoteIndex = noteEvents[1].getEventSampleIndex();

				final int diff = secondNoteIndex - firstNoteIndex;

				if( diff > 0 && diff < minSamplesBetweenNotes )
				{
					minSamplesBetweenNotes = diff;
					final long nanosBetween = AudioTimingUtils.getNumNanosecondsForBufferLength( sampleRate, (int)minSamplesBetweenNotes );
					final float millisBetween = (nanosBetween / 1000000.0f);
					log.trace("Minimum samples between notes is now " + minSamplesBetweenNotes + " which is " + millisBetween + " millis" );
					final float secondsBetween = millisBetween / 1000.0f;
					final float numEventsPerSecond = 1.0f / secondsBetween;
					log.trace("This is " + secondsBetween + " seconds between with " + numEventsPerSecond + " eps");
				}
			}

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

				// Hack until I get unified event/note processing working
				if( frameOffset != 0 || numFrames != numFramesPerPeriod )
				{
					return RealtimeMethodReturnCodeEnum.SUCCESS;
				}

				eventProcessor.emptyPeriod( numFrames );
			}
			else
			{
				// Hack until I get unified event/note processing working
				if( frameOffset != 0 || numFrames != numFramesPerPeriod )
				{
					return RealtimeMethodReturnCodeEnum.SUCCESS;
				}

				// Process the messages
				for( int n = 0 ; n < numNotes ; n++ )
				{
					final MadChannelNoteEvent ne = noteEvents[ n ];
					switch( ne.getEventType() )
					{
						case CONTROLLER:
						{
							// Only process events on our channel
							if( (desiredChannel == -1 || desiredChannel == ne.getChannel() )
								&&
								(desiredController == -1 || desiredController == ne.getParamOne() )
								)
							{
	//							log.debug("Processing event " + ne.toString() );
								eventProcessor.processEvent( ne );
							}
							break;
						}
						default:
						{
							break;
						}
					}
				}

				if( numNotes == 0 )
				{
					eventProcessor.emptyPeriod( numFrames );
				}
			}

			if( outCvConnected )
			{
				final float[] outCvFloats = outCvCb.floatBuffer;
				// Spit out values.
				eventProcessor.outputCv( numFrames, outCvFloats, currentInterpolator );
				eventProcessor.done();
			}
			else
			{
				eventProcessor.done();
			}
		}
		else if( outCvConnected )
		{
			if( isLearning )
			{

			}
			else
			{
				final float[] outCvFloats = outCvCb.floatBuffer;

				eventProcessor.emptyPeriod( numFrames );

				// Output nothing.
				eventProcessor.outputCv( numFrames, outCvFloats, currentInterpolator );
			}
		}
		currentInterpolator.checkForDenormal();

		return RealtimeMethodReturnCodeEnum.SUCCESS;
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
}
