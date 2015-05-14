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

package uk.co.modularaudio.mads.base.djeq.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilter24DB;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.Limiter;
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
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class DJEQMadInstance extends MadInstance<DJEQMadDefinition, DJEQMadInstance>
{
//	private static Log log = LogFactory.getLog( DJEQMadInstance.class.getName() );

	private final static float MAX_EQ_OVERDRIVE = AudioMath.dbToLevelF( 10.0f );

	private final static float LP_CROSSOVER_FREQ = 120.0f;
	private final static float HP_CROSSOVER_FREQ = 2500.0f;

	private int sampleRate;
	private int sampleFramesPerFrontEndPeriod;

	private boolean active;
	private int numSamplesProcessed;
	private float curLeftMeterReading;
	private float curRightMeterReading;

	private final SpringAndDamperDoubleInterpolator highSad = new SpringAndDamperDoubleInterpolator( 0.0f, MAX_EQ_OVERDRIVE );
	private final SpringAndDamperDoubleInterpolator midSad = new SpringAndDamperDoubleInterpolator( 0.0f, MAX_EQ_OVERDRIVE );
	private final SpringAndDamperDoubleInterpolator lowSad = new SpringAndDamperDoubleInterpolator( 0.0f, MAX_EQ_OVERDRIVE );

	private final SpringAndDamperDoubleInterpolator faderSad = new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f );

	private final ButterworthFilter24DB leftLpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB leftNonLpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB leftMpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB leftHpFilter = new ButterworthFilter24DB();

	private final ButterworthFilter24DB rightLpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB rightNonLpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB rightMpFilter = new ButterworthFilter24DB();
	private final ButterworthFilter24DB rightHpFilter = new ButterworthFilter24DB();

	private final Limiter limiterRt = new Limiter( 0.99, 5 );

	public DJEQMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final DJEQMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		highSad.hardSetValue( 1.0f );
		midSad.hardSetValue( 1.0f );
		lowSad.hardSetValue( 1.0f );

		faderSad.hardSetValue( 0.0f );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		sampleFramesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
		numSamplesProcessed = 0;
		curLeftMeterReading = 0.0f;
		curRightMeterReading = 0.0f;

		highSad.reset( sampleRate );
		midSad.reset( sampleRate );
		lowSad.reset( sampleRate );

		faderSad.reset( sampleRate );

		leftLpFilter.clear();
		leftNonLpFilter.clear();
		leftMpFilter.clear();
		leftHpFilter.clear();

		rightLpFilter.clear();
		rightNonLpFilter.clear();
		rightMpFilter.clear();
		rightHpFilter.clear();
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		final int hiCvOffset = 0;
		final int midCvOffset = numFrames;
		final int lowCvOffset = numFrames * 2;
		final int faderCvOffset = numFrames * 3;

		final int tmpSamples1Offset = numFrames * 4;
		final int tmpSamples2Offset = numFrames * 5;

		// Generate our control values (used by both left and right)
		highSad.generateControlValues( tmpBuffer, hiCvOffset, numFrames );
		highSad.checkForDenormal();
		midSad.generateControlValues( tmpBuffer, midCvOffset, numFrames );
		midSad.checkForDenormal();
		lowSad.generateControlValues( tmpBuffer, lowCvOffset, numFrames );
		lowSad.checkForDenormal();
		faderSad.generateControlValues( tmpBuffer, faderCvOffset, numFrames );
		faderSad.checkForDenormal();

		final float[] leftInputBuffer = channelBuffers[ DJEQMadDefinition.CONSUMER_WAVE_LEFT ].floatBuffer;
		final float[] leftOutputBuffer = channelBuffers[ DJEQMadDefinition.PRODUCER_WAVE_LEFT ].floatBuffer;

		// Zero the output we'll place things in
		Arrays.fill( leftOutputBuffer, frameOffset, frameOffset + numFrames, 0.0f );

		// Low pass
		System.arraycopy( leftInputBuffer, frameOffset, tmpBuffer, tmpSamples1Offset, numFrames );
		leftLpFilter.filter( tmpBuffer, tmpSamples1Offset, numFrames, LP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.LP, sampleRate );
		// And then add into the output using the attentuation
		for( int i = 0 ; i < numFrames ; ++i )
		{
			leftOutputBuffer[frameOffset + i] = tmpBuffer[tmpSamples1Offset + i] * tmpBuffer[lowCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		// Non low pass
		System.arraycopy( leftInputBuffer, frameOffset, tmpBuffer, tmpSamples1Offset, numFrames );
		leftNonLpFilter.filter( tmpBuffer, tmpSamples1Offset, numFrames, LP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.HP, sampleRate );

		// Band Pass
		System.arraycopy( tmpBuffer, tmpSamples1Offset, tmpBuffer, tmpSamples2Offset, numFrames );
		leftMpFilter.filter( tmpBuffer, tmpSamples2Offset, numFrames, HP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.LP, sampleRate );
		for( int i = 0 ; i < numFrames ; ++i )
		{
			leftOutputBuffer[frameOffset+i] += tmpBuffer[tmpSamples2Offset + i] * tmpBuffer[midCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		// High pass
		System.arraycopy( tmpBuffer, tmpSamples1Offset, tmpBuffer, tmpSamples2Offset, numFrames );
		leftHpFilter.filter( tmpBuffer, tmpSamples2Offset, numFrames, HP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.HP, sampleRate );
		for( int i = 0 ; i < numFrames ; ++i )
		{
			leftOutputBuffer[frameOffset+i] += tmpBuffer[tmpSamples2Offset + i] * tmpBuffer[hiCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		final float[] rightInputBuffer = channelBuffers[ DJEQMadDefinition.CONSUMER_WAVE_RIGHT ].floatBuffer;
		final float[] rightOutputBuffer = channelBuffers[ DJEQMadDefinition.PRODUCER_WAVE_RIGHT ].floatBuffer;

		// Zero the output we'll place things in
		Arrays.fill( rightOutputBuffer, frameOffset, frameOffset + numFrames, 0.0f );

		// Low pass
		System.arraycopy( rightInputBuffer, frameOffset, tmpBuffer, tmpSamples1Offset, numFrames );
		rightLpFilter.filter( tmpBuffer, tmpSamples1Offset, numFrames, LP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.LP, sampleRate );
		// And then add into the output using the attentuation
		for( int i = 0 ; i < numFrames ; ++i )
		{
			rightOutputBuffer[frameOffset + i] = tmpBuffer[tmpSamples1Offset + i] * tmpBuffer[lowCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		// Non low pass
		System.arraycopy( rightInputBuffer, frameOffset, tmpBuffer, tmpSamples1Offset, numFrames );
		rightNonLpFilter.filter( tmpBuffer, tmpSamples1Offset, numFrames, LP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.HP, sampleRate );

		// Band Pass
		System.arraycopy( tmpBuffer, tmpSamples1Offset, tmpBuffer, tmpSamples2Offset, numFrames );
		rightMpFilter.filter( tmpBuffer, tmpSamples2Offset, numFrames, HP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.LP, sampleRate );
		for( int i = 0 ; i < numFrames ; ++i )
		{
			rightOutputBuffer[frameOffset+i] += tmpBuffer[tmpSamples2Offset + i] * tmpBuffer[midCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		// High pass
		System.arraycopy( tmpBuffer, tmpSamples1Offset, tmpBuffer, tmpSamples2Offset, numFrames );
		rightHpFilter.filter( tmpBuffer, tmpSamples2Offset, numFrames, HP_CROSSOVER_FREQ, 0.0f, FrequencyFilterMode.HP, sampleRate );
		for( int i = 0 ; i < numFrames ; ++i )
		{
			rightOutputBuffer[frameOffset+i] += tmpBuffer[tmpSamples2Offset + i] * tmpBuffer[hiCvOffset+i] * tmpBuffer[faderCvOffset+i];
		}

		int currentSampleIndex = 0;
		while( currentSampleIndex < numFrames )
		{
			if( active && numSamplesProcessed >= sampleFramesPerFrontEndPeriod )
			{
				final long emitFrameTime = periodStartFrameTime + frameOffset + currentSampleIndex;

				emitMeterReading( tempQueueEntryStorage,
						emitFrameTime,
						curLeftMeterReading,
						curRightMeterReading );

				curLeftMeterReading = 0.0f;
				curRightMeterReading = 0.0f;

				numSamplesProcessed = 0;
			}

			final int numFramesAvail = numFrames - currentSampleIndex;
			final int numLeftForPeriod = ( active ? sampleFramesPerFrontEndPeriod - numSamplesProcessed : numFramesAvail );
			final int numThisRound = (numLeftForPeriod < numFramesAvail ? numLeftForPeriod : numFramesAvail );

			// Update meter reading with left/right
			final int lastIndex = currentSampleIndex + numThisRound;
			for( int s = 0 ; s < numThisRound ; ++s )
			{
				final int meterSampleIndex = frameOffset + currentSampleIndex + s;
				final float lFloat = leftOutputBuffer[ meterSampleIndex ];
				final float lAbsFloat = ( lFloat < 0.0f ? -lFloat : lFloat );
				if( lAbsFloat > curLeftMeterReading )
				{
					curLeftMeterReading = lAbsFloat;
				}
				final float rFloat = rightOutputBuffer[ meterSampleIndex ];
				final float rAbsFloat = ( rFloat < 0.0f ? -rFloat : rFloat );
				if( rAbsFloat > curRightMeterReading )
				{
					curRightMeterReading = rAbsFloat;
				}
			}

			currentSampleIndex = lastIndex;
			numSamplesProcessed += numThisRound;
		}

		// Finally run a limiter on the output
		limiterRt.filter( leftOutputBuffer, frameOffset, numFrames );
		limiterRt.filter( rightOutputBuffer, frameOffset, numFrames );

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredHpAmp( final float ampVal )
	{
		highSad.notifyOfNewValue( ampVal );
	}

	public void setDesiredBpAmp( final float ampVal )
	{
		midSad.notifyOfNewValue( ampVal );
	}

	public void setDesiredLpAmp( final float ampVal )
	{
		lowSad.notifyOfNewValue( ampVal );
	}

	public void setDesiredFaderAmp( final float ampVal )
	{
		faderSad.notifyOfNewValue( ampVal );
	}

	public void emitMeterReading( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final float leftReading,
			final float rightReading )
	{
		if( active )
		{
			final int lFloatIntBits = Float.floatToIntBits( leftReading );
			final int rFloatIntBits = Float.floatToIntBits( rightReading );
			final long joinedParts = ((long)lFloatIntBits << 32) | rFloatIntBits;
			localBridge.queueTemporalEventToUi( tses, frameTime, DJEQIOQueueBridge.COMMAND_OUT_METER_READINGS, joinedParts, null );
		}
	}

	public void setActive( final boolean active )
	{
		this.active = active;
	}
}
