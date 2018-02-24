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

package uk.co.modularaudio.mads.base.crossover.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class CrossoverIOQueueBridge extends MadLocklessQueueBridge<CrossoverMadInstance>
{
	private static Log log = LogFactory.getLog( CrossoverIOQueueBridge.class.getName() );

	public static final int COMMAND_FREQUENCY = 0;
	public static final int COMMAND_DBTOGGLE = 1;

	public CrossoverIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final CrossoverMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final int U_periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_FREQUENCY:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float floatVal = Float.intBitsToFloat( truncVal );
				instance.setDesiredFrequency( floatVal );
				break;
			}
			case COMMAND_DBTOGGLE:
			{
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final boolean bVal = ( truncVal == 0 ? false : true );
				instance.setDesired24dB( bVal );
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
