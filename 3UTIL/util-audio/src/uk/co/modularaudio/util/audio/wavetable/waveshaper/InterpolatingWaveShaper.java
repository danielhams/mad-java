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

public class InterpolatingWaveShaper
{
//	private static Log log = LogFactory.getLog( InterpolatingWaveShaper.class.getName() );

	public static void shape( RawWaveTable shaperTable, float[] floats, int periodLength )
	{
		int tableLength = shaperTable.capacity;
		
		for( int s = 0 ; s < periodLength ; s++ )
		{
			float inVal = floats[s];
			if( inVal > 1.0f ) inVal = 1.0f;
			if( inVal < -1.0f ) inVal = -1.0f;
			// Normalise from 0 -> 1
			float normalisedVal = (inVal + 1.0f) / 2.0f;
			// Lookup based on index
			float fIndex = normalisedVal * (tableLength-1);
			int iIndex = (int)(fIndex);
			float over = fIndex - iIndex;
			int eIndex = (iIndex + 1 >= tableLength - 1 ? tableLength - 1 : iIndex + 1 );
			float a = shaperTable.floatBuffer[ iIndex ];
			float b = shaperTable.floatBuffer[ eIndex ];
			float bMinusA = b - a;
			float outVal = a + (over * bMinusA );
			floats[s] = outVal;
		}
	}

}
