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

package uk.co.modularaudio.mads.internal.paramfade.mu;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.lookuptable.raw.RawLookupTable;

public class PFadeOutWaveTable extends RawLookupTable
{
	public PFadeOutWaveTable( final DataRate dataRate, final int millisForFadeOut)
	{
		super( dataRate.calculateSamplesForLatency( millisForFadeOut), false);

		// Now loop over the length drawing in our lookup table

		// We will use simple linear fade for now
		final int genLength = capacity - 1;
		for( int i = 0; i < capacity ; i++ )
		{
			final float normalisedVal = (float)i / (float)genLength;
			floatBuffer[i] = 1.0f - ( normalisedVal * normalisedVal );
		}
	}

}
