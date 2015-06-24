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

package uk.co.modularaudio.mads.base.frequencyfilter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilter24DB;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
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

public class FrequencyFilterMadInstance extends MadInstance<FrequencyFilterMadDefinition,FrequencyFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadInstance.class.getName() );

	public final static float FREQ_MIN_VAL = 40.0f;
	public final static float FREQ_MAX_VAL = 22050.0f;
	public final static float FREQ_DEFAULT_VAL = 500.0f;
	public final static float BW_MIN_VAL = 40.0f;
	public final static float BW_MAX_VAL = 22050.0f;
	public final static float BW_DEFAULT_VAL = 500.0f;

	private int sampleRate;

	private FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	private float desiredFrequency = FREQ_DEFAULT_VAL;
	private float desiredBandwidth = BW_DEFAULT_VAL;
	private boolean desired24dB = false;

	private boolean was24dB = false;

	private final SpringAndDamperDoubleInterpolator freqSad = new SpringAndDamperDoubleInterpolator(
			FREQ_MIN_VAL, FREQ_MAX_VAL );
	private final SpringAndDamperDoubleInterpolator bwSad = new SpringAndDamperDoubleInterpolator( BW_MIN_VAL, BW_MAX_VAL );

	private final ButterworthFilter leftChannelButterworth = new ButterworthFilter();
	private final ButterworthFilter rightChannelButterworth = new ButterworthFilter();
	private final ButterworthFilter24DB leftChannel24db = new ButterworthFilter24DB();
	private final ButterworthFilter24DB rightChannel24db = new ButterworthFilter24DB();

	public FrequencyFilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final FrequencyFilterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		freqSad.reset( sampleRate );
		freqSad.hardSetValue( desiredFrequency );
		bwSad.reset( sampleRate );
		bwSad.hardSetValue( desiredBandwidth );

		leftChannelButterworth.clear();
		leftChannel24db.clear();
		rightChannelButterworth.clear();
		rightChannel24db.clear();
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		final boolean inLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = inLcb.floatBuffer;
		final boolean inRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = inRcb.floatBuffer;
		final boolean inCvFreqConnected = channelConnectedFlags.get(  FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
		final MadChannelBuffer inFreq = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
		final float[] inCvFreqFloats = inFreq.floatBuffer;

		final boolean outLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = outLcb.floatBuffer;
		final boolean outRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = outRcb.floatBuffer;

		final int freqOffset = 0;
		final int bwOffset = numFrames;

		// Start off true, set to false if one denormals (already converged enough)
		boolean isSteadyState = true;

		if( inLConnected || inRConnected )
		{
			if( !freqSad.checkForDenormal() || !bwSad.checkForDenormal() )
			{
				isSteadyState = false;
			}

			if( !isSteadyState )
			{
				freqSad.generateControlValues( tmpBuffer, freqOffset, numFrames );
				bwSad.generateControlValues( tmpBuffer, bwOffset, numFrames );
			}
		}

		if( desired24dB != was24dB )
		{
			leftChannel24db.clear();
			rightChannel24db.clear();
			leftChannelButterworth.clear();
			rightChannelButterworth.clear();
			was24dB = desired24dB;
		}

		if( !inLConnected )
		{
			if( outLConnected )
			{
				Arrays.fill( inLfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}

		if( outLConnected )
		{
			System.arraycopy( inLfloats, frameOffset, outLfloats, frameOffset, numFrames );

			if( inCvFreqConnected || !isSteadyState)
			{
				final float[] srcFreqs = (inCvFreqConnected ? inCvFreqFloats : tmpBuffer );
				final int srcFreqOffset = (inCvFreqConnected ? numFrames : freqOffset );
				if( desired24dB )
				{
					leftChannel24db.filterWithFreqAndBw( outLfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset,
							tmpBuffer, bwOffset,
							desiredFilterMode, sampleRate );
				}
				else
				{
					leftChannelButterworth.filterWithFreqAndBw( outLfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset,
							tmpBuffer, bwOffset,
							desiredFilterMode, sampleRate );
				}
			}
			else
			{
				if( desired24dB )
				{
					leftChannel24db.filter( outLfloats, frameOffset, numFrames,
							desiredFrequency, desiredBandwidth,
							desiredFilterMode, sampleRate );
				}
				else
				{
					leftChannelButterworth.filter( outLfloats, frameOffset, numFrames,
							desiredFrequency, desiredBandwidth,
							desiredFilterMode, sampleRate );
				}
			}
		}


		if( !inRConnected )
		{
			if( outRConnected )
			{
				Arrays.fill( inRfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}

		if( outRConnected )
		{
			System.arraycopy( inRfloats, frameOffset, outRfloats, frameOffset, numFrames );

			if( inCvFreqConnected || !isSteadyState)
			{
				final float[] srcFreqs = (inCvFreqConnected ? inCvFreqFloats : tmpBuffer );
				final int srcFreqOffset = (inCvFreqConnected ? numFrames : freqOffset );
				if( desired24dB )
				{
					rightChannel24db.filterWithFreqAndBw( outRfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset,
							tmpBuffer, bwOffset,
							desiredFilterMode, sampleRate );
				}
				else
				{
					rightChannelButterworth.filterWithFreqAndBw( outRfloats, frameOffset, numFrames,
							srcFreqs, srcFreqOffset,
							tmpBuffer, bwOffset,
							desiredFilterMode, sampleRate );
				}
			}
			else
			{
				if( desired24dB )
				{
					rightChannel24db.filter( outRfloats, frameOffset, numFrames,
							desiredFrequency, desiredBandwidth,
							desiredFilterMode, sampleRate );
				}
				else
				{
					rightChannelButterworth.filter( outRfloats, frameOffset, numFrames,
							desiredFrequency, desiredBandwidth,
							desiredFilterMode, sampleRate );
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFilterMode( final FrequencyFilterMode mode )
	{
		this.desiredFilterMode = mode;
	}

	public void setDesiredFrequency( final float freq )
	{
		this.desiredFrequency = freq;
		freqSad.notifyOfNewValue( freq );
	}

	public void setDesiredBandwidth( final float bw )
	{
		this.desiredBandwidth = bw;
		bwSad.notifyOfNewValue( bw );
	}

	public void setDesired24dB( final boolean is24 )
	{
		this.desired24dB = is24;
	}
}
