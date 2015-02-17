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

import uk.co.modularaudio.mads.masterio.MasterIOComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventCopier;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.IOBuffers;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MasterInMadInstance extends MadInstance<MasterInMadDefinition,MasterInMadInstance>
{
//	private static Log log = LogFactory.getLog( MasterInMadInstance.class.getName() );

	private final static MadChannelNoteEventCopier NOTE_COPIER = new MadChannelNoteEventCopier();

	private final IOMadConfiguration ioConfiguration;
	private final int numAudioChannels;
	private final int numNoteChannels;

	private int audioBufferLength;
	private int noteBufferLength;
	private IOBuffers producerBuffers;

	public MasterInMadInstance( final MasterIOComponentsCreationContext creationContext,
			final String instanceName,
			final MasterInMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		ioConfiguration = MasterInMadDefinition.CHAN_CONFIG;
		this.numAudioChannels = ioConfiguration.getNumAudioChannels();
		this.numNoteChannels = ioConfiguration.getNumNoteChannels();
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		try
		{
			audioBufferLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			noteBufferLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();

			producerBuffers = new IOBuffers( numAudioChannels,
					audioBufferLength,
					numNoteChannels,
					noteBufferLength);
		}
		catch (Exception e)
		{
			final String msg = "Exception caught starting up master in instance: " + e.toString();
			throw new MadProcessingException( msg, e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
		producerBuffers = null;
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers ,
			int frameOffset , final int numFrames  )
	{
		// We assume that the actual card IO has already filled in the necessary data in the buffers
		// Iterate over the channels only processing them if they are connected
		for( int a = 0 ; a < numAudioChannels ; a++ )
		{
			final int audioChannelIndex = ioConfiguration.getAudioChannelIndex( a );
			if( channelConnectedFlags.get( audioChannelIndex ) )
			{
				if( a < producerBuffers.numAudioBuffers )
				{
					final MadChannelBuffer producerChannelBuffer = producerBuffers.audioBuffers[ a ];
					final float[] inBuffer = producerChannelBuffer.floatBuffer;
					final MadChannelBuffer aucb = channelBuffers[ audioChannelIndex ];
					final float[] floatBuffer = aucb.floatBuffer;
					System.arraycopy( inBuffer, 0, floatBuffer, 0, numFrames );
				}
			}
		}
		for( int n = 0 ; n < numNoteChannels ; n++ )
		{
			final int noteChannelIndex = ioConfiguration.getNoteChannelIndex( n );
			if( channelConnectedFlags.get( noteChannelIndex ) )
			{
				if(  n < producerBuffers.numMidiBuffers )
				{
					final MadChannelBuffer producerChannelBuffer = producerBuffers.noteBuffers[ n ];
					final MadChannelNoteEvent[] inBuffer = producerChannelBuffer.noteBuffer;
					final MadChannelBuffer aucb = channelBuffers[ noteChannelIndex ];
					final MadChannelNoteEvent[] noteBuffer = aucb.noteBuffer;
					final int numNotes = producerChannelBuffer.numElementsInBuffer;
					for( int note = 0 ; note < numNotes ; note++ )
					{
						NOTE_COPIER.copyValues( inBuffer[ note ], noteBuffer[ note ] );
					}
					aucb.numElementsInBuffer = numNotes;
				}
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public IOBuffers getMasterIOBuffers()
	{
		return producerBuffers;
	}
}
