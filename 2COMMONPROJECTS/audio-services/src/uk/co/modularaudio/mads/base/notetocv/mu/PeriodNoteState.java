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

package uk.co.modularaudio.mads.base.notetocv.mu;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.notetocv.ui.NoteOnTypeChoiceUiJComponent.NoteOnType;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEvent;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventCopier;
import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;

public class PeriodNoteState
{
	private static final float GATE_ON_VALUE = 1.0f;

	private static Log log = LogFactory.getLog( PeriodNoteState.class.getName() );

	private NoteOnType noteOnType = NoteOnType.FOLLOW_FIRST;

	private final MadChannelNoteEventCopier copier = new MadChannelNoteEventCopier();

	private final MadChannelNoteEvent lastEvent = new MadChannelNoteEvent();

	private int audioChannelBufferLength;
	private int[] segmentBounds;
	private MadChannelNoteEvent[] segmentEvents;

	private int numSegmentsReady;

	private int curOutputCounter;

	private float freqGlideCurValueRatio;
	private float freqGlideNewValueRatio;

	private float ampGlideCurValueRatio;
	private float ampGlideNewValueRatio;

	private float lastFreqVal;

	private float lastVelAmpMultiplierVal;
	private float targetVelAmpMultiplierVal = 1.0f;

	public PeriodNoteState()
	{
	}

	public void setGlideRatios( final float freqGlideCurValueRatio, final float freqGlideNewValueRatio,
			final float ampGlideCurValueRatio, final float ampGlideNewValueRatio )
	{
		this.freqGlideCurValueRatio = freqGlideCurValueRatio;
		this.freqGlideNewValueRatio = freqGlideNewValueRatio;
		this.ampGlideCurValueRatio = ampGlideCurValueRatio;
		this.ampGlideNewValueRatio = ampGlideNewValueRatio;
	}

	public void resize( final int audioChannelBufferLength, final int noteChannelBufferLength )
	{
		this.audioChannelBufferLength = audioChannelBufferLength;

		segmentBounds = new int[ noteChannelBufferLength * 2 ];
		segmentEvents = new MadChannelNoteEvent[ noteChannelBufferLength ];
		for( int i = 0 ; i < noteChannelBufferLength ; i++ )
		{
			segmentBounds[ i * 2 ] = 0;
			segmentBounds[ ( i * 2 ) + 1 ] = 0;
			final MadChannelNoteEvent tmpEvent = new MadChannelNoteEvent();
			tmpEvent.reset();
			segmentEvents[ i ] = tmpEvent;
		}
	}

	public void startNewPeriod( final NoteOnType noteOnType )
	{
		curOutputCounter = 0;
		numSegmentsReady = 0;
		this.noteOnType = noteOnType;
//		log.debug("Started new period");
	}

