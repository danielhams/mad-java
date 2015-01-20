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

package uk.co.modularaudio.mads.base.waveshaper.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperWaveTables.WaveType;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class WaveShaperIOQueueBridge extends MadLocklessQueueBridge<WaveShaperMadInstance>
{
	private static Log log = LogFactory.getLog( WaveShaperIOQueueBridge.class.getName() );
	
	public static final int COMMAND_WAVE = 0;
	
	public WaveShaperIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( WaveShaperMadInstance instance, ThreadSpecificTemporaryEventStorage tses, long periodTimestamp, IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_WAVE:
			{
				// Is just the integer index of the enum
				long value = queueEntry.value;
				int truncVal = (int)value;
				WaveShaperWaveTables.WaveType waveType = WaveType.values()[ truncVal ];
				instance.curWaveType = waveType;
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
