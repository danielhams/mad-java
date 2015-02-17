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

package uk.co.modularaudio.mads.base.audiocvconverter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class AudioCvConverterMadInstance extends MadInstance<AudioCvConverterMadDefinition,AudioCvConverterMadInstance>
{
//	private static Log log = LogFactory.getLog( AudioCvConverterMadInstance.class.getName());
	
	private AudioCvConverterMadInstanceConfiguration instanceConfiguration = null;
	
	public AudioCvConverterMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			AudioCvConverterMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
		throws MadProcessingException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		instanceConfiguration = new AudioCvConverterMadInstanceConfiguration( creationParameterValues );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			MadChannelConnectedFlags channelConnectedFlags ,
			MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		MadChannelType inChannelType = instanceConfiguration.getInChannelType();
		int numChannels = instanceConfiguration.getNumInputChannels();
		for( int i = 0 ; i < numChannels ; i++ )
		{
			int inChannelIndex = instanceConfiguration.getIndexForInputChannel( i );
			int outChannelIndex = instanceConfiguration.getIndexForOutputChannel( i );
			boolean inConnected = channelConnectedFlags.get( inChannelIndex );
			MadChannelBuffer inBuffer = channelBuffers[ inChannelIndex ];
			boolean outConnected = channelConnectedFlags.get( outChannelIndex );
			MadChannelBuffer outBuffer = channelBuffers[ outChannelIndex ];
			
			if( inConnected && outConnected )
			{
				switch( inChannelType )
				{
					case CV:
					{
						for( int s = 0 ; s < numFrames ; s++ )
						{
							float sample = inBuffer.floatBuffer[ s ];
							sample = ( sample < -1.0f ? -1.0f : (sample > 1.0f ? 1.0f : sample ) );
							outBuffer.floatBuffer[ s ] = sample;
						}
						break;
					}
					case AUDIO:
					{
						// Just copy over
						System.arraycopy( inBuffer.floatBuffer, 0, outBuffer.floatBuffer, 0, numFrames );
						break;
					}
					default:
					{
					}
				}
			}
			else if( outConnected )
			{
				Arrays.fill( outBuffer.floatBuffer, 0.0f );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public AudioCvConverterMadInstanceConfiguration getInstanceConfiguration()
	{
		return instanceConfiguration;
	}
}

