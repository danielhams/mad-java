/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

import java.util.Arrays;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class ButterworthCrossover24DB
{
//	private final static Log LOG = LogFactory.getLog( ButterworthCrossover24DB.class );

	private final float[] feedbackDelaySamples = new float[8];

	private float prevFreq = -1.0f;
	private float prevSr = -1.0f;

	private float lpA, lpA1, lpA2, lpB1, lpB2, hpA, hpA1, hpA2, hpB1, hpB2;
	private float lpTanthe, hpTanthe;
	private float lpSqrtan, hpSqrtan;
	private float lpTansq, hpTansq;

	public ButterworthCrossover24DB()
	{
		clear();
	}

	public final void clear()
	{
		Arrays.fill( feedbackDelaySamples, 0.0f );
	}

	// private final static boolean DEBUG_NAN = false;

	private final void recompute( final float sr,
			final float iFreq )
	{
		if( iFreq == prevFreq && sr == prevSr )
		{
			return;
		}
		float freq = iFreq;
		if (freq < 10.0f)
		{
			freq = 10.0f;
		}

		lpTanthe = (float) (1.0f / FastMath.tan( MathDefines.ONE_PI_D * freq / sr ));
		hpTanthe = (float) FastMath.tan( MathDefines.ONE_PI_D * freq / sr );
		lpSqrtan = MathDefines.SQRT_TWO_F * lpTanthe;
		hpSqrtan = MathDefines.SQRT_TWO_F * hpTanthe;
		lpTansq = lpTanthe * lpTanthe;
		hpTansq = hpTanthe * hpTanthe;
		lpA = 1.0f / (1.0f + lpSqrtan + lpTansq);
		hpA = 1.0f / (1.0f + hpSqrtan + hpTansq);
		lpA2 = lpA;
		hpA2 = hpA;
		lpA1 = 2.0f * lpA;
		lpB1 = 2.0f * (1.0f - lpTansq) * lpA;
		hpA1 = -2.0f * hpA;
		hpB1 = 2.0f * (hpTansq - 1.0f) * hpA;
		lpB2 = (1.0f - lpSqrtan + lpTansq) * lpA;
		hpB2 = (1.0f - hpSqrtan + hpTansq) * hpA;

		prevFreq = freq;
		prevSr = sr;
	}

	public void filter( final float[] input,
			final int offset,
			final int length,
			final float frequency,
			final float sampleRate,
			final float[] clpBuffer,
			final int clpOffset,
			final float[] chpBuffer,
			final int chpOffset )
	{
		// Will do for now
		recompute( sampleRate, frequency );

		for( int i = 0 ; i < length ; ++i )
		{
			final float inputFloat = input[offset + i];
			final float lpW = inputFloat - lpB1 * feedbackDelaySamples[0] - lpB2 * feedbackDelaySamples[1];
			final float lpResult = (lpA * lpW  + lpA1 * feedbackDelaySamples[0] + lpA2 * feedbackDelaySamples[1]);

			final float tmpLpResult = lpResult;

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = lpW;

			final float lpW2 = tmpLpResult - lpB1 * feedbackDelaySamples[2] - lpB2 * feedbackDelaySamples[3];
			final float lpResult2 = (lpA * lpW2  + lpA1 * feedbackDelaySamples[2] + lpA2 * feedbackDelaySamples[3]);

			clpBuffer[clpOffset + i] = lpResult2;

			feedbackDelaySamples[3] = feedbackDelaySamples[2];
			feedbackDelaySamples[2] = lpW2;

			final float hpW = inputFloat - hpB1 * feedbackDelaySamples[0+4] - hpB2 * feedbackDelaySamples[1+4];
			final float hpResult = (hpA * hpW  + hpA1 * feedbackDelaySamples[0+4] + hpA2 * feedbackDelaySamples[1+4]);

			final float tmpHpResult = hpResult;

			feedbackDelaySamples[1+4] = feedbackDelaySamples[0+4];
			feedbackDelaySamples[0+4] = hpW;

			final float hpW2 = tmpHpResult - hpB1 * feedbackDelaySamples[2+4] - hpB2 * feedbackDelaySamples[3+4];
			final float hpResult2 = (hpA * hpW2  + hpA1 * feedbackDelaySamples[2+4] + hpA2 * feedbackDelaySamples[3+4]);

			chpBuffer[chpOffset + i] = hpResult2;

			feedbackDelaySamples[3+4] = feedbackDelaySamples[2+4];
			feedbackDelaySamples[2+4] = hpW2;
		}
		denormalCheck();
	}

	public void filterWithFreq( final float[] input,
			final int offset,
			final int length,
			final float[] srcFreqs, final int srcFreqOffset,
			final float sampleRate,
			final float[] clpBuffer,
			final int clpOffset,
			final float[] chpBuffer,
			final int chpOffset )
	{
		for( int i = 0 ; i < length ; ++i )
		{
			recompute( sampleRate, srcFreqs[srcFreqOffset+i] );
			final float inputFloat = input[offset + i];
			final float lpW = inputFloat - lpB1 * feedbackDelaySamples[0] - lpB2 * feedbackDelaySamples[1];
			final float lpResult = (lpA * lpW  + lpA1 * feedbackDelaySamples[0] + lpA2 * feedbackDelaySamples[1]);

			final float tmpLpResult = lpResult;

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = lpW;

			final float lpW2 = tmpLpResult - lpB1 * feedbackDelaySamples[2] - lpB2 * feedbackDelaySamples[3];
			final float lpResult2 = (lpA * lpW2  + lpA1 * feedbackDelaySamples[2] + lpA2 * feedbackDelaySamples[3]);

			clpBuffer[clpOffset + i] = lpResult2;

			feedbackDelaySamples[3] = feedbackDelaySamples[2];
			feedbackDelaySamples[2] = lpW2;

			final float hpW = inputFloat - hpB1 * feedbackDelaySamples[0+4] - hpB2 * feedbackDelaySamples[1+4];
			final float hpResult = (hpA * hpW  + hpA1 * feedbackDelaySamples[0+4] + hpA2 * feedbackDelaySamples[1+4]);

			final float tmpHpResult = hpResult;

			feedbackDelaySamples[1+4] = feedbackDelaySamples[0+4];
			feedbackDelaySamples[0+4] = hpW;

			final float hpW2 = tmpHpResult - hpB1 * feedbackDelaySamples[2+4] - hpB2 * feedbackDelaySamples[3+4];
			final float hpResult2 = (hpA * hpW2  + hpA1 * feedbackDelaySamples[2+4] + hpA2 * feedbackDelaySamples[3+4]);

			chpBuffer[chpOffset + i] = hpResult2;

			feedbackDelaySamples[3+4] = feedbackDelaySamples[2+4];
			feedbackDelaySamples[2+4] = hpW2;
		}
		denormalCheck();
	}

	private final void denormalCheck()
	{
		for( int i = 0 ; i < feedbackDelaySamples.length ; ++i )
		{
			final float val = feedbackDelaySamples[i];
			final float absVal = val < 0.0f ? -val : val;
			final boolean tooSmall = absVal < AudioMath.MIN_SIGNED_FLOATING_POINT_32BIT_VAL_F;
			if( tooSmall )
			{
				feedbackDelaySamples[i] = 0.0f;
			}
		}
	}
}
