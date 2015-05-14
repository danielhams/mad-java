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
import uk.co.modularaudio.mads.base.interptester.utils.InterpTesterSliderModels;
import uk.co.modularaudio.mads.base.interptester.utils.SliderModelValueConverter;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LowPassInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperInterpolator;
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
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class InterpTesterMadInstance extends MadInstance<InterpTesterMadDefinition, InterpTesterMadInstance>
{
	private static Log log = LogFactory.getLog( InterpTesterMadInstance.class.getName() );

	private final NoneInterpolator noneInterpolator = new NoneInterpolator();
	private final LinearInterpolator liInterpolator = new LinearInterpolator();
	private final HalfHannWindowInterpolator hhInterpolator = new HalfHannWindowInterpolator();
	private final SpringAndDamperInterpolator sdInterpolator = new SpringAndDamperInterpolator( -1.0f, 1.0f );
	private final LowPassInterpolator lpInterpolator = new LowPassInterpolator();
	private final SpringAndDamperDoubleInterpolator sddInterpolator = new SpringAndDamperDoubleInterpolator( -1.0f, 1.0f );

	private final NoneInterpolator noneInterpolatorNoTs = new NoneInterpolator();
	private final HalfHannWindowInterpolator hhInterpolatorNoTs = new HalfHannWindowInterpolator();

	private int sampleRate;

	private float desValueChaseMillis = 10.0f;

//	private final byte[] lChannelMask;
	private int framesBetweenUiEvents;
	private int numFramesToNextUiEvent;

	private long lastNoneNanos;
	private long lastLinNanos;
	private long lastHHNanos;
	private long lastSDNanos;
	private long lastLPNanos;
	private long lastSDDNanos;

	private boolean uiActive;

	private final InterpTesterSliderModels sliderModels = new InterpTesterSliderModels();

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

		liInterpolator.reset( sampleRate, desValueChaseMillis );
		hhInterpolator.reset( sampleRate, desValueChaseMillis );
		sdInterpolator.reset( sampleRate );
		lpInterpolator.reset( sampleRate, desValueChaseMillis );
		sddInterpolator.reset( sampleRate );

		hhInterpolatorNoTs.reset( sampleRate, desValueChaseMillis );

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
			final int frameOffset,
			final int numFrames  )
	{
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

		final float[] rawNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_RAW_NOTS ].floatBuffer;
		noneInterpolatorNoTs.generateControlValues( rawNoTsBuf, frameOffset, numFrames );
		noneInterpolatorNoTs.checkForDenormal();

		final float[] hhNoTsBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_HALFHANN_NOTS ].floatBuffer;
		hhInterpolatorNoTs.generateControlValues( hhNoTsBuf, frameOffset, numFrames );
		hhInterpolatorNoTs.checkForDenormal();


		final float[] rawBuf = channelBuffers[ InterpTesterMadDefinition.PRODUCER_CV_RAW ].floatBuffer;
		long before = System.nanoTime();
		noneInterpolator.generateControlValues( rawBuf, frameOffset, numFrames );
		noneInterpolator.checkForDenormal();
		long after = System.nanoTime();
		lastNoneNanos = after - before;

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

		numFramesToNextUiEvent -= numFrames;

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredAmp( final float amp )
	{
//		log.trace( "Received amp change: " + amp );

		// Set all the TS based interpolators
		noneInterpolator.notifyOfNewValue( amp );
		liInterpolator.notifyOfNewValue( amp );
		hhInterpolator.notifyOfNewValue( amp );
		sdInterpolator.notifyOfNewValue( amp );
		lpInterpolator.notifyOfNewValue( amp );
		sddInterpolator.notifyOfNewValue( amp );
	}

	public void setDesiredAmpNoTs( final float amp )
	{
		noneInterpolatorNoTs.notifyOfNewValue( amp );
		hhInterpolatorNoTs.notifyOfNewValue( amp );
	}

	public void setChaseMillis( final float chaseMillis )
	{
		desValueChaseMillis = chaseMillis;
		liInterpolator.reset( sampleRate, chaseMillis );
		hhInterpolator.reset( sampleRate, chaseMillis );
		sdInterpolator.reset( sampleRate );
		lpInterpolator.reset( sampleRate, chaseMillis );
		sddInterpolator.reset( sampleRate );

		hhInterpolatorNoTs.reset( sampleRate, chaseMillis );
	}

	public void setUiActive( final boolean active )
	{
		this.uiActive = active;
	}

	public void setModelIndex( final int value )
	{
		final SliderDisplayModel sdm = sliderModels.getModelAt( value );
		final SliderModelValueConverter smvc = sliderModels.getValueConverterAt( value );
		final float minModelValue = sdm.getMinValue();
		final float maxModelValue = sdm.getMaxValue();

		final float minValue = (smvc == null ? minModelValue : smvc.convertValue( minModelValue ) );
		final float maxValue = (smvc == null ? maxModelValue : smvc.convertValue( maxModelValue ) );

		if( log.isTraceEnabled() )
		{
			log.trace("Resetting min max values to " + minValue + " " + maxValue );
		}

		noneInterpolator.resetLowerUpperBounds( minValue, maxValue );
		liInterpolator.resetLowerUpperBounds( minValue, maxValue );
		hhInterpolator.resetLowerUpperBounds( minValue, maxValue );
		sdInterpolator.resetLowerUpperBounds( minValue, maxValue );
		lpInterpolator.resetLowerUpperBounds( minValue, maxValue );
		sddInterpolator.resetLowerUpperBounds( minValue, maxValue );

		hhInterpolatorNoTs.resetLowerUpperBounds( minValue, maxValue );
	}

	public InterpTesterSliderModels getModels()
	{
		return sliderModels;
	}
}
