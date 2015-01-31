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
	public float getValueAtNormalisedPosition( CubicPaddedRawWaveTable waveTable, float normalisedPosition )
	{
		int finalIndex = waveTable.finalIndex;
		float[] floatBuffer = waveTable.buffer;
		
		// We add one to start at index 1
		float realInternalPos = 1.0f + (normalisedPosition * finalIndex );

		int intPos = (int)realInternalPos;

		float frac = realInternalPos - intPos;
		float fracOther = 1.0f - frac;
		float a0 = floatBuffer[ intPos ];
		float a1 = floatBuffer[ intPos + 1 ];

		return( (a0 * fracOther)  + (a1 * frac) );
	}

}
