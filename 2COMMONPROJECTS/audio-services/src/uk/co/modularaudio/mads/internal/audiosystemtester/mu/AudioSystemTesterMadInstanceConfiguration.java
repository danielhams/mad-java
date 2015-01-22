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

package uk.co.modularaudio.mads.internal.audiosystemtester.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class AudioSystemTesterMadInstanceConfiguration
{
	private static Log log = LogFactory.getLog( AudioSystemTesterMadInstance.class.getName() );

	private int numOutputChannels;
	private final int[] outputChannelIndexes;
	private final int totalNumChannels;

	private final MadChannelConfiguration channelConfiguration;
	private final MadChannelDefinition[] channelDefinitions;

	public AudioSystemTesterMadInstanceConfiguration( final Map<MadParameterDefinition, String> parameterValues )
		throws MadProcessingException
	{
		numOutputChannels = 2;
		if( parameterValues.containsKey( AudioSystemTesterMadDefinition.NUM_CHANNELS_PARAMETER ) )
		{
			final String numChannelsStr = parameterValues.get( AudioSystemTesterMadDefinition.NUM_CHANNELS_PARAMETER );
			if( numChannelsStr != null && numChannelsStr.length() > 0 )
			{
				try
				{
					numOutputChannels = Integer.parseInt(  numChannelsStr );
				}
				catch( NumberFormatException nfe )
				{
					log.warn( "NumChannels failed parsing as an int - defaulting to two channels" );
				}
			}
		}
		else
		{
			log.warn( "AudioSystemTester defaulting to two output channels" );
		}

		totalNumChannels = numOutputChannels;
		channelDefinitions = new MadChannelDefinition[ totalNumChannels ];

		int curChannelCounter = 0;
		outputChannelIndexes = new int[ numOutputChannels ];
		for( int ic = 0 ; ic < numOutputChannels ; ic++ )
		{
			outputChannelIndexes[ ic ] = curChannelCounter;
			channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Output Channel " + (ic + 1),
					MadChannelType.AUDIO,
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

	public int getIndexForOutputChannel( final int c )
	{
		return outputChannelIndexes[ c ];
	}

 	public int getNumTotalChannels()
	{
		return totalNumChannels;
	}
}