	public void addNewEvent( final MadChannelNoteEvent eventToAdd )
	{
		boolean isValidEvent = false;
		final MadChannelNoteEventType lastEventType = lastEvent.getEventType();
		final MadChannelNoteEventType eventToAddEventType = eventToAdd.getEventType();

		switch( lastEventType )
		{
			case EMPTY:
			case NOTE_OFF:
			{
				switch( eventToAddEventType )
				{
					case NOTE_ON:
					{
						isValidEvent = true;
						break;
					}
					case NOTE_OFF:
					{
						isValidEvent = true;
						break;
					}
					default:
					{
					}
				}
				break;
			}
			case NOTE_CONTINUATION:
			case NOTE_ON:
			{
				switch( eventToAddEventType )
				{
					case EMPTY:
					{
						isValidEvent = true;
						break;
					}
					case NOTE_OFF:
					{
						// Only allow note off for the note that is being played.
						if( eventToAdd.getParamOne() == lastEvent.getParamOne() )
						{
							isValidEvent = true;
						}
						else
						{
//							log.debug("Note off for note not playing says ignore event.");
						}
						break;
					}
					case NOTE_ON:
					{
						switch( noteOnType )
						{
							case FOLLOW_FIRST:
							{
								// Isn't valid, ignore it
//								log.debug("Follow first says ignore event.");
								break;
							}
							case FOLLOW_LAST:
							{
								isValidEvent = true;
//								eventToAdd.eventType = MadNoteEventType.NOTE_CONTINUATION;
								break;
							}
						}
						break;
					}
					case NOTE_CONTINUATION:
					{
						isValidEvent = true;
						break;
					}
					default:
					{
						if( log.isDebugEnabled() )
						{
							log.debug("Unhandled case of eventoaddtype says ignore event eta: " + eventToAddEventType );
						}
					}
				}
				break;
			}
			default:
			{
				if( log.isDebugEnabled() )
				{
					log.debug("Unhandled case of lasteventype says ignore event let: " + lastEventType );
				}
				break;
			}
		}

		if( isValidEvent )
		{
//			log.debug("Adding valid event");
			if( curOutputCounter == 0 )
			{
//				log.debug("Is first event in period, will prefill with last event up to sample index");
				// It's the first event in this period - if the start sample index isn't 0, we need to insert the end of the segment
				// that didn't finish the previous time around
				if( eventToAdd.getEventSampleIndex() > 0 )
				{
					addSegmentData( 0, lastEvent );
				}
			}

			// Now add a segment for this event
			addSegmentData( eventToAdd.getEventSampleIndex(), eventToAdd );

			// Finally copy over to last event
			copier.copyValues( eventToAdd, lastEvent );
		}
		else
		{
//			log.debug("Wasn't valid event, skipped it.");
		}

	}

	private void addSegmentData( final int segmentStartIndex, final MadChannelNoteEvent event )
	{
//		log.debug("Adding segment data for " + event.eventType + " from " + segmentStartIndex );
		segmentBounds[ ( curOutputCounter * 2) ] = segmentStartIndex;
		if( curOutputCounter > 0 )
		{
			segmentBounds[ ( curOutputCounter * 2 ) - 1 ] = segmentStartIndex;
//			log.debug("Marking previous segment as ending at " + segmentStartIndex );
		}
		copier.copyValues( event, segmentEvents[ curOutputCounter ] );
		copier.copyValues( event, lastEvent );
//		log.debug("Set last event to " + lastEvent.eventType );
		curOutputCounter++;
	}

	public void doPeriodNoEvents( final int numFrames )
	{
//		MadNoteEventType eventType = lastEvent.getEventType();
//		if( eventType == MadNoteEventType.NOTE_OFF )
//		{
//			lastEvent.reset();
//		}
//		log.debug("Filling period no events with " + lastEvent.getEventType() );
		segmentBounds[ 0 ] = 0;
		segmentBounds[ 1 ] = numFrames - 1;
		copier.copyValues( lastEvent, segmentEvents[ 0 ] );
	}

	public void turnOffNotes( final int numFrames )
	{
		segmentBounds[ 0 ] = 0;
		segmentBounds[ 1 ] = numFrames;
		segmentEvents[ 0 ].setEventType( MadChannelNoteEventType.NOTE_OFF );
		copier.copyValues( segmentEvents[0], lastEvent );
		curOutputCounter++;
	}

	public void endPeriod( final int numFrames )
	{
		if( curOutputCounter == 0 )
		{
//			log.debug("Doing period with no events.");
			doPeriodNoEvents( numFrames );
		}

		if( curOutputCounter > 0 )
		{
//			log.debug("Closing off segment " + curOutputCounter + " to end at " + audioChannelBufferLength );
			final int previousSegmentNum = curOutputCounter - 1;
			final int segmentEndIndex = (previousSegmentNum * 2) + 1;
//			log.debug("Setting segment end index " + segmentEndIndex + " to " + audioChannelBufferLength );
			segmentBounds[ segmentEndIndex ] = numFrames;
			copier.copyValues( segmentEvents[ previousSegmentNum ], lastEvent );
		}
		else
		{
			// No action during the period, fill in a single empty period
			segmentBounds[ 0 ] = 0;
			segmentBounds[ 1 ] = numFrames;
			final MadChannelNoteEventType eventType = lastEvent.getEventType();
			if( eventType == MadChannelNoteEventType.NOTE_OFF )
			{
				lastEvent.reset();
			}
			else
			{
				copier.copyValues( lastEvent, segmentEvents[ 0 ] );
			}
			curOutputCounter++;
		}
		numSegmentsReady = curOutputCounter;
//		log.debug("Ended period");
	}

