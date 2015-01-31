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

package uk.co.modularaudio.util.audio.lookuptable.cubic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;

public class CubicLookupTable extends RawLookupTable
{
	private static Log log = LogFactory.getLog( CubicLookupTable.class.getName() );

	private int length = -1;

	public CubicLookupTable( final float[] cachedWave )
	{
		super( cachedWave.length, false );
		this.length = cachedWave.length - 3;
		System.arraycopy( cachedWave, 0, floatBuffer, 0, cachedWave.length );
	}

	public CubicLookupTable( final int capacity, final boolean initialiseToZero )
	{
		super( capacity + 3, initialiseToZero );
		this.length = capacity;
	}

	protected void completeCubicWaveTableSetupForWrapAround()
	{
		// Copy over values needed for cubic interpolation.
		floatBuffer[ 0 ] = floatBuffer[ length ];
		floatBuffer[ length + 1 ] = floatBuffer[ 1 ];
		floatBuffer[ length + 2 ] = floatBuffer[ 2 ];
	}

	protected void completeCubicWaveTableSetupForOneShot()
	{
		// Copy over values needed for cubic interpolation.
		floatBuffer[ 0 ] = floatBuffer[ 1 ];
		floatBuffer[ length + 1 ] = floatBuffer[ length ];
		floatBuffer[ length + 2 ] = floatBuffer[ length ];
	}

	public int getCubicTableLength()
	{
		return length;
	}

	@Override
	public float getValueAtNormalisedPosition( final float normalisedPosition )
	{
		assert( normalisedPosition >= 0.0f );
		assert( normalisedPosition <= 1.0f );

		// We add one to start at index 1
		final float realInternalPos = 1.0f + (normalisedPosition * (length - 1) );

		// Now cubic interpolate it.

		float frac,  fracsq;
		float y0, y1, y2, y3;

		// Cubic interpolation
		final int intPos = (int)realInternalPos;
		if( (intPos - 1) < 0 || (intPos + 2) > capacity )
		{
			log.error("Overstepped the limits.");
		}
		frac = realInternalPos - intPos;
		fracsq = frac * frac;

		y0 = floatBuffer[ intPos - 1 ];
		y1 = floatBuffer[ intPos ];
		y2 = floatBuffer[ intPos + 1 ];
		y3 = floatBuffer[ intPos + 2 ];

		final float a0 = y3 - y2 - y0 + y1;
		final float a1 = y0 - y1 -a0;
		final float a2 = y2 - y0;
		final float a3 = y1;

		return( a0 * frac * fracsq  + a1 * fracsq + a2 * frac + a3 );
	}

}
