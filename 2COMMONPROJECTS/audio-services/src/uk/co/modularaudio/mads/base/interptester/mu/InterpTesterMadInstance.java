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

package uk.co.modularaudio.mads.base.interptester.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.interptester.ui.InterpTesterModelChoiceUiJComponent;
import uk.co.modularaudio.mads.base.interptester.ui.InterpTesterValueChaseMillisSliderUiJComponent;
import uk.co.modularaudio.mads.base.interptester.utils.InterpTesterSliderModels;
import uk.co.modularaudio.mads.base.interptester.utils.SliderModelValueConverter;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class InterpTesterMadInstance extends MadInstance<InterpTesterMadDefinition, InterpTesterMadInstance>
{
	private static Log log = LogFactory.getLog( InterpTesterMadInstance.class.getName() );

	private static final int NUM_INTERPOLATORS = InterpolatorType.values().length;

	private final ControlValueInterpolator[] noTsInterpolators = new ControlValueInterpolator[NUM_INTERPOLATORS];
	private final ControlValueInterpolator[] tsInterpolators = new ControlValueInterpolator[NUM_INTERPOLATORS];

	private final int[] tsInterpolatorsDurations = new int[NUM_INTERPOLATORS];

	private int sampleRate = DataRate.CD_QUALITY.getValue();
	private int periodLengthFrames = 1024;

	private float desValueChaseMillis = InterpTesterValueChaseMillisSliderUiJComponent.DEFAULT_CHASE_MILLIS;
	private int desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
			desValueChaseMillis );

	private int framesBetweenUiEvents;
	private int numFramesToNextUiEvent;

	private boolean uiActive;

	private final InterpTesterSliderModels sliderModels = new InterpTesterSliderModels();

	private int modelIndex = InterpTesterModelChoiceUiJComponent.DEFAULT_MODEL_CHOICE.ordinal();

	private enum ImpulseState
	{
		NO_IMPULSE,
		IMPULSE_MAX,
		IMPULSE_MIN
	};

	private boolean doImpulse = false;
	private int numImpulseSamplesLeft = 0;
	private ImpulseState impulseState = ImpulseState.NO_IMPULSE;

	private boolean haveTsValueWaiting = false;
	private float tsValueWaiting = 0.0f;

	public InterpTesterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final InterpTesterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		try
		{
			for( final InterpolatorType it : InterpolatorType.values() )
			{
				final Class<? extends ControlValueInterpolator> clazz = it.getInterpolatorClass();

				final ControlValueInterpolator noTsI = clazz.newInstance();
				noTsInterpolators[it.ordinal()] = noTsI;

				final ControlValueInterpolator tsI = clazz.newInstance();
				tsInterpolators[it.ordinal()] = tsI;
			}

			final SliderDisplayModel sdm = sliderModels.getModelAt( modelIndex );
			final SliderModelValueConverter smvc = sliderModels.getValueConverterAt( modelIndex );
			final float minModelValue = sdm.getMinValue();
			final float maxModelValue = sdm.getMaxValue();

			final float minValue = (smvc == null ? minModelValue : smvc.convertValue( minModelValue ) );
			final float maxValue = (smvc == null ? maxModelValue : smvc.convertValue( maxModelValue ) );

			for( final ControlValueInterpolator cvi : noTsInterpolators )
			{
				cvi.resetLowerUpperBounds( minValue, maxValue );
			}
			for( final ControlValueInterpolator cvi : tsInterpolators )
			{
				cvi.resetLowerUpperBounds( minValue, maxValue );
			}
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught instantiating interpolators: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		periodLengthFrames = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();

		desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, desValueChaseMillis );

		// The nots interpolators default the interpolation period
		// to the current audio period
		for( final ControlValueInterpolator cvi : noTsInterpolators )
		{
			cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, periodLengthFrames );
		}
		for( final ControlValueInterpolator cvi : tsInterpolators )
		{
			cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, desValueChaseSamples );
		}

		// 6 updates a second is fine for the nanos duration of the interpolators
		framesBetweenUiEvents = sampleRate / 6;
		numFramesToNextUiEvent = 0;
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
			final MadChannelBuffer[] channelBuffers ,
			final int iFrameOffset,
			final int iNumFrames  )
	{
		if( haveTsValueWaiting )
		{
			for( final ControlValueInterpolator cvi : noTsInterpolators )
			{
				cvi.notifyOfNewValue( tsValueWaiting );
			}
			haveTsValueWaiting = false;
		}

		int frameOffset = iFrameOffset;
		int numFrames = iNumFrames;

		if( numFramesToNextUiEvent <= 0 && uiActive )
		{
			for( final InterpolatorType it : InterpolatorType.values() )
			{
				final int interpolatorIndex = it.ordinal();
				final int interpolatorNanos = tsInterpolatorsDurations[interpolatorIndex];
				sendInterpolatorNanos( tempQueueEntryStorage,
						periodStartFrameTime,
						interpolatorIndex,
						interpolatorNanos );
			}

			numFramesToNextUiEvent = framesBetweenUiEvents;
		}

		if( doImpulse )
		{
			final SliderDisplayModel sdm = sliderModels.getModelAt( modelIndex );
			final SliderModelValueConverter smvc = sliderModels.getValueConverterAt( modelIndex );
			final float minModelValue = sdm.getMinValue();
			final float maxModelValue = sdm.getMaxValue();

			final float minValue = (smvc == null ? minModelValue : smvc.convertValue( minModelValue ) );
			final float maxValue = (smvc == null ? maxModelValue : smvc.convertValue( maxModelValue ) );

			while( numFrames > 0 )
			{
				final int framesThisRound = (numFrames < numImpulseSamplesLeft ? numFrames : numImpulseSamplesLeft );

				switch( impulseState )
				{
					case NO_IMPULSE:
					{
						setDesiredAmp( maxValue );
						setDesiredAmpNoTs( maxValue );
						fillInterpolationBuffers( channelBuffers, frameOffset, 1 );
						frameOffset++;
						numFrames--;
						numImpulseSamplesLeft--;
						impulseState = ImpulseState.IMPULSE_MAX;
//						log.trace("Did one frame and switched to impulse max");
						break;
					}
					case IMPULSE_MAX:
					{
						fillInterpolationBuffers( channelBuffers, frameOffset, framesThisRound );
						frameOffset += framesThisRound;
						numFrames -= framesThisRound;
						numImpulseSamplesLeft -= framesThisRound;

//						log.trace("Did " + framesThisRound + " impulse max frames");

						if( numImpulseSamplesLeft == 0 )
						{
							numImpulseSamplesLeft = desValueChaseSamples;
							impulseState = ImpulseState.IMPULSE_MIN;
							setDesiredAmp( minValue );
							setDesiredAmpNoTs( minValue );
//							log.trace( "Finished impulse max, switched to impulse min" );
						}
						break;
					}
					case IMPULSE_MIN:
					{
						fillInterpolationBuffers( channelBuffers, frameOffset, framesThisRound );
						frameOffset += framesThisRound;
						numFrames -= framesThisRound;
						numImpulseSamplesLeft -= framesThisRound;

//						log.trace("Did " + framesThisRound + " impulse min frames");

						if( numImpulseSamplesLeft == 0 )
						{
							impulseState = ImpulseState.NO_IMPULSE;
							doImpulse = false;
//							log.trace( "Finished impulse min, switched off impulse" );
							fillInterpolationBuffers( channelBuffers, frameOffset, numFrames );
							numFrames = 0;
						}
						break;
					}
				}
			}
		}

		if( numFrames > 0 )
		{
			fillInterpolationBuffers( channelBuffers, frameOffset, numFrames );
		}

		numFramesToNextUiEvent -= numFrames;

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private void sendInterpolatorNanos( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final long periodStartFrameTime, final int interpolatorIndex, final int interpolatorNanos )
	{
		final long value = ((long)interpolatorIndex << 32) | interpolatorNanos;
		localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
				periodStartFrameTime,
				InterpTesterIOQueueBridge.COMMAND_TO_UI_INTERP_NANOS,
				value,
				null );
	}

	private void fillInterpolationBuffers( final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		int channelNum = 0;
		for( final ControlValueInterpolator cvi : noTsInterpolators )
		{
			final float[] iBuf = channelBuffers[ channelNum ].floatBuffer;
			cvi.generateControlValues( iBuf, frameOffset, numFrames );
			cvi.checkForDenormal();
			channelNum++;
		}
		int interpolatorNum = 0;
		for( final ControlValueInterpolator cvi : tsInterpolators )
		{
			final float[] iBuf = channelBuffers[ channelNum ].floatBuffer;
			final long tsBefore = System.nanoTime();
			cvi.generateControlValues( iBuf, frameOffset, numFrames );
			cvi.checkForDenormal();
			final long tsAfter = System.nanoTime();
			final long diff = tsAfter - tsBefore;
			tsInterpolatorsDurations[interpolatorNum] = (int)diff;
			channelNum++;
			interpolatorNum++;
		}
	}

	public void setDesiredAmp( final float amp )
	{
//		log.trace( "Received amp change: " +
//				MathFormatter.fastFloatPrint( amp, 12, true ) );

		// Set all the TS based interpolators
		for( final ControlValueInterpolator cvi : tsInterpolators )
		{
			cvi.notifyOfNewValue( amp );
		}
	}

	public void setDesiredAmpNoTs( final float amp )
	{
		haveTsValueWaiting = true;
		tsValueWaiting = amp;
	}

	public void setChaseMillis( final float chaseMillis )
	{
		desValueChaseMillis = chaseMillis;
		desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, desValueChaseMillis );

