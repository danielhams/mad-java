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

public class ResettingCountingSemaphore
{
	protected static Log log = LogFactory.getLog( ResettingCountingSemaphore.class.getName() );
	
	protected int count;
	
	public ResettingCountingSemaphore( int numToStartWith )
	{
		this.count = ( numToStartWith < 0 ? 0 : numToStartWith );
	}
	
	public synchronized void acquire()
		throws SemaphoreShutdownException
	{
		while(count == 0)
		{
			try
			{
				wait();
			}
			catch(InterruptedException ie)
			{
			}
		}
		if( count == -1)
		{
			log.debug("Semaphore shutdown occuring.");
			throw new SemaphoreShutdownException("Semaphore has been notified of a shutdown.");
		}
		else
		{
			count = 0;
		}
	}
	
	public synchronized void release()
	{
		count++;
		notify();
	}
	
	public synchronized void shutdown()
	{
		log.debug("Shutdown called.");
		// Set the count to -1 to fall out of acquire, 
		// and then notify all waiting threads.
		count = -1;
		notifyAll();
	}
}
