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
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.MoogFilter;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MoogFilterMadInstance extends MadInstance<MoogFilterMadDefinition,MoogFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( MoogFilterMadInstance.class.getName() );

	private int sampleRate = -1;
	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	public FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	public float desiredFrequency = 400.0f;
	public float desiredQ = 1.0f;

	protected float curFrequency = 400.0f;
	protected float curQ = 1.0f;

	protected MoogFilter leftFilter = new MoogFilter();
	protected MoogFilter rightFilter = new MoogFilter();

	private float[] tmpFreq;
	private float[] tmpQ;

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
			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;

			final int numFramesPerPeriod = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			tmpFreq = new float[ numFramesPerPeriod ];
			tmpQ = new float[ numFramesPerPeriod ];

			leftFilter.reset();
			rightFilter.reset();
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
			final MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		final boolean inLConnected = channelConnectedFlags.get( MoogFilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		final boolean inRConnected = channelConnectedFlags.get( MoogFilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );
//		boolean inCvFreqConnected = channelConnectedFlags.get(  MoogFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
//		MadChannelBuffer inFreq = channelBuffers[ MoogFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
//		float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		final boolean outRConnected = channelConnectedFlags.get( MoogFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ MoogFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );

		if( inLConnected || inRConnected )
		{
			for( int s = 0 ; s < numFrames ; ++s )
			{
				curFrequency = (curFrequency * curValueRatio) + (desiredFrequency * newValueRatio);
				curQ = (curQ * curValueRatio) + (desiredQ * newValueRatio);
				tmpFreq[s] = curFrequency / (sampleRate /2.0f);
				tmpQ[s] = curQ;
			}
		}

		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				leftFilter.filter( tmpFreq, tmpQ, inLfloats, 0, outLfloats, 0, numFrames);
			}
			else
			{
				System.arraycopy(inLfloats, 0, outLfloats, 0, numFrames);
			}
		}

		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				rightFilter.filter( tmpFreq, tmpQ, inRfloats, 0, outRfloats, 0, numFrames);
			}
			else
			{
				System.arraycopy(inRfloats, 0, outRfloats, 0, numFrames);
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void recomputeFilterParameters()
	{
		if( desiredFilterMode != FrequencyFilterMode.NONE )
		{
//			leftFilterRt.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
//			rightFilterRt.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
		}
	}
}
