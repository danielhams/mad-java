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

package uk.co.modularaudio.util.audio.gui.patternsequencer.model;

import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PatternSequencePersistenceHelper
{
//	private static Log log = LogFactory.getLog( PatternSequencePersistenceHelper.class.getName() );

	private static final int NUM_STEPS_LEN = 3;
	private static final int COMMA_LEN = 1;
	private static final int DEFAULT_AMP_LEN = 10;
	private static final int DEFAULT_CONT_LEN = 5;
	private static final int NOTE_LEN = 12;

	public static String toString( PatternSequenceModelImpl patternSequenceModelImpl )
	{
		int numSteps = patternSequenceModelImpl.getNumSteps();
		StringBuilder outSb = new StringBuilder( NUM_STEPS_LEN + COMMA_LEN +
				DEFAULT_AMP_LEN + COMMA_LEN +
				DEFAULT_CONT_LEN + COMMA_LEN +
				(numSteps * (NOTE_LEN + COMMA_LEN ) ) );
		
		// Add in the general bits first, notes at the end
		outSb.append( numSteps );
		outSb.append( "," );
		outSb.append( patternSequenceModelImpl.defaultAmplitude );
		outSb.append( "," );
		outSb.append( patternSequenceModelImpl.defaultContinuation );
		for( int i = 0 ; i < numSteps ; i++ )
		{
			outSb.append( "," );
			PatternSequenceStep psn = patternSequenceModelImpl.getNoteAtStep( i );
			if( psn.note != null )
			{
				outSb.append( psn.note.getMidiNumber() );
				outSb.append( "|" );
				outSb.append( psn.amp );
				outSb.append( "|" );
				outSb.append( psn.isContinuation );
			}
		}
		
		return outSb.toString();
	}

	public static void fromString( String persistenceString, PatternSequenceModelImpl patternSequenceModelImpl )
	{
		String[] rawVals = persistenceString.split( "," );
		
		if( rawVals.length > 4 )
		{
			int numSteps = Integer.parseInt( rawVals[0] );
			patternSequenceModelImpl.defaultAmplitude = Float.parseFloat( rawVals[1] );
			patternSequenceModelImpl.defaultContinuation = Boolean.parseBoolean( rawVals[2] );
	
			if( patternSequenceModelImpl.stepNotes.length != numSteps )
			{
				patternSequenceModelImpl.initialiseStepsArray( numSteps );
			}
			
			for( int i = 3 ; i < rawVals.length ; i++ )
			{
				String noteString = rawVals[i];
				String[] noteParts = noteString.split( "\\|" );
				PatternSequenceStep psn = patternSequenceModelImpl.stepNotes[ i - 3 ];
				
				if( noteParts.length == 3 )
				{
					int midiNum = Integer.parseInt( noteParts[0] );
					psn.note = MidiUtils.getMidiNoteFromNumberReturnNull( midiNum );
					if( psn.note == null )
					{
						break;
					}
					psn.amp = Float.parseFloat( noteParts[1] );
					psn.isContinuation = Boolean.parseBoolean( noteParts[2] );
				}
				else
				{
					psn.note = null;
					psn.amp = 0.0f;
					psn.isContinuation = false;
				}
				
			}
			patternSequenceModelImpl.emitNoteAmpRefreshEvent( 0, numSteps );
		}
	}

}
