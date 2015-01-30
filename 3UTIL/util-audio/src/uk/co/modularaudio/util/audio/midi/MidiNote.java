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

public class MidiNote
{
	private final int midiNumber;
	private final char noteChar;
	private final int noteOctave;
	private final boolean whiteKey;
	private final float frequency;

	public MidiNote( final int midiNumber,
			final char noteChar,
			final int noteOctave,
			final boolean whiteKey,
			final float frequency )
	{
		this.midiNumber = midiNumber;
		this.noteChar = noteChar;
		this.noteOctave = noteOctave;
		this.whiteKey = whiteKey;
		this.frequency = frequency;
	}

	public int getMidiNumber()
	{
		return midiNumber;
	}

	public String getNoteName()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( noteChar );
		sb.append( noteOctave );
		return sb.toString();
	}

	public char getNoteChar()
	{
		return noteChar;
	}

	public boolean isWhiteKey()
	{
		return whiteKey;
	}

	public float getFrequency()
	{
		return frequency;
	}

	@Override
	public String toString()
	{
		return "MN(" + midiNumber + ") Name(" + getNoteName() + ") FQ(" + frequency + ")";
	}
}
