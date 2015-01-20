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

package uk.co.modularaudio.mads.base.notemultiplexer.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.notestate.NoteStateManager;
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

public class NoteMultiplexerMadInstance extends MadInstance<NoteMultiplexerMadDefinition,NoteMultiplexerMadInstance>
{
	private static Log log = LogFactory.getLog( NoteMultiplexerMadInstance.class.getName() );
	
	private int notePeriodLength = -1;
	
	private NoteMultiplexerMadInstanceConfiguration instanceConfiguration = null;
	
	private NoteStateManager noteStateManager = null;

	private int noteInChannelIndex = -1;
	private int noteOutStartIndex =  -1;
	private int numChannelsPolyphony = -1;
	
	private MadChannelBuffer[] outgoingNoteBuffers = null;
	
	public NoteMultiplexerMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			NoteMultiplexerMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration ) throws MadProcessingException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		
		instanceConfiguration = new NoteMultiplexerMadInstanceConfiguration( creationParameterValues );
		noteInChannelIndex = 0;
		noteOutStartIndex = 1;
		numChannelsPolyphony = instanceConfiguration.getNumOutputChannels();
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			notePeriodLength = hardwareChannelSettings.getNoteChannelSetting().getChannelBufferLength();
			noteStateManager = new NoteStateManager( numChannelsPolyphony, notePeriodLength );
			outgoingNoteBuffers = new MadChannelBuffer[ numChannelsPolyphony ];
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
		boolean noteConnected = channelConnectedFlags.get( noteInChannelIndex );
		
		if( noteConnected )
		{
			MadChannelBuffer noteBuffer = channelBuffers[ noteInChannelIndex ];
			
			for( int i = 0 ; i < numChannelsPolyphony ; i++ )
			{
				MadChannelBuffer onb = channelBuffers[ noteOutStartIndex + i ];
				if( onb != null )
				{
					outgoingNoteBuffers[ i ] = onb;
				}
			}

			try
			{
				noteStateManager.processNotes( noteBuffer, outgoingNoteBuffers );
			}
			catch (Exception e)
			{
				String msg = "Exception caught processing notes: " + e.toString();
				log.error( msg, e );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public NoteMultiplexerMadInstanceConfiguration getInstanceConfiguration()
	{
		return instanceConfiguration;
	}
}
