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

import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class ButterworthCrossover24DB
{
	private final float[] lpFeedbackDelaySamples = new float[4];
	private final float[] hpFeedbackDelaySamples = new float[4];

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
		lpFeedbackDelaySamples[0] = 0.0f;
		lpFeedbackDelaySamples[1] = 0.0f;
		lpFeedbackDelaySamples[2] = 0.0f;
		lpFeedbackDelaySamples[3] = 0.0f;
		hpFeedbackDelaySamples[0] = 0.0f;
		hpFeedbackDelaySamples[1] = 0.0f;
		hpFeedbackDelaySamples[2] = 0.0f;
		hpFeedbackDelaySamples[3] = 0.0f;
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
			final float[] chpBuffer )
	{
		// Will do for now
		recompute( sampleRate, frequency );
		for( int i = 0 ; i < length ; ++i )
		{
			final float inputFloat = input[offset + i];
			final float lpW = inputFloat - lpB1 * lpFeedbackDelaySamples[0] - lpB2 * lpFeedbackDelaySamples[1];
			final float lpResult = (lpA * lpW  + lpA1 * lpFeedbackDelaySamples[0] + lpA2 * lpFeedbackDelaySamples[1]);

			final float tmpLpResult = lpResult;

			lpFeedbackDelaySamples[1] = lpFeedbackDelaySamples[0];
			lpFeedbackDelaySamples[0] = lpW;

			final float lpW2 = tmpLpResult - lpB1 * lpFeedbackDelaySamples[2] - lpB2 * lpFeedbackDelaySamples[3];
			final float lpResult2 = (lpA * lpW2  + lpA1 * lpFeedbackDelaySamples[2] + lpA2 * lpFeedbackDelaySamples[3]);

			clpBuffer[offset + i] = lpResult2;

			lpFeedbackDelaySamples[3] = lpFeedbackDelaySamples[2];
			lpFeedbackDelaySamples[2] = lpW2;

			final float hpW = inputFloat - hpB1 * hpFeedbackDelaySamples[0] - hpB2 * hpFeedbackDelaySamples[1];
			final float hpResult = (hpA * hpW  + hpA1 * hpFeedbackDelaySamples[0] + hpA2 * hpFeedbackDelaySamples[1]);

			final float tmpHpResult = hpResult;

			hpFeedbackDelaySamples[1] = hpFeedbackDelaySamples[0];
			hpFeedbackDelaySamples[0] = hpW;

			final float hpW2 = tmpHpResult - hpB1 * hpFeedbackDelaySamples[2] - hpB2 * hpFeedbackDelaySamples[3];
			final float hpResult2 = (hpA * hpW2  + hpA1 * hpFeedbackDelaySamples[2] + hpA2 * hpFeedbackDelaySamples[3]);

			chpBuffer[offset + i] = hpResult2;

			hpFeedbackDelaySamples[3] = hpFeedbackDelaySamples[2];
			hpFeedbackDelaySamples[2] = hpW2;
		}
	}

	public void filterWithFreq( final float[] input,
			final int offset,
			final int length,
			final float[] srcFreqs, final int srcFreqOffset,
			final float sampleRate,
			final float[] clpBuffer,
			final float[] chpBuffer )
	{
		for( int i = 0 ; i < length ; ++i )
		{
			recompute( sampleRate, srcFreqs[srcFreqOffset+i] );
			final float inputFloat = input[offset + i];
			final float lpW = inputFloat - lpB1 * lpFeedbackDelaySamples[0] - lpB2 * lpFeedbackDelaySamples[1];
			final float lpResult = (lpA * lpW  + lpA1 * lpFeedbackDelaySamples[0] + lpA2 * lpFeedbackDelaySamples[1]);

			final float tmpLpResult = lpResult;

			lpFeedbackDelaySamples[1] = lpFeedbackDelaySamples[0];
			lpFeedbackDelaySamples[0] = lpW;

			final float lpW2 = tmpLpResult - lpB1 * lpFeedbackDelaySamples[2] - lpB2 * lpFeedbackDelaySamples[3];
			final float lpResult2 = (lpA * lpW2  + lpA1 * lpFeedbackDelaySamples[2] + lpA2 * lpFeedbackDelaySamples[3]);

			clpBuffer[offset + i] = lpResult2;

			lpFeedbackDelaySamples[3] = lpFeedbackDelaySamples[2];
			lpFeedbackDelaySamples[2] = lpW2;

			final float hpW = inputFloat - hpB1 * hpFeedbackDelaySamples[0] - hpB2 * hpFeedbackDelaySamples[1];
			final float hpResult = (hpA * hpW  + hpA1 * hpFeedbackDelaySamples[0] + hpA2 * hpFeedbackDelaySamples[1]);

			final float tmpHpResult = hpResult;

			hpFeedbackDelaySamples[1] = hpFeedbackDelaySamples[0];
			hpFeedbackDelaySamples[0] = hpW;

			final float hpW2 = tmpHpResult - hpB1 * hpFeedbackDelaySamples[2] - hpB2 * hpFeedbackDelaySamples[3];
			final float hpResult2 = (hpA * hpW2  + hpA1 * hpFeedbackDelaySamples[2] + hpA2 * hpFeedbackDelaySamples[3]);

			chpBuffer[offset + i] = hpResult2;

			hpFeedbackDelaySamples[3] = hpFeedbackDelaySamples[2];
			hpFeedbackDelaySamples[2] = hpW2;
		}
	}

}
