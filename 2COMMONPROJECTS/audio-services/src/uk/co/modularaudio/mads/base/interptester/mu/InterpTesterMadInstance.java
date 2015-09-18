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
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPassInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;
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

	private final NoneInterpolator noneInterpolator = new NoneInterpolator();
	private final SumOfRatiosInterpolator sorInterpolator = new SumOfRatiosInterpolator();
	private final LinearInterpolator liInterpolator = new LinearInterpolator();
	private final HalfHannWindowInterpolator hhInterpolator = new HalfHannWindowInterpolator();
	private final SpringAndDamperInterpolator sdInterpolator = new SpringAndDamperInterpolator( -1.0f, 1.0f );
	private final LowPassInterpolator lpInterpolator = new LowPassInterpolator();
	private final SpringAndDamperDoubleInterpolator sddInterpolator = new SpringAndDamperDoubleInterpolator( -1.0f, 1.0f );

	private final NoneInterpolator noneInterpolatorNoTs = new NoneInterpolator();
	private final SumOfRatiosInterpolator sorInterpolatorNoTs = new SumOfRatiosInterpolator();
	private final LinearInterpolator liInterpolatorNoTs = new LinearInterpolator();
	private final HalfHannWindowInterpolator hhInterpolatorNoTs = new HalfHannWindowInterpolator();
	private final LowPassInterpolator lpInterpolatorNoTs = new LowPassInterpolator();
	private final SpringAndDamperDoubleInterpolator sddInterpolatorNoTs = new SpringAndDamperDoubleInterpolator( -1.0f, 1.0f );

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private float desValueChaseMillis = InterpTesterValueChaseMillisSliderUiJComponent.DEFAULT_CHASE_MILLIS;
	private int desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate,
			desValueChaseMillis );

//	private final byte[] lChannelMask;
	private int framesBetweenUiEvents;
	private int numFramesToNextUiEvent;

	private long lastNoneNanos;
	private long lastSorNanos;
	private long lastLinNanos;
	private long lastHHNanos;
	private long lastSDNanos;
	private long lastLPNanos;
	private long lastSDDNanos;

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

	public InterpTesterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final InterpTesterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

//		final MadChannelConnectedFlags lMaskCcf = new MadChannelConnectedFlags( InterpTesterMadDefinition.NUM_CHANNELS );
//		lMaskCcf.set( InterpTesterMadDefinition.CONSUMER_CHAN1_LEFT );
//		lMaskCcf.set( InterpTesterMadDefinition.PRODUCER_OUT_LEFT );
//		lChannelMask = lMaskCcf.createMaskForSetChannels();
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, desValueChaseMillis );

		sorInterpolator.reset( sampleRate, desValueChaseMillis );
		liInterpolator.reset( sampleRate, desValueChaseMillis );
		hhInterpolator.reset( sampleRate, desValueChaseMillis );
		sdInterpolator.reset( sampleRate );
		lpInterpolator.reset( sampleRate, desValueChaseMillis );
		sddInterpolator.reset( sampleRate );

		sorInterpolatorNoTs.reset( sampleRate, desValueChaseMillis );
		liInterpolatorNoTs.reset( sampleRate, desValueChaseMillis );
		hhInterpolatorNoTs.reset( sampleRate, desValueChaseMillis );
		lpInterpolatorNoTs.reset( sampleRate, desValueChaseMillis );
		sddInterpolatorNoTs.reset( sampleRate );

		// 6 updates a second is fine for the period length
		framesBetweenUiEvents = timingParameters.getSampleFramesPerFrontEndPeriod() * 10;
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
		int frameOffset = iFrameOffset;
		int numFrames = iNumFrames;
