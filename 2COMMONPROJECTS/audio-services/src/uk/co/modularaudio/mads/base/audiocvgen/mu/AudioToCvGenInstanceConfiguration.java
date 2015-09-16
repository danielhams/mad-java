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

package uk.co.modularaudio.mads.base.audiocvgen.mu;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;

public class AudioToCvGenInstanceConfiguration
{
	private final MadChannelType consumerChannelType;
	private final int numConversionChannels;
	private final int totalNumChannels;

	private final String[] channelNames;
	private final MadChannelType[] channelTypes;
	private final MadChannelDirection[] channelDirections;
	private final MadChannelPosition[] channelPositions;

	public AudioToCvGenInstanceConfiguration( final MadChannelType consumerChannelType,
			final int numConversionChannels )
		throws MadProcessingException
	{
		this.consumerChannelType = consumerChannelType;
		this.numConversionChannels = numConversionChannels;
		totalNumChannels = numConversionChannels * 2;

		channelNames = new String[ totalNumChannels ];
		channelTypes = new MadChannelType[ totalNumChannels ];
		channelDirections = new MadChannelDirection[ totalNumChannels ];
		channelPositions = new MadChannelPosition[ totalNumChannels ];

		int curChannelCounter = 0;

		for( int il = 0 ; il < numConversionChannels ; il++ )
		{
			channelNames[ curChannelCounter ] = consumerChannelType == MadChannelType.AUDIO ?
					"Audio Input " + (il + 1) :
					"CV Input " + (il + 1);
			channelTypes[ curChannelCounter ] = consumerChannelType;
			channelDirections[ curChannelCounter ] = MadChannelDirection.CONSUMER;
			channelPositions[ curChannelCounter ] = MadChannelPosition.MONO;
			curChannelCounter++;

			channelNames[ curChannelCounter ] = consumerChannelType == MadChannelType.AUDIO ?
					"CV Output " + (il + 1) :
					"Audio Output " + (il + 1);
			channelTypes[ curChannelCounter ] = (consumerChannelType == MadChannelType.AUDIO ?
					MadChannelType.CV :
					MadChannelType.AUDIO );
			channelDirections[ curChannelCounter ] = MadChannelDirection.PRODUCER;
			channelPositions[ curChannelCounter ] = MadChannelPosition.MONO;
			curChannelCounter++;
		}
	}

	public int getNumConversionChannels()
	{
		return numConversionChannels;
	}

	public MadChannelType getConsumerChannelType()
	{
		return consumerChannelType;
	}

	public int getNumTotalChannels()
	{
		return totalNumChannels;
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

	public int getConsumerChannelIndex( final int consChanIndex )
	{
		return (consChanIndex * 2);
	}

	public int getProducerChannelIndex( final int prodChanIndex )
	{
		return (prodChanIndex * 2) + 1;
	}
}
