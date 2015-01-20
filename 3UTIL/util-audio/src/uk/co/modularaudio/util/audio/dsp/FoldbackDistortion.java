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


public class FoldbackDistortion
{
//	private static Log log = LogFactory.getLog( FoldbackDistortion.class.getName() );
	
	public static float filter( FoldbackDistortionRT rt, float[] input )
	{
		
		for( int i = 0 ; i < input.length ; i++ )
		{
			float val = input[i];
			float absVal = Math.abs( val );
			float diff = absVal - rt.threshold;
			int numFoldbacks = 0;
			if( diff > 0.0f && rt.maxFoldbacks == 0 )
			{
				if( val < 0.0f )
				{
					val = -rt.threshold;
				}
				else
				{
					val = rt.threshold;
				}
			}
			else
			{
				while( diff > 0.0f && numFoldbacks < rt.maxFoldbacks)
				{
					if( val < 0.0f )
					{
						val = - ( rt.threshold - diff );
					}
					else
					{
						val = rt.threshold -diff;
					}
					absVal = Math.abs( val );
					diff = absVal - rt.threshold;
					numFoldbacks++;
				}
				
				if( absVal > rt.threshold )
				{
					if( val > 0.0f )
					{
						val = rt.threshold;
					}
					else
					{
						val = -rt.threshold;
					}
				}
			}
			input[i] = val;
		}		
		return input[0];
	}
}
