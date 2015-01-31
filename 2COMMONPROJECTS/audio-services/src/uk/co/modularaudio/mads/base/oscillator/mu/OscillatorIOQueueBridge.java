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

package uk.co.modularaudio.mads.base.oscillator.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;

public class OscillatorIOQueueBridge extends MadLocklessQueueBridge<OscillatorMadInstance>
{
	private static Log log = LogFactory.getLog( OscillatorIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_FREQUENCY = 0;
	public static final int COMMAND_IN_FREQUENCY_IMMEDIATE = 1;
	public static final int COMMAND_IN_WAVE = 2;

	public OscillatorIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final OscillatorMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_FREQUENCY:
			{
				// Is just a float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float freq = Float.intBitsToFloat( truncVal );
				instance.oscillationFrequency = freq;
				break;
			}
			case COMMAND_IN_FREQUENCY_IMMEDIATE:
			{
				// Is just a float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float freq = Float.intBitsToFloat( truncVal );
				instance.oscillationFrequency = freq;
				instance.runtimeOscillationFrequency = freq;
				break;
			}
			case COMMAND_IN_WAVE:
			{
				// Is just the integer index of the enum
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final OscillatorWaveShape waveShape = OscillatorWaveShape.values()[ truncVal ];
				instance.curWaveShape = waveShape;
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
