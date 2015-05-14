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

package uk.co.modularaudio.mads.base.moogfilter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.dsp.MoogFilter;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MoogFilterMadInstance extends MadInstance<MoogFilterMadDefinition,MoogFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( MoogFilterMadInstance.class.getName() );

	// Parameters for the spring and dampers
	private final static float CUTOFF_MIN = 0.0f;
	private final static float CUTOFF_MAX = 1.0f;
	private final static float Q_MIN = 0.0f;
	private final static float Q_MAX = 4.0f;

	private static final float AMP_AT_ZERO = 1.0f;
	private static final float AMP_AT_ONE_QUARTER = 1.0f / 0.548f;
	private static final float AMP_AT_TWO_QUARTER = 1.0f / 0.380f;
	private static final float AMP_AT_THREE_QUARTER = 1.0f / 0.2907f;
	private static final float AMP_AT_ONE = 1.0f / 0.235f;

	private final static float AMP_CORR_MIN = 0.0f;
	private final static float AMP_CORR_MAX = AMP_AT_ONE;

	// Parameters for the mapping from user visible vals to internal sensible filter values
	private static final float CUTOFF_START_VAL = 0.007043739f;
	private static final float CUTOFF_RANGE = 1.0f - CUTOFF_START_VAL;
	private static final float Q_START_VAL = 3.0f;
	private static final float Q_RANGE = Q_MAX - Q_START_VAL;

	// Instance related vars
	int sampleRate = DataRate.CD_QUALITY.getValue();

	private FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	private float desiredCutoff = 1.0f;
	private float desiredQ = 0.0f;
	private float desiredAmp = 1.0f;

	protected MoogFilter leftFilter = new MoogFilter();
	protected MoogFilter rightFilter = new MoogFilter();

	private final SpringAndDamperDoubleInterpolator cutoffSad = new SpringAndDamperDoubleInterpolator( CUTOFF_MIN, CUTOFF_MAX );
	private final SpringAndDamperDoubleInterpolator qSad = new SpringAndDamperDoubleInterpolator( Q_MIN, Q_MAX );
	private final SpringAndDamperDoubleInterpolator ampSad = new SpringAndDamperDoubleInterpolator( AMP_CORR_MIN, AMP_CORR_MAX );

	private final Limiter outputLimiter = new Limiter( 0.99f, 20 );

	public MoogFilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final MoogFilterMadDefinition definition,
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

		leftFilter.reset();
		rightFilter.reset();

		cutoffSad.reset( sampleRate );
		cutoffSad.hardSetValue( desiredCutoff );
		qSad.reset( sampleRate );
		qSad.hardSetValue( desiredQ );
		ampSad.reset( sampleRate );
		ampSad.hardSetValue( desiredAmp );
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	private static final float mapCutoffValue( final float inValue )
	{
		return CUTOFF_START_VAL + (inValue * CUTOFF_RANGE);
	}

	private static final void processCutoffCv( final float[] cvCutoffFloats, final int cvCutoffOffset,
			final float[] cutoffOutFloats, final int cutoffOutOffset,
			final int numFrames )
	{
		for( int i = 0 ; i < numFrames ; ++i )
		{
			cutoffOutFloats[ cutoffOutOffset + i] = mapCutoffValue( cvCutoffFloats[ cvCutoffOffset + i] );
		}
	}

	private static final float mapQValue( final float inCutoff,
			final float inQ )
	{
		return (Q_START_VAL + (inCutoff * Q_RANGE)) * inQ;
	}

	private static final void processQCv( final float[] cvCutoffFloats, final int cvCutoffOffset,
			final float [] cvQFloats, final int cvQOffset,
			final float[] qOutFloats, final int qOutOffset,
			final int numFrames )
	{
		for( int i = 0 ; i < numFrames ; ++i )
		{
			qOutFloats[ qOutOffset + i ] = mapQValue( cvCutoffFloats[ cvCutoffOffset + i],
					cvQFloats[ cvQOffset + i] );
		}
	}

	private static final float mapQToAmpValue( final float inQ )
	{
		// Four point linear interpolation to some empirical values
		// Not ideal but a first stab
		if( inQ <= 0.25f )
		{
			// f(0.0) = 1.0
			// f(0.25) = 0.548
			final float normVal = inQ / 0.25f;
			return (AMP_AT_ZERO * (1.0f - normVal)) + (AMP_AT_ONE_QUARTER * normVal);
		}
		else if( inQ <= 0.5f )
		{
			final float normVal = (inQ - 0.25f) / 0.25f;
			return (AMP_AT_ONE_QUARTER * (1.0f - normVal)) + (AMP_AT_TWO_QUARTER * normVal);
		}
		else if( inQ <= 0.75f )
		{
			final float normVal = (inQ - 0.5f) / 0.25f;
			return (AMP_AT_TWO_QUARTER * (1.0f - normVal)) + (AMP_AT_THREE_QUARTER * normVal);
		}
		else
		{
			final float normVal = (inQ - 0.75f) / 0.25f;
			return (AMP_AT_THREE_QUARTER * (1.0f - normVal)) + (AMP_AT_ONE * normVal);
		}
	}

	private final static void generateAmpsFromQ( final float[] qArray,
			final int qOffset,
			final float[] ampArray,
			final int ampOffset,
			final int numFrames )
	{
		for( int i = 0 ; i < numFrames ; ++i )
		{
			ampArray[ ampOffset + i ] = mapQToAmpValue( qArray[ qOffset + i ]  );
		}
	}

	private final static void applyAmps( final float[] outFloats, final int outOffset,
			final float[] ampArray, final int ampOffset,
			final int numFrames )
	{
		for( int i = 0 ; i < numFrames ; ++i )
		{
			outFloats[ outOffset + i ] *= ampArray[ ampOffset + i ];
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final float[] tmpArray = tempQueueEntryStorage.temporaryFloatArray;

		final boolean inLConnected = channelConnectedFlags.get( MoogFilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = inLcb.floatBuffer;
		final boolean inRConnected = channelConnectedFlags.get( MoogFilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = inRcb.floatBuffer;
		final boolean inCvCutoffConnected = channelConnectedFlags.get(  MoogFilterMadDefinition.CONSUMER_IN_CV_CUTOFF );
		final MadChannelBuffer inCutoff = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_CV_CUTOFF ];
		final float[] inCvCutoffFloats = inCutoff.floatBuffer;
		final boolean inCvQConnected = channelConnectedFlags.get(  MoogFilterMadDefinition.CONSUMER_IN_CV_Q );
		final MadChannelBuffer inQ = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_CV_Q ];
		final float[] inCvQFloats = inQ.floatBuffer;

		final boolean outLConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = outLcb.floatBuffer;
		final boolean outRConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = outRcb.floatBuffer;

		final int cutoffOffset = 0;
		final int qOffset = numFrames;
		final int ampOffset = numFrames * 2;

		if( inLConnected || inRConnected )
		{
			if( inCvCutoffConnected )
			{
				processCutoffCv( inCvCutoffFloats, frameOffset,
						tmpArray, cutoffOffset,
						numFrames );
				if( inCvQConnected )
				{
					processQCv( tmpArray, cutoffOffset,
							inCvQFloats, frameOffset,
							tmpArray, qOffset,
							numFrames );

					generateAmpsFromQ( tmpArray, qOffset,
							tmpArray, ampOffset,
							numFrames );
				}
				else // !inCvQConnected
				{
					qSad.generateControlValues( tmpArray, qOffset, numFrames );
					qSad.checkForDenormal();
					processQCv( tmpArray, cutoffOffset,
							tmpArray, qOffset,
							tmpArray, qOffset,
							numFrames );

					ampSad.generateControlValues( tmpArray, ampOffset, numFrames );
					ampSad.checkForDenormal();
				}
			}
			else // !inCvCutoffConnected
			{
				cutoffSad.generateControlValues( tmpArray, cutoffOffset, numFrames );
				cutoffSad.checkForDenormal();

				processCutoffCv( tmpArray, cutoffOffset,
						tmpArray, cutoffOffset,
						numFrames );

				if( inCvQConnected )
				{
					processQCv( tmpArray, cutoffOffset,
							inCvQFloats, frameOffset,
							tmpArray, qOffset,
							numFrames );

					generateAmpsFromQ( tmpArray, qOffset,
							tmpArray, ampOffset,
							numFrames );
				}
				else // !inCvQConnected
				{
					qSad.generateControlValues( tmpArray, qOffset, numFrames );
					qSad.checkForDenormal();
					processQCv( tmpArray, cutoffOffset,
							tmpArray, qOffset,
							tmpArray, qOffset,
							numFrames );

					ampSad.generateControlValues( tmpArray, ampOffset, numFrames );
					ampSad.checkForDenormal();
				}
			}
		}

		if( outLConnected )
		{
			if( !inLConnected )
			{
				Arrays.fill( outLfloats, 0.0f );
			}
			else
			{
				if( desiredFilterMode == FrequencyFilterMode.NONE )
				{
					System.arraycopy(inLfloats, frameOffset, outLfloats, frameOffset, numFrames);
				}
				else
				{
					leftFilter.filter( tmpArray, cutoffOffset, tmpArray, qOffset, inLfloats, frameOffset, outLfloats, frameOffset, numFrames);
					applyAmps( outLfloats, frameOffset, tmpArray, ampOffset, numFrames );
				}
			}
			outputLimiter.filter( outLfloats, frameOffset, numFrames );
		}

		if( outRConnected )
		{
			if( !inRConnected )
			{
				Arrays.fill( outRfloats, 0.0f );
			}
			else
			{
				if( desiredFilterMode == FrequencyFilterMode.NONE )
				{
					System.arraycopy(inRfloats, frameOffset, outRfloats, frameOffset, numFrames);
				}
				else
				{
					rightFilter.filter( tmpArray, cutoffOffset, tmpArray, qOffset, inRfloats, frameOffset, outRfloats, frameOffset, numFrames);
					applyAmps( outRfloats, frameOffset, tmpArray, ampOffset, numFrames );
				}
			}
			outputLimiter.filter( outRfloats, frameOffset, numFrames );
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFilterMode( final FrequencyFilterMode mode )
	{
		this.desiredFilterMode = mode;
	}

	public void setDesiredNormalisedCutoff( final float cutoff )
	{
		this.desiredCutoff = cutoff;
		cutoffSad.notifyOfNewValue( desiredCutoff );
	}

	public void setDesiredNormalisedQ( final float Q )
	{
		this.desiredQ = Q;
		qSad.notifyOfNewValue( desiredQ );

		desiredAmp = mapQToAmpValue( desiredQ );
		ampSad.notifyOfNewValue( desiredAmp );
	}
}
