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

package uk.co.modularaudio.util.pooling.dumb;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectPool<A>
{
	private static Log log = LogFactory.getLog( ObjectPool.class.getName() );

	public interface ObjectPoolLifecycleManager<A>
	{
		A createNewInstance();
		void resetInstanceForReuse( A objectToBeReused );
	}

	private final ObjectPoolLifecycleManager<A> lifecycleManager;

	private final Vector<A> freeObjects = new Vector<A>();
	private final Vector<A> usedObjects = new Vector<A>();
	private int maxAllocated;

	public ObjectPool( final ObjectPoolLifecycleManager<A> lifecycleManager, final int initialSize, final boolean prepopulate )
	{
		this.lifecycleManager = lifecycleManager;
		this.maxAllocated = initialSize;

		if( prepopulate )
		{
			for( int i = 0 ; i < initialSize ; i++ )
			{
				freeObjects.add( lifecycleManager.createNewInstance() );
			}
		}
//		debugPool( "init" );
	}

	public A reserveObject()
	{
		A retVal = null;

		final int numFree = freeObjects.size();
		if( numFree > 0 )
		{
			retVal = freeObjects.remove( 0 );
		}
		else
		{
			final int numUsed = usedObjects.size();
			if( numFree + numUsed < maxAllocated )
			{
				retVal = lifecycleManager.createNewInstance();
			}
		}

		if( retVal != null )
		{
			usedObjects.add( retVal );
		}
//		debugPool("PostUse");

		return retVal;
	}

	public void releaseObject( final A object )
	{
		usedObjects.remove( object );
		lifecycleManager.resetInstanceForReuse( object );
		final int numFree = freeObjects.size();
		final int numUsed = usedObjects.size();
		if( numFree + numUsed < maxAllocated )
		{
			freeObjects.add( object );
//			log.debug("Returned object to free pool ma(" + maxAllocated + ") nf(" + numFree +") nu(" + numUsed + ")");
		}
		else
		{
			// Remove it and let it expire
//			log.debug( "Removed and expired pooled object." );
		}
//		debugPool("PostRelease");
	}

	public int getNumUsed()
	{
		return usedObjects.size();
	}

	public int getNumFree()
	{
		return freeObjects.size();
	}

	public void resetMaxAllocated( final int newMaxAllocated )
	{
//		log.debug("Resetting max allocated. Was " + maxAllocated + " will be " + newMaxAllocated );
		maxAllocated = newMaxAllocated;
	}

	public void debugPool( final String op )
	{
		final int numUsed = getNumUsed();
		final int numFree = getNumFree();
		if( log.isDebugEnabled() )
		{
			log.debug("POOL SIZE OP(" + op + ") : " + (numFree + numUsed) );
			log.debug("Pool used: " + numUsed );
			log.debug("Pool free: " + numFree );
		}
	}

	public void removeObject( final A objectToRemove )
	{
		usedObjects.remove( objectToRemove );
	}
}
