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

package uk.co.modularaudio.mads.base.rbjfilter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.RBJFilter;
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

public class RBJFilterMadInstance extends MadInstance<RBJFilterMadDefinition,RBJFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadInstance.class.getName() );

	private int sampleRate = -1;

	public FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	public float desiredFrequency = 80.0f;
	public float desiredQ = 20.0f;

	private final RBJFilter leftFilter = new RBJFilter();
	private final RBJFilter rightFilter = new RBJFilter();

	public RBJFilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final RBJFilterMadDefinition definition,
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		final boolean inLConnected = channelConnectedFlags.get( RBJFilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		final boolean inRConnected = channelConnectedFlags.get( RBJFilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );

//		boolean inCvFreqConnected = channelConnectedFlags.get(  RBJFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
//		MadChannelBuffer inFreq = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
//		float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( RBJFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ RBJFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		final boolean outRConnected = channelConnectedFlags.get( RBJFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ RBJFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );

		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, frameOffset, numFrames, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				leftFilter.filter( inLfloats, frameOffset, outLfloats, frameOffset, numFrames);
			}
			else
			{
				System.arraycopy(inLfloats, frameOffset, outLfloats, frameOffset, numFrames);
			}
		}

		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, frameOffset, numFrames, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				rightFilter.filter( inRfloats, frameOffset, outRfloats, frameOffset, numFrames);
			}
			else
			{
				System.arraycopy(inRfloats, frameOffset, outRfloats, frameOffset, numFrames);
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void recomputeFilterParameters()
	{
		if( desiredFilterMode != FrequencyFilterMode.NONE )
		{
			leftFilter.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
			rightFilter.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
		}
	}
}
