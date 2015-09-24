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

import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class CDButterworthFilter24DBDouble
{
//	private static Log log = LogFactory.getLog( OptimisedButterworthFilter.class.getName() );

	private final double[] feedbackDelaySamples = new double[4];

	private double prevFreq = -1.0;
	private FrequencyFilterMode prevMode = FrequencyFilterMode.NONE;
	private double prevSr = -1.0;
	private double prevBw = -1.0;

	private double a, a1, a2, b1, b2;
	private double tanthe, sqrtan, tansq;

	public CDButterworthFilter24DBDouble()
	{
		clear();
	}

	public final void clear()
	{
		feedbackDelaySamples[0] = 0.0;
		feedbackDelaySamples[1] = 0.0;
		feedbackDelaySamples[2] = 0.0;
		feedbackDelaySamples[3] = 0.0;
	}

	// private final static boolean DEBUG_NAN = false;

	private final void recompute( final FrequencyFilterMode mode,
			final int sr,
			final double iFreq,
			final double bw )
	{
		double freq = iFreq;
		if (freq < 10.0)
		{
			freq = 10.0;
		}

		if( freq == prevFreq && mode == prevMode && sr == prevSr && bw == prevBw )
		{
			return;
		}

		switch (mode)
		{
			case LP:
				tanthe = (1.0 / FastMath.tan( MathDefines.ONE_PI_D * freq / sr ));
				sqrtan = 2.0 * tanthe;
				tansq = tanthe * tanthe;
				a = 1.0 / (1.0 + sqrtan + tansq);
				a1 = 2.0 * a;
				a2 = a;
				b1 = 2.0 * (1.0 - tansq) * a;
				b2 = (1.0 - sqrtan + tansq) * a;
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

	public final void filter( final double[] input,
			final int offset,
			final int length,
			final double freq,
			final double bw,
			final FrequencyFilterMode mode,
			final int sr )
	{
		if( mode == FrequencyFilterMode.NONE ||
				mode != FrequencyFilterMode.LP )
		{
			return;
		}

		recompute( mode, sr, freq, bw );

		for (int i = 0; i < length; i++)
		{
			final double w = input[offset + i] - b1 * feedbackDelaySamples[0] - b2 * feedbackDelaySamples[1];
			final double result = (a * w + a1 * feedbackDelaySamples[0] + a2 * feedbackDelaySamples[1]);
//			if( result < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//					result > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//			{
//				result = 0.0f;
//			}

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = w;

			// And second pass (for 24 db)
			final double we = result - b1 * feedbackDelaySamples[2] - b2 * feedbackDelaySamples[3];
			final double resulte = (a * we + a1 * feedbackDelaySamples[2] + a2 * feedbackDelaySamples[3]);
//			if( resulte < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//					resulte > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//			{
//				resulte = 0.0f;
//			}

			feedbackDelaySamples[3] = feedbackDelaySamples[2];
			feedbackDelaySamples[2] = we;

			input[offset + i] = resulte;
		}

//		if( feedbackDelaySamples[0] < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//				feedbackDelaySamples[0] > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//		{
//			feedbackDelaySamples[0] = 0.0f;
//		}
//		if( feedbackDelaySamples[1] < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//				feedbackDelaySamples[1] > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//		{
//			feedbackDelaySamples[1] = 0.0f;
//		}
//		if( feedbackDelaySamples[2] < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//				feedbackDelaySamples[2] > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//		{
//			feedbackDelaySamples[2] = 0.0f;
//		}
//		if( feedbackDelaySamples[3] < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F &&
//				feedbackDelaySamples[3] > -AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
//		{
//			feedbackDelaySamples[3] = 0.0f;
//		}
	}
}
