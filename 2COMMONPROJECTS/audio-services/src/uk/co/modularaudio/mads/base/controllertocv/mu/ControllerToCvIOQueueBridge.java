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

package uk.co.modularaudio.mads.base.controllertocv.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class ControllerToCvIOQueueBridge extends MadLocklessQueueBridge<ControllerToCvMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerToCvIOQueueBridge.class.getName() );

	public static final int COMMAND_SILENCE = 0;
	public static final int COMMAND_EVENT_MAPPING = 1;
	public static final int COMMAND_CHANNEL_NUMBER = 2;
	public static final int COMMAND_CONTROLLER_NUMBER = 3;

	public ControllerToCvIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final ControllerToCvMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_SILENCE:
			{
				break;
			}
			case COMMAND_EVENT_MAPPING:
			{
				final long value = queueEntry.value;
				final ControllerEventMapping mapping = ControllerEventMapping.values()[ (int)value ];
				instance.desiredMapping = mapping;
				break;
			}
			case COMMAND_CHANNEL_NUMBER:
			{
				final int channelNumber = (int)queueEntry.value;
				instance.desiredChannel = channelNumber;
				break;
			}
			case COMMAND_CONTROLLER_NUMBER:
			{
				final int controllerNumber = (int)queueEntry.value;
				instance.desiredController = controllerNumber;
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
