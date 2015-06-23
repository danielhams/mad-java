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

package uk.co.modularaudio.util.audio.lookuptable.listenable;

import uk.co.modularaudio.util.audio.lookuptable.LookupTableUtils;
import uk.co.modularaudio.util.math.MathDefines;

public class ListenableWaveTableFourierGenerator
{
	public static void fillTable( final ListenableWaveTable destTable, final int harms, final ListenableWaveTable amps, final float iPhase )
	{
		float phase = iPhase;
		float a;
		double w;
		phase *= MathDefines.TWO_PI_F;
		final int destTableLength = destTable.length;
		/*
		for( int i = 0 ; i < destTable.getLength() ; i++ )
		{
			destTable.setValueAt(i, 0.0f );
		}
		*/

		for( int i = 0 ; i < harms ; i++ )
		{
			a = ( amps == null ? 1.0f : amps.getValueAt(i) );
			for( int n = 0 ; n < destTableLength ; n++ )
			{
				w = ( i + 1 ) * ( n * MathDefines.TWO_PI_F / destTableLength - 2 );
				final double valueToAdd = (a * Math.cos( w + phase ) );
				final float curValue = destTable.getValueAt( n );
				destTable.setValueAt( n , (float)(curValue + valueToAdd ));
			}
		}
		LookupTableUtils.normaliseFloats( destTable.floatBuffer, 0, destTableLength );
	}

}
