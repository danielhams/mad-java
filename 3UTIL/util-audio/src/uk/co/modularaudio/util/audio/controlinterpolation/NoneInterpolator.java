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

public class NoneInterpolator implements ControlValueInterpolator
{

	private float curVal;
	private float desVal;

	public NoneInterpolator()
	{
	}

	public void reset()
	{
	}


	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		curVal = desVal;

		Arrays.fill( output, outputIndex, outputIndex + length, curVal );
	}

	@Override
	public void notifyOfNewValue( final float newValue )
	{
		this.desVal = newValue;
	}

	@Override
	public boolean checkForDenormal()
	{
		return false;
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
