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



public class LinearInterpolatingWaveTableValueFetcher implements WaveTableValueFetcher
{
//	private static Log log = LogFactory.getLog( LinearInterpolatingWaveTableValueFetcher.class.getName() );

	@Override
	public float getValueAtNormalisedPosition( final CubicPaddedRawWaveTable waveTable, final float normalisedPosition )
	{
		final int waveTableLength = waveTable.origWaveLength;
		final float[] floatBuffer = waveTable.buffer;

		// We add one to start at index 1
		final float realInternalPos = 1.0f + (normalisedPosition * waveTableLength );

		final int intPos = (int)realInternalPos;

		final float frac = realInternalPos - intPos;
		final float fracOther = 1.0f - frac;
		final float a0 = floatBuffer[ intPos ];
		final float a1 = floatBuffer[ intPos + 1 ];

		return( (a0 * fracOther)  + (a1 * frac) );
	}

}
