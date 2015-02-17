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

package test.uk.co.modularaudio.util.audio.mad.units.stereotee;

import java.util.Arrays;
import java.util.Map;

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

public class StereoTeeMadInstance extends MadInstance<StereoTeeMadDefinition,
	StereoTeeMadInstance>
{
	protected StereoTeeMadDefinition definition = null;

	public StereoTeeMadInstance( String instanceName,
			StereoTeeMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		this.definition = definition;
	}

	@Override
	public void startup( HardwareIOChannelSettings dataRateConfiguration, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
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
			long currentTime ,
			MadChannelConnectedFlags channelConnectedFlags ,
			MadChannelBuffer[] channelBuffers ,
			int frameOffset , int numFrames  )
	{
		int leftInputIndex = StereoTeeMadDefinition.CONSUMER_LEFT;
		MadChannelBuffer leftInputChannelBuffer = channelBuffers[ leftInputIndex ];
		float[] leftInputFloats = leftInputChannelBuffer.floatBuffer;
		
		int firstOutputLeftIndex = StereoTeeMadDefinition.PRODUCER_ONE_LEFT;
		MadChannelBuffer firstOutputLeftChannelBuffer = channelBuffers[ firstOutputLeftIndex ];
		float[] firstOutputLeftFloats = firstOutputLeftChannelBuffer.floatBuffer;
		
		int secondOutputLeftIndex = StereoTeeMadDefinition.PRODUCER_TWO_LEFT;
		MadChannelBuffer secondOutputLeftChannelBuffer = channelBuffers[ secondOutputLeftIndex ];
		float[] secondOutputLeftFloats = secondOutputLeftChannelBuffer.floatBuffer;
		
		if( channelConnectedFlags.get( leftInputIndex ) )
		{
			if( channelConnectedFlags.get(  firstOutputLeftIndex ) )
			{
				System.arraycopy(  leftInputFloats, 0, firstOutputLeftFloats, 0, leftInputFloats.length );
			}
			
			if( channelConnectedFlags.get(  secondOutputLeftIndex ) )
			{
				System.arraycopy(  leftInputFloats, 0, secondOutputLeftFloats, 0, leftInputFloats.length );
			}
		}
		else
		{
			// No input
			if( channelConnectedFlags.get( firstOutputLeftIndex ) )
			{
				Arrays.fill( firstOutputLeftFloats, 0.0f );
			}
			
			if( channelConnectedFlags.get( secondOutputLeftIndex ) )
			{
				Arrays.fill( secondOutputLeftFloats, 0.0f );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
