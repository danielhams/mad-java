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

package uk.co.modularaudio.util.audio.dsp;

public class MidSideProcessor
{
	public final static void lrToMs( float[] left, float[] right, float[] mid, float[] side, int length )
	{
		for( int i = 0 ; i < length ; i++ )
		{
			mid[ i ] = 0.5f * (left[i] + right[i]);
			side[ i ] = 0.5f * (left[i] - right[i]);
		}
	}
	
	public final static void msToLr( float[] mid, float[] side, float[] left, float[] right, int length )
	{
		for( int i = 0 ; i < length ; i++ )
		{
			left[ i ] = mid[ i ] + side[ i ];
			right[ i ] = mid[ i ] - side[ i ];
		}
	}
}
