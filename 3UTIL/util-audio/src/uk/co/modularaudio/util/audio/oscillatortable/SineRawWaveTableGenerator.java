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

import uk.co.modularaudio.util.math.MathDefines;

public class SineRawWaveTableGenerator extends RawWaveTableGenerator
{

	@Override
	public String getWaveTypeId()
	{
		return "sine";
	}

	@Override
	public CubicPaddedRawWaveTable reallyGenerateWaveTable( final int cycleLength, final int numHarmonics )
	{
		final CubicPaddedRawWaveTable retVal = new CubicPaddedRawWaveTable( cycleLength );

		double currentSineRadians = 0.0;
		final double incrementalRadians = MathDefines.TWO_PI_D / (cycleLength-1);
		for( int i = 0 ; i < cycleLength ; i++ )
		{
			retVal.buffer[ 1 + i ] = (float)Math.sin( currentSineRadians );
			currentSineRadians += incrementalRadians;
		}

		retVal.completeCubicBufferFillAndNormalise();

		return retVal;
	}

}
