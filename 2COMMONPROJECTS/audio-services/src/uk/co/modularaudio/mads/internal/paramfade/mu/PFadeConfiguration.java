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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;

public class PFadeConfiguration
{
	private static Log log = LogFactory.getLog( PFadeConfiguration.class.getName() );

	protected final int numChannels;

	private final MadChannelConfiguration channelConfiguration;

	public PFadeConfiguration( final Map<MadParameterDefinition, String> parameterValues )
	{
		final String numChannelsStr = parameterValues.get( PFadeDefinitions.NUM_CHANNELS_PARAMETER );
		boolean parsed = true;

		if( numChannelsStr != null )
		{
			numChannels = Integer.parseInt( numChannelsStr );
			parsed = parsed & true;
		}
		else
		{
			log.warn("Missing num channels parameter");
			numChannels = 1;
		}

		// Consumers and producers for each channel
		final MadChannelDefinition[] channelDefinitions = new MadChannelDefinition[ numChannels * 2 ];
		int curChanNum = 0;
		for( int i = 0 ; i < numChannels ; ++i )
		{
			channelDefinitions[ curChanNum++ ] = new MadChannelDefinition( "In " + i,
					MadChannelType.AUDIO,
					MadChannelDirection.CONSUMER,
					MadChannelPosition.MONO );
		}
		for( int i = 0 ; i < numChannels ; ++i )
		{
			channelDefinitions[ curChanNum++ ] = new MadChannelDefinition( "Out " + i,
					MadChannelType.AUDIO,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO );
		}

		channelConfiguration = new MadChannelConfiguration(channelDefinitions);
	}

	public MadChannelConfiguration getChannelConfiguration()
	{
		return channelConfiguration;
	}

	public int getConsumerChannelIndex( final int c )
	{
		return c;
	}

	public int getProducerChannelIndex( final int p )
	{
		return numChannels + p;
	}
}
