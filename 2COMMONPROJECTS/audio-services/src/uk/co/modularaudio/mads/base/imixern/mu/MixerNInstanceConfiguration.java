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

package uk.co.modularaudio.mads.base.imixern.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class MixerNInstanceConfiguration
{
	private static final int NUM_CHANNELS_PER_LANE = 2;

	private final int numMixerLanes;

	private final int numChannelsPerLane;
	private final int numInputChannels;
	private final int numOutputChannels;
	private final MadChannelType channelType = MadChannelType.AUDIO;
	private final int[] inputChannelIndexes;
	private final int[] outputChannelIndexes;
	private final int totalNumChannels;

	private final String[] channelNames;
	private final MadChannelType[] channelTypes;
	private final MadChannelDirection[] channelDirections;
	private final MadChannelPosition[] channelPositions;

	public MixerNInstanceConfiguration( final int numMixerLanes )
		throws MadProcessingException
	{
		this.numMixerLanes = numMixerLanes;

		numChannelsPerLane = NUM_CHANNELS_PER_LANE;
		numInputChannels = (numMixerLanes * numChannelsPerLane );
		numOutputChannels = numChannelsPerLane;
		totalNumChannels = numInputChannels + numOutputChannels;

		channelNames = new String[ totalNumChannels ];
		channelTypes = new MadChannelType[ totalNumChannels ];
		channelDirections = new MadChannelDirection[ totalNumChannels ];
		channelPositions = new MadChannelPosition[ totalNumChannels ];

		int curChannelCounter = 0;
		inputChannelIndexes = new int[ numInputChannels ];
		for( int il = 0 ; il < numMixerLanes ; il++ )
		{
			for( int ic = 0 ; ic < numChannelsPerLane ; ic++ )
			{
				inputChannelIndexes[ (il * numChannelsPerLane) + ic ] = curChannelCounter;
				channelNames[ curChannelCounter ] = "Lane " + (il + 1) + " Channel " + (ic + 1);
				channelTypes[ curChannelCounter ] = channelType;
				channelDirections[ curChannelCounter ] = MadChannelDirection.CONSUMER;
				channelPositions[ curChannelCounter ] = MadChannelPosition.MONO;
				curChannelCounter++;
			}
		}
		outputChannelIndexes = new int[ numOutputChannels ];
		for( int c = 0 ; c < numOutputChannels ; c++ )
		{
			outputChannelIndexes[ c ] = curChannelCounter;

			channelNames[ curChannelCounter ] = "Output Channel " + ( c + 1);
			channelTypes[ curChannelCounter ] = channelType;
			channelDirections[ curChannelCounter ] = MadChannelDirection.PRODUCER;
			channelPositions[ curChannelCounter ] = MadChannelPosition.MONO;

			curChannelCounter++;
		}
	}

	public int getNumMixerLanes()
	{
		return numMixerLanes;
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
		return inputChannelIndexes[ (l * numChannelsPerLane) + c ];
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

	public String[] getChannelNames()
	{
		return channelNames;
	}

	public MadChannelType[] getChannelTypes()
	{
		return channelTypes;
	}

	public MadChannelDirection[] getChannelDirections()
	{
		return channelDirections;
	}

	public MadChannelPosition[] getChannelPositions()
	{
		return channelPositions;
	}

	public int getNumChannelsPerLane()
	{
		return numChannelsPerLane;
	}
}
