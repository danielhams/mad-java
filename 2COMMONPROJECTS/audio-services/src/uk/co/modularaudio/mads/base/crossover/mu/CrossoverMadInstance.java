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

package uk.co.modularaudio.mads.base.crossover.mu;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.dsp.ButterworthCrossover;
import uk.co.modularaudio.util.audio.dsp.ButterworthCrossover24DB;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class CrossoverMadInstance extends MadInstance<CrossoverMadDefinition,CrossoverMadInstance>
{
	private static Log LOG = LogFactory.getLog( CrossoverMadInstance.class.getName() );

	public final static float FREQ_MIN_VAL = 40.0f;
	public final static float FREQ_MAX_VAL = 22050.0f;
	public final static float FREQ_DEFAULT_VAL = 500.0f;
	public final static float BW_MIN_VAL = 40.0f;
	public final static float BW_MAX_VAL = 22050.0f;
	public final static float BW_DEFAULT_VAL = 500.0f;

	private int sampleRate;

	private float desiredFrequency = FREQ_DEFAULT_VAL;
	private boolean desired24dB = false;

	private boolean was24dB = false;

	private final SpringAndDamperDouble24Interpolator freqSad = new SpringAndDamperDouble24Interpolator();

	private final ButterworthCrossover leftChannelCrossover = new ButterworthCrossover();
	private final ButterworthCrossover rightChannelCrossover = new ButterworthCrossover();
	private final ButterworthCrossover24DB leftChannel24db = new ButterworthCrossover24DB();
	private final ButterworthCrossover24DB rightChannel24db = new ButterworthCrossover24DB();

	public CrossoverMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CrossoverMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		freqSad.resetLowerUpperBounds( FREQ_MIN_VAL, FREQ_MAX_VAL );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		freqSad.reset( sampleRate );
		freqSad.hardSetValue( desiredFrequency );

		leftChannelCrossover.clear();
		leftChannel24db.clear();
		rightChannelCrossover.clear();
		rightChannel24db.clear();
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final int U_periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		final boolean inLConnected = channelConnectedFlags.get( CrossoverMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ CrossoverMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = inLcb.floatBuffer;
		final boolean inRConnected = channelConnectedFlags.get( CrossoverMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ CrossoverMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = inRcb.floatBuffer;
		final boolean inCvFreqConnected = channelConnectedFlags.get(  CrossoverMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
		final MadChannelBuffer inFreq = channelBuffers[ CrossoverMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
		final float[] inCvFreqFloats = inFreq.floatBuffer;

		final boolean outLLConnected = channelConnectedFlags.get( CrossoverMadDefinition.PRODUCER_OUT_LOW_LEFT );
		final MadChannelBuffer outLLcb = channelBuffers[ CrossoverMadDefinition.PRODUCER_OUT_LOW_LEFT ];
		final float[] outLLfloats = outLLcb.floatBuffer;
		final boolean outHLConnected = channelConnectedFlags.get( CrossoverMadDefinition.PRODUCER_OUT_HIGH_LEFT );
		final MadChannelBuffer outHLcb = channelBuffers[ CrossoverMadDefinition.PRODUCER_OUT_HIGH_LEFT ];
		final float[] outHLfloats = outHLcb.floatBuffer;
		final boolean outLRConnected = channelConnectedFlags.get( CrossoverMadDefinition.PRODUCER_OUT_LOW_RIGHT );
		final MadChannelBuffer outLRcb = channelBuffers[ CrossoverMadDefinition.PRODUCER_OUT_LOW_RIGHT ];
		final float[] outLRfloats = outLRcb.floatBuffer;
		final boolean outHRConnected = channelConnectedFlags.get( CrossoverMadDefinition.PRODUCER_OUT_HIGH_RIGHT );
		final MadChannelBuffer outHRcb = channelBuffers[ CrossoverMadDefinition.PRODUCER_OUT_HIGH_RIGHT ];
		final float[] outHRfloats = outHRcb.floatBuffer;

		final int freqOffset = 0;
		final int bwOffset = numFrames;

		// Start off true, set to false if one denormals (already converged enough)
		boolean isSteadyState = true;

		if( inLConnected || inRConnected )
		{
			if( !freqSad.checkForDenormal() )
			{
				isSteadyState = false;
			}

			if( !isSteadyState )
			{
				freqSad.generateControlValues( tmpBuffer, freqOffset, numFrames );
			}
		}

		if( desired24dB != was24dB )
		{
			leftChannel24db.clear();
			rightChannel24db.clear();
			leftChannelCrossover.clear();
			rightChannelCrossover.clear();
			was24dB = desired24dB;
		}

		if( !inLConnected )
		{
			if( outLLConnected )
			{
				Arrays.fill( inLfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}

		final boolean isVarying = inCvFreqConnected || !isSteadyState;

		if( outLLConnected )
		{
			System.arraycopy( inLfloats, frameOffset, outLLfloats, frameOffset, numFrames );

			if( isVarying)
			{
				final float[] srcFreqs = (inCvFreqConnected ? inCvFreqFloats : tmpBuffer );
				final int srcFreqOffset = (inCvFreqConnected ? numFrames : freqOffset );
				if( desired24dB )
				{
					leftChannel24db.filterWithFreq( outLLfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset, sampleRate,
							outLLfloats, outHLfloats );
				}
				else
				{
					leftChannelCrossover.filterWithFreq( outLLfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset, sampleRate,
							outLLfloats, outHLfloats );
				}
			}
			else
			{
				if( desired24dB )
				{
					leftChannel24db.filter( outLLfloats, frameOffset, numFrames,
							desiredFrequency, sampleRate,
							outLLfloats, outHLfloats );
				}
				else
				{
					leftChannelCrossover.filter( outLLfloats, frameOffset, numFrames,
							desiredFrequency, sampleRate,
							outLLfloats, outHLfloats );
				}
			}
		}


		if( !inRConnected )
		{
			if( outLRConnected )
			{
				Arrays.fill( inRfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}

		if( outLRConnected )
		{
			System.arraycopy( inRfloats, frameOffset, outLRfloats, frameOffset, numFrames );

			if( isVarying)
			{
				final float[] srcFreqs = (inCvFreqConnected ? inCvFreqFloats : tmpBuffer );
				final int srcFreqOffset = (inCvFreqConnected ? numFrames : freqOffset );
				if( desired24dB )
				{
					rightChannel24db.filterWithFreq( outLRfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset, sampleRate,
							outLRfloats, outHRfloats );
				}
				else
				{
					rightChannelCrossover.filterWithFreq( outLRfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset, sampleRate,
							outLRfloats, outHRfloats );
				}
			}
			else
			{
				if( desired24dB )
				{
					rightChannel24db.filter( outLRfloats, frameOffset, numFrames,
							desiredFrequency, sampleRate,
							outLRfloats, outHRfloats );
				}
				else
				{
					rightChannelCrossover.filter( outLRfloats, frameOffset, numFrames,
							desiredFrequency, sampleRate,
							outLRfloats, outHRfloats );
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFrequency( final float freq )
	{
		this.desiredFrequency = freq;
		freqSad.notifyOfNewValue( freq );
	}

	public void setDesired24dB( final boolean is24 )
	{
		this.desired24dB = is24;
	}
}
