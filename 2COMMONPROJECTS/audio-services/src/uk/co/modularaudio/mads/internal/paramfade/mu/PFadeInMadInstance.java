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

package uk.co.modularaudio.mads.internal.paramfade.mu;

import java.util.Arrays;
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

public class PFadeInMadInstance extends MadInstance<PFadeInMadDefinition, PFadeInMadInstance>
{
//	private static Log log = LogFactory.getLog( PFadeInMadInstance.class.getName() );
	
	private final PFadeConfiguration instanceConfiguration;

	private AtomicInteger curTablePosition = new AtomicInteger( 0 );
	private PFadeInWaveTable waveTable = null;

	public PFadeInMadInstance( InternalComponentsCreationContext creationContext,
			String instanceName,
			PFadeInMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
		 throws MadProcessingException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		instanceConfiguration = new PFadeConfiguration( creationParameterValues );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		waveTable = new PFadeInWaveTable( hardwareChannelSettings.getAudioChannelSetting().getDataRate(), PFadeDefinitions.FADE_MILLIS );
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
//		log.trace("Process method called");
//		log.trace("The channel connected flags are " + channelConnectedFlags.toString() );
		// Only do some processing if we are connected
		int runningTablePosition = curTablePosition.get();
		
		for( int c = 0 ; c < instanceConfiguration.numChannels ; ++c )
		{
			int prodChanIndex = instanceConfiguration.getProducerChannelIndex( c );
			
			if( channelConnectedFlags.get( prodChanIndex ) )
			{
				MadChannelBuffer out = channelBuffers[ prodChanIndex ];
				float[] outBuffer = out.floatBuffer;
		
				int consChanIndex = instanceConfiguration.getConsumerChannelIndex( c );
				if( channelConnectedFlags.get( consChanIndex ) )
				{
					MadChannelBuffer in = channelBuffers[ consChanIndex ];
					float[] inBuffer = in.floatBuffer;
	
					// Use the fade wave table and our current position to pull out the fade value to use.
					int chanPos = runningTablePosition;
					for( int i = 0 ; i < numFrames ; i++ )
					{
						float curVal = inBuffer[i];
						float currentFadeMultiplier = waveTable.getValueAt( chanPos++ );
						outBuffer[i] = curVal * currentFadeMultiplier;
					}
				}
				else
				{
					Arrays.fill( outBuffer, 0.0f );
				}
			}
		}
		runningTablePosition += numFrames;

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
	
	public PFadeConfiguration getInstanceConfiguration()
	{
		return instanceConfiguration;
	}
}
