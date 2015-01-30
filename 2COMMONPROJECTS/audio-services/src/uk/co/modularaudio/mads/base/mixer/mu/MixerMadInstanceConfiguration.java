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

package uk.co.modularaudio.mads.base.mixer.mu;

import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class MixerMadInstanceConfiguration
{
	private static final int NUM_CHANNELS_PER_LANE = 2;

	private int numMixerLanes;

	private final int numInputChannels;
	private final int numOutputChannels;
	private final MadChannelType channelType = MadChannelType.AUDIO;
	private final int[] inputChannelIndexes;
	private final int[] outputChannelIndexes;
	private final int totalNumChannels;

	private final MadChannelConfiguration channelConfiguration;
	private final MadChannelDefinition[] channelDefinitions;

	public MixerMadInstanceConfiguration( final Map<MadParameterDefinition, String> parameterValues ) throws MadProcessingException
	{
		final String numMixerLanesStr = parameterValues.get( MixerMadDefinition.NUM_LANES_DEF );
		try
		{
			numMixerLanes = Integer.parseInt( numMixerLanesStr );
			if( numMixerLanes < 1 || numMixerLanes > 8 )
			{
				throw new MadProcessingException( "Num mixer lanes must fall between 1 and 8" );
//				numMixerLanes = 8;
			}
		}
		catch(final NumberFormatException nfe )
		{
			numMixerLanes = 8;
//			throw new MadProcessingException( "Num mixer lanes must fall between 1 and 8" );
		}
		numInputChannels = (numMixerLanes * NUM_CHANNELS_PER_LANE );
		numOutputChannels = NUM_CHANNELS_PER_LANE;
		totalNumChannels = numInputChannels + numOutputChannels;
		channelDefinitions = new MadChannelDefinition[ totalNumChannels ];

		int curChannelCounter = 0;
		inputChannelIndexes = new int[ numInputChannels ];
		for( int il = 0 ; il < numMixerLanes ; il++ )
		{
			for( int ic = 0 ; ic < NUM_CHANNELS_PER_LANE ; ic++ )
			{
				inputChannelIndexes[ (il * NUM_CHANNELS_PER_LANE) + ic ] = curChannelCounter;
				channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Lane " + (il + 1) + " Channel " + (ic + 1),
						channelType,
						MadChannelDirection.CONSUMER,
						MadChannelPosition.MONO );
				curChannelCounter++;
			}
		}
		outputChannelIndexes = new int[ numOutputChannels ];
		for( int c = 0 ; c < numOutputChannels ; c++ )
		{
			outputChannelIndexes[ c ] = curChannelCounter;

				channelDefinitions[ curChannelCounter ] = new MadChannelDefinition( "Output Channel " + ( c + 1),
						channelType,
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

	public int getNumInputChannels()
	{
		return numInputChannels;
	}

	public int getNumOutputChannels()
	{
		return numOutputChannels;
	}

	public MadChannelType getChannelType()
	{
		return channelType;
	}

	public int getIndexForInputLaneChannel( final int l, final int c )
	{
		return inputChannelIndexes[ (l * NUM_CHANNELS_PER_LANE) + c ];
	}

	public int getIndexForOutputChannel( final int c )
	{
		return outputChannelIndexes[ c];
	}

	public int getNumTotalChannels()
	{
		return totalNumChannels;
	}

	public int getNumInputLanes()
	{
		return numMixerLanes;
	}

	public int getNumChannelsPerLane()
	{
		return NUM_CHANNELS_PER_LANE;
	}
}
