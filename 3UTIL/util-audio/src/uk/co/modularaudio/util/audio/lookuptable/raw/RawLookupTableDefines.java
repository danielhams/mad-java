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

	// 32K per standard wave table
	public static final int OSCILLATOR_BUFFER_LENGTH = 32 * 1024;
//	public static final int OSCILLATOR_BUFFER_LENGTH = 128;
//	public static final int OSCILLATOR_BUFFER_LENGTH = 8 * 1024;

	// 10 harmonics and we'll see how that sounds
//	public static final int OSCILLATOR_NUM_HARMONICS = 4;
//	public static final int OSCILLATOR_NUM_HARMONICS = 10;
//	public static final int OSCILLATOR_NUM_HARMONICS = 20;

//	public static final int OSCILLATOR_NUM_HARMONICS = 30;

//	public static final int OSCILLATOR_NUM_HARMONICS = 40;
//	public static final int OSCILLATOR_NUM_HARMONICS = 60;

	public static final int OSCILLATOR_NUM_HARMONICS = 150;
//	public static final int OSCILLATOR_NUM_HARMONICS = 305;

	// Must always be an odd number to get the middle at 0.0
	public static final int SHAPER_BUFFER_LENGTH = (5  *1024 ) + 1;

}
