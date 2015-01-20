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
import uk.co.modularaudio.util.audio.dsp.RBJFilterRT;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class RBJFilterMadInstance extends MadInstance<RBJFilterMadDefinition,RBJFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadInstance.class.getName() );
	
	private int sampleRate = -1;

	public FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	public float desiredFrequency = 80.0f;
	public float desiredQ = 20.0f;
	
	protected RBJFilterRT leftFilterRT = new RBJFilterRT();
	protected RBJFilterRT rightFilterRT = new RBJFilterRT();
	
	public RBJFilterMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			RBJFilterMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		boolean inLConnected = channelConnectedFlags.get( RBJFilterMadDefinition.CONSUMER_IN_LEFT );
		MadChannelBuffer inLcb = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_LEFT ];
		float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		boolean inRConnected = channelConnectedFlags.get( RBJFilterMadDefinition.CONSUMER_IN_RIGHT );
		MadChannelBuffer inRcb = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_RIGHT ];
		float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );

//		boolean inCvFreqConnected = channelConnectedFlags.get(  RBJFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
//		MadChannelBuffer inFreq = channelBuffers[ RBJFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
//		float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );
		
		boolean outLConnected = channelConnectedFlags.get( RBJFilterMadDefinition.PRODUCER_OUT_LEFT );
		MadChannelBuffer outLcb = channelBuffers[ RBJFilterMadDefinition.PRODUCER_OUT_LEFT ];
		float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		boolean outRConnected = channelConnectedFlags.get( RBJFilterMadDefinition.PRODUCER_OUT_RIGHT );
		MadChannelBuffer outRcb = channelBuffers[ RBJFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );
		
		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				RBJFilter.filterIt(leftFilterRT, inLfloats, 0, outLfloats, 0, numFrames);
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
				RBJFilter.filterIt(rightFilterRT, inRfloats, 0, outRfloats, 0, numFrames);
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
			leftFilterRT.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
			rightFilterRT.recompute(sampleRate, desiredFilterMode, desiredFrequency, desiredQ);
		}
	}
}
