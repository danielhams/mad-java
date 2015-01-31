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

package uk.co.modularaudio.util.audio.oscillatortable;

import uk.co.modularaudio.util.audio.lookuptable.LookupTableUtils;

public class CubicPaddedRawWaveTable
{
	public final static int NUM_EXTRA_SAMPLES_IN_BUFFER = 3;
	protected float[] buffer;
	protected int bufferLength;
	protected int origWaveLength;
	protected int finalIndex;

	public CubicPaddedRawWaveTable( float[] data )
	{
		bufferLength = data.length;
		origWaveLength = bufferLength - NUM_EXTRA_SAMPLES_IN_BUFFER;
		finalIndex = origWaveLength - 1;
		buffer = new float[ bufferLength ];
		System.arraycopy( data, 0, buffer, 0, bufferLength );
	}

	public CubicPaddedRawWaveTable( int cycleLength )
	{
		origWaveLength = cycleLength;
		bufferLength = origWaveLength + NUM_EXTRA_SAMPLES_IN_BUFFER;
		finalIndex = origWaveLength - 1;
		buffer = new float[ bufferLength ];
	}

	protected void completeCubicBufferFillAndNormalise()
	{
		// Add the wrapped bits to allow cubic interpolation to work.
		buffer[ 0 ] = buffer[ bufferLength - 3 ];
		buffer[ bufferLength - 2 ] = buffer[ 1 ];
		buffer[ bufferLength - 1 ] = buffer[ 2 ];
		
		LookupTableUtils.normaliseFloats( buffer, 0, bufferLength );
	}
}
