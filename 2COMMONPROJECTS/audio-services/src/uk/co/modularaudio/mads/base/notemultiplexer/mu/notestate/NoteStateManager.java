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

package uk.co.modularaudio.mads.base.notemultiplexer.mu.notestate;

import java.nio.BufferOverflowException;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenObjectIntHashMap;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventCopier;
import uk.co.modularaudio.util.audio.mad.note.LocklessChannelNoteRingBuffer;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class NoteStateManager
{
	private static Log log = LogFactory.getLog( NoteStateManager.class.getName() );

	private final int polyphony;

	private final OpenObjectIntHashMap<MidiNote> noteToChannelMap = new OpenObjectIntHashMap<MidiNote>();
	private final Queue<Integer> freeChannelStack = new ArrayDeque<Integer>();

	private final LocklessChannelNoteRingBuffer[] outputChannelNoteRings;

	private final MadChannelNoteEventCopier noteCopier = new MadChannelNoteEventCopier();

	public NoteStateManager( final int polyphony, final int noteChannelBufferLength )
	{
		this.polyphony = polyphony;
		outputChannelNoteRings = new LocklessChannelNoteRingBuffer[ polyphony ];
		for( int i = 0 ; i < polyphony ; i++ )
		{
			freeChannelStack.add( i );

			outputChannelNoteRings[ i ] = new LocklessChannelNoteRingBuffer( noteChannelBufferLength );
		}
	}

	public void processNotes( final MadChannelBuffer incomingNoteBuffer, final MadChannelBuffer[] outgoingNoteBuffers )
			throws BufferOverflowException, InterruptedException
	{
		final int numNotes = incomingNoteBuffer.numElementsInBuffer;
//		if( numNotes > 0 )
//		{
//			log.debug("Processing " + numNotes + " incoming notes");
//		}
		final MadChannelNoteEvent[] incomingNotes = incomingNoteBuffer.noteBuffer;

		// Now process note on and continuations
		for( int i = 0 ; i < numNotes ; i++ )
		{
			final MadChannelNoteEvent ne = incomingNotes[ i ];
			switch( ne.getEventType() )
			{
				case NOTE_OFF:
				{
//					log.debug("NoteOff");
					doNoteOff( ne );
					break;
				}
				case NOTE_CONTINUATION:
				{
//					log.debug("NoteContinuation");
					doNoteContinuation( ne );
					break;
				}
				case NOTE_ON:
				{
//					log.debug("NoteOn");
					doNoteOn( ne );
					break;
				}
				default:
				{
					break;
				}
			}
		}

		// Now for each channel spit out the entries in the associated ring onto the output buffers
		for( int i =0 ; i < polyphony ; i++ )
		{
			final LocklessChannelNoteRingBuffer channelNoteRing = outputChannelNoteRings[ i ];
			final int numReadable = channelNoteRing.getNumReadable();

			final MadChannelBuffer outgoingBuffer = outgoingNoteBuffers[ i ];
			final MadChannelNoteEvent[] outgoingNotes = outgoingBuffer.noteBuffer;
			if( numReadable > 0 )
			{
				channelNoteRing.read( outgoingNotes, 0, numReadable );
			}
			outgoingBuffer.numElementsInBuffer = numReadable;
		}
	}

	private void doNoteOff( final MadChannelNoteEvent ne ) throws BufferOverflowException, InterruptedException
	{
		// Find if one of the current channels is outputting this note
		final MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( ne.getParamOne() );
		if( mn != null )
		{
			if( noteToChannelMap.containsKey( mn ) )
			{
				final int channelNum = noteToChannelMap.get( mn );
				final MadChannelNoteEvent offEvent = new MadChannelNoteEvent();
				noteCopier.copyValues( ne, offEvent );

				outputChannelNoteRings[ channelNum ].writeOne( offEvent );

				noteToChannelMap.removeKey( mn );
				freeChannelStack.add( channelNum );
//				log.debug( "Stopped note at index " + offEvent.eventSampleIndex + " " + mn.toString() + " on channel " + channelNum );
			}
			else
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Received stop for note we aren't playing: " + mn.toString() );
				}
			}
		}
	}

	private void doNoteOn( final MadChannelNoteEvent ne ) throws BufferOverflowException, InterruptedException
	{
		// Try and find a free channel
		try
		{
			final int freeChannelNumber = freeChannelStack.remove();
			final MadChannelNoteEvent onEvent = new MadChannelNoteEvent();
			noteCopier.copyValues( ne, onEvent );

			outputChannelNoteRings[ freeChannelNumber ].writeOne( onEvent );

			final MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( ne.getParamOne() );

			if( mn != null )
			{
				noteToChannelMap.put( mn, freeChannelNumber );
//				log.debug( "Started note at index " + onEvent.eventSampleIndex + " " + mn.toString() + " on channel " + freeChannelNumber );
			}
		}
		catch(final NoSuchElementException ese)
		{
			if( log.isWarnEnabled() )
			{
				log.warn( "Out of channels for note on");
			}
			debugChannelAssignments();
		}
	}

	private void doNoteContinuation( final MadChannelNoteEvent ne ) throws BufferOverflowException, InterruptedException
	{
		// Find the channel it's already on
		try
		{
			final MidiNote oldNote = MidiUtils.getMidiNoteFromNumberReturnNull( ne.getParamThree() );
			if( oldNote != null )
			{
				if( noteToChannelMap.containsKey( oldNote ) )
				{
					final int oldChannelNum = noteToChannelMap.get( oldNote );
					final MadChannelNoteEvent continuationEvent = new MadChannelNoteEvent();
					noteCopier.copyValues( ne, continuationEvent );

					outputChannelNoteRings[ oldChannelNum ].writeOne( continuationEvent );

					noteToChannelMap.removeKey( oldNote );
					final MidiNote newNote = MidiUtils.getMidiNoteFromNumberReturnNull( ne.getParamOne() );
					if( newNote != null )
					{
						noteToChannelMap.put( newNote, oldChannelNum );
//						log.debug( "Continued old note " + oldNote.toString() + " at index " + continuationEvent.eventSampleIndex + " " + newNote.toString() + " on channel " + oldChannelNum );
					}
				}
			}
		}
		catch(final NoSuchElementException ese)
		{
			if( log.isWarnEnabled() )
			{
				log.warn( "Out of channels for continuation");
			}
			debugChannelAssignments();
		}
	}

	private void debugChannelAssignments()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Currently assigned notes and channels: ");
			for( final MidiNote mn : noteToChannelMap.keys() )
			{
				final int channel = noteToChannelMap.get( mn );
				log.debug( mn.toString() + " - channel " + channel );
			}
		}
	}
}
