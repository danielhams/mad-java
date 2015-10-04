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

public abstract class AbstractCDLowPassInterpolator implements ControlValueInterpolator
{
//	private static Log log = LogFactory.getLog( AbstractCDLowPassInterpolator.class.getName() );

	protected float desVal;
	protected float curVal;

	protected int sampleRate;
	protected float lowPassFrequency;

	public AbstractCDLowPassInterpolator()
	{
		this.lowPassFrequency = LowPassInterpolatorConstants.LOW_PASS_CUTOFF;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		if( curVal == desVal )
		{
			Arrays.fill( output, outputIndex, outputIndex + length, curVal );
		}
		else
		{
			final int lastIndex = outputIndex + length;
			Arrays.fill( output, outputIndex, lastIndex, desVal );
			lowPassFilter( output, outputIndex, length );

			curVal = output[outputIndex + length - 1 ];
		}
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		desVal = newValue;
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
		this.desVal = value;
		hardSetLowPass( value );
		curVal = desVal;
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
		resetLowPass( sampleRate );
	}

	@Override
	public float getValue()
	{
		return desVal;
	}

	protected abstract void lowPassFilter( float[] output, int outputIndex, int length );

	protected abstract void hardSetLowPass( float value );

	protected abstract void resetLowPass( int sampleRate );
}
