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
	private float in1, in2, in3, in4;
	private float out1, out2, out3, out4;

	public MoogFilter()
	{
		reset();
	}

	public final void reset()
	{
		in1 = 0.0f;
		in2 = 0.0f;
		in3 = 0.0f;
		in4 = 0.0f;
		out1 = 0.0f;
		out2 = 0.0f;
		out3 = 0.0f;
		out4 = 0.0f;
	}

	public void filter( final float[] cutoff, final float[] res, final float[] in, final int inPos, final float[] out, final int outPos, final int length )
	{
		for( int i = inPos ; i < inPos + length ; ++i )
		{
			float input = in[ i ];
			final float f = cutoff[ i ] * 1.16f;
			final float oneMinusF = (1.0f - f);
			final float f2 = f * f;
			final float fb = res[i] * (1.0f - 0.15f * f2 );
			input = input - (out4 * fb);
			input = input * 0.35013f * f2 * f2;
			out1 = input + (0.3f * in1) + (oneMinusF * out1);
			in1 = input;
			out2 = out1 + (0.3f * in2) + (oneMinusF * out2);
			in2  = out1;
			out3 = out2 + (0.3f * in3) + (oneMinusF * out3);
			in3 = out2;
			out4 = out3 + (0.3f * in4) + (oneMinusF * out4);
			in4 = out3;
			out[i] = out4;
		}
	}

	public void filter( final float[] cutoff, final int cutoffOffset,
			final float[] res, final int resOffset,
			final float[] in, final int inPos,
			final float[] out, final int outPos,
			final int length )
	{
		for( int i = 0 ; i < length ; ++i )
		{
			float input = in[ inPos + i ];
			final float f = cutoff[ cutoffOffset + i ] * 1.16f;
			final float oneMinusF = (1.0f - f);
			final float f2 = f * f;
			final float fb = res[ resOffset + i ] * (1.0f - 0.15f * f2 );
			input = input - (out4 * fb);
			input = input * 0.35013f * f2 * f2;
			out1 = input + (0.3f * in1) + (oneMinusF * out1);
			in1 = input;
			out2 = out1 + (0.3f * in2) + (oneMinusF * out2);
			in2  = out1;
			out3 = out2 + (0.3f * in3) + (oneMinusF * out3);
			in3 = out2;
			out4 = out3 + (0.3f * in4) + (oneMinusF * out4);
			in4 = out3;
			out[ outPos + i ] = out4;
		}
	}
}
