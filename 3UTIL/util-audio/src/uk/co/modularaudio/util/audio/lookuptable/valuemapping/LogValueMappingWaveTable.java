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

package uk.co.modularaudio.util.audio.lookuptable.valuemapping;

import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class LogValueMappingWaveTable extends ValueMappingWaveTable
{

	public LogValueMappingWaveTable( int mapBufferLength, boolean isDecay )
	{
		super( mapBufferLength );

		// It's cubic so data goes from 1
		for( int s = 0 ; s < mapBufferLength ; s++ )
		{
			int index = s+1;
			float linValue = ((float)s / (mapBufferLength-1));
			float mappedValue = NormalisedValuesMapper.logMapF( linValue );
			floatBuffer[ index ] = (isDecay ? 1.0f - mappedValue : mappedValue );
		}
		
		completeCubicWaveTableSetupForOneShot();
	}
}
