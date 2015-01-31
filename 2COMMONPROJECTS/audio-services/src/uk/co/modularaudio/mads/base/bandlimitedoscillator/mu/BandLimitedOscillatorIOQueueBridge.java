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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;

public class BandLimitedOscillatorIOQueueBridge extends MadLocklessQueueBridge<BandLimitedOscillatorMadInstance>
{
	private static Log log = LogFactory.getLog( BandLimitedOscillatorIOQueueBridge.class.getName() );

	public static final int COMMAND_FREQUENCY = 0;
	public static final int COMMAND_FREQUENCY_IMMEDIATE = 1;
	public static final int COMMAND_WAVE = 2;
	public static final int COMMAND_PULSE_WIDTH = 3;
	public static final int COMMAND_PHASE = 4;

	public BandLimitedOscillatorIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( final BandLimitedOscillatorMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_FREQUENCY:
			{
				// Is just a float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float freq = Float.intBitsToFloat( truncVal );
				instance.oscillationFrequency = freq;
				break;
			}
			case COMMAND_FREQUENCY_IMMEDIATE:
			{
				// Is just a float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float freq = Float.intBitsToFloat( truncVal );
				instance.oscillationFrequency = freq;
				instance.runtimeOscillationFrequency = freq;
				break;
			}
			case COMMAND_WAVE:
			{
				// Is just the integer index of the enum
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final OscillatorWaveShape waveShape = OscillatorWaveShape.values()[ truncVal ];
				instance.desiredWaveShape = waveShape;
				log.debug("Received wave shape change");
				break;
			}
			case COMMAND_PULSE_WIDTH:
			{
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float pw = Float.intBitsToFloat( truncVal );
				instance.desiredPulsewidth = pw;
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
