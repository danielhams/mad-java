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
	private static final int CUTOFF_VALUE_CHASE_MILLIS = 20;
	private static final int Q_VALUE_CHASE_MILLIS = 10;

	private final static float CUTOFF_MIN = 0.0f;
	private final static float CUTOFF_MAX = 1.0f;
	private final static float Q_MIN = 0.1f;
	private final static float Q_MAX = 4.0f;

	// Parameters for the mapping from user visible vals to internal sensible filter values
	private static final float CUTOFF_START_VAL = 0.007043739f;
	private static final float CUTOFF_RANGE = 1.0f - CUTOFF_START_VAL;
	private static final float Q_START_VAL = 3.0f;
	private static final float Q_RANGE = 4.0f - Q_START_VAL;

	// Instance related vars
	int sampleRate = DataRate.CD_QUALITY.getValue();

	private FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	private float desiredNormalisedCutoff = 1.0f;
	private float desiredNormalisedQ = 0.0f;
	private float desiredCutoff = 1.0f;
	private float desiredQ = 0.0f;

	protected MoogFilter leftFilter = new MoogFilter();
	protected MoogFilter rightFilter = new MoogFilter();

	private final SpringAndDamperDoubleInterpolator cutoffSad = new SpringAndDamperDoubleInterpolator( CUTOFF_MIN, CUTOFF_MAX );
	private final SpringAndDamperDoubleInterpolator qSad = new SpringAndDamperDoubleInterpolator( Q_MIN, Q_MAX );

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
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			leftFilter.reset();
			rightFilter.reset();

			cutoffSad.reset( sampleRate, CUTOFF_VALUE_CHASE_MILLIS );
			cutoffSad.hardSetValue( desiredCutoff );
			qSad.reset( sampleRate, Q_VALUE_CHASE_MILLIS );
			qSad.hardSetValue( desiredQ );
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
//		boolean inCvFreqConnected = channelConnectedFlags.get(  MoogFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
//		MadChannelBuffer inFreq = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
//		float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = outLcb.floatBuffer;
		final boolean outRConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = outRcb.floatBuffer;

		final int cutoffOffset = 0;
		final int qOffset = numFrames;

		if( inLConnected || inRConnected )
		{
			cutoffSad.generateControlValues( tmpArray, cutoffOffset, numFrames );
			cutoffSad.checkForDenormal();
			qSad.generateControlValues( tmpArray, qOffset, numFrames );
			qSad.checkForDenormal();
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
				}
			}
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
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFilterMode( final FrequencyFilterMode mode )
	{
		this.desiredFilterMode = mode;
	}

	private static final float mapUserQToFilterQ( final float normQ, final float normCutoff )
	{
		return normQ * ((normCutoff * Q_RANGE) + Q_START_VAL);
	}

	private void recomputeParams()
	{
		// Scaled user supplied values into something sensible for the actual filter
		desiredCutoff = (desiredNormalisedCutoff * CUTOFF_RANGE) + CUTOFF_START_VAL;
		desiredQ = mapUserQToFilterQ( desiredNormalisedQ, desiredNormalisedCutoff );

		// Feed these values to the interpolators
		cutoffSad.notifyOfNewValue( desiredCutoff );
		qSad.notifyOfNewValue( desiredQ );
	}

	public void setDesiredNormalisedCutoff( final float cutoff )
	{
		this.desiredNormalisedCutoff = cutoff;
		recomputeParams();
	}

	public void setDesiredNormalisedQ( final float Q )
	{
		this.desiredNormalisedQ = Q;
		recomputeParams();
	}
}
