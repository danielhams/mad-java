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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;

public class LinearInterpolator implements ControlValueInterpolator
{
	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	private int curWindowPos;
	private int lastWindowPos;
	private int interpolationLength;

	private float curVal;
	private float desVal;
	private float deltaVal;

	private float lowerBound;
	private float upperBound;

	public LinearInterpolator( final float lowerBound, final float upperBound )
	{
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
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
		if( diffVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
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
	public void resetSampleRateAndPeriod( final int sampleRate, final int periodLengthFrames )
	{
		curWindowPos = 0;
		interpolationLength = periodLengthFrames;
		lastWindowPos = periodLengthFrames - 1;
	}
}
