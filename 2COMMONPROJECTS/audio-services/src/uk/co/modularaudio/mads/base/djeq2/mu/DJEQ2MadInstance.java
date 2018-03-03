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

package uk.co.modularaudio.mads.base.djeq2.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.dsp.ButterworthCrossover24DB;
import uk.co.modularaudio.util.audio.dsp.LimiterCrude;
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

public class DJEQ2MadInstance extends MadInstance<DJEQ2MadDefinition, DJEQ2MadInstance>
{
//	private static Log log = LogFactory.getLog( DJEQMadInstance.class.getName() );

	private final static float MAX_EQ_OVERDRIVE = AudioMath.dbToLevelF( 10.0f );

	private final static float LP_CROSSOVER_FREQ = 80.0f;

	private int sampleRate;
	private int sampleFramesPerFrontEndPeriod;

	private boolean active;
	private int numSamplesProcessed;
	private float curLeftMeterReading;
	private float curRightMeterReading;

	private final SpringAndDamperDouble24Interpolator highSad = new SpringAndDamperDouble24Interpolator();
	private final SpringAndDamperDouble24Interpolator lowSad = new SpringAndDamperDouble24Interpolator();

	private final SpringAndDamperDouble24Interpolator faderSad = new SpringAndDamperDouble24Interpolator();

	private final ButterworthCrossover24DB leftLpCoFilter = new ButterworthCrossover24DB();
	private final ButterworthCrossover24DB leftNonLpCoFilter = new ButterworthCrossover24DB();

	private final ButterworthCrossover24DB rightLpCoFilter = new ButterworthCrossover24DB();
	private final ButterworthCrossover24DB rightNonLpCoFilter = new ButterworthCrossover24DB();

	private final LimiterCrude limiterRt = new LimiterCrude( 0.99, 5 );

	public DJEQ2MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final DJEQ2MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		highSad.resetLowerUpperBounds( 0.0f, MAX_EQ_OVERDRIVE );
		highSad.hardSetValue( 1.0f );
		lowSad.resetLowerUpperBounds( 0.0f, MAX_EQ_OVERDRIVE );
		lowSad.hardSetValue( 1.0f );

		faderSad.resetLowerUpperBounds( 0.0f, 1.0f );
		faderSad.hardSetValue( 0.0f );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		sampleFramesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
		numSamplesProcessed = 0;
		curLeftMeterReading = 0.0f;
		curRightMeterReading = 0.0f;

		highSad.reset( sampleRate );
		lowSad.reset( sampleRate );

		faderSad.reset( sampleRate );

		leftLpCoFilter.clear();;
		leftNonLpCoFilter.clear();

		rightLpCoFilter.clear();;
		rightNonLpCoFilter.clear();
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public final RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters,
			final int U_periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		final int hiCvOffset = 0;
		final int lowCvOffset = numFrames * 1;
		final int faderCvOffset = numFrames * 2;

		// Generate our control values (used by both left and right)
		highSad.generateControlValues( tmpBuffer, hiCvOffset, numFrames );
		highSad.checkForDenormal();
		lowSad.generateControlValues( tmpBuffer, lowCvOffset, numFrames );
		lowSad.checkForDenormal();
		faderSad.generateControlValues( tmpBuffer, faderCvOffset, numFrames );
		faderSad.checkForDenormal();

		final float[] leftInputBuffer = channelBuffers[ DJEQ2MadDefinition.CONSUMER_WAVE_LEFT ].floatBuffer;
		final float[] leftOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_WAVE_LEFT ].floatBuffer;
		final float[] rightInputBuffer = channelBuffers[ DJEQ2MadDefinition.CONSUMER_WAVE_RIGHT ].floatBuffer;
		final float[] rightOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_WAVE_RIGHT ].floatBuffer;

		final float[] leftHighOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_HIGH_LEFT ].floatBuffer;
		final float[] leftLowOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_LOW_LEFT ].floatBuffer;

		leftLpCoFilter.filter( leftInputBuffer, frameOffset, numFrames, LP_CROSSOVER_FREQ, sampleRate,
				leftLowOutputBuffer, frameOffset, leftHighOutputBuffer, frameOffset );

		final float[] rightHighOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_HIGH_RIGHT ].floatBuffer;
		final float[] rightLowOutputBuffer = channelBuffers[ DJEQ2MadDefinition.PRODUCER_LOW_RIGHT ].floatBuffer;

		rightLpCoFilter.filter( rightInputBuffer, frameOffset, numFrames, LP_CROSSOVER_FREQ, sampleRate,
				rightLowOutputBuffer, frameOffset, rightHighOutputBuffer, frameOffset );

		for( int i = 0 ; i < numFrames ; ++i )
		{
			final float faderVal = tmpBuffer[faderCvOffset+i];
			final float lowMultiplier = tmpBuffer[lowCvOffset+i] * faderVal;
			final float hiMultiplier = tmpBuffer[hiCvOffset+i] * faderVal;

			leftLowOutputBuffer[frameOffset + i] *= lowMultiplier;
			leftHighOutputBuffer[frameOffset + i] *= hiMultiplier;
			leftOutputBuffer[frameOffset + i] =
					leftLowOutputBuffer[frameOffset + i]
					+
					leftHighOutputBuffer[frameOffset + i];

			rightLowOutputBuffer[frameOffset + i] *= lowMultiplier;
			rightHighOutputBuffer[frameOffset + i] *= hiMultiplier;
			rightOutputBuffer[frameOffset + i] =
					rightLowOutputBuffer[frameOffset + i]
					+
					rightHighOutputBuffer[frameOffset + i];
		}

		int currentSampleIndex = 0;
		while( currentSampleIndex < numFrames )
		{
			if( active && numSamplesProcessed >= sampleFramesPerFrontEndPeriod )
			{
				final int U_emitFrameTime = U_periodStartFrameTime + frameOffset + currentSampleIndex;

				emitMeterReading( tempQueueEntryStorage,
						U_emitFrameTime,
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

	public void setDesiredLpAmp( final float ampVal )
	{
		lowSad.notifyOfNewValue( ampVal );
	}

	public void setDesiredFaderAmp( final float ampVal )
	{
		faderSad.notifyOfNewValue( ampVal );
	}

	public void emitMeterReading( final ThreadSpecificTemporaryEventStorage tses,
			final int U_frameTime,
			final float leftReading,
			final float rightReading )
	{
		if( active )
		{
			final int lFloatIntBits = Float.floatToIntBits( leftReading );
			final int rFloatIntBits = Float.floatToIntBits( rightReading );
			final long joinedParts = ((long)lFloatIntBits << 32) | rFloatIntBits;
			localBridge.queueTemporalEventToUi( tses, U_frameTime, DJEQ2IOQueueBridge.COMMAND_OUT_METER_READINGS, joinedParts, null );
		}
	}

	public void setActive( final boolean active )
	{
		this.active = active;
	}
}