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

package uk.co.modularaudio.util.audio.lookuptable.raw;

public class RawLookupTableDefines
{
	public static final int OSCILLATOR_MIN_BUFFER_LENGTH = 256;
	public static final int OSCILLATOR_MAX_BUFFER_LENGTH = 32 * 1024;

	// Defaults for non-band-limited waves (LFO)
	public static final int LFO_NUM_HARMONICS = 150;
	public static final int LFO_BUFFER_LENGTH = calculateCycleLengthForHarmonics( LFO_NUM_HARMONICS );

	public static int calculateCycleLengthForHarmonics( final int numHarmonics )
	{
		// Cycle length must be long enough that we have enough FFT bins to accommadate the number of harmonics
		// with some wiggle room so interpolation works.
		// Nyquist needs mean we need double of (DC offset + (harms*2))
		final int calculatedNyquistNeeds = (1 + (numHarmonics * 2)) * 2;
		// And then provide at enough extra points that so interpolation can do its thing
		int calculatedCycleLength = calculatedNyquistNeeds * 16;
		calculatedCycleLength = (calculatedCycleLength < RawLookupTableDefines.OSCILLATOR_MIN_BUFFER_LENGTH ?
				RawLookupTableDefines.OSCILLATOR_MIN_BUFFER_LENGTH :
					(calculatedCycleLength > RawLookupTableDefines.OSCILLATOR_MAX_BUFFER_LENGTH ?
							RawLookupTableDefines.OSCILLATOR_MAX_BUFFER_LENGTH :
								calculatedCycleLength ) );
		return calculatedCycleLength;
	}
}
