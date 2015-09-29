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

import uk.co.modularaudio.util.audio.dsp.CDButterworthFilter24DBDouble;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.math.AudioMath;


public class CDSCLowPass24Interpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( CDSCLowPassInterpolator24.class.getName() );

	protected float desVal;

	protected int sampleRate;
	protected final CDButterworthFilter24DBDouble lpFilter;

	private static final int TMP_LENGTH = 1024;
	private static final int NUM_RESET_ITERS = 10;

	private float lastValue = Float.MIN_NORMAL;

	private final double[] internalDoubleArray = new double[TMP_LENGTH];

	public CDSCLowPass24Interpolator()
	{
		lpFilter = new CDButterworthFilter24DBDouble();
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;

		if( lastValue != desVal )
		{
			Arrays.fill( internalDoubleArray, 0, length, desVal );

			int numLeft = length;
			int outOffset = outputIndex;

			while( numLeft > 0 )
			{
				final int numThisRound = ( numLeft < TMP_LENGTH ? numLeft : TMP_LENGTH );

				Arrays.fill( internalDoubleArray, 0, numThisRound, desVal );

				lpFilter.filter(
						internalDoubleArray,
						0,
						numThisRound,
						LowPassInterpolatorConstants.LOW_PASS_CUTOFF,
						0.5f,
						FrequencyFilterMode.LP,
						sampleRate );
				for( int s = 0 ; s < numThisRound ; ++s )
				{
					output[outOffset++] = (float)internalDoubleArray[s];
				}
				numLeft -= numThisRound;
			}
			lastValue = output[lastIndex-1];
//			log.trace( "lastValue set to " +
//					MathFormatter.fastFloatPrint( lastValue, 12, true ) );
		}
		else
		{
			Arrays.fill( output, outputIndex, lastIndex, desVal );
		}
	}

	@Override
	public boolean checkForDenormal()
	{
		float diffVal = desVal - lastValue;
		diffVal = (diffVal < 0.0f ? -diffVal : diffVal);
		if( diffVal != 0.0f && diffVal < AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_F )
		{
//			log.trace("Found a minimum diff that looks like stability: " +
//					MathFormatter.slowDoublePrint( diffVal, 12, true ) );
//			log.trace("Settled on: " +
//					MathFormatter.slowDoublePrint( desVal, 12, true ) );
			lastValue = desVal;
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
		this.desVal = value;
		Arrays.fill( internalDoubleArray, desVal );

		for( int i = 0 ; i < NUM_RESET_ITERS ; ++i )
		{
			lpFilter.filter( internalDoubleArray,
					0,
					TMP_LENGTH,
					LowPassInterpolatorConstants.LOW_PASS_CUTOFF,
					0.5f,
					FrequencyFilterMode.LP,
					sampleRate );
		}
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
	}

	@Override
	public void resetSampleRateAndPeriod( final int sampleRate,
			final int periodLengthFrames,
			final int interpolatorLengthFrames )
	{
		this.sampleRate = sampleRate;
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		desVal = newValue;
	}

	@Override
	public float getValue()
	{
		return desVal;
	}

}
