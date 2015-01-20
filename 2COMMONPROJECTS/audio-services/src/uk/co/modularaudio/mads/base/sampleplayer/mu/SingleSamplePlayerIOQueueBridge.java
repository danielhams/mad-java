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

package uk.co.modularaudio.mads.base.sampleplayer.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.midi.MidiNote;
import uk.co.modularaudio.util.audio.midi.MidiUtils;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class SingleSamplePlayerIOQueueBridge extends MadLocklessQueueBridge<SingleSamplePlayerMadInstance>
{
	private static Log log = LogFactory.getLog( SingleSamplePlayerIOQueueBridge.class.getName() );

	public static final int COMMAND_ROOT_NOTE = 0;
	public static final int COMMAND_START_POS = 1;

	public SingleSamplePlayerIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( SingleSamplePlayerMadInstance instance, ThreadSpecificTemporaryEventStorage tses, long periodTimestamp, IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_ROOT_NOTE:
			{
				MidiNote relatedNote = MidiUtils.getMidiNoteFromNumberReturnNull( (int )queueEntry.value );
				if( relatedNote != null )
				{
					instance.currentRootNoteFreq = relatedNote.getFrequency();
				}
				break;
			}
			case COMMAND_START_POS:
			{
				float startPosMillis = Float.intBitsToFloat( (int)queueEntry.value );
				int startPosFrameNum = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( instance.sampleRate, startPosMillis );
				instance.currentStartPosFrameNum = startPosFrameNum;
				break;
			}
			default:
			{
				String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
			}
		}
	}
}
