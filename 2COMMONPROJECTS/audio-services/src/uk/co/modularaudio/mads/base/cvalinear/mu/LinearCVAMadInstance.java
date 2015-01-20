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

package uk.co.modularaudio.mads.base.cvalinear.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
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

public class LinearCVAMadInstance extends MadInstance<LinearCVAMadDefinition,LinearCVAMadInstance>
{
	//	private static Log log = LogFactory.getLog( OscillatorMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 10;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	private long sampleRate = -1;

	public LinearCVAMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			LinearCVAMadDefinition definition,
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
		boolean inWaveConnected = channelConnectedFlags.get( LinearCVAMadDefinition.CONSUMER_IN_WAVE );
		MadChannelBuffer inWaveCb = channelBuffers[ LinearCVAMadDefinition.CONSUMER_IN_WAVE ];
		float[] inWaveFloats = (inWaveConnected ? inWaveCb.floatBuffer : null );
		
		boolean inAmpConnected = channelConnectedFlags.get( LinearCVAMadDefinition.CONSUMER_IN_AMP_CV );
		MadChannelBuffer inAmpCb = channelBuffers[ LinearCVAMadDefinition.CONSUMER_IN_AMP_CV ];
		float[] inAmpFloats = (inAmpConnected ? inAmpCb.floatBuffer : null );
		
		boolean outWaveConnected = channelConnectedFlags.get( LinearCVAMadDefinition.PRODUCER_OUT_WAVE );
		MadChannelBuffer outWaveCb = channelBuffers[ LinearCVAMadDefinition.PRODUCER_OUT_WAVE ];
		float[] outWaveFloats = (outWaveConnected ? outWaveCb.floatBuffer : null );

		// Now mix them together with the precomputed amps
		if( outWaveConnected && inWaveConnected && inAmpConnected )
		{
			for( int i = 0 ; i < numFrames ; i++ )
			{
				outWaveFloats[ i ] = inWaveFloats[ i ] * inAmpFloats[ i ];
			}
		}
		else if( outWaveConnected && inWaveConnected )
		{
			// Copy across
			System.arraycopy( inWaveFloats, 0, outWaveFloats, 0, numFrames );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
