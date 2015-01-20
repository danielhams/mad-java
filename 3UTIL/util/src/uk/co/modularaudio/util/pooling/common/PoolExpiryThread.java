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

package uk.co.modularaudio.util.pooling.common;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <P>Seperate Runnable object that wakes every so often to check for expired resources.</P>
 * <P>Each resource that is currently busy in the pool is passed to the chain of expiry Arbiters to determine if the resource has expired.</P>
 * <P>An Arbiter returning the value of FAIL from arbitrateResource indicates that the resource has expired.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool
 * @see uk.co.modularaudio.util.pooling.common.Arbiter*/
public class PoolExpiryThread extends Thread
{
	protected static Log log = LogFactory.getLog(PoolExpiryThread.class.getName());

	public PoolExpiryThread(
		long milliSecondsToCheck,
		Integer poolSemaphore,
		Pool pool,
		PoolStructure poolStructure)
	{
		this.setName("PoolExpiryThread");
		// The sleep uses thousandths of a second.
		this.milliSecondsToCheck = milliSecondsToCheck;
		this.poolSemaphore = poolSemaphore;
		this.pool = pool;
		this.poolStructure = poolStructure;
	}

	public void run()
	{
		while (!shouldHalt)
		{
			// Lock the pool, and get back all resources in use
			Collection<Resource> usedResources = null;
			synchronized (poolSemaphore)
			{
				//Log.debug(className, "Expiry thread checking for expired resources.");
				usedResources = poolStructure.getAllBusyResources();
			}
			
			Iterator<Resource> iter = usedResources.iterator();
			while (iter.hasNext())
			{
				Resource res = (Resource) iter.next();

				// Now call all expiry arbiters - if an arbiter returns
				// FAIL, that means the resource has expired.

				synchronized(poolSemaphore)
				{
					int whatToDo = pool.arbitrateExpiration(res);
					if (whatToDo == Arbiter.FAIL)
					{
						log.warn("Expiring a resource.");
						pool.removeResource(res);
					}
				}
			}
			// Now go to sleep for a bit.
			try
			{
				Thread.sleep(milliSecondsToCheck);
			}
			catch (InterruptedException ie)
			{
			}
		}
		log.info("PoolExpiryThread halting.");
	}

	public void halt()
	{
		// Make sure only one person writes to it at once.
		synchronized (accessSemaphore)
		{
			shouldHalt = true;
		}
	}

	private boolean shouldHalt = false;
	private Integer accessSemaphore = new Integer(0);
	private long milliSecondsToCheck = 0;
	private Integer poolSemaphore = null;
	private Pool pool = null;
	private PoolStructure poolStructure = null;
}