	public void fillGate( final float[] gb, final int frameOffset, final int numFrames )
	{
//		if( numSegmentsReady > 0 )
//		{
//			log.debug("OUT Filling out " + numSegmentsReady + " periods with gate data from " + segmentBounds[ 0 ] + " to " +
//					segmentBounds[ ( (numSegmentsReady - 1) * 2) + 1] );
//		}
		for( int p = 0 ; p < numSegmentsReady ; p++ )
		{
			final int startIndex = segmentBounds[ (p * 2) ];
			final int endIndex = segmentBounds[ (p*2) + 1 ];
			final MadChannelNoteEvent event = segmentEvents[ p ];
			float gateVal = 0.0f;
			switch( event.getEventType() )
			{
				case NOTE_ON:
				{
					gateVal = GATE_ON_VALUE;
					break;
				}
				case NOTE_CONTINUATION:
				{
					gateVal = GATE_ON_VALUE;
					break;
				}
				default:
				{
				}
			}

//			if( log.isDebugEnabled() )
//			{
//				if( numSegmentsReady > 0 )
//				{
//					log.debug("Gate output event type " + event.eventType + " fill from " + startIndex + " to " + endIndex + " with " + gateVal );
//				}
//			}

			if( startIndex == -1 || endIndex == -1 )
			{
				log.error("Bugger");
			}
			else if( endIndex < startIndex )
			{
				log.error( "And blast");
			}
			else
			{
				Arrays.fill( gb, frameOffset + startIndex, frameOffset + endIndex, gateVal );
			}
		}

	}

	private final MadChannelNoteEvent lastTriggerEvent = new MadChannelNoteEvent();

