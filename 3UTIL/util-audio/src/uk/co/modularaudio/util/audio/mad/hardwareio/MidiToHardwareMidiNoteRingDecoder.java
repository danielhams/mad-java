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

package uk.co.modularaudio.util.audio.mad.hardwareio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelNoteEventType;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MidiToHardwareMidiNoteRingDecoder
{
	private static Log log = LogFactory.getLog(  MidiToHardwareMidiNoteRingDecoder.class.getName() );
	
	private LocklessHardwareMidiNoteRingBuffer noteEventRing = null;
	
	private HardwareMidiNoteEvent internalEvent = new HardwareMidiNoteEvent();
	
	public MidiToHardwareMidiNoteRingDecoder( LocklessHardwareMidiNoteRingBuffer noteEventRing )
	{
		this.noteEventRing = noteEventRing;
	}

	public RealtimeMethodReturnCodeEnum decodeMessage( int command, int channel, int data1, int data2, long timestampNanoseconds )
	{
		log.debug( "SO EV (" + AudioTimingUtils.formatTimestampForLogging( timestampNanoseconds ) + ")");

		switch( command )
		{
			case 0x80:
			{
				MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( data1 );
				if( mn != null )
				{
					log.debug( "Note off " + mn.getNoteName() + " velocity " + data2 );
					internalEvent.set( channel, timestampNanoseconds, MadChannelNoteEventType.NOTE_OFF, data1, data2, -1 );
					noteEventRing.writeOne( internalEvent );
				}
				else
				{
					log.warn( "Received note num that couldn't be mapped: " + data1 );
				}
				break;
			}
			case 0x90:
			{
				MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( data1 );
				if( mn != null )
				{
					if( data2 == 0 )
					{
						log.debug( "Note off " + mn.getNoteName() + " velocity " + data2 );
						internalEvent.set( channel, timestampNanoseconds, MadChannelNoteEventType.NOTE_OFF, data1, data2, -1 );
					}
					else
					{
						log.debug( "Note on " + mn.getNoteName() + " velocity " + data2 );
						internalEvent.set( channel, timestampNanoseconds, MadChannelNoteEventType.NOTE_ON, data1, data2, -1 );
					}
					noteEventRing.writeOne( internalEvent );
	//				log.debug( "Added event to ring." );
				}
				else
				{
					log.warn( "Received note num that couldn't be mapped: " + data1 );
				}
				break;
			}
			case 0xa0:
			{
				MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( data1 );
				if( mn != null )
				{
					log.debug( "Polyphonic key pressure " + mn.getNoteName() + " pressure " + data2 );
				}
				else
				{
					log.error( "Unknown midi note received: " + data1 );
				}
				break;
			}
			case 0xb0:
			{
				log.debug( "Control change on channel " + channel + " " + data1 + " value " + data2  + " with timestamp " + timestampNanoseconds );
				internalEvent.set( channel, timestampNanoseconds, MadChannelNoteEventType.CONTROLLER, data1, data2, -1 );
				noteEventRing.writeOne( internalEvent );
				break;
			}
			case 0xc0:
			{
				log.debug( "Program change " + data1 );
				break;
			}
			case 0xd0:
			{
				MidiNote mn = MidiUtils.getMidiNoteFromNumberReturnNull( data1 );
				if( mn != null )
				{
					log.debug( "Key pressure " + mn.getNoteName() + " pressure " + data2 );
				}
				else
				{
					log.error( "Unknown midi note received: " + data1 );
				}
				break;
			}
			case 0xe0:
			{
				int rawValue = get14bitValue( data1, data2 );
				log.debug( "Pitch wheel change p1(" + data1 + ") p2(" + data2 + ") 14bitval(" + rawValue +")");
				if( rawValue == 8192 )
				{
					log.debug("No pitch bend");
				}
				else if( rawValue < 8192 )
				{
					log.debug( "Pitch down" );
				}
				else if( rawValue > 8192 )
				{
					log.debug( "Pitch up" );
				}
				break;
			}
			case 0xF0:
			{
				break;
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private int get14bitValue( int lower, int higher )
	{
		int lowerVal = (lower & 0x7F);
		int upperVal = (higher & 0x7F);
		int shiftedUpper = upperVal << 7;
//		log.debug("OLV(" + lower +") OUV(" + higher + ") LV(" + lowerVal +") UV(" + upperVal + ") SU(" + shiftedUpper + ")");
		return lowerVal | shiftedUpper;
	}

}