//		final boolean lConnected = channelConnectedFlags.logicalAnd( lChannelMask );

		if( numFramesToNextUiEvent <= 0 && uiActive )
		{
			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_NONE_NANOS,
					lastNoneNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_SOR_NANOS,
					lastSorNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_LIN_NANOS,
					lastLinNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_HH_NANOS,
					lastHHNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_SD_NANOS,
					lastSDNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_LP_NANOS,
					lastLPNanos,
					null );

			localBridge.queueTemporalEventToUi( tempQueueEntryStorage,
					periodStartFrameTime,
					InterpTesterIOQueueBridge.COMMAND_TO_UI_SDD_NANOS,
					lastSDDNanos,
					null );

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

	private void fillInterpolationBuffers( final MadChannelBuffer[] channelBuffers, final int frameOffset,
			final int numFrames )
	{
		final float[] rawNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_RAW_NOTS ].floatBuffer;
		noneInterpolatorNoTs.generateControlValues( rawNoTsBuf, frameOffset, numFrames );
		noneInterpolatorNoTs.checkForDenormal();

		final float[] sorNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_SUM_OF_RATIOS_NOTS ].floatBuffer;
		sorInterpolatorNoTs.generateControlValues( sorNoTsBuf, frameOffset, numFrames );
		sorInterpolatorNoTs.checkForDenormal();

		final float[] liNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_LINEAR_NOTS ].floatBuffer;
		liInterpolatorNoTs.generateControlValues( liNoTsBuf, frameOffset, numFrames );
		liInterpolatorNoTs.checkForDenormal();

		final float[] hhNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_HALFHANN_NOTS ].floatBuffer;
		hhInterpolatorNoTs.generateControlValues( hhNoTsBuf, frameOffset, numFrames );
		hhInterpolatorNoTs.checkForDenormal();

		final float[] lpNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_LOWPASS_NOTS ].floatBuffer;
		lpInterpolatorNoTs.generateControlValues( lpNoTsBuf, frameOffset, numFrames );
		lpInterpolatorNoTs.checkForDenormal();

		final float[] sddNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_SPRINGDAMPER_DOUBLE_NOTS ].floatBuffer;
		sddInterpolatorNoTs.generateControlValues( sddNoTsBuf, frameOffset, numFrames );
		sddInterpolatorNoTs.checkForDenormal();

		final float[] rawBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_RAW ].floatBuffer;
		long before = System.nanoTime();
		noneInterpolator.generateControlValues( rawBuf, frameOffset, numFrames );
		noneInterpolator.checkForDenormal();
		long after = System.nanoTime();
		lastNoneNanos = after - before;

		final float[] sorBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_SUM_OF_RATIOS ].floatBuffer;
		before = System.nanoTime();
		sorInterpolator.generateControlValues( sorBuf, frameOffset, numFrames );
		sorInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastSorNanos = after - before;

		final float[] linearBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_LINEAR ].floatBuffer;
		before = System.nanoTime();
		liInterpolator.generateControlValues( linearBuf, frameOffset, numFrames );
		liInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastLinNanos = after - before;

		final float[] hhBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_HALFHANN ].floatBuffer;
		before = System.nanoTime();
		hhInterpolator.generateControlValues( hhBuf, frameOffset, numFrames );
		hhInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastHHNanos = after - before;

		final float[] sdBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_SPRINGDAMPER ].floatBuffer;
		before = System.nanoTime();
		sdInterpolator.generateControlValues( sdBuf, frameOffset, numFrames );
		sdInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastSDNanos = after - before;

		final float[] lpBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_LOWPASS ].floatBuffer;
		before = System.nanoTime();
		lpInterpolator.generateControlValues( lpBuf, frameOffset, numFrames );
		lpInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastLPNanos = after - before;

		final float[] sddBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_SPRINGDAMPER_DOUBLE ].floatBuffer;
		before = System.nanoTime();
		sddInterpolator.generateControlValues( sddBuf, frameOffset, numFrames );
		sddInterpolator.checkForDenormal();
		after = System.nanoTime();
		lastSDDNanos = after - before;
	}

	public void setDesiredAmp( final float amp )
	{
//		log.trace( "Received amp change: " + amp );

		// Set all the TS based interpolators
		noneInterpolator.notifyOfNewValue( amp );
		sorInterpolator.notifyOfNewValue( amp );
		liInterpolator.notifyOfNewValue( amp );
		hhInterpolator.notifyOfNewValue( amp );
		sdInterpolator.notifyOfNewValue( amp );
		lpInterpolator.notifyOfNewValue( amp );
		sddInterpolator.notifyOfNewValue( amp );
	}

	public void setDesiredAmpNoTs( final float amp )
	{
		noneInterpolatorNoTs.notifyOfNewValue( amp );
		sorInterpolatorNoTs.notifyOfNewValue( amp );
		liInterpolatorNoTs.notifyOfNewValue( amp );
		hhInterpolatorNoTs.notifyOfNewValue( amp );
		lpInterpolatorNoTs.notifyOfNewValue( amp );
		sddInterpolatorNoTs.notifyOfNewValue( amp );
	}

	public void setChaseMillis( final float chaseMillis )
	{
		desValueChaseMillis = chaseMillis;
		desValueChaseSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, desValueChaseMillis );
		sorInterpolator.reset( sampleRate, chaseMillis );
		liInterpolator.reset( sampleRate, chaseMillis );
		hhInterpolator.reset( sampleRate, chaseMillis );
		sdInterpolator.reset( sampleRate );
		lpInterpolator.reset( sampleRate, chaseMillis );
		sddInterpolator.reset( sampleRate );

		sorInterpolatorNoTs.reset( sampleRate, chaseMillis );
		liInterpolatorNoTs.reset( sampleRate, chaseMillis );
		hhInterpolatorNoTs.reset( sampleRate, chaseMillis );
		lpInterpolatorNoTs.reset( sampleRate, chaseMillis );
		sddInterpolatorNoTs.reset( sampleRate );
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

		if( log.isTraceEnabled() )
		{
			log.trace("Resetting min max values to " + minValue + " " + maxValue );
		}

		noneInterpolator.resetLowerUpperBounds( minValue, maxValue );
		sorInterpolator.resetLowerUpperBounds( minValue, maxValue );
		liInterpolator.resetLowerUpperBounds( minValue, maxValue );
		hhInterpolator.resetLowerUpperBounds( minValue, maxValue );
		sdInterpolator.resetLowerUpperBounds( minValue, maxValue );
		lpInterpolator.resetLowerUpperBounds( minValue, maxValue );
		sddInterpolator.resetLowerUpperBounds( minValue, maxValue );

		sorInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
		liInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
		hhInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
		lpInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
		sddInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
	}

	public InterpTesterSliderModels getModels()
	{
		return sliderModels;
	}

	public void startImpulse()
	{
		doImpulse = true;
		numImpulseSamplesLeft = desValueChaseSamples;
	}
}