//		for( final ControlValueInterpolator cvi : noTsInterpolators )
//		{
//			cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, desValueChaseSamples );
//		}
		for( final ControlValueInterpolator cvi : tsInterpolators )
		{
			cvi.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, desValueChaseSamples );
		}
	}

	public void setUiActive( final boolean active )
	{
		this.uiActive = active;
	}

	public void setModelIndex( final int value )
	{
		modelIndex = value;
		final SliderDisplayModel sdm = sliderModels.getModelAt( modelIndex );
		final SliderModelValueConverter smvc = sliderModels.getValueConverterAt( modelIndex );
		final float minModelValue = sdm.getMinValue();
		final float maxModelValue = sdm.getMaxValue();

		final float minValue = (smvc == null ? minModelValue : smvc.convertValue( minModelValue ) );
		final float maxValue = (smvc == null ? maxModelValue : smvc.convertValue( maxModelValue ) );

//		if( log.isTraceEnabled() )
//		{
//			log.trace("Resetting min max values to " + minValue + " " + maxValue );
//		}

		for( final ControlValueInterpolator cvi : noTsInterpolators )
		{
			cvi.resetLowerUpperBounds( minValue, maxValue );
		}
		for( final ControlValueInterpolator cvi : tsInterpolators )
		{
			cvi.resetLowerUpperBounds( minValue, maxValue );
		}
	}

	public InterpTesterSliderModels getModels()
	{
		return sliderModels;
	}

	public void startImpulse()
	{
		doImpulse = true;
		numImpulseSamplesLeft = periodLengthFrames+1;
	}
}
