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

package uk.co.modularaudio.util.audio.format;

public enum SampleBits
{
	SAMPLE_8( 1, "8", false, false ),
	SAMPLE_16LE( 2, "16", true, false ),
	SAMPLE_16BE( 2, "16", true, true ),
	SAMPLE_24PLE( 3, "24", true, false ),
	SAMPLE_24PBE( 3, "24", true, true ),
	SAMPLE_24ULE( 4, "24", true, false ),
	SAMPLE_24UBE( 4, "24", true, true ),
	SAMPLE_32LE( 4, "32", true, false ),
	SAMPLE_32BE( 4, "32", true, true ),
	SAMPLE_FLOAT( 4, "32", true, false );

	private int sizeBytes = -1;
	private String id = null;
	private boolean signed = false;
	private boolean bigEndian = false;

	private SampleBits( final int sizeBytes, final String id, final boolean signed, final boolean bigEndian )
	{
		this.sizeBytes = sizeBytes;
		this.id = id;
		this.signed = signed;
		this.bigEndian = bigEndian;
	}

	@Override
	public String toString()
	{
		return id;
	}

	public int getSizeBytes()
	{
		return sizeBytes;
	}

	public boolean isSigned()
	{
		return signed;
	}

	public boolean isBigEndian()
	{
		return bigEndian;
	}
}
