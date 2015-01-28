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

package uk.co.modularaudio.util.audio.midi;

public enum MidiMessageStatusByte
{
	NOTE_OFF( (byte)0x80 ),
	NOTE_ON( (byte)0x90 ),
	POLY_KEY_PRESSURE( (byte)0xA0 ),
	CONTROL_CHANGE( (byte)0xB0 ),
	PROGRAM_CHANGE( (byte)0xC0 ),
	CHANNEL_PRESSURE( (byte)0xD0 ),
	PITCH_BEND( (byte)0xE0 ),

	// SEQUENCE STARTERS
	CHANNEL_MODE( (byte)0x70 ),
	SYSTEM_MESSAGE( (byte)0xF0 );

	private final byte internalVal;
	private MidiMessageStatusByte( final byte val )
	{
		this.internalVal = val;
	}

	public byte getValue()
	{
		return this.internalVal;
	}

	public boolean checkForMatch( final byte actualByte )
	{
		return ( (actualByte & internalVal) != 0 );
	}
}
