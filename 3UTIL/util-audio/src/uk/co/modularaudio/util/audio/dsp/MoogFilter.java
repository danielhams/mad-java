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

public class MoogFilter
{
	public static void filterIt( MoogFilterRT rt, float[] cutoff, float[] res, float[] in, int inPos, float[] out, int outPos, int length )
	{
		for( int i = inPos ; i < inPos + length ; ++i )
		{
			float input = in[ i ];
			float f = cutoff[ i ] * 1.16f;
			float oneMinusF = (1.0f - f);
			float f2 = f * f;
			float fb = res[i] * (1.0f - 0.15f * f2 );
			input = input - (rt.out4 * fb);
			input = input * 0.35013f * f2 * f2;
			rt.out1 = input + (0.3f * rt.in1) + (oneMinusF * rt.out1);
			rt.in1 = input;
			rt.out2 = rt.out1 + (0.3f * rt.in2) + (oneMinusF * rt.out2);
			rt.in2  =rt.out1;
			rt.out3 = rt.out2 + (0.3f * rt.in3) + (oneMinusF * rt.out3);
			rt.in3 = rt.out2;
			rt.out4 = rt.out3 + (0.3f * rt.in4) + (oneMinusF * rt.out4);
			rt.in4 = rt.out3;
			out[i] = rt.out4;
		}
	}
}
