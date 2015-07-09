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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.notetocv.ui.NoteOnTypeChoiceUiJComponent.NoteOnType;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class NoteToCvIOQueueBridge extends MadLocklessQueueBridge<NoteToCvMadInstance>
{
	private static Log log = LogFactory.getLog( NoteToCvIOQueueBridge.class.getName() );

	public static final int COMMAND_NOTE_ON_TYPE = 0;
	public static final int COMMAND_FREQ_GLIDE_MILLIS = 1;
	public static final int COMMAND_CHANNEL_NUM = 2;

	public NoteToCvIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final NoteToCvMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_NOTE_ON_TYPE:
			{
				final int val = (int)queueEntry.value;
				final NoteOnType not = NoteOnType.values()[val];
				instance.setDesiredNoteOnType( not );
				break;
			}
			case COMMAND_FREQ_GLIDE_MILLIS:
			{
				final float val = Float.intBitsToFloat( (int)queueEntry.value );
				instance.setFrequencyGlideMillis( val );
				break;
			}
			case COMMAND_CHANNEL_NUM:
			{
				final int channelNum = (int)queueEntry.value;
				instance.setDesiredChannelNum( channelNum );
				break;
			}
			default:
			{
				final String msg = "Unknown command passed on incoming queue: " + queueEntry.command;
				log.error( msg );
			}
		}
	}
}
