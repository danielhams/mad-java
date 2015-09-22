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

	private float curVal;
	private float desVal;
	private float deltaVal;

	public LinearInterpolator()
	{
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int numLeftInInterpolation = lastWindowPos - curWindowPos;
		final int numWithInterpolation = (numLeftInInterpolation < length ? numLeftInInterpolation : length );
		final int numAfter = length - numWithInterpolation;
		int curIndex = outputIndex;

		if( numWithInterpolation > 0 )
		{
			for( int i = 0 ; i < numWithInterpolation ; ++i )
			{
				output[ curIndex + i ] = curVal;
				curVal += deltaVal;
			}
			curIndex += numWithInterpolation;
			curWindowPos += numWithInterpolation;
		}

		// No further use of interpolation necessary, move to desVal
		// and make it curVal
		if( numAfter > 0 )
		{
			Arrays.fill( output, curIndex, outputIndex + length, curVal );
		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
//		if( curVal != desVal )
//		{
//			log.debug("Restarting interpolation");
//		}
		// Restart the interpolation
		curWindowPos = 0;
		desVal = newValue;
		deltaVal = (desVal - curVal) / (lastWindowPos - 1);
	}

	@Override
	public boolean checkForDenormal()
	{
		if( curVal > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && curVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		{
			curVal = 0.0f;
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
		curWindowPos = lastWindowPos;
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
	}

	@Override
	public void resetSampleRateAndPeriod( final int sampleRate, final int periodLengthFrames )
	{
		curWindowPos = 0;
		lastWindowPos = periodLengthFrames;
	}
}
