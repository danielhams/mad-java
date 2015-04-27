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

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class SumOfRatiosInterpolator implements ControlValueInterpolator
{

	private float curValueRatio = 0.5f;
	private float newValueRatio = 0.5f;

	private float curVal;
	private float desVal;

	public SumOfRatiosInterpolator()
	{
	}

	public void reset( final long sampleRate, final float valueChaseMillis )
	{
		newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, valueChaseMillis );
		curValueRatio = 1.0f - newValueRatio;
	}


	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;
		for( int i = outputIndex ; i < lastIndex ; ++i )
		{
			curVal = (curVal * curValueRatio) + (desVal * newValueRatio);
			output[i] = curVal;
		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		this.desVal = newValue;
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
	}

	@Override
	public void resetLowerUpperBounds( final float lowerBound, final float upperBound )
	{
	}
}
