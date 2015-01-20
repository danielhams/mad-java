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

package uk.co.modularaudio.util.audio.wavetable.waveshaper;

import java.util.Arrays;

import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;

public class SquareShaperWaveTable extends RawWaveTable
{

	public SquareShaperWaveTable( int capacity )
	{
		super(capacity, false);
		
		// Be low (0) for first half, then high
		int midPoint = capacity / 2;
		Arrays.fill( floatBuffer, 0, midPoint, -1.0f );
		Arrays.fill( floatBuffer, midPoint, capacity, 1.0f );
	}

}
