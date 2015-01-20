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

package uk.co.modularaudio.util.audio.wavetable.waveshaper;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;

public class TruncatingWaveShaper
{
//	private static Log log = LogFactory.getLog( TruncatingWaveShaper.class.getName() );

	public static void shape( RawWaveTable shaperTable, float[] floats, int periodLength )
	{
		int tableLength = shaperTable.capacity;
		
		for( int s = 0 ; s < periodLength ; s++ )
		{
			float inVal = floats[s];
			// Normalise from 0 -> 1
			float normalisedVal = (inVal + 1.0f) / 2.0f;
			// Lookup based on index
			int index = (int)(normalisedVal * tableLength);
			float outVal = shaperTable.getValueAt( index );
			floats[s] = outVal;
		}
	}

}
