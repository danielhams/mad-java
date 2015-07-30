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

package uk.co.modularaudio.util.thread;

import uk.co.modularaudio.util.exception.DatastoreException;

public class ThreadUtils
{
//	private static Log log = LogFactory.getLog( ThreadUtils.class.getName() );

	public enum MAThreadPriority
	{
		IDLE,
		BACKGROUND,
		APPLICATION,
		REALTIME_SUPPORT,
		REALTIME,
		NUM_PRIORITIES
	};

	public static void setCurrentThreadPriority( final MAThreadPriority priority )
		throws DatastoreException
	{
		final int maxPriority = Thread.MAX_PRIORITY;
		final int minPriority = Thread.MIN_PRIORITY;

		final int priorityRange = maxPriority - minPriority;

		if( priorityRange < MAThreadPriority.NUM_PRIORITIES.ordinal() )
		{
			throw new DatastoreException("OS lacks appropriate priority granularity");
		}

		final float jpPerMaPInt = (priorityRange / (float)MAThreadPriority.NUM_PRIORITIES.ordinal());

		final int jPriority = (int)(minPriority + (jpPerMaPInt * priority.ordinal()));

		Thread.currentThread().setPriority( jPriority );
	}
}
