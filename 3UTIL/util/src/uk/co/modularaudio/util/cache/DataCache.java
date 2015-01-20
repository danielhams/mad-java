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
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dan
 *
 */
public class DataCache
{
	private static Log log = LogFactory.getLog( DataCache.class.getName() );

	private Hashtable<Object,CacheEntry> cache = new Hashtable<Object,CacheEntry>();

	private Integer cacheSemaphore = new Integer(0);
	private long cacheTimeoutMinutes = -1;
	private long minutesBetweenChecks = -1;

	private EntryExpirationThread expirationThread = null;

	public DataCache(long cacheTimeoutMinutes, long minutesBetweenChecks)
	{
		this.cacheTimeoutMinutes = cacheTimeoutMinutes;
		this.minutesBetweenChecks = minutesBetweenChecks;
	}

	/**
	 * @return
	 */
	public long getCacheTimeoutMinutes()
	{
		return cacheTimeoutMinutes;
	}

	public CacheEntry getCacheEntry(Object key)
		throws NoSuchCacheEntryException
	{

		CacheEntry cacheEntry = null;

		synchronized(cacheSemaphore)
		{
			cacheEntry = cache.get( key );
		}

		if (cacheEntry == null)
		{
			throw new NoSuchCacheEntryException("Key not found in cache");
		}
		else
		{
			return(cacheEntry);
		}

	}

	public void touchCacheEntry(Object key)
		throws NoSuchCacheEntryException
	{
		CacheEntry cacheEntry = getCacheEntry(key);

		cacheEntry.setLastSetDate( new java.util.Date());
	}

	public Object getCacheEntryValue(Object key)
		throws NoSuchCacheEntryException
	{
		Object retVal = null;
		CacheEntry cacheEntry = this.getCacheEntry( key );
		retVal = cacheEntry.getValue();
		return(retVal);
	}

	public void setCacheEntry(Object key, Object value)
	{
		// Look for an existing entry - if it exists, update that, otherwise add a new one.
		CacheEntry newCacheEntry = new CacheEntry(value, new java.util.Date());

		synchronized(cacheSemaphore)
		{
//			CacheEntry existingEntry = (CacheEntry)cache.get( key );
			cache.put( key, newCacheEntry);
//			existingEntry = null;

		}
	}

	protected CacheEntry removeEntryByKey(Object key)
		throws NoSuchCacheEntryException
	{
		CacheEntry retVal = null;
		synchronized(cacheSemaphore)
		{
			retVal = cache.get( key );
			if (retVal != null)
			{
				cache.remove( key );
			}
			else
			{
				throw new NoSuchCacheEntryException("Cache Entry key not found.");
			}
		}
		return(retVal);
	}

	protected CacheEntry removeEntryByValue(Object value)
		throws NoSuchCacheEntryException
	{
		boolean foundEntry = false;
		CacheEntry retVal = null;
		synchronized(cacheSemaphore)
		{
			Enumeration<Object> keys = cache.keys();
			while(keys.hasMoreElements())
			{
				Object key = keys.nextElement();
				CacheEntry entry = cache.get( key );
				if (entry.getValue().equals( value ))
				{
					cache.remove( key );
					foundEntry = true;
				}
			}
		}
		if (!foundEntry)
		{
			throw new NoSuchCacheEntryException("Cache Entry key not found.");
		}
		return(retVal);
	}

	protected Enumeration<Object> getCacheKeys()
	{
		Enumeration<Object> retVal = null;
		synchronized(cacheSemaphore)
		{
			retVal = cache.keys();
		}
		return(retVal);
	}

	public void init()
	{
		synchronized(cacheSemaphore)
		{
			// Start the thread that check and expires cache entries older than a certain date
			if (expirationThread == null)
			{
				log.debug("Starting the datacache expiry thread.");
				expirationThread = new EntryExpirationThread( cacheSemaphore, this, cacheTimeoutMinutes, minutesBetweenChecks);
				expirationThread.start();
			}
		}
	}

	public void shutdown()
	{
		synchronized(cacheSemaphore)
		{
			// Stop the expiration thread and clean the cache
			if (expirationThread != null && expirationThread.isAlive())
			{
				log.debug("Halting the datacache expiry thread.");
				expirationThread.halt();
				expirationThread.interrupt();
				try
				{
					expirationThread.join();
				}
				catch(InterruptedException ie)
				{
				}
				this.expirationThread = null;
			}
		}
	}

	/**
	 * @return
	 */
	public long getMinutesBetweenChecks()
	{
		return minutesBetweenChecks;
	}

}
