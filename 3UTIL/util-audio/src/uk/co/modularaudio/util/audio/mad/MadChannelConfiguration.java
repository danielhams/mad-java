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

package uk.co.modularaudio.util.audio.mad;

import java.util.HashMap;
import java.util.Map;

public class MadChannelConfiguration
{
	private static MadChannelConfiguration emptyChannelConfiguration = new MadChannelConfiguration( new MadChannelDefinition[0] );

	private final MadChannelDefinition[] channelDefinitions;
	private final Map<String,MadChannelDefinition> nameToChannelDefinitionMap = new HashMap<String, MadChannelDefinition>();

	public MadChannelConfiguration( final MadChannelDefinition[] channelDefinitions )
	{
		this.channelDefinitions = channelDefinitions;
		for( final MadChannelDefinition cd : channelDefinitions )
		{
			nameToChannelDefinitionMap.put( cd.name, cd );
		}
	}

	public MadChannelDefinition[] getOrderedChannelDefinitions()
	{
		return channelDefinitions;
	}

	public static MadChannelConfiguration getEmptyChannelConfiguration()
	{
		return emptyChannelConfiguration;
	}

}
