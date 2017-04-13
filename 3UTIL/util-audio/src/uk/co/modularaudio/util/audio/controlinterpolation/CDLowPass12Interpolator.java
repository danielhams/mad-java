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

package uk.co.modularaudio.util.audio.controlinterpolation;

import java.util.Arrays;

import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;


public class CDLowPass12Interpolator extends AbstractCDLowPassInterpolator
{
	private final static int TEST_VALUES_LENGTH = 16;

	private final double[] feedbackDelaySamples = new double[4];

	private double a, a1, a2, b1, b2;
	private double tanthe, sqrtan, tansq;

	public CDLowPass12Interpolator()
	{
	}

	@Override
	protected void lowPassFilter( final float[] output, final int outputIndex, final int length )
	{
		for (int i = 0; i < length; i++)
		{
			final double w = output[outputIndex + i] - b1 * feedbackDelaySamples[0] - b2 * feedbackDelaySamples[1];
			final double result = (a * w + a1 * feedbackDelaySamples[0] + a2 * feedbackDelaySamples[1]);

			feedbackDelaySamples[1] = feedbackDelaySamples[0];
			feedbackDelaySamples[0] = w;

//			// And second pass (for 24 db)
//			final double we = result - b1 * feedbackDelaySamples[2] - b2 * feedbackDelaySamples[3];
//			final double resulte = (a * we + a1 * feedbackDelaySamples[2] + a2 * feedbackDelaySamples[3]);
//
//			feedbackDelaySamples[3] = feedbackDelaySamples[2];
//			feedbackDelaySamples[2] = we;

			output[outputIndex + i] = (float)result;
		}
	}

	@Override
	protected void hardSetLowPass( final float value )
	{
		final float[] testValues = new float[TEST_VALUES_LENGTH];
		Arrays.fill( testValues, value );
		for( int i = 0; i < 100 ; ++i )
		{
			lowPassFilter( testValues, 0, TEST_VALUES_LENGTH );
			Arrays.fill( testValues, value );
		}
	}

	@Override
	protected void resetLowPass( final int sampleRate )
	{
		double freq = lowPassFrequency;
		if (freq < 10.0)
		{
			freq = 10.0;
		}

		tanthe = (1.0 / FastMath.tan( MathDefines.ONE_PI_D * freq / sampleRate ));
		sqrtan = 2.0 * tanthe;
		tansq = tanthe * tanthe;
		a = 1.0 / (1.0 + sqrtan + tansq);
		a1 = 2.0 * a;
		a2 = a;
		b1 = 2.0 * (1.0 - tansq) * a;
		b2 = (1.0 - sqrtan + tansq) * a;
	}
}
