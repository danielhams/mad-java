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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStep;
import uk.co.modularaudio.util.audio.gui.patternsequencer.model.PatternSequenceStepCopier;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventCopier;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.midi.MidiNote;

public class PatternSequencerOutputProcessor
{
	private static final int HARDCODED_CHANNEL = 0;

	private static Log log = LogFactory.getLog( PatternSequencerOutputProcessor.class.getName() );

	private final long sampleRate;

	private final MadChannelNoteEventCopier noteCopier = new MadChannelNoteEventCopier();

	private PatternSequencerRuntimePattern patternToUse;
	private boolean running;

	private float bpm = 128.0f;

	private int numStepsTimesTwo;
	private float beatsPerSecond;
	private float halfStepOffsetPerSample;

	private final PatternSequenceStep currentPatternStep = new PatternSequenceStep();

	private int curOutputNoteCounter;
	private final MadChannelNoteEvent[] internalOutputNotes;

	private float savedOutputStepIndexFloat;

	public PatternSequencerOutputProcessor( final long sampleRate, final int periodLength, final int notePeriodLength )
	{
		this.sampleRate = sampleRate;

		internalOutputNotes = new MadChannelNoteEvent[ notePeriodLength ];
		for( int i = 0 ; i < notePeriodLength ; i++ )
		{
			internalOutputNotes[ i ] = new MadChannelNoteEvent();
		}
	}

	public void setPatternDataBeforeStep( final PatternSequencerRuntimePattern patternToUse, final float bpmToUse, final boolean running )
	{
		curOutputNoteCounter = 0;

		if( running != this.running )
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Spotted running status change ->(" + running + ")");
			}
			if( this.running )
			{
				// Going from running -> stopped
				log.debug("Stopping...");
			}
			else
			{
				// Going from stopped to running
				// Reset our position to the start of the pattern.
				savedOutputStepIndexFloat = numStepsTimesTwo;
				this.patternToUse = patternToUse;
			}
		}
		else
		{
			if( this.running )
			{
				// Is running
				if( patternToUse.used == false )
				{
					// It's a reset of the pattern. If the current step on/off is different, we need to spit out a note off
					// for the currently playing note, and spit out a note on for the new note.
					// Will need to do similar checks for CV values, too
					this.patternToUse = patternToUse;
				}
				else
				{
					// No change in pattern.
				}
			}
			else
			{
				// Is not running
				this.patternToUse = patternToUse;
			}
		}
		this.bpm = bpmToUse;
		this.running = running;

		numStepsTimesTwo = patternToUse.numSteps * 2;
		beatsPerSecond = (bpm / 60.0f);
		halfStepOffsetPerSample = (beatsPerSecond / sampleRate) * 2;
//		log.debug("Bpm calcs bpm(" + bpm + ") beatsPerSecond(" + beatsPerSecond + ") halfStepOffset(" + halfStepOffsetPerSample +")");
	}

	public void doStep( final int numFrames )
	{
		if( running )
		{
			int curOutputStepIndex = (int)savedOutputStepIndexFloat;
			float curOutputStepIndexFloat = savedOutputStepIndexFloat;

			for( int s = 0 ; s < numFrames ; s++ )
			{
				curOutputStepIndexFloat += halfStepOffsetPerSample;
				if( curOutputStepIndexFloat >= (numStepsTimesTwo ) )
				{
					curOutputStepIndexFloat = 0.0f;
				}
				final int tstIntIndex = (int)curOutputStepIndexFloat;
				if( tstIntIndex != curOutputStepIndex )
				{
					// We progressed a half-step - check which type - note boundary or half-step
					if( tstIntIndex % 2 == 0 )
					{
						// Over new note boundary
						final PatternSequenceStep pss = patternToUse.stepNotes[ tstIntIndex / 2 ];
						processNewStep( pss, s );
					}
					else
					{
						// Over half-step boundary
						processHalfStep( s );
					}
					curOutputStepIndex = tstIntIndex;
				}
			}
			savedOutputStepIndexFloat = curOutputStepIndexFloat;
		}
		else
		{
			// If we are outputting a note, stop it
			if( currentPatternStep.note != null )
			{
				internalOutputNotes[ curOutputNoteCounter ].set( HARDCODED_CHANNEL,
						0,
						MadChannelNoteEventType.NOTE_OFF,
						currentPatternStep.note.getMidiNumber(),
						-1,
						-1 );
				curOutputNoteCounter++;
				currentPatternStep.note = null;
			}
		}

		// Finally mark the new pattern as used
		patternToUse.used = true;
	}

	public void outputNoteData( final MadChannelBuffer outputNoteBuffer )
	{
		final MadChannelNoteEvent[] outputNoteEvents = outputNoteBuffer.noteBuffer;
		outputNoteBuffer.numElementsInBuffer = curOutputNoteCounter;
		for( int n = 0 ; n < curOutputNoteCounter ; n++ )
		{
			noteCopier.copyValues( internalOutputNotes[n], outputNoteEvents[n] );
		}
	}

	public void outputCvData( final float[] outputCvData )
	{
	}

	private void processNewStep( final PatternSequenceStep newStep, final int sampleIndex )
	{
		final boolean previousStepWasNote = (currentPatternStep == null ? false : currentPatternStep.note != null );
		final boolean previousStepWasContinuation = (previousStepWasNote ? currentPatternStep.isContinuation : false );

		final boolean newStepIsNote = ( newStep == null ? false : newStep.note != null );

		if( newStepIsNote )
		{
			final int paramOne = newStep.note.getMidiNumber();

			int roundedAmp = (int)(newStep.amp * 127.0f);
			roundedAmp = (roundedAmp > 127 ? 127 : (roundedAmp < 0 ? 0 : roundedAmp ) );
			final int paramTwo = roundedAmp;

			MadChannelNoteEventType eventType;
			int paramThree;
			if( previousStepWasContinuation )
			{

				eventType = MadChannelNoteEventType.NOTE_CONTINUATION;
				paramThree = currentPatternStep.note.getMidiNumber();
			}
			else
			{
				eventType = MadChannelNoteEventType.NOTE_ON;
				paramThree = -1;
			}
			internalOutputNotes[ curOutputNoteCounter ].set( HARDCODED_CHANNEL,
					sampleIndex,
					eventType,
					paramOne,
					paramTwo,
					paramThree );
			curOutputNoteCounter++;
		}
		else
		{
			if( previousStepWasContinuation )
			{
				// Wasn't turned off at half step
				internalOutputNotes[ curOutputNoteCounter ].set( HARDCODED_CHANNEL,
						sampleIndex,
						MadChannelNoteEventType.NOTE_OFF,
						currentPatternStep.note.getMidiNumber(),
						-1,
						-1 );
				curOutputNoteCounter++;
			}
		}

		PatternSequenceStepCopier.copyValues( newStep, currentPatternStep );
	}

	private void processHalfStep( final int sampleIndex )
	{
		if( currentPatternStep != null )
		{
			final MidiNote mn = currentPatternStep.note;
			if( mn != null && !currentPatternStep.isContinuation )
			{
				internalOutputNotes[ curOutputNoteCounter ].set( HARDCODED_CHANNEL,
						sampleIndex,
						MadChannelNoteEventType.NOTE_OFF,
						mn.getMidiNumber(),
						-1,
						-1 );

				curOutputNoteCounter++;
			}
		}
	}
}
