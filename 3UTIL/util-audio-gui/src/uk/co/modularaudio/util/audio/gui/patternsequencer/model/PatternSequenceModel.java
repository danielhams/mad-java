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

import uk.co.modularaudio.util.audio.midi.MidiNote;

public interface PatternSequenceModel
{
	public int getNumSteps();

	public void setNoteAtStep( int stepNum, MidiNote note );
	public void setNoteAtStepWithAmp( int stepNum, MidiNote note, boolean isContinuation, float amp );
	public void setAmpAndContinuationAtStep( int stepNum, boolean isContinuation, float amp );
	public PatternSequenceStep getNoteAtStep( int stepNum );
	public void unsetNoteAtStep( int stepNum );
	
	// Amplitude
	public float getDefaultAmplitude();
	public void setCurrentAmplitude( float newAmplitude );
	
	// Is continue  / start again
	public boolean getContinuationState();
	public void setContinuationState( boolean newState );
	
	public void clear();
	
	public void addListener( PatternSequenceModelListener listener );
	public void removeListener( PatternSequenceModelListener listener );

	public String toPersistenceString();

	public void initFromPersistenceString( String persistenceString );

	public void transpose( PatternSequenceModelTranspose transposition );
}
