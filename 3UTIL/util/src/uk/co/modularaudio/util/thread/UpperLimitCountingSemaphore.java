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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpperLimitCountingSemaphore extends CountingSemaphore
{
	private static Log log = LogFactory.getLog( UpperLimitCountingSemaphore.class.getName() );

	private int lowerBound = 0;
	private int upperBound = 0;

	public UpperLimitCountingSemaphore( final int numToStartWith, final int lowerBound, final int upperBound )
	{
		super( numToStartWith );
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public synchronized void acquire() throws SemaphoreShutdownException
	{
		while(count <= lowerBound )
		{
			try
			{
//				log.debug("Blocking aquire (consumer)");
				wait();
			}
			catch(final InterruptedException ie)
			{
			}
		}
		if( count == -1)
		{
			throw new SemaphoreShutdownException("Semaphore has been notified of a shutdown.");
		}
		else
		{
			count--;
			notifyAll();
		}
	}

	public synchronized void nonBlockingAcquire() throws SemaphoreShutdownException
	{
		if( count == -1)
		{
			throw new SemaphoreShutdownException("Semaphore has been notified of a shutdown.");
		}
		else	if( count <= lowerBound )
		{
			return;
		}
		else
		{
			count--;
			notifyAll();
		}
	}

	@Override
	public synchronized void release() throws SemaphoreShutdownException
	{
		while( count >= upperBound )
		{
			try
			{
//				log.debug("Blocking release (producer)");
				wait();
			}
			catch(final InterruptedException ie)
			{
			}
		}

		if( count == -1)
		{
			throw new SemaphoreShutdownException("Semaphore has been notified of a shutdown.");
		}
		else
		{
			count++;
			notifyAll();
		}
	}

	public synchronized void debug()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Count is currently: " + count );
		}
	}
}
