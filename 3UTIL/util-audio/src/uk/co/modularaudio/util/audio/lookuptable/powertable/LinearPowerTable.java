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

package uk.co.modularaudio.util.audio.lookuptable.powertable;

public class LinearPowerTable extends RawCrossfadePowerTable
{
	public LinearPowerTable( int bufferLength )
	{
		super( bufferLength );

		assert( bufferLength %2 == 1 ); // Make sure we are odd so there is a sample in the middle
		// Loop around from 1 -> length - 1 setting the powers so that halfway through = 0.5
		for( int i = 0 ; i < bufferLength ; i++ )
		{
			float leftVal = ((float)bufferLength - i) / bufferLength;
			leftFloatBuffer[ i ] = leftVal;
			float rightVal = 1.0f - leftVal;
			rightFloatBuffer[ i ] = rightVal;
		}
		// We make sure that the outermost samples are one and zero
		leftFloatBuffer[ 0 ] = 1.0f;
		leftFloatBuffer[ bufferLength - 1 ] = 0.0f;
		rightFloatBuffer[ 0 ] = 0.0f;
		rightFloatBuffer[ bufferLength - 1 ] = 1.0f;
	}
}
