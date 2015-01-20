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

package uk.co.modularaudio.util.audio.wavetablent;

import uk.co.modularaudio.util.audio.wavetable.generation.FourierTableGenerator;
import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;

public class SquareRawWaveTableGenerator extends RawWaveTableGenerator
{

	@Override
	public String getWaveTypeId()
	{
		return "square";
	}

	@Override
	public CubicPaddedRawWaveTable reallyGenerateWaveTable( int cycleLength, int numHarmonics )
	{
		CubicPaddedRawWaveTable retVal = new CubicPaddedRawWaveTable( cycleLength );
		
		// Initialise the harmonics table and set them up
		RawWaveTable harmonics = new RawWaveTable( numHarmonics, true );
		for( int i = 0 ; i < numHarmonics ; i+=2 )
		{
			harmonics.floatBuffer[i] = 1.0f / (i + 1 );
		}
		
		FourierTableGenerator.fillTable( retVal.buffer, 1, retVal.origWaveLength, numHarmonics, harmonics.floatBuffer, -0.25f );
		
		retVal.completeCubicBufferFillAndNormalise();
		
		return retVal;
	}

}
