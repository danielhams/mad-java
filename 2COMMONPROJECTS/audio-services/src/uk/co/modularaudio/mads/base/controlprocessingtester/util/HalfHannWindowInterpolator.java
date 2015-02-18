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

package uk.co.modularaudio.mads.base.controlprocessingtester.util;

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
		fullHannWindow = new HannFftWindow( halfWindowLength * 2 );
		hannBuffer = fullHannWindow.getAmps();
		curWindowPos = 0;
		lastWindowPos = halfWindowLength;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		if( lastWindowPos == curWindowPos )
		{
			// Hann fade over
			if( haveValWaiting )
			{
				desVal = nextVal;
				haveValWaiting = false;
				curWindowPos = 0;

				generateControlValues( output, outputIndex, length );
			}
			else
			{
				Arrays.fill( output, outputIndex, outputIndex + length, curVal );
			}
		}
		else
		{
			// Still doing a fade using the hann table
			final int numLeftInHann = lastWindowPos - curWindowPos;
			final int numWithHann = (numLeftInHann < length ? numLeftInHann : length );
			final int numAfter = length - numWithHann;

			for( int i = 0 ; i < numWithHann ; ++i )
			{
				final float hannVal = hannBuffer[curWindowPos++];
				final float nonHannVal = 1.0f - hannVal;
				final float newVal = (curVal * nonHannVal) + (desVal * hannVal );
				output[ outputIndex + i ] = newVal;
			}

			if( curWindowPos == lastWindowPos )
			{
				curVal = desVal;
				if( haveValWaiting )
				{
					desVal = nextVal;
					haveValWaiting = false;
					curWindowPos = 0;

					if( numAfter > 0 )
					{
						generateControlValues( output, outputIndex + numWithHann, numAfter );
					}
				}
				else
				{
					if( numAfter > 0 )
					{
						Arrays.fill( output, outputIndex + numWithHann, outputIndex + length, curVal );
					}
				}
			}

		}
	}

	@Override
	public void notifyOfNewIncomingAmp( final float amp )
	{
		if( curWindowPos < lastWindowPos )
		{
			haveValWaiting = true;
			nextVal = amp;
		}
		else
		{
			// Restart the hann half window fade
			curWindowPos = 0;
			desVal = amp;
		}
	}

	@Override
	public void checkForDenormal()
	{
		if( curVal > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && curVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		{
			curVal = 0.0f;
		}
	}
}
