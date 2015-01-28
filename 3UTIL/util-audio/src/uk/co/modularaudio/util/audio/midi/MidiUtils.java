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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.math.map.OpenIntObjectHashMap;

public class MidiUtils
{
//	private static Log log = LogFactory.getLog( MidiUtils.class.getName() );

	private final static OpenIntObjectHashMap<MidiNote> INT_TO_NOTE_MAP = new OpenIntObjectHashMap<MidiNote>();
	private final static Map<String, MidiNote> NAME_TO_NOTE_MAP = new HashMap<String,MidiNote>();
	private final static ArrayList<MidiNote> ORDERED_NOTES = new ArrayList<MidiNote>();

	static
	{
		// Starts from A as that is midi note 0
		final char[] noteKeys = new char[] { 'C', 'c', 'D', 'd', 'E', 'F', 'f', 'G', 'g', 'A', 'a', 'B' };
		// Build the map assuming 440hz as pitch of A4
		int curNoteIndex = 0;
		char noteChar = noteKeys[ curNoteIndex ];
		int octave = 0;
		final int a = 440;
		for( int i = 0 ; i <= 127 ; i++ )
		{
			final float frequency = (float)( ( a / 32.0) * Math.pow( 2.0, ((i - 9)/12.0)));
			final boolean isWhiteKey = ( noteChar >= 'A' && noteChar <= 'Z' );
			final MidiNote midiNote = new MidiNote( i, noteChar, octave, isWhiteKey, frequency );
			final String noteName = midiNote.getNoteName();
			INT_TO_NOTE_MAP.put( i, midiNote );
			NAME_TO_NOTE_MAP.put( noteName, midiNote );
			ORDERED_NOTES.add( midiNote );
			curNoteIndex++;
			if( curNoteIndex == 12 )
			{
				curNoteIndex = 0;
			}
			noteChar = noteKeys[ curNoteIndex ];
			if( noteChar == 'C' )
			{
				octave++;
			}
//			log.debug("Setting up note " + i + " to be " + midiNote.toString() );
		}
	}

	public final static MidiNote getMidiNoteFromNumberReturnNull( final int keyNumber )
	{
		return INT_TO_NOTE_MAP.get( keyNumber );
	}

	public final static int getNumMidiNotes()
	{
		return INT_TO_NOTE_MAP.size();
	}

	public final static List<MidiNote> getOrderedMidiNotes()
	{
		return ORDERED_NOTES;
	}

	public static MidiNote getMidiNoteFromStringReturnNull( final String name )
	{
		return NAME_TO_NOTE_MAP.get( name );
	}
}
