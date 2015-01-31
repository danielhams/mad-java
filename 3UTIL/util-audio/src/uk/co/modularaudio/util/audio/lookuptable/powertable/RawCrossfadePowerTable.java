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

public class RawCrossfadePowerTable
{
	public float[] leftFloatBuffer = null;
	public float[] rightFloatBuffer = null;
	public int length = -1;
	
	public RawCrossfadePowerTable( int capacity )
	{
		this.length = capacity;
		leftFloatBuffer = new float[ length ];
		rightFloatBuffer = new float[ length ];
	}
	
	public int getLength()
	{
		return length;
	}
	
	public float getLeftValueAt( double newValue )
	{
		int index = getIndex( newValue );
		if( index >= length )
		{
			return leftFloatBuffer[length - 1];
		}
		else
		{
			return leftFloatBuffer[index];
		}
	}

	public float getRightValueAt( double newValue )
	{
		int index = getIndex( newValue );
		if( index >= length )
		{
			return rightFloatBuffer[length - 1];
		}
		else
		{
			return rightFloatBuffer[index];
		}
	}

	private int getIndex(double newValue)
	{
		double valToTruncate = (newValue + 1.0f) / 2;
		valToTruncate *= length;
		return (int)valToTruncate;
	}
}
