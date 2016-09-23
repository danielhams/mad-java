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

import uk.co.modularaudio.util.audio.controlinterpolation.CDSpringAndDamperDouble24Interpolator;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SingleChannelVolumeMadInstance extends MadInstance<SingleChannelVolumeMadDefinition,
	SingleChannelVolumeMadInstance>
{
	protected SingleChannelVolumeMadDefinition definition;

	private final CDSpringAndDamperDouble24Interpolator volSad = new CDSpringAndDamperDouble24Interpolator();

	// Values set / read by the queue bridge
	private float inVolumeMultiplier;

	public SingleChannelVolumeMadInstance( final String instanceName,
			final SingleChannelVolumeMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void start( final HardwareIOChannelSettings dataRateConfiguration, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		final HardwareIOOneChannelSetting audioChannelSetting = dataRateConfiguration.getAudioChannelSetting();
		final DataRate dataRate = audioChannelSetting.getDataRate();

		volSad.resetSampleRateAndPeriod( dataRate.getValue(), audioChannelSetting.getChannelBufferLength(), 200 );
		volSad.hardSetValue( inVolumeMultiplier );
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final int U_currentTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final float[] tmpFloats = tempQueueEntryStorage.temporaryFloatArray;
		volSad.checkForDenormal();
		volSad.generateControlValues( tmpFloats, 0, numFrames );

		final int inputIndex = SingleChannelVolumeMadDefinition.CONSUMER;
		final MadChannelBuffer inputChannelBuffer = channelBuffers[ inputIndex ];
		final float[] inputFloats = inputChannelBuffer.floatBuffer;

		final int outputIndex = SingleChannelVolumeMadDefinition.PRODUCER;
		final MadChannelBuffer outputChannelBuffer = channelBuffers[ outputIndex ];
		final float[] outputFloats = outputChannelBuffer.floatBuffer;

		if( channelConnectedFlags.get( inputIndex ) )
		{
			if( channelConnectedFlags.get(  outputIndex ) )
			{
				for( int i = 0 ; i < inputFloats.length ; i++ )
				{
					outputFloats[ frameOffset + i ] = inputFloats[ frameOffset + i ] *
							tmpFloats[ i ];
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

	public void setInVolumeMultiplier( final float volMultiplier )
	{
		volSad.notifyOfNewValue( volMultiplier );
		inVolumeMultiplier = volMultiplier;
	}
}
