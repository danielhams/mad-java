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

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class CDButterworthFilter
{
//	private static Log log = LogFactory.getLog( CDButterworthFilter.class.getName() );

	private final float[] feedbackDelaySamples = new float[2];

	private float prevFreq = -1.0f;
	private FrequencyFilterMode prevMode = FrequencyFilterMode.NONE;
	private float prevSr = -1.0f;
	private float prevBw = -1.0f;

	private float a, a1, a2, b1, b2;
	private float tanthe, sqrtan, tansq, w;

	public CDButterworthFilter()
	{
		clear();
	}

	public final void clear()
	{
		feedbackDelaySamples[0] = 0.0f;
		feedbackDelaySamples[1] = 0.0f;
	}

	// private final static boolean DEBUG_NAN = false;

	private final void recompute( final FrequencyFilterMode mode,
			final float sr,
			final float iFreq,
			final float bw )
	{
		float freq = iFreq;
		if (freq < 10.0f)
		{
			freq = 10.0f;
		}

		if( freq == prevFreq && mode == prevMode && sr == prevSr && bw == prevBw )
		{
			return;
		}

		switch (mode)
		{
			case LP:
				tanthe = (float) (1.0f / FastMath.tan( MathDefines.ONE_PI_D * freq / sr ));
				sqrtan = 2.0f * tanthe;
				tansq = tanthe * tanthe;
				a = 1.0f / (1.0f + sqrtan + tansq);
				a1 = 2.0f * a;
				a2 = a;
				b1 = 2.0f * (1.0f - tansq) * a;
				b2 = (1.0f - sqrtan + tansq) * a;
				break;
			default:
				a = 0;
				a1 = 0;
				a2 = 0;
				b1 = 0;
				b2 = 0;
				break;
		}

		prevFreq = freq;
		prevMode = mode;
		prevSr = sr;
		prevBw = bw;
	}

	public final void filter( final float[] input, final int offset, final int length, final float freq, final float bw,
			final FrequencyFilterMode mode, final float sr )
	{
		if( mode == FrequencyFilterMode.NONE ||
				mode != FrequencyFilterMode.LP )
		{
			return;
		}

		recompute( mode, sr, freq, bw );

		for (int i = 0; i < length; i++)
		{
			w = input[offset + i] - b1 * feedbackDelaySamples[0] - b2 * feedbackDelaySamples[1];
			final float absW = ( w < 0.0f ? -w : w );

			if( absW < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
			{
				w = 0.0f;
			}
			float result = (a * w + a1 * feedbackDelaySamples[0] + a2 * feedbackDelaySamples[1]);

			if ((result > 0.0f && result < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F)
					|| (result < 0.0f && result > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F))
			{
				result = 0.0f;
			}

			input[offset + i] = result;

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = w;
		}
	}

	public final void filterWithFreq( final float[] input, final int offset, final int length,
			final float[] freqs, final int freqsOffset,
			final float bw, final FrequencyFilterMode mode, final float sr )
	{
		if( mode == FrequencyFilterMode.NONE ||
				mode != FrequencyFilterMode.LP )
		{
			return;
		}

		for (int i = 0; i < length; i++)
		{
			float freq = freqs[ freqsOffset + i];
			if (freq < 1.0f)
			{
				freq = 1.0f;
			}
			recompute( mode, sr, freq, bw );

			w = input[offset + i] - b1 * feedbackDelaySamples[0] - b2 * feedbackDelaySamples[1];
			final float result = (a * w + a1 * feedbackDelaySamples[0] + a2 * feedbackDelaySamples[1]);

			input[offset + i] = result;

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = w;
		}
	}

	public final void filterWithFreqAndBw( final float[] input, final int offset, final int length,
			final float[] freqs, final int freqsOffset,
			final float[] bws, final int bwsOffset,
			final FrequencyFilterMode mode, final float sr )
	{
		if( mode == FrequencyFilterMode.NONE ||
				mode != FrequencyFilterMode.LP )
		{
			return;
		}

		for (int i = 0; i < length; i++)
		{
			float freq = freqs[ freqsOffset + i];
			if (freq < 1.0f)
			{
				freq = 1.0f;
			}
			final float bw = bws[ bwsOffset + i];
			recompute( mode, sr, freq, bw );

			w = input[offset + i] - b1 * feedbackDelaySamples[0] - b2 * feedbackDelaySamples[1];
			final float result = (a * w + a1 * feedbackDelaySamples[0] + a2 * feedbackDelaySamples[1]);

			input[offset + i] = result;

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = w;
		}
	}
}
