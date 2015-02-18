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

package uk.co.modularaudio.mads.base.oscilloscope.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class OscilloscopeIOQueueBridge extends MadLocklessQueueBridge<OscilloscopeMadInstance>
{
	private static Log log = LogFactory.getLog( OscilloscopeIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_ACTIVE = 0;
	public static final int COMMAND_IN_SCOPE_DATA = 1;
	public static final int COMMAND_IN_CAPTURE_TRIGGER = 2;
	public static final int COMMAND_IN_CAPTURE_REPETITIONS = 3;
	public static final int COMMAND_OUT_SCOPE_DATA = 4;

	public OscilloscopeIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final OscilloscopeMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_SCOPE_DATA:
			{
				// Is just a float
				final Object obj = queueEntry.object;
				final OscilloscopeWriteableScopeData scopeData = (OscilloscopeWriteableScopeData)obj;
				instance.bufferedScopeData.add( scopeData );
//				log.debug("Consumed a scope data command");
				break;
			}
			case COMMAND_IN_ACTIVE:
			{
				final boolean active = ( queueEntry.value == 1 );
				instance.active = active;
				break;
			}
			case COMMAND_IN_CAPTURE_TRIGGER:
			{
				final OscilloscopeCaptureTriggerEnum ct = OscilloscopeCaptureTriggerEnum.values()[ (int)queueEntry.value ];
				instance.captureTrigger = ct;
				break;
			}
			case COMMAND_IN_CAPTURE_REPETITIONS:
			{
				final OscilloscopeCaptureRepetitionsEnum rt = OscilloscopeCaptureRepetitionsEnum.values()[ (int)queueEntry.value ];
				instance.captureRepetitions = rt;
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
