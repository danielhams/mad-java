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

package uk.co.modularaudio.mads.base.interptester.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class InterpTesterIOQueueBridge extends MadLocklessQueueBridge<InterpTesterMadInstance>
{
	private static Log log = LogFactory.getLog( InterpTesterIOQueueBridge.class.getName() );

	public static final int COMMAND_AMP = 0;
	public static final int COMMAND_AMP_NOTS = 1;
	public static final int COMMAND_CHASE_MILLIS = 2;
	public static final int COMMAND_SET_MODEL = 3;

	public static final int COMMAND_TO_UI_NONE_NANOS = 4;
	public static final int COMMAND_TO_UI_LIN_NANOS = 5;
	public static final int COMMAND_TO_UI_HH_NANOS = 6;
	public static final int COMMAND_TO_UI_SD_NANOS = 7;
	public static final int COMMAND_TO_UI_LP_NANOS = 8;
	public static final int COMMAND_TO_UI_SDD_NANOS = 9;

	public static final int COMMAND_UIACTIVE = 10;


	public InterpTesterIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final InterpTesterMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp,
			final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_AMP:
			{
				final long value = queueEntry.value;
				final int lower32Bits = (int)((value ) & 0xFFFFFFFF);
				final float amp = Float.intBitsToFloat( lower32Bits );
				instance.setDesiredAmp( amp );
				break;
			}
			case COMMAND_AMP_NOTS:
			{
				final long value = queueEntry.value;
				final int lower32Bits = (int)((value ) & 0xFFFFFFFF);
				final float amp = Float.intBitsToFloat( lower32Bits );
				instance.setDesiredAmpNoTs( amp );
				break;
			}
			case COMMAND_CHASE_MILLIS:
			{
				final float chaseMillis = Float.intBitsToFloat( (int)queueEntry.value );
				instance.setChaseMillis( chaseMillis );
				break;
			}
			case COMMAND_SET_MODEL:
			{
				instance.setModelIndex( (int)queueEntry.value );
				break;
			}
			case COMMAND_UIACTIVE:
			{
				instance.setUiActive( queueEntry.value == 1 );
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
