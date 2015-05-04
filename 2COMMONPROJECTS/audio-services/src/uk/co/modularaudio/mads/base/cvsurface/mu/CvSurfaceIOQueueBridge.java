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

package uk.co.modularaudio.mads.base.cvsurface.mu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessIOQueue;
import uk.co.modularaudio.util.audio.mad.ioqueue.MadLocklessQueueBridge;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;

public class CvSurfaceIOQueueBridge extends MadLocklessQueueBridge<CvSurfaceMadInstance>
{
	private static Log log = LogFactory.getLog( CvSurfaceIOQueueBridge.class.getName() );

	public static final int COMMAND_NEWX = 0;
	public static final int COMMAND_NEWY = 1;

	private static final int CV_SURFACE_TEMPORAL_TO_INSTANCE_QUEUE_LENGTH = 256;

	public CvSurfaceIOQueueBridge()
	{
		super( MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH,
				CV_SURFACE_TEMPORAL_TO_INSTANCE_QUEUE_LENGTH,
				MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH,
				MadLocklessIOQueue.DEFAULT_QUEUE_LENGTH );
	}

	@Override
	public void receiveQueuedEventsToInstance( final CvSurfaceMadInstance instance, final ThreadSpecificTemporaryEventStorage tses, final long periodTimestamp, final IOQueueEvent queueEntry )
	{
		switch( queueEntry.command )
		{
			case COMMAND_NEWX:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float aa = Float.intBitsToFloat( truncVal );
				instance.setDesiredX( aa );
				break;
			}
			case COMMAND_NEWY:
			{
				// float
				final long value = queueEntry.value;
				final int truncVal = (int)value;
				final float ab = Float.intBitsToFloat( truncVal );
				instance.setDesiredY( ab );
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
