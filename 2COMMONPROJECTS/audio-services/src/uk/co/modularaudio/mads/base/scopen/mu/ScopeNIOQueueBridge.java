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

package uk.co.modularaudio.mads.base.scopen.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.scopen.ui.ScopeNRepetitionsChoiceUiJComponent.RepetitionChoice;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNTriggerChoiceUiJComponent.TriggerChoice;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class ScopeNIOQueueBridge<I extends ScopeNMadInstance<?,I>>
	extends MadLocklessQueueBridge<I>
{
	private static Log log = LogFactory.getLog( ScopeNIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_ACTIVE = 0;

	public static final int COMMAND_IN_CAPTURE_MILLIS = 1;
	public static final int COMMAND_IN_TRIGGER = 2;
	public static final int COMMAND_IN_REPETITION = 3;
	public static final int COMMAND_IN_RECAPTURE = 4;

	public static final int COMMAND_OUT_DATA_START = 5;
	public static final int COMMAND_OUT_RINGBUFFER_WRITE_INDEX = 6;

	public ScopeNIOQueueBridge()
	{
		// Needs a bit of headroom for very short capture lengths < 2ms
		super( 0,
			MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH,
			0,
			MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH * 2 );
	}

	@Override
	public void receiveQueuedEventsToInstance( final I instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final int U_periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				final boolean active = ( queueEntry.value == 1 );
				instance.setActive( active );
				break;
			}
			case COMMAND_IN_CAPTURE_MILLIS:
			{
				final int intBits = (int)queueEntry.value;
				final float captureMillis = Float.intBitsToFloat( intBits );
				instance.setCaptureMillis( captureMillis );
				break;
			}
			case COMMAND_IN_TRIGGER:
			{
				final TriggerChoice trigger = TriggerChoice.values()[ (int)queueEntry.value ];
				instance.setTriggerChoice( trigger );
				break;
			}
			case COMMAND_IN_REPETITION:
			{
				final RepetitionChoice repetition = RepetitionChoice.values()[ (int)queueEntry.value ];
				instance.setRepetitionChoice( repetition );
				break;
			}
			case COMMAND_IN_RECAPTURE:
			{
				instance.doRecapture( tses, U_periodTimestamp );
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
