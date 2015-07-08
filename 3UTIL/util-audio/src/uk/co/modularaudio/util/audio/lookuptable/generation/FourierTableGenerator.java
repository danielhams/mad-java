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

package uk.co.modularaudio.util.audio.lookuptable.generation;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.lookuptable.LookupTableUtils;
import uk.co.modularaudio.util.math.MathDefines;

public class FourierTableGenerator
{
	public static void fillTable( final float[] buffer, final int position, final int length, final int harms, final float[] amps, final float phase )
	{
		double a;
		double w;
		final double doublePhase = phase * MathDefines.TWO_PI_D;
		Arrays.fill( buffer, 0.0f );

		for( int i = 0 ; i < harms ; i++ )
		{
			a = ( amps == null ? 1.0f : amps[i] );
			for( int n = 0 ; n < length ; n++ )
			{
				w = ( i + 1 ) * ( n * MathDefines.TWO_PI_D / (length - 1) );
				buffer[ position + n ] += (a * Math.cos( w + doublePhase ) );
			}
		}
		LookupTableUtils.normaliseFloats( buffer, position, length );
	}

}
