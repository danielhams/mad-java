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

public class TriangleShaperTable extends RawWaveTable
{

	public TriangleShaperTable(int capacity)
	{
		super(capacity, false);
		
		// Assume we go from -1 to +1 at the end of the table
		float initialValue = -1.0f;
		// The minus one on the end ensures we end with +1
		float increment = 2.0f / (capacity - 1);
		float curValue = initialValue;
		
		for( int s = 0 ; s < capacity  ; s++ )
		{
			// Get the raw value and sign, square the raw value, put back the sign
			float sign = Math.signum( curValue );
			float absVal = Math.abs( curValue );
			floatBuffer[ s ] = sign * (absVal * absVal);
			curValue += increment;
		}
		
		// And set the middle value to zero
		int centerIndex = ( capacity / 2);
		floatBuffer[ centerIndex ] = 0.0f;

	}

}
