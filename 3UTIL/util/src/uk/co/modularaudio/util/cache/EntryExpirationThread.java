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

package uk.co.modularaudio.util.cache;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dan
 *
 */
public class EntryExpirationThread extends Thread
{
	private static Log log = LogFactory.getLog( EntryExpirationThread.class.getName() );

	private boolean shouldHalt = false;
	private Integer jobsWaitingSemaphore = new Integer(0);
	private Integer cacheSemaphore = null;
	private DataCache cache = null;
	
	private long minutesBetweenChecks = 0;

	private long entryExpiryMinutes = 0;

	public EntryExpirationThread( Integer cacheSemaphore,
		DataCache cache,
		long minutesBetweenChecks,
		long entryExpiryMinutes)
	{
		this.cacheSemaphore = cacheSemaphore;
		this.cache = cache;
		this.minutesBetweenChecks = minutesBetweenChecks;
		this.entryExpiryMinutes = entryExpiryMinutes;
	}

	public void run()
	{
		//log.debug("Expiration thread starting.");
		try
		{
			boolean localShouldHalt = false;
			synchronized (jobsWaitingSemaphore)
			{
				localShouldHalt = shouldHalt;
			}

			// Here we loop around checking to see if we should be halted.
			while (!localShouldHalt)
			{
				java.util.Date testDate = new java.util.Date();
				long testTime = testDate.getTime();
				testTime = testTime - (entryExpiryMinutes * 60 * 1000);
				//testTime = testTime - (entryExpiryMinutes *  1000);
				testDate.setTime( testTime );

				//log.debug("Expiration thread examining items in the cache.");
				// Check the cache to get which entries are currently expired.
				synchronized(cacheSemaphore)
				{
					// Grab all elements in the cache
					Enumeration<Object> keys = cache.getCacheKeys();
					while(keys.hasMoreElements())
					{
						Object key = keys.nextElement();
						CacheEntry testEntry = cache.getCacheEntry( key );
						
						if (testEntry.getLastSetDate().before( testDate ) )
						{
							// Remove it
							//log.debug("Expiration thread expiring cache entry.");
							cache.removeEntryByKey( key );
						}
					}
				}
				
				//log.debug("Expiration thread going to sleep.");
				// Now sleep for a while
				try
				{
					Thread.sleep( minutesBetweenChecks * 60 * 1000 );
				}
				catch(InterruptedException ie)
				{
				}
				//Thread.currentThread().sleep( minutesBetweenChecks * 5 * 1000 );
				
				// Now retest the should halt variable
				synchronized(jobsWaitingSemaphore)
				{
					localShouldHalt = shouldHalt;
				}
			}
			//log.debug("EntryExpirationThread halting.");
			return;
		}
		catch (Exception e)
		{
			log.error(
				"Caught exception in entry expiration thread: " + e.toString(), e);
		}
	}
	
	public void halt()
	{
		synchronized (jobsWaitingSemaphore)
		{
			shouldHalt = true;
			jobsWaitingSemaphore.notifyAll();
		}
	}

}
