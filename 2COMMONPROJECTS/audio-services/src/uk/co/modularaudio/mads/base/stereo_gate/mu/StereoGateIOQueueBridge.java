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

package uk.co.modularaudio.mads.base.stereo_gate.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.stereo_gate.ui.ThresholdTypeEnum;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class StereoGateIOQueueBridge extends MadLocklessQueueBridge<StereoGateMadInstance>
{
	private static Log log = LogFactory.getLog( StereoGateIOQueueBridge.class.getName() );

	public static final int COMMAND_IN_THRESHOLD = 0;
	public static final int COMMAND_IN_THRESHOLD_TYPE = 1;

	public StereoGateIOQueueBridge()
	{
	}

	@Override
	public void receiveQueuedEventsToInstance( 	final StereoGateMadInstance instance,
			final ThreadSpecificTemporaryEventStorage tses,
			final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_IN_THRESHOLD:
			{
				final float valueAsFloat = Float.intBitsToFloat( (int)queueEntry.value );
				final float desiredThreshold = (float)AudioMath.dbToLevel( valueAsFloat );
				instance.desiredThreshold = desiredThreshold;
				if( log.isDebugEnabled() )
				{
					log.debug("Set desired threshold to " + desiredThreshold );
				}
				break;
			}
			case COMMAND_IN_THRESHOLD_TYPE:
			{
				final int valueAsInt = (int)queueEntry.value;
				instance.desiredThresholdType = ThresholdTypeEnum.values()[ valueAsInt ];
				break;
			}
			default:
			{
				final String msg ="Unknown command to instance: " + queueEntry.command;
				log.error( msg );
				break;
			}
		}

	}
}
