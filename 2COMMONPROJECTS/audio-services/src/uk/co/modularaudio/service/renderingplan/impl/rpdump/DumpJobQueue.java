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

package uk.co.modularaudio.service.renderingplan.impl.rpdump;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

public class DumpJobQueue extends ArrayBlockingQueue<Runnable>
{
	/**
	 *
	 */
	private static final long serialVersionUID = -4860604642254737033L;

	public DumpJobQueue( final int capacity )
	{
		super( capacity, false );
	}

	public DumpJobQueue( final int capacity, final Collection<? extends Runnable> c)
	{
		super(capacity, false, c);
	}


	@SuppressWarnings("unchecked")
	public DumpJobQueue( final DumpJobQueue inQueue ) throws CloneNotSupportedException
	{
		super( inQueue.size(), false, (Collection<Runnable>)(inQueue.clone()) );
	}
}
