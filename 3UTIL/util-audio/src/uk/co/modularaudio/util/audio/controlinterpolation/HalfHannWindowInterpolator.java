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

import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class HalfHannWindowInterpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( HalfHannWindowInterpolator.class.getName() );

	private HannFftWindow fullHannWindow;
	private float[] hannBuffer;

	private int curWindowPos;
	private int lastWindowPos;

	private float curVal;
	private float desVal;
	private boolean haveValWaiting = false;
	private float nextVal;

	public HalfHannWindowInterpolator()
	{
	}

	public void reset( final int sampleRate, final float valueChaseMillis )
	{
		final int halfWindowLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, valueChaseMillis );
//		log.debug("Using a half window length of " + valueChaseMillis + " ms or " + halfWindowLength + " samples");
		fullHannWindow = new HannFftWindow( halfWindowLength * 2 );
		final float[] hwAmps = fullHannWindow.getAmps();
		hannBuffer = new float[halfWindowLength];
		System.arraycopy( hwAmps, 1, hannBuffer, 0, halfWindowLength );
		curWindowPos = 0;
		lastWindowPos = halfWindowLength;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		int numLeftInHann = lastWindowPos - curWindowPos;
		int numWithHann = (numLeftInHann < length ? numLeftInHann : length );
		int numAfter = length - numWithHann;
		int curIndex = outputIndex;

		while( numWithHann > 0 )
		{
			for( int i = 0 ; i < numWithHann ; ++i )
			{
				final float hannVal = hannBuffer[curWindowPos++];
				final float nonHannVal = 1.0f - hannVal;
				final float newVal = (curVal * nonHannVal) + (desVal * hannVal );
				output[ curIndex + i ] = newVal;
			}
			curIndex += numWithHann;

			if( curWindowPos == lastWindowPos )
			{
				curVal = desVal;
				if( haveValWaiting )
				{
					desVal = nextVal;
					haveValWaiting = false;
					curWindowPos = 0;

					numLeftInHann = lastWindowPos;
					numWithHann = (numLeftInHann < numAfter ? numLeftInHann : numAfter );
					numAfter -= numWithHann;

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

		// No further use of hann window necessary, move to desVal
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