	public void fillTrigger( final float[] tb, final int frameOffset, final int numFrames )
	{
		for( int p = 0 ; p < numSegmentsReady ; p++ )
		{
			final int startIndex = segmentBounds[ (p * 2) ];
			final int endIndex = segmentBounds[ (p*2) + 1 ];
			final MadChannelNoteEvent event = segmentEvents[ p ];

			boolean shouldTrigger = false;
			switch( event.getEventType() )
			{
				case NOTE_ON:
				{
					final MadChannelNoteEventType eventType = lastTriggerEvent.getEventType();
					if( eventType != MadChannelNoteEventType.NOTE_ON ||
							(eventType == MadChannelNoteEventType.NOTE_ON && lastTriggerEvent.getParamOne() != event.getParamOne() ) )
					{
//						log.debug("Triggering...");
						shouldTrigger = true;
					}
					break;
				}
				case NOTE_CONTINUATION:
				{
//					log.debug("Check to see if continuation to be triggered - lasteventtype is " + lastEvent.eventType.toString() );
//					if( lastEvent.eventType != MadNoteEventType.NOTE_CONTINUATION )
//					{
//						shouldTrigger = true;
//					}
					break;
				}
				default:
				{
				}
			}

			if( startIndex == -1 || endIndex == -1 || startIndex == audioChannelBufferLength || endIndex > audioChannelBufferLength )
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Dodgy indexes again... " + startIndex + " " + endIndex );
				}
			}
			else
			{
				if( shouldTrigger )
				{
					tb[ startIndex ] = 1.0f;
					final int spikeIndex = startIndex + 1;
					if( spikeIndex < tb.length && endIndex  > spikeIndex )
					{
//						log.debug("Filled trigger spike at index " + spikeIndex );
						Arrays.fill( tb, spikeIndex, endIndex, 0.0f );
					}
				}
				else
				{
					Arrays.fill( tb, startIndex, endIndex, 0.0f );
				}
				copier.copyValues( event, lastTriggerEvent );
//				log.debug("Last trigger event type is " + lastTriggerEvent.eventType.toString() );
			}
		}
	}

	private float curOutputFrequency = 0.0f;

	public void fillFrequency( final float[] fb, final int frameOffset, final int numFrames )
	{
		for( int p = 0 ; p < numSegmentsReady ; p++ )
		{
			final int startIndex = segmentBounds[ (p * 2) ];
			final int endIndex = segmentBounds[ (p*2) + 1 ];
			final MadChannelNoteEvent event = segmentEvents[ p ];
			switch( event.getEventType() )
			{
				case NOTE_ON:
//				{
//					MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( event.getParamOne() );
//					// Quick hack
//					MadChannelNoteEventType eventType = lastTriggerEvent.getEventType();
//					if( eventType != MadChannelNoteEventType.NOTE_ON ||
//							(eventType == MadChannelNoteEventType.NOTE_ON && lastTriggerEvent.getParamOne() != event.getParamOne() ) )
//					{
//						curOutputFrequency = mn.getFrequency();
//						lastFreqVal = curOutputFrequency;
//					}
//					break;
//				}
				case NOTE_CONTINUATION:
				{
					final MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( event.getParamOne() );
//					log.debug("Note on/cont with " + mn.toString() );
					if( mn != null )
					{
						lastFreqVal = mn.getFrequency();
					}
					break;
				}
				default:
				{
					break;
				}
			}
			if( startIndex == -1 || endIndex == -1 )
			{
			}
			else
			{
				for( int s = startIndex ; s < endIndex ; s++ )
				{
					curOutputFrequency = (curOutputFrequency * freqGlideCurValueRatio ) + ( lastFreqVal * freqGlideNewValueRatio );
					fb[ s ] = curOutputFrequency;
				}
			}
		}
	}

	public void fillVelocity( final float[] vb, final int frameOffset, final int numFrames )
	{
		for( int p = 0 ; p < numSegmentsReady ; p++ )
		{
			final int startIndex = segmentBounds[ (p * 2) ];
			final int endIndex = segmentBounds[ (p*2) + 1 ];
			final MadChannelNoteEvent event = segmentEvents[ p ];
			float velVal = 0.0f;
			switch( event.getEventType() )
			{
				case NOTE_ON:
				case NOTE_CONTINUATION:
				{
					velVal = event.getParamTwo() / 128.0f;
//					log.debug("Outputting velocity of " + velVal );
					break;
				}
				default:
				{
					break;
				}

			}

			if( startIndex == -1 || endIndex == -1 )
			{
				log.debug("Not outputting velocity due to bad indexes...");
			}
			else
			{
				Arrays.fill( vb, startIndex, endIndex, velVal );
			}
		}

	}

	public void fillVelAmpMultiplier( final float[] vb, final int frameOffset, final int numFrames )
	{
		for( int p = 0 ; p < numSegmentsReady ; p++ )
		{
			final int startIndex = segmentBounds[ (p * 2) ];
			final int endIndex = segmentBounds[ (p*2) + 1 ];
			final MadChannelNoteEvent event = segmentEvents[ p ];
			switch( event.getEventType() )
			{
				case NOTE_ON:
				case NOTE_CONTINUATION:
				{
					final float linearVal = event.getParamTwo() / 128.0f;
					targetVelAmpMultiplierVal = linearVal;
//					targetVelAmpMultiplierVal = NormalisedValuesMapper.logMapF( linearVal );
//					targetVelAmpMultiplierVal = NormalisedValuesMapper.expMapF( linearVal );
					break;
				}
				default:
				{
					break;
				}
			}

			if( startIndex == -1 || endIndex == -1 )
			{
				log.debug("Not outputting velocity due to bad indexes...");
			}
			else
			{
				for( int i = startIndex ; i < endIndex ; ++i )
				{
					lastVelAmpMultiplierVal = (lastVelAmpMultiplierVal * ampGlideCurValueRatio) + (targetVelAmpMultiplierVal * ampGlideNewValueRatio);
					vb[i] = lastVelAmpMultiplierVal;
				}
//				lastVelAmpMultiplierVal = vb[endIndex-1];
			}
		}

	}

}
