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
import java.util.concurrent.locks.ReentrantLock;

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

	public PoolExpiryThread( final long milliSecondsToCheck )
	{
		this.setName("PoolExpiryThread");
		// The sleep uses thousandths of a second.
		this.milliSecondsToCheck = milliSecondsToCheck;
	}

	public void startExpiryThread( final Pool pool )
	{
		this.pool = pool;
		this.poolStructure = pool.structure;
		this.poolLock = pool.poolLock;
	}

	@Override
	public void run()
	{
		shouldHaltLock.lock();
		boolean localShouldHalt = shouldHalt;
		shouldHaltLock.unlock();

		while (!localShouldHalt)
		{
			// Lock the pool, and get back all resources in use
			Collection<Resource> usedResources = null;
			poolLock.lock();
			try
			{
				//Log.debug(className, "Expiry thread checking for expired resources.");
				usedResources = poolStructure.getAllBusyResources();
			}
			finally
			{
				poolLock.unlock();
			}

			final Iterator<Resource> iter = usedResources.iterator();
			while (iter.hasNext())
			{
				final Resource res = iter.next();

				// Now call all expiry arbiters - if an arbiter returns
				// FAIL, that means the resource has expired.
				poolLock.lock();
				try
				{
					final int whatToDo = pool.arbitrateExpiration(res);
					if (whatToDo == Arbiter.FAIL)
					{
						log.warn("Expiring a resource.");
						pool.removeResource(res);
					}
				}
				finally
				{
					poolLock.unlock();
				}
			}
			// Now go to sleep for a bit.
			try
			{
				Thread.sleep(milliSecondsToCheck);
			}
			catch (final InterruptedException ie)
			{
			}

			shouldHaltLock.lock();
			localShouldHalt = shouldHalt;
			shouldHaltLock.unlock();
		}
		log.info("PoolExpiryThread halting.");
	}

	public void halt()
	{
		// Make sure only one person writes to it at once.
		shouldHaltLock.lock();
		shouldHalt = true;
		shouldHaltLock.unlock();
	}

	private boolean shouldHalt = false;
	private final ReentrantLock shouldHaltLock = new ReentrantLock();
	private final long milliSecondsToCheck ;
	private ReentrantLock poolLock;
	private Pool pool;
	private PoolStructure poolStructure;
}
