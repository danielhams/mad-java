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

package uk.co.modularaudio.mads.base.limiter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.limiter.ui.LimiterHardLimitCheckboxUiJComponent;
import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.dsp.LimiterFast;
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
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LimiterFallofSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LimiterKneeSliderModel;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class LimiterMadInstance extends MadInstance<LimiterMadDefinition,LimiterMadInstance>
{
//	private static Log log = LogFactory.getLog( LimiterMadInstance.class.getName() );

	private final CDSpringAndDamperDouble24Interpolator kneeSad = new CDSpringAndDamperDouble24Interpolator();
	private final CDSpringAndDamperDouble24Interpolator falloffSad = new CDSpringAndDamperDouble24Interpolator();

//	private final LimiterTanhApprox limiter = new LimiterTanhApprox( LimiterKneeSliderModel.DEFAULT_KNEE, LimiterFallofSliderModel.DEFAULT_FALLOFF );;
	private final LimiterFast limiter = new LimiterFast( LimiterKneeSliderModel.DEFAULT_KNEE, LimiterFallofSliderModel.DEFAULT_FALLOFF );;

	private boolean desiredUseHardLimit = LimiterHardLimitCheckboxUiJComponent.DEFAULT_USE_HARD_LIMIT;

	public LimiterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final LimiterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void start( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		final int periodLengthFrames = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
		final int interpolatorLengthFrames = periodLengthFrames;

		kneeSad.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, interpolatorLengthFrames );
		falloffSad.resetSampleRateAndPeriod( sampleRate, periodLengthFrames, interpolatorLengthFrames );
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
		//
		final boolean inLConnected = channelConnectedFlags.get( LimiterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ LimiterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );

		final boolean inRConnected = channelConnectedFlags.get( LimiterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ LimiterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( LimiterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ LimiterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );

		final boolean outRConnected = channelConnectedFlags.get( LimiterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ LimiterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );

		final float[] tmpArray = tempQueueEntryStorage.temporaryFloatArray;

		final int kneeTmpIndex = 0;
		final int falloffTmpIndex = kneeTmpIndex + numFrames;

		kneeSad.generateControlValues( tmpArray, kneeTmpIndex, numFrames );
		boolean isSteadyState = kneeSad.checkForDenormal();

		falloffSad.generateControlValues( tmpArray, falloffTmpIndex, numFrames );
		isSteadyState = isSteadyState & falloffSad.checkForDenormal();

		if( isSteadyState )
		{
			limiter.setFalloff( falloffSad.getValue() );
			limiter.setKnee( kneeSad.getValue() );
		}
		// Copy over the data from ins to outs and then filter
		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, frameOffset, frameOffset + numFrames, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			System.arraycopy( inLfloats, frameOffset, outLfloats, frameOffset, numFrames );

			if( desiredUseHardLimit )
			{
				for( int s = 0 ; s < numFrames ; ++s )
				{
					final float curVal = outLfloats[frameOffset+s];
					final float knee = tmpArray[kneeTmpIndex+s];
					final int sign = curVal < 0.0f ? -1 : 1;
					float absVal = curVal * sign;
					if( absVal > knee )
					{
						absVal = knee;
					}
					outLfloats[frameOffset+s] = absVal * sign;
				}
			}
			else
			{
				if( isSteadyState )
				{
					limiter.filter( outLfloats, frameOffset, numFrames );
				}
				else
				{
					limiter.filter( outLfloats, frameOffset, numFrames,
							tmpArray, kneeTmpIndex,
							tmpArray, falloffTmpIndex );
				}
			}
		}

		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, frameOffset, frameOffset + numFrames, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			System.arraycopy( inRfloats, frameOffset, outRfloats, frameOffset, numFrames );
			if( desiredUseHardLimit )
			{
				for( int s = 0 ; s < numFrames ; ++s )
				{
					final float curVal = outRfloats[frameOffset+s];
					final float knee = tmpArray[kneeTmpIndex+s];
					final int sign = curVal < 0.0f ? -1 : 1;
					float absVal = curVal * sign;
					if( absVal > knee )
					{
						absVal = knee;
					}
					outRfloats[frameOffset+s] = absVal * sign;
				}
			}
			else
			{
				if( isSteadyState )
				{
					limiter.filter( outRfloats, frameOffset, numFrames );
				}
				else
				{
					limiter.filter( outRfloats, frameOffset, numFrames,
							tmpArray, kneeTmpIndex,
							tmpArray, falloffTmpIndex );
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setKnee( final float kn )
	{
		kneeSad.notifyOfNewValue( kn );
	}

	public void setFalloff( final float fo )
	{
		falloffSad.notifyOfNewValue( fo );
	}

	public void setUseHardLimit( final boolean useHardLimit )
	{
		this.desiredUseHardLimit = useHardLimit;
	}
}
