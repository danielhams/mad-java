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

package uk.co.modularaudio.util.audio.fft;

import java.util.Arrays;

public class FftUtils
{
	public static void autocorr( final float[] input, final float[] output )
	{
		float tmp = 0.0f;
		for( int i = 0 ; i < input.length ; i++ )
		{
			for( int j = i ; j < input.length ; j++ )
			{
				tmp += input[j-i] * input[j];
			}
			output[i] = tmp / (input.length - i );
			tmp = 0.0f;
		}
	}

    public static float maxValue( final float[] in )
    {
        float retVal = 0.0f;
        for( int i = 0 ; i < in.length ; i++ )
            {
                if( in[i] > retVal )
                    {
                        retVal = in[i];
                    }
            }
        return retVal;
    }

	public static int maxValuePos(final float[] in)
	{
        float maxVal = 0.0f;
        int maxPos = 0;
        for( int i = 0 ; i < in.length ; i++ )
            {
                if( in[i] > maxVal )
                    {
                        maxVal = in[i];
                        maxPos = i;
                    }
            }
        return maxPos;
	}

	public static void weight(final float[] input, final float[] weights)
	{
		final int length = ( input.length < weights.length ? input.length : weights.length );
		for( int i = 0 ; i < length ; i++ )
		{
			input[i] = input[i] * weights[i];
		}
	}

	public static void rev(final float[] input)
	{
		float tmp;
		for( int i = 0 ; i < (int)Math.floor( input.length / 2) ; i++ )
		{
			tmp = input[i];
			input[i] = input[ input.length - 1 - i];
			input[ input.length - 1 -i] = tmp;
		}
	}

	public static void zeros(final float[] input)
	{
		Arrays.fill(input, 0, input.length, 0.0f );
	}

	public static void ones(final float[] input)
	{
		Arrays.fill(input, 0, input.length, 1.0f );
	}

	public static float quadint(final float[] input, final int pos)
	{
		float s0, s1, s2;
		final int x0 = (pos < 1 ? pos : pos - 1 );
		final int x2 = (pos + 1 < input.length ? pos + 1 : pos );
		if( x0 == pos )
		{
			return ( input[pos] <= input[x2] ? input[pos] : input[x2] );
		}
		if( x2 == pos )
		{
			return( input[pos] <= input[x0] ? input[pos] : input[x0] );
		}
		s0 = input[x0];
		s1 = input[pos];
		s2 = input[x2];
		return pos + 0.5f * (s2 - s0) / (s2 - 2.0f * s1 + s0 );
	}

	public static void smbFft(final float[] fftBuffer, final long fftFrameSize, final long sign)
	/*
		FFT routine, (C)1996 S.M.Bernsee. Sign = -1 is FFT, 1 is iFFT (inverse)
		Fills fftBuffer[0...2*fftFrameSize-1] with the Fourier transform of the
		time domain data in fftBuffer[0...2*fftFrameSize-1]. The FFT array takes
		and returns the cosine and sine parts in an interleaved manner, ie.
		fftBuffer[0] = cosPart[0], fftBuffer[1] = sinPart[0], asf. fftFrameSize
		must be a power of 2. It expects a complex input signal (see footnote 2),
		ie. when working with 'common' audio signals our input signal has to be
		passed as {in[0],0.,in[1],0.,in[2],0.,...} asf. In that case, the transform
		of the frequencies of interest is in fftBuffer[0...fftFrameSize].
	*/
	{
		float wr, wi, arg,  temp;
		float tr, ti, ur, ui;
		int p1, p2,i, bitm, j, le, le2, k, p1r, p1i, p2r, p2i;

		for (i = 2; i < 2*fftFrameSize-2; i += 2) {
			for (bitm = 2, j = 0; bitm < 2*fftFrameSize; bitm <<= 1) {
				if ((i & bitm) != 0) j++;
				j <<= 1;
			}
			if (i < j) {
				p1 = i;
				p2 = j;
				temp = fftBuffer[p1];
				fftBuffer[p1] = fftBuffer[p2];
				p1++;
				fftBuffer[p2] = temp;
				p2++;
				temp = fftBuffer[p1];
				fftBuffer[p1] = fftBuffer[p2];
				fftBuffer[p2] = temp;
			}
		}

		for (k = 0, le = 2; k < (long)(Math.log(fftFrameSize)/Math.log(2.)+.5); k++) {
			le <<= 1;
			le2 = le>>1;
			ur = 1.0f;
			ui = 0.0f;
			arg = (float) (Math.PI / (le2>>1));
			wr = (float) Math.cos(arg);
			wi = (float) (sign*Math.sin(arg));
			for (j = 0; j < le2; j += 2) {
				p1r = j; p1i = p1r+1;
				p2r = p1r+le2; p2i = p2r+1;
				for (i = j; i < 2*fftFrameSize; i += le) {
					tr = fftBuffer[p2r] * ur - fftBuffer[p2i] * ui;
					ti = fftBuffer[p2r] * ui + fftBuffer[p2i] * ur;
					fftBuffer[p2r] = fftBuffer[p1r] - tr; fftBuffer[p2i] = fftBuffer[p1i] - ti;
					fftBuffer[p1r] += tr; fftBuffer[p1i] += ti;

					p1r += le; p1i += le;

					p2r += le; p2i += le;

				}

				tr = ur*wr - ui*wi;

				ui = ur*wi + ui*wr;

				ur = tr;

			}
		}
	}

	public static void ones(final double[] input)
	{
		Arrays.fill(input, 0, input.length, 1.0 );
	}
}
