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

import uk.co.modularaudio.util.audio.lookuptable.generation.FourierTableGenerator;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;

public class JunoRawWaveTableGenerator extends RawWaveTableGenerator
{

	private final static float[] loopingAmpAmounts = new float[] {
			1.0f,
			0.0f,
			0.35f,
			0.0f,
			0.85f,
			0.0f,
			0.75f,
			0.0f
	};

	@Override
	public String getWaveTypeId()
	{
		return "juno";
	}

	@Override
	public CubicPaddedRawWaveTable generateWaveTableAdditiveFourier( final int cycleLength, final int numHarmonics )
	{
		final CubicPaddedRawWaveTable retVal = new CubicPaddedRawWaveTable( cycleLength );

		// Initialise the harmonics table and set them up
		final RawLookupTable harmonics = getHarmonics( numHarmonics );

		final float phase = getPhase();

		FourierTableGenerator.fillTable( retVal.buffer, 1, retVal.origWaveLength, numHarmonics, harmonics.floatBuffer, phase );

		retVal.completeCubicBufferFillAndNormalise();

		return retVal;
	}

	@Override
	public RawLookupTable getHarmonics( final int numHarmonics )
	{
		final RawLookupTable harmonics = new RawLookupTable( numHarmonics, true );
		float ampSetMultiplier = 1.0f;
		int curAmpCounter = 0;
		for( int i = 0 ; i < numHarmonics ; i++ )
		{
			final float loopAmp = loopingAmpAmounts[curAmpCounter];
			harmonics.floatBuffer[i] = (1.0f / (i + 1 )) * loopAmp * ampSetMultiplier;
			curAmpCounter++;
			if( curAmpCounter > loopingAmpAmounts.length - 1 )
			{
				curAmpCounter = 0;
				ampSetMultiplier = ampSetMultiplier / 2.0f;
			}
		}
		return harmonics;
	}

	@Override
	public float getPhase()
	{
		return -0.25f;
	}

}
