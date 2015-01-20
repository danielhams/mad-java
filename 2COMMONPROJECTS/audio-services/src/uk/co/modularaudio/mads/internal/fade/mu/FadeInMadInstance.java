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

package uk.co.modularaudio.mads.internal.fade.mu;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
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

public class FadeInMadInstance extends MadInstance<FadeInMadDefinition, FadeInMadInstance>
{
	private AtomicInteger curTablePosition = new AtomicInteger( 0 );
	private FadeInWaveTable waveTable = null;

	public FadeInMadInstance( InternalComponentsCreationContext creationContext,
			String instanceName,
			FadeInMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		waveTable = new FadeInWaveTable( hardwareChannelSettings.getAudioChannelSetting().getDataRate(), FadeDefinitions.FADE_MILLIS );
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
		// Only do some processing if we are connected
		int runningTablePosition = curTablePosition.get();
		if( channelConnectedFlags.get( FadeInMadDefinition.CONSUMER ) &&
				channelConnectedFlags.get( FadeInMadDefinition.PRODUCER ) )
		{
			MadChannelBuffer in = channelBuffers[ FadeInMadDefinition.CONSUMER ];
			float[] inBuffer = in.floatBuffer;
	
			MadChannelBuffer out = channelBuffers[ FadeInMadDefinition.PRODUCER ];
			float[] outBuffer = out.floatBuffer;

			// Use the fade wave table and our current position to pull out the fade value to use.
			for( int i = 0 ; i < numFrames ; i++ )
			{
				float curVal = inBuffer[i];
				float currentFadeMultiplier = waveTable.getValueAt( runningTablePosition );
				outBuffer[i] = curVal * currentFadeMultiplier;
				runningTablePosition++;
			}
		}
		else
		{
			runningTablePosition += numFrames;
		}

		curTablePosition.set( runningTablePosition );
		
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public boolean completed()
	{
		if( waveTable != null )
		{
			return curTablePosition.get() >= waveTable.capacity;
		}
		else
		{
			return false;
		}
	}
}
