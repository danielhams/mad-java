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
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.dsp.LimiterRT;
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

public class LimiterMadInstance extends MadInstance<LimiterMadDefinition,LimiterMadInstance>
{
//	private static Log log = LogFactory.getLog( LimiterMadInstance.class.getName() );
	
	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	private long sampleRate = -1;

	public float hardLimit = 0.0f;
	
	private LimiterRT leftLimiterRt = null;
	private LimiterRT rightLimiterRt = null;

	protected float desiredKnee = 0.9f;
	protected float desiredFalloff = 0.0f;
	
	public float currentKnee = 0.9f;
	public float currentFalloff = 0.0f;
	
	public LimiterMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			LimiterMadDefinition definition,
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
			
			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;
			
			leftLimiterRt = new LimiterRT( currentKnee, currentFalloff );
			rightLimiterRt = new LimiterRT( currentKnee, currentFalloff );
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
		//
		boolean inLConnected = channelConnectedFlags.get( LimiterMadDefinition.CONSUMER_IN_LEFT );
		MadChannelBuffer inLcb = channelBuffers[ LimiterMadDefinition.CONSUMER_IN_LEFT ];
		float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );

		boolean inRConnected = channelConnectedFlags.get( LimiterMadDefinition.CONSUMER_IN_RIGHT );
		MadChannelBuffer inRcb = channelBuffers[ LimiterMadDefinition.CONSUMER_IN_RIGHT ];
		float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );
		
		boolean outLConnected = channelConnectedFlags.get( LimiterMadDefinition.PRODUCER_OUT_LEFT );
		MadChannelBuffer outLcb = channelBuffers[ LimiterMadDefinition.PRODUCER_OUT_LEFT ];
		float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );

		boolean outRConnected = channelConnectedFlags.get( LimiterMadDefinition.PRODUCER_OUT_RIGHT );
		MadChannelBuffer outRcb = channelBuffers[ LimiterMadDefinition.PRODUCER_OUT_RIGHT ];
		float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );
		
		// TODO find out why falloff doesn't seem to do what it should
		
		currentKnee = (currentKnee * curValueRatio ) + (desiredKnee * newValueRatio );
		currentFalloff = (currentFalloff * curValueRatio ) + (desiredFalloff * newValueRatio );
		
		leftLimiterRt.setKnee( currentKnee );
		leftLimiterRt.setFalloff( currentFalloff );
		rightLimiterRt.setKnee( currentKnee );
		rightLimiterRt.setFalloff( currentFalloff );
		
		// Copy over the data from ins to outs then apply the filtering
		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			System.arraycopy( inLfloats, 0, outLfloats, 0, numFrames );
			Limiter.filter( leftLimiterRt, outLfloats );
		}
		
		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			System.arraycopy( inRfloats, 0, outRfloats, 0, numFrames );
			Limiter.filter( rightLimiterRt, outRfloats );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
