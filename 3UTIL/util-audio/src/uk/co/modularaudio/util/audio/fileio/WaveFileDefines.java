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

package uk.co.modularaudio.util.audio.fileio;

public class WaveFileDefines
{

	final static int FMT_CHUNK_ID = 0x20746D66;
	final static int DATA_CHUNK_ID = 0x61746164;
	final static int RIFF_CHUNK_ID = 0x46464952;
	final static int RIFF_TYPE_ID = 0x45564157;
	final static int FLOAT_BUFFER_LENGTH = 2048;
	static final long OFFSET_FOR_CHUNKSIZE = 4;
	static final long OFFSET_FOR_CHUNKSIZE2 = 40;

}
