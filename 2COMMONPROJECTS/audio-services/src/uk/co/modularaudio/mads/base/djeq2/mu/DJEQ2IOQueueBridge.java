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

package uk.co.modularaudio.mads.base.djeq2.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class DJEQ2IOQueueBridge extends MadLocklessQueueBridge<DJEQ2MadInstance>
{
	private static Log log = LogFactory.getLog( DJEQ2IOQueueBridge.class.getName() );

	public static final int COMMAND_IN_ACTIVE = 0;
	public static final int COMMAND_IN_HP_AMP = 1;
	public static final int COMMAND_IN_LP_AMP = 2;
	public static final int COMMAND_IN_FADER_AMP = 3;

	public static final int COMMAND_OUT_METER_READINGS = 4;

	public DJEQ2IOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final DJEQ2MadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final int U_periodTimestamp,
			final IOQueueEvent queueEntry )
	{
//		log.debug("Received queued event : " + queueEntry.command);
		switch( queueEntry.command )
		{
			case COMMAND_IN_ACTIVE:
			{
				final boolean active = (queueEntry.value == 1);
				instance.setActive( active );
				break;
			}
			case COMMAND_IN_HP_AMP:
			{
				final int lower32Bits = (int)queueEntry.value;
				final float ampVal = Float.intBitsToFloat( lower32Bits );
				instance.setDesiredHpAmp( ampVal );
				break;
			}
			case COMMAND_IN_LP_AMP:
			{
				final int lower32Bits = (int)queueEntry.value;
				final float ampVal = Float.intBitsToFloat( lower32Bits );
				instance.setDesiredLpAmp( ampVal );
				break;
			}
			case COMMAND_IN_FADER_AMP:
			{
				final int lower32Bits = (int)queueEntry.value;
				final float ampVal = Float.intBitsToFloat( lower32Bits );
				instance.setDesiredFaderAmp( ampVal );
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