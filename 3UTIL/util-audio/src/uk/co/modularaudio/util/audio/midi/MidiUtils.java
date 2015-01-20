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
	
	private static OpenIntObjectHashMap<MidiNote> intToMidiNoteMap = new OpenIntObjectHashMap<MidiNote>();
	private static Map<String, MidiNote> nameToMidiNoteMap = new HashMap<String,MidiNote>();
	private static ArrayList<MidiNote> orderedMidiNotes = new ArrayList<MidiNote>();
	
	static
	{
		// Starts from A as that is midi note 0
		char[] noteKeys = new char[] { 'C', 'c', 'D', 'd', 'E', 'F', 'f', 'G', 'g', 'A', 'a', 'B' };
		// Build the map assuming 440hz as pitch of A4
		int curNoteIndex = 0;
		char noteChar = noteKeys[ curNoteIndex ];
		int octave = 0;
		int a = 440;
		for( int i = 0 ; i <= 127 ; i++ )
		{
			float frequency = (float)( ( a / 32.0) * Math.pow( 2.0, ((i - 9)/12.0)));
			boolean isWhiteKey = ( noteChar >= 'A' && noteChar <= 'Z' );
			MidiNote midiNote = new MidiNote( i, noteChar, octave, isWhiteKey, frequency );
			String noteName = midiNote.getNoteName();
			intToMidiNoteMap.put( i, midiNote );
			nameToMidiNoteMap.put( noteName, midiNote );
			orderedMidiNotes.add( midiNote );
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
	
	public final static MidiNote getMidiNoteFromNumberReturnNull( int keyNumber )
	{
		return intToMidiNoteMap.get( keyNumber );
	}

	public final static int getNumMidiNotes()
	{
		return intToMidiNoteMap.size();
	}
	
	public final static List<MidiNote> getOrderedMidiNotes()
	{
		return orderedMidiNotes;
	}

	public static MidiNote getMidiNoteFromStringReturnNull( String name )
	{
		return nameToMidiNoteMap.get( name );
	}
}
