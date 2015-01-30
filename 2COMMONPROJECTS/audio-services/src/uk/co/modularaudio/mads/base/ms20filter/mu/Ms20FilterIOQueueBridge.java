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

package uk.co.modularaudio.mads.base.ms20filter.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class Ms20FilterIOQueueBridge extends MadLocklessQueueBridge<Ms20FilterMadInstance>
{
	private static Log log = LogFactory.getLog( Ms20FilterIOQueueBridge.class.getName() );

	public static final int COMMAND_FILTER_MODE = 0;
	public static final int COMMAND_FREQUENCY = 1;
	public static final int COMMAND_FILTER_RESONANCE = 2;
	public static final int COMMAND_THRESHOLD = 3;

	public Ms20FilterIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final Ms20FilterMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_FILTER_MODE:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				instance.desiredFilterMode = FrequencyFilterMode.values()[ truncVal ];
				break;
			}
			case COMMAND_FREQUENCY:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float floatVal = Float.intBitsToFloat( truncVal );
//				log.debug("DesiredFreq from raw value " + MathFormatter.floatPrint( floatVal, 3 ) );
				instance.desiredFrequency = floatVal;
				break;
			}
			case COMMAND_FILTER_RESONANCE:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float floatVal = Float.intBitsToFloat( truncVal );
				final float innerResonance = floatVal * Ms20FilterMadInstance.MAXIMUM_RESONANCE_VALUE;
//				log.debug("DesiredQ from raw value " + MathFormatter.floatPrint( floatVal, 3 ) + " to " + MathFormatter.floatPrint( innerQ, 3 ) );
				instance.desiredFilterResonance = innerResonance;
				break;
			}
			case COMMAND_THRESHOLD:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float floatVal = Float.intBitsToFloat( truncVal );
				final float innerThreshold = floatVal * Ms20FilterMadInstance.MAXIMUM_THRESHOLD_VALUE;
//				log.debug("DesiredSat from raw value " + MathFormatter.floatPrint( floatVal, 3 ) + " to " + MathFormatter.floatPrint( innerSat, 3 ) );
				instance.desiredSaturationThreshold = innerThreshold;
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
