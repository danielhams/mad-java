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



public class CubicInterpolatingWaveTableValueFetcher implements WaveTableValueFetcher
{
//	private static Log log = LogFactory.getLog( CubicInterpolatingWaveTableValueFetcher.class.getName() );

	@Override
	public float getValueAtNormalisedPosition( final CubicPaddedRawWaveTable waveTable, final float normalisedPosition )
	{
		final int finalIndex = waveTable.finalIndex;
		final float[] floatBuffer = waveTable.buffer;

		// We add one to start at index 1
		final float realInternalPos = (normalisedPosition * finalIndex );

		// Now cubic interpolate it.

		float frac,  fracsq;
		float y0, y1, y2, y3;

		// Cubic interpolation
		final int intPos = (int)realInternalPos;
		frac = realInternalPos - intPos;
		fracsq = frac * frac;

		y0 = floatBuffer[ intPos ];
		y1 = floatBuffer[ intPos + 1 ];
		y2 = floatBuffer[ intPos + 2 ];
		y3 = floatBuffer[ intPos + 3 ];

//		final float a0 = y3 - y2 - y0 + y1;
//		final float a1 = y0 - y1 -a0;
//		final float a2 = y2 - y0;
//		final float a3 = y1;

		final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
		final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
		final float a2 = -0.5f*y0 + 0.5f*y2;
		final float a3 = y1;

		return( (a0 * frac * fracsq)  + (a1 * fracsq) + (a2 * frac) + a3 );
	}

}
