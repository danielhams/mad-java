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

package uk.co.modularaudio.util.audio.wavetable.listenable;

import uk.co.modularaudio.util.math.MathDefines;

public class ListenableWaveTableFourierGenerator
{
	public static void fillTable( ListenableWaveTable destTable, int harms, ListenableWaveTable amps, float phase )
	{
		float a;
		double w;
		phase *= MathDefines.TWO_PI_F;
		int destTableLength = destTable.getLength();
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
				double valueToAdd = (a * Math.cos( w + phase ) );
				float curValue = destTable.getValueAt( n );
				destTable.setValueAt( n , (float)(curValue + valueToAdd ));
			}
		}
		ListenableWaveTableUtils.normalise_table( destTable, 1.0f );
	}
	
}
