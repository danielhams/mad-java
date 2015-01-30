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

package uk.co.modularaudio.mads.base.pattern_sequencer.mu;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceModel;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;

public class PatternSequencerRuntimePattern
{
	public int numSteps;
	public PatternSequenceStep[] stepNotes;
	public boolean used;

	public PatternSequencerRuntimePattern( final int numSteps )
	{
		this.numSteps = numSteps;
		stepNotes = new PatternSequenceStep[ numSteps ];
		initialiseArray();
	}

	public void copyTo( final PatternSequencerRuntimePattern target )
	{
		if( target.numSteps != numSteps )
		{
			target.stepNotes = new PatternSequenceStep[ numSteps ];
			target.numSteps = numSteps;
		}
		System.arraycopy( stepNotes, 0,  target.stepNotes, 0, numSteps );
	}

	public void copyFrom( final PatternSequenceModel dataModel )
	{
		final int newNumSteps = dataModel.getNumSteps();
		if( newNumSteps != numSteps )
		{
			numSteps = newNumSteps;
			initialiseArray();
		}

		for( int i = 0 ; i < numSteps ; i++ )
		{
			final PatternSequenceStep psn = dataModel.getNoteAtStep( i );
			stepNotes[ i ].note = psn.note;
			stepNotes[ i ].isContinuation = psn.isContinuation;
			stepNotes[ i ].amp = psn.amp;
		}
	}

	private void initialiseArray()
	{
		stepNotes = new PatternSequenceStep[ numSteps ];
		for( int i = 0 ; i < numSteps ; i++ )
		{
			stepNotes[ i ] = new PatternSequenceStep();
		}
	}
}
