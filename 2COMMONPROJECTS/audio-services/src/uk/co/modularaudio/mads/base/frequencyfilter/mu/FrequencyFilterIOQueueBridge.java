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

package uk.co.modularaudio.mads.base.frequencyfilter.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class FrequencyFilterIOQueueBridge extends MadLocklessQueueBridge<FrequencyFilterMadInstance>
{
	private static Log log = LogFactory.getLog( FrequencyFilterIOQueueBridge.class.getName() );

	public static final int COMMAND_FILTER_MODE = 0;
	public static final int COMMAND_FREQUENCY = 1;
	public static final int COMMAND_BANDWIDTH = 2;
	public static final int COMMAND_DBTOGGLE = 3;

	public FrequencyFilterIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( FrequencyFilterMadInstance instance,
			ThreadSpecificTemporaryEventStorage tses,
			long periodTimestamp,
			IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_FILTER_MODE:
			{
				// float
				long value = queueEntry.value;
				int truncVal = (int)value;
				instance.desiredFilterMode = FrequencyFilterMode.values()[ truncVal ];
				break;
			}
			case COMMAND_FREQUENCY:
			{
				// float
				long value = queueEntry.value;
				int truncVal = (int)value;
				float floatVal = Float.intBitsToFloat( truncVal );
				instance.desiredFrequency = floatVal;
				break;
			}
			case COMMAND_BANDWIDTH:
			{
				// float
				long value = queueEntry.value;
				int truncVal = (int)value;
				float floatVal = Float.intBitsToFloat( truncVal );
				instance.desiredBandwidth = floatVal;
				break;
			}
			case COMMAND_DBTOGGLE:
			{
				long value = queueEntry.value;
				int truncVal = (int)value;
				boolean bVal = ( truncVal == 0 ? false : true );
				instance.desired24dB = bVal;
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
