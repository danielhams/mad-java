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

public class LinearInterpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	private int interpolationLength;
	private int numLeftToInterpolate;

	private float curVal;
	private float desVal;
	private float deltaVal;

	private float lowerBound;
	private float upperBound;

	private boolean hasValueWaiting = false;
	private float waitingVal;

	public LinearInterpolator()
	{
		this.lowerBound = Float.NEGATIVE_INFINITY;
		this.upperBound = Float.POSITIVE_INFINITY;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
//		log.trace( "Interpolation has " + numLeftToInterpolate + " left" );
		int interpolationLoopFrames = (numLeftToInterpolate < length ? numLeftToInterpolate : length );
		int numLeft = length;
		int curIndex = outputIndex;

		// Interpolation loop
		if( interpolationLoopFrames > 0 )
		{
			do
			{
//				log.trace( "Will do " + interpolationLoopFrames + " this round of interpolation");
				for( int i = 0 ; i < interpolationLoopFrames ; ++i )
				{
					curVal += deltaVal;
//					log.trace( "Moved to value " +
//							MathFormatter.fastFloatPrint( curVal, 8, true ));
					curVal = (curVal < lowerBound ? lowerBound :
						(curVal > upperBound ? upperBound :
							curVal ) );
					output[ curIndex + i ] = curVal;
				}
				curIndex += interpolationLoopFrames;
				numLeftToInterpolate -= interpolationLoopFrames;
				numLeft -= interpolationLoopFrames;

				if( numLeftToInterpolate == 0 )
				{
					curVal = desVal;

					if( hasValueWaiting )
					{
						numLeftToInterpolate = interpolationLength;
						desVal = waitingVal;
						deltaVal = (desVal - curVal) / interpolationLength;
						hasValueWaiting = false;

						interpolationLoopFrames = (numLeftToInterpolate < numLeft ? numLeftToInterpolate : numLeft );
					}
					else
					{
						interpolationLoopFrames = 0;
					}
				}
				else
				{
					interpolationLoopFrames = 0;
				}
			}
			while( interpolationLoopFrames > 0 );
		}

		// Fill in any steady state
		if( numLeft > 0 )
		{
//			log.trace("Have " + numLeft + " left to steady state fill");
			Arrays.fill( output, curIndex, outputIndex + length, curVal );
		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		if( !hasValueWaiting && numLeftToInterpolate == 0 )
		{
//			log.trace("Notified of value " + newValue + " and is currently steady state");
			// Restart the interpolation
			numLeftToInterpolate = interpolationLength;
			desVal = newValue;
			deltaVal = (desVal - curVal) / interpolationLength;
		}
		else
		{
//			log.trace("Notified of value " + newValue + " but we are already interpolating or have value waiting");
			hasValueWaiting = true;
			waitingVal = newValue;
		}
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
		numLeftToInterpolate = 0;
		hasValueWaiting = false;
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
		interpolationLength = interpolatorLengthFrames;
		hardSetValue( desVal );
	}

	@Override
	public float getValue()
	{
		return desVal;
	}
}
