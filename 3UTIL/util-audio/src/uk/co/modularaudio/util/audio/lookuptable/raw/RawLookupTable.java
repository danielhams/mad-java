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

package uk.co.modularaudio.util.audio.lookuptable.raw;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.lookuptable.LookupTable;


public class RawLookupTable implements LookupTable
{
	public float[] floatBuffer = null;
	public int capacity = -1;

	public RawLookupTable( final float[] dataToUse )
	{
		this( dataToUse.length, false );
		System.arraycopy( dataToUse, 0, floatBuffer, 0, capacity );
	}

	public RawLookupTable( final int capacity, final boolean initialiseToZero )
	{
		this.capacity = capacity;
		floatBuffer = new float[ capacity ];
		if( initialiseToZero )
		{
			Arrays.fill( floatBuffer, 0.0f );
		}
	}

	public int getBufferCapacity()
	{
		return capacity;
	}

	public float getValueAt( final int position )
	{
		if( position >= capacity )
		{
			return floatBuffer[capacity - 1];
		}
		else
		{
			return floatBuffer[position];
		}
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.audio.wavetable.WaveTable#getValueAtNormalisedPosition(float)
	 */
	@Override
	public float getValueAtNormalisedPosition( final float iNormalisedPosition )
	{
		final float normalisedPosition  = ( iNormalisedPosition < 0.0f ? 0.0f : (iNormalisedPosition > 1.0f ? 1.0f : iNormalisedPosition ) );
		final float realPosFloat = normalisedPosition * capacity;
		return floatBuffer[ (int)realPosFloat ];
	}

	@Override
	public float[] getUnderlyingData()
	{
		return floatBuffer;
	}
}
