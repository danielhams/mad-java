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


public class RBJFilter
{
	public static void filterIt( RBJFilterRT rt, float[] input, int inPos, float[] output, int outPos, int length )
	{
		for( int s = 0 ; s < length ; ++s )
		{
			float sample = input[ inPos + s];
			float result = rt.b0a0 * sample + rt.b1a0 * rt.x1 + rt.b2a0 * rt.x2 - rt.a1a0 * rt.y1 - rt.a2a0 * rt.y2;
			
			rt.x2 = rt.x1;
			rt.x1 = sample;
			rt.y2 = rt.y1;
			rt.y1 = result;
			
			output[outPos + s] = result;
		}
	}
	
	public static float filterIt( RBJFilterRT rt, float sample )
	{
		float result = rt.b0a0 * sample + rt.b1a0 * rt.x1 + rt.b2a0 * rt.x2 - rt.a1a0 * rt.y1 - rt.a2a0 * rt.y2;
		
		rt.x2 = rt.x1;
		rt.x1 = sample;
		rt.y2 = rt.y1;
		rt.y1 = result;
		
		return result;
	}
}
