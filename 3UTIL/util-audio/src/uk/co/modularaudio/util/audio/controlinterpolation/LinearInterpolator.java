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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class LinearInterpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	private float[] interpolationBuffer;

	private int curWindowPos;
	private int lastWindowPos;

	private float curVal;
	private float desVal;
	private boolean haveValWaiting = false;
	private float nextVal;

	public LinearInterpolator()
	{
	}

	public void reset( final int sampleRate, final float valueChaseMillis )
	{
		final int interpolationLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, valueChaseMillis );
		interpolationBuffer = new float[interpolationLength];
		for( int i = 0 ; i < interpolationLength ; ++i )
		{
			interpolationBuffer[i] = ((float)i) / (float)(interpolationLength - 1);
		}
		curWindowPos = 0;
		lastWindowPos = interpolationLength;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		int numLeftInInterpolation = lastWindowPos - curWindowPos;
		int numWithInterpolation = (numLeftInInterpolation < length ? numLeftInInterpolation : length );
		int numAfter = length - numWithInterpolation;
		int curIndex = outputIndex;

		while( numWithInterpolation > 0 )
		{
			for( int i = 0 ; i < numWithInterpolation ; ++i )
			{
				final float interpVal = interpolationBuffer[curWindowPos++];
				final float nonInterpVal = 1.0f - interpVal;
				final float newVal = (curVal * nonInterpVal) + (desVal * interpVal );
				output[ curIndex + i ] = newVal;
			}
			curIndex += numWithInterpolation;

			if( curWindowPos == lastWindowPos )
			{
				curVal = desVal;
				if( haveValWaiting )
				{
					desVal = nextVal;
					haveValWaiting = false;
					curWindowPos = 0;

					numLeftInInterpolation = lastWindowPos;
					numWithInterpolation = (numLeftInInterpolation < numAfter ? numLeftInInterpolation : numAfter );
					numAfter -= numWithInterpolation;

					// We go back around the loop again with the next value
				}
				else
				{
					// Fall out of loop and fill in with curVal;
					break;
				}
			}
			else
			{
				// Haven't exhausted the window and have filled the length
				return;
			}
		}

		// No further use of window necessary, move to desVal
		// and make it curVal
		if( numAfter > 0 )
		{
			Arrays.fill( output, curIndex, outputIndex + length, curVal );
		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		if( curWindowPos < lastWindowPos )
		{
			haveValWaiting = true;
			nextVal = newValue;
		}
		else
		{
			// Restart the hann half window fade
			curWindowPos = 0;
			desVal = newValue;
		}
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
		haveValWaiting = false;
		curWindowPos = lastWindowPos;
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
	}
}
