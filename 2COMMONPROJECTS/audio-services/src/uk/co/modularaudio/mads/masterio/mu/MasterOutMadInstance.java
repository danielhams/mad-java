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

package uk.co.modularaudio.mads.masterio.mu;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import uk.co.modularaudio.mads.internal.fade.mu.FadeDefinitions;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInWaveTable;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutWaveTable;
import uk.co.modularaudio.mads.masterio.MasterIOComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadState;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.IOBuffers;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MasterOutMadInstance extends MadInstance<MasterOutMadDefinition, MasterOutMadInstance>
{
//	private static Log log = LogFactory.getLog( MasterOutMadInstance.class.getName() );
	
	public enum FadeType
	{
		NONE,
		IN,
		OUT
	}

	private int audioBufferLength = -1;
	private int noteBufferLength = -1;
	private IOBuffers consumerBuffers = null;
	
	private float[] emptyFloatBuffer = null;
	
	private AtomicInteger curTablePosition = new AtomicInteger( 0 );
	private AtomicReference<FadeType> curFadeTable = new AtomicReference<FadeType>( FadeType.NONE );
	private FadeInWaveTable fadeInWaveTable = null;
	private FadeOutWaveTable fadeOutWaveTable = null;
	private int fadeTableLength = -1;
	
	public MasterOutMadInstance( MasterIOComponentsCreationContext creationContext,
			String instanceName,
			MasterOutMadDefinition definition,
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
			audioBufferLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			emptyFloatBuffer = new float[ audioBufferLength ];
			noteBufferLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();

			consumerBuffers = new IOBuffers( MasterOutMadDefinition.NUM_AUDIO_CHANNELS,
					audioBufferLength,
					MasterOutMadDefinition.NUM_NOTE_CHANNELS,
					noteBufferLength );
			
			fadeInWaveTable = new FadeInWaveTable( hardwareChannelSettings.getAudioChannelSetting().getDataRate(), FadeDefinitions.FADE_MILLIS );
			fadeOutWaveTable = new FadeOutWaveTable( hardwareChannelSettings.getAudioChannelSetting().getDataRate(), FadeDefinitions.FADE_MILLIS );
			fadeTableLength = fadeInWaveTable.getBufferCapacity();
		}
		catch (Exception e)
		{
			String msg = "Exception caught starting up master out instance: " + e.toString();
			throw new MadProcessingException( msg, e );
		}

	}

	@Override
	public void stop() throws MadProcessingException
	{
		consumerBuffers = null;
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		FadeType localFadeTable = curFadeTable.get();
		
		RawWaveTable localFadeWaveTable = null;
		
		int localFadePosition = fadeTableLength;
		
		if( localFadeTable == FadeType.NONE )
		{
		}
		else if( localFadeTable == FadeType.IN )
		{
			localFadeWaveTable = fadeInWaveTable;
			localFadePosition = curTablePosition.get();
		}
		else if( localFadeTable == FadeType.OUT )
		{
			localFadeWaveTable = fadeOutWaveTable;
			localFadePosition = curTablePosition.get();
		}
		
		// Assume our IO buffers are already pointing to the correct place.
		// Copy our incoming channel data into the channels in the consumer buffers
		for( int c = 0 ; c < channelInstances.length ; c++ )
		{
			MadChannelInstance auci = channelInstances[ c ];
			
			if( auci.definition.type == MadChannelType.AUDIO )
			{
				float[] floatBuffer = null;
				if( channelConnectedFlags.get(  c  ) )
				{
					MadChannelBuffer aucb = channelBuffers[ c ];
					floatBuffer = aucb.floatBuffer;
				}
				else
				{
					floatBuffer = emptyFloatBuffer;
				}
				
				// Audio channels are from 0, we don't need to do any channel mapping magic
				if( c < consumerBuffers.numAudioBuffers )
				{
					MadChannelBuffer cb = consumerBuffers.audioBuffers[ c ];
					float[] outBuffer = cb.floatBuffer;
					if( localFadeTable == FadeType.NONE )
					{
						// no fade, straight copy of it.
						System.arraycopy( floatBuffer, 0,  outBuffer, 0, numFrames );
					}
					else
					{
						// We are in a fade
						for( int f = 0 ; f < numFrames ; f++ )
						{
							int position = localFadePosition + f;
							position = ( position >= fadeTableLength ? fadeTableLength - 1 : position );
							outBuffer[ f ] = floatBuffer[ f ] * localFadeWaveTable.getValueAt( position );
						}
					}
				}
				else
				{
					// We don't have a bound buffer
				}
			}
			else if( auci.definition.type == MadChannelType.NOTE )
			{
			}
			else if( auci.definition.type == MadChannelType.CV )
			{
			}
		}

		if( localFadeTable != FadeType.NONE )
		{
			localFadePosition += numFrames;
//			log.debug("Moving fade table forwards by " + numFrames );
			curTablePosition.set( localFadePosition );
		}
		
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
	
	public void setAndStartFade( FadeType fadeTable )
	{
		curFadeTable.set( fadeTable );
		curTablePosition.set( 0 );
	}
	
	public boolean isFadeFinished( int numSamplesClockSourceLatency )
	{
		if( state == MadState.RUNNING )
		{
			int fadePosition = curTablePosition.get();
//			log.debug("FadePosition is " + fadePosition + " while ftl("  +fadeTableLength + ") NSCSL(" + numSamplesClockSourceLatency + ")");
			return( fadePosition >= (fadeTableLength + (numSamplesClockSourceLatency * 2) ) );
		}
		else
		{
			return true;
		}
	}
	
	public IOBuffers getMasterIOBuffers()
	{
		return consumerBuffers;
	}

}
