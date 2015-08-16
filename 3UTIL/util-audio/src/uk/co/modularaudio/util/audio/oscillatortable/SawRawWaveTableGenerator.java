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

public class SawRawWaveTableGenerator extends RawWaveTableGenerator
{

	@Override
	public String getWaveTypeId()
	{
		return "saw";
	}

	@Override
	public CubicPaddedRawWaveTable reallyGenerateWaveTable( final int cycleLength, final int numHarmonics )
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
		for( int i = 0 ; i < numHarmonics ; i++ )
		{
			harmonics.floatBuffer[i] = 1.0f / (i + 1 );
		}
		return harmonics;
	}

	@Override
	public float getPhase()
	{
		return -0.25f;
	}
}
