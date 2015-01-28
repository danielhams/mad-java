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

package uk.co.modularaudio.util.audio.mad.helper;

import java.util.ArrayList;

import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;

public class MadChannelDefinitionBuilder
{
	public static MadChannelDefinition[] buildDefaultChannelDefinitions( final MadChannelDefinitionIndexedCreator indexedCreator )
	{
		final int numChannelDefinitions = indexedCreator.getNumChannelDefinitions();
		final ArrayList<MadChannelDefinition> defsList = new ArrayList<MadChannelDefinition>( numChannelDefinitions );

		for( int c = 0 ; c < numChannelDefinitions ; c++ )
		{
			defsList.add( indexedCreator.buildChannelDefinitionForIndex( c ) );
		}

		final MadChannelDefinition[] retVal = defsList.toArray( new MadChannelDefinition[ defsList.size() ] );
		return retVal;
	}
}
