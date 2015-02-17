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

package test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SingleChannelVolumeMadInstance extends MadInstance<SingleChannelVolumeMadDefinition,
	SingleChannelVolumeMadInstance>
{
	// Try with 5 milliseconds as the upper limit for volume changes
	private static final int MILLIS_FOR_CHASE = 5;

	protected SingleChannelVolumeMadDefinition definition = null;
	
	private float currentVolumeMultiplier = 0.5f;
	
	// Calculated ratios based on temporal fades using the latency configuration
	private float newValueRatio = 0.0f;
	private float oldValueRatio = 0.0f;
	
	// Values set / read by the queue bridge
	protected float inVolumeMultiplier = 0.0f;
	
	public SingleChannelVolumeMadInstance( String instanceName,
			SingleChannelVolumeMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings dataRateConfiguration, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		HardwareIOOneChannelSetting audioChannelSetting = dataRateConfiguration.getAudioChannelSetting();
		DataRate dataRate = audioChannelSetting.getDataRate();
		// Ideally I should really be simulating volume slider velocity
		newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( dataRate.getValue(), MILLIS_FOR_CHASE );
		oldValueRatio = 1.0f - newValueRatio;
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
//		float volumeMultiplier = realPeriodValues.volumeMultiplier;
		
		// Recompute our current volume multiplier
		currentVolumeMultiplier = (this.inVolumeMultiplier * newValueRatio) + (currentVolumeMultiplier * oldValueRatio );
		
		int inputIndex = SingleChannelVolumeMadDefinition.CONSUMER;
		MadChannelBuffer inputChannelBuffer = channelBuffers[ inputIndex ];
		float[] inputFloats = inputChannelBuffer.floatBuffer;
		
		int outputIndex = SingleChannelVolumeMadDefinition.PRODUCER;
		MadChannelBuffer outputChannelBuffer = channelBuffers[ outputIndex ];
		float[] outputFloats = outputChannelBuffer.floatBuffer;
		
		if( channelConnectedFlags.get( inputIndex ) )
		{
			if( channelConnectedFlags.get(  outputIndex ) )
			{
				for( int i = 0 ; i < inputFloats.length ; i++ )
				{
					outputFloats[ i ] = inputFloats[ i ] * currentVolumeMultiplier;
				}
			}
		}
		else
		{
			// No input
			if( channelConnectedFlags.get( outputIndex ) )
			{
				Arrays.fill( outputFloats, 0.0f );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
