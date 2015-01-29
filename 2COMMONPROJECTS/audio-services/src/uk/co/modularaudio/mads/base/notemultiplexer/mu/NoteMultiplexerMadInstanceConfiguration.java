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

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class NoteMultiplexerMadInstanceConfiguration
{
	private int numOutputChannels;

	private final int totalNumChannels;

	private final MadChannelConfiguration channelConfiguration;
	private final MadChannelDefinition[] channelDefinitions;

	public NoteMultiplexerMadInstanceConfiguration( final Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		final String numChannelsStr = parameterValues.get( NoteMultiplexerMadDefinition.NUM_CHANNELS_PARAMETER );
		boolean parsed = true;
		numOutputChannels = -1;
		if( numChannelsStr != null)
		{
			numOutputChannels = Integer.parseInt(  numChannelsStr );
			parsed = parsed & true;
		}
		else
		{
			parsed = false;
		}

		if( !parsed )
		{
//			String msg = "Note Multiplexer requires the NumChannels parameter to be set for instance creation.";
//			throw new MadProcessingException(  msg  );
			numOutputChannels = 2;
		}

		totalNumChannels = 1 + numOutputChannels;
		channelDefinitions = new MadChannelDefinition[ totalNumChannels ];

		channelDefinitions[ 0 ] = new MadChannelDefinition( "Input Notes",
				MadChannelType.NOTE,
				MadChannelDirection.CONSUMER,
				MadChannelPosition.MONO );

		int curChannelCounter = 1;

		for( int ic = 0 ; ic < numOutputChannels ; ic++ )
		{
			channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Output Notes Channel " + (ic + 1),
					MadChannelType.NOTE,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO );
			curChannelCounter++;
		}

		// Build the channel configuration
		channelConfiguration = new MadChannelConfiguration( channelDefinitions );
	}

	public MadChannelConfiguration getChannelConfiguration()
	{
		return channelConfiguration;
	}

	public int getNumOutputChannels()
	{
		return numOutputChannels;
	}

	public int getNumTotalChannels()
	{
		return totalNumChannels;
	}
}
