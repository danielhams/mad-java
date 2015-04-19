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

import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PatternSequenceModelImpl implements PatternSequenceModel
{
//	private static Log log = LogFactory.getLog( PatternSequenceModelImpl.class.getName() );

	protected final int numSteps;
	protected final PatternSequenceStep[] stepNotes;

	private final Set<PatternSequenceModelListener> listeners = new HashSet<PatternSequenceModelListener>();

	protected float defaultAmplitude = 1.0f;
	protected boolean defaultContinuation = false;

	public PatternSequenceModelImpl( final int numSteps )
	{
		this.numSteps = numSteps;
		stepNotes = initialiseStepsArray( numSteps );
	}

	protected PatternSequenceStep[] initialiseStepsArray( final int numSteps )
	{
		final PatternSequenceStep[] initNotes = new PatternSequenceStep[ numSteps ];
		for( int i = 0; i < numSteps ; i++ )
		{
			initNotes[ i ] = new PatternSequenceStep();
		}
		return initNotes;
	}

	@Override
	public void setNoteAtStep( final int stepNum, final MidiNote note )
	{
		stepNotes[ stepNum ].note = note;
		emitNoteRefreshEvent( stepNum, stepNum );
	}

	@Override
	public void setNoteAtStepWithAmp( final int stepNum, final MidiNote note, final boolean isContinuation, final float amp )
	{
		stepNotes[ stepNum ].note = note;
		stepNotes[ stepNum ].isContinuation = isContinuation;
		stepNotes[ stepNum ].amp = amp;
		emitNoteAmpRefreshEvent( stepNum, stepNum );
	}

	@Override
	public void setAmpAndContinuationAtStep( final int stepNum, final boolean isContinuation, final float amp )
	{
		stepNotes[ stepNum ].isContinuation = isContinuation;
		stepNotes[ stepNum ].amp = amp;
		emitAmpRefreshEvent( stepNum, stepNum );
	}

	@Override
	public PatternSequenceStep getNoteAtStep( final int stepNum )
	{
		return stepNotes[ stepNum ];
	}

	@Override
	public void unsetNoteAtStep( final int stepNum )
	{
		stepNotes[ stepNum ].note = null;
		stepNotes[ stepNum ].isContinuation = false;
		stepNotes[ stepNum ].amp = 0.0f;
		emitNoteAmpRefreshEvent( stepNum, stepNum );
	}

	@Override
	public void clear()
	{
		for( int i = 0 ; i < numSteps ; i++ )
		{
			stepNotes[ i ].note = null;
			stepNotes[ i ].isContinuation = false;
			stepNotes[ i ].amp = 0.0f;
		}
		emitNoteAmpRefreshEvent( 0, numSteps );
	}

	@Override
	public void addListener( final PatternSequenceModelListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void removeListener( final PatternSequenceModelListener listener )
	{
		listeners.remove( listener );
	}

	@Override
	public int getNumSteps()
	{
		return numSteps;
	}

	@Override
	public float getDefaultAmplitude()
	{
		return defaultAmplitude;
	}

	@Override
	public void setCurrentAmplitude( final float newAmplitude )
	{
		defaultAmplitude = newAmplitude;
	}

	@Override
	public boolean getContinuationState()
	{
		return defaultContinuation;
	}

	@Override
	public void setContinuationState( final boolean newState )
	{
		defaultContinuation = newState;
	}

	@Override
	public String toPersistenceString()
	{
		return PatternSequencePersistenceHelper.toString( this );
	}

	@Override
	public void initFromPersistenceString( final String persistenceString )
	{
		PatternSequencePersistenceHelper.fromString( persistenceString, this );
	}

	@Override
	public void transpose( final PatternSequenceModelTranspose transposition )
	{
		switch( transposition )
		{
			case OCTAVE_UP:
			{
				internalTranspose( 12 );
				break;
			}
			case OCTAVE_DOWN:
			{
				internalTranspose( -12 );
				break;
			}
			case SEMITONE_UP:
			{
				internalTranspose( 1 );
				break;
			}
			case SEMITONE_DOWN:
			{
				internalTranspose( -1 );
				break;
			}
		}
	}

	private void internalTranspose( final int num )
	{
		for( int i = 0 ; i < numSteps ; i++ )
		{
			final MidiNote mn = stepNotes[ i ].note;
			if( mn != null )
			{
				int midiNum = mn.getMidiNumber();
				midiNum += num;
				if( midiNum < 0 ) midiNum = 0;
				if( midiNum > 128 ) midiNum = 128;
				// Ignore null, we just fetched the midinum
				final MidiNote newNote = MidiUtils.getMidiNoteFromNumberReturnNull( midiNum );
				stepNotes[i].note = newNote;
			}
		}
		emitNoteRefreshEvent( 0, numSteps );
	}

	protected void emitNoteRefreshEvent( final int firstStep, final int lastStep )
	{
		for( final PatternSequenceModelListener l : listeners )
		{
			l.receiveStepnoteChange( firstStep, lastStep );
		}
	}

	protected void emitNoteAmpRefreshEvent( final int firstStep, final int lastStep )
	{
		for( final PatternSequenceModelListener l : listeners )
		{
			l.receiveStepNoteAndAmpChange( firstStep, lastStep );
		}
	}

	protected void emitAmpRefreshEvent( final int firstStep, final int lastStep )
	{
		for( final PatternSequenceModelListener l : listeners )
		{
			l.receiveStepAmpChange( firstStep, lastStep );
		}
	}
}
