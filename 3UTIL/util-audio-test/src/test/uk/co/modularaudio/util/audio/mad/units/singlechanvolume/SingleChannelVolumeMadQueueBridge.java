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

package test.uk.co.modularaudio.util.audio.mad.units.singlechanvolume;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class SingleChannelVolumeMadQueueBridge extends MadLocklessQueueBridge<SingleChannelVolumeMadInstance>
{
	public static final int COMMAND_VOLUME = 0;
	
	public SingleChannelVolumeMadQueueBridge()
	{
	}
	
	@Override
	public void receiveQueuedEventsToInstance( SingleChannelVolumeMadInstance instance, ThreadSpecificTemporaryEventStorage tses, long timingInfo, IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_VOLUME:
				instance.inVolumeMultiplier = Float.intBitsToFloat( (int)(queueEntry.value) );
				break;
		}
	}
}
