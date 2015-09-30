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

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class LinearLowPass12Interpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	private int curWindowPos;
	private int interpolationLength;

	private float curVal;
	private float desVal;
	private float deltaVal;

	private float lowerBound;
	private float upperBound;

	private final double[] feedbackDelaySamples = new double[4];

	private double a, a1, a2, b1, b2;
	private double tanthe, sqrtan, tansq;

	private final float lowPassFrequency;

	public LinearLowPass12Interpolator()
	{
		this.lowerBound = Float.NEGATIVE_INFINITY;
		this.upperBound = Float.POSITIVE_INFINITY;
		this.lowPassFrequency = LowPassInterpolatorConstants.LOW_PASS_CUTOFF;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int numLeftInInterpolation = interpolationLength - curWindowPos;
//		log.trace( "Interpolation has " + numLeftInInterpolation + " left" );
		final int numWithInterpolation = (numLeftInInterpolation < length ? numLeftInInterpolation : length );
		final int numAfter = length - numWithInterpolation;
//		log.trace( "Will do " + numWithInterpolation + " this round along with fill of " + numAfter );
		int curIndex = outputIndex;

		if( numWithInterpolation > 0 )
		{
			for( int i = 0 ; i < numWithInterpolation ; ++i )
			{
				curVal += deltaVal;
//				log.trace( "Moved to value " +
//						MathFormatter.fastFloatPrint( curVal, 8, true ));
				curVal = (curVal < lowerBound ? lowerBound :
					(curVal > upperBound ? upperBound :
						curVal ) );
				output[ curIndex + i ] = curVal;
			}
			curIndex += numWithInterpolation;
			curWindowPos += numWithInterpolation;
		}

		// No further use of interpolation necessary, move to desVal
		// and make it curVal
		if( numAfter > 0 )
		{
			curVal = desVal;
			Arrays.fill( output, curIndex, outputIndex + length, curVal );
		}

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
	public void notifyOfNewValue( final float newValue )
	{
//		if( log.isTraceEnabled() )
//		{
//			log.trace( "Received notification of new value " +
//					MathFormatter.fastFloatPrint( newValue, 8, true ) );
//			log.trace( "CurWindowPos(" + curWindowPos + ") curVal(" +
//					MathFormatter.fastFloatPrint( curVal, 8, true ) +
//					") desVal(" +
//					MathFormatter.fastFloatPrint( desVal, 8, true ) +
//					")");
//		}
		// Restart the interpolation
		curWindowPos = 0;
		desVal = newValue;
		deltaVal = (desVal - curVal) / interpolationLength;
//		if( log.isTraceEnabled() )
//		{
//			log.trace("Reset cur window pos and reset desVal.");
//			log.trace("deltaVal is now (" + MathFormatter.fastFloatPrint( deltaVal, 8, true ) + ")");
//		}
	}

	@Override
	public boolean checkForDenormal()
	{
		float diffVal = desVal - curVal;
		diffVal = (diffVal < 0.0f ? -diffVal : diffVal);
		if( diffVal < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
		{
			curVal = desVal;
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void hardSetValue( final float value )
	{
		this.curVal = value;
		this.desVal = value;
		curWindowPos = interpolationLength;
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public void resetSampleRateAndPeriod( final int sampleRate,
			final int periodLengthFrames,
			final int interpolatorLengthFrames )
	{
		curWindowPos = 0;
		interpolationLength = interpolatorLengthFrames;

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

	@Override
	public float getValue()
	{
		return desVal;
	}
}
