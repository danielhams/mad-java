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
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the base abstract class for any resource pool. It abstracts away
 * resource creation using the factory, and provides arbiters as a way to
 * 'plug in' different functionality at differing stages in the resources
 * lifetime and use.
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Factory
 * @see uk.co.modularaudio.util.pooling.common.Arbiter
 */
abstract public class Pool
{
	protected static Log log = LogFactory.getLog( Pool.class.getName() );

	public Resource useResource() throws ResourceNotAvailableException
	{
		Resource retResource = null;
		// Do any arbitration before getting one out
		int ar = arbitratePreUse(retResource);

		poolLock.lock();
		try
		{
			// Before we call use, arbirate the pool size
			// We don't care what the return is.

			if (structure.freeSize() > 0)
			{
				retResource = structure.useFreeResource();
			}
		}
		finally
		{
			poolLock.unlock();
		}
		if( retResource == null )
		{
			throw new ResourceNotAvailableException();
		}
		// Do any arbitration we need before handing it back.
		ar = arbitratePostUse(retResource);

		if (ar != Arbiter.CONTINUE)
		{
			log.warn( "PostUse arbiter failure inside useResource" );
			// If the resource failed arbitration, remove it
			this.removeResource(retResource);
			throw new ResourceNotAvailableException();
		}

		return retResource;
	}

	public void releaseResource(final Resource res)
	{
		//log.debug( "releaseResource called.");
		// Before we release it, arbitrate it
		boolean releaseFailure = false;
		int ar = arbitratePreRelease(res);

		if (ar != Arbiter.CONTINUE)
		{
			//log.debug( "prerelease arbiter failed.");
			releaseFailure = true;
		}
		else
		{
			poolLock.lock();
			try
			{
				structure.releaseUsedResource(res);
				notEmpty.signal();
			}
			finally
			{
				poolLock.unlock();
			}
			ar = arbitratePostRelease(res);

			if (ar != Arbiter.CONTINUE)
			{
				//log.debug( "postrelease arbiter failed");
				releaseFailure = true;
			}
		}

		if (releaseFailure)
		{
			// If the resource failed arbitration, remove it.
			this.removeResource(res);
			if( log.isWarnEnabled() )
			{
				log.warn("(Pre/Post)Release arbitration of " + res.toString() + " failed.");
			}
		}
	}

	public Resource useResourceWait() throws InterruptedException
	{
		return useResourceWait(0);
	}

	public Resource useResourceWait( final long iWaitTime) throws InterruptedException
	{
	    // Need a flag to indicate we should wait forever, or take our time.
		long waitTime = iWaitTime;
	    final boolean waitForever = (waitTime == 0 ? true : false);
	    final boolean doneWaiting = false;

		Resource retResource = null;

		while (retResource == null && !doneWaiting)
		{
			int ar = arbitratePreUse(retResource);

			poolLock.lock();
			try
			{
				// Before we call use, arbirate the pool size
				// We don't care what the return is.

				while (structure.freeSize() < 1 && !doneWaiting)
				{
				    // If the user specified a maximum time to wait, we need to find out how long we waited for
				    // in this wait (since we can be woken up when there are no free resources).
				    long beforeWaitTime = 0;
				    if (!waitForever)
				    {
				    	beforeWaitTime = System.currentTimeMillis();
				    }

				    notEmpty.await( waitTime, TimeUnit.MILLISECONDS );

					if (!waitForever && structure.freeSize() < 1)
					{
					    final long afterWaitTime = System.currentTimeMillis();
					    waitTime = waitTime - (afterWaitTime - beforeWaitTime);
					    if (waitTime <= 0)
					    {
					        // Its too late - the client has already waiting too long.
					        return null;
					    }
					}
				}

			    retResource = structure.useFreeResource();
			}
			finally
			{
				poolLock.unlock();
			}

			// Do any arbitration we need before handing it back.
			ar = arbitratePostUse(retResource);

			if (ar != Arbiter.CONTINUE)
			{
				log.warn("PostUse arbiter failure inside useResourceWait");

				// If the resource failed arbitration, remove it
				this.removeResource(retResource);
				retResource = null;
			}
		}
		return (retResource);
	}

	public void addResource(final Resource res)
	{
		// Before we add it it, arbitrate its creation
		final int ar = arbitrateCreation(res);

		if (ar == Arbiter.CONTINUE)
		{
			// Now look the pool, and add it into the structure.
			poolLock.lock();
			try
			{
				structure.addResource(res);
				notEmpty.signal();
			}
			finally
			{
				poolLock.unlock();
			}
		}
		else
		{
			if( log.isWarnEnabled() )
			{
				log.warn("Create arbitration of " + res.toString() + " failed.");
			}
		}
	}

	public Resource removeAnyFreeResource() throws ResourceNotAvailableException
	{
		Resource retVal = null;
		poolLock.lock();
		try
		{
			if (structure.freeSize() > 0)
			{
				retVal = structure.removeAnyFreeResource();
				arbitrateRemoval( retVal );
			}
		}
		finally
		{
			poolLock.unlock();
		}
		if( retVal == null )
		{
			throw new ResourceNotAvailableException();
		}
        return retVal;
	}

	public Resource removeResource(final Resource res)
	{
        Resource retVal = null;
		//log.debug( "Remove resource called.");
		// Before we delete it, arbitrate its deletion
		final int ar = arbitrateRemoval(res);
		if (ar == Arbiter.CONTINUE)
		{
			//log.debug( "Arbitrate returned continue");
			poolLock.lock();
			try
			{
			    retVal = structure.removeResource(res);
			}
			finally
			{
				poolLock.unlock();
			}
		}
		else
		{
			if( log.isWarnEnabled() )
			{
				log.warn("Removal arbitration  of " + res.toString() + " failed.");
			}
		}
        return retVal;
	}

	public void addCreationArbiter(final Arbiter arb)
	{
		creationArbiters.addLast(arb);
	}

	public void addPreUseArbiter(final Arbiter arb)
	{
		preUseArbiters.addLast(arb);
	}

	public void addPostUseArbiter(final Arbiter arb)
	{
		postUseArbiters.addLast(arb);
	}

	public void addPreReleaseArbiter(final Arbiter arb)
	{
		preReleaseArbiters.addLast(arb);
	}

	public void addPostReleaseArbiter(final Arbiter arb)
	{
		postReleaseArbiters.addLast(arb);
	}

	public void addExpirationArbiter(final Arbiter arb)
	{
		expirationArbiters.addLast(arb);
	}

	public void addRemovalArbiter(final Arbiter arb)
	{
		removalArbiters.addLast(arb);
	}

	public void shutdown()
	{
        log.trace("Pool shutdown commencing.");

		// First stop the threads.
		if (sizingThread != null)
		{
			//log.debug( "About to stop pool sizing thread.");
			sizingThread.halt();
			sizingThread.interrupt();
			try
			{
				sizingThread.join();
			}
			catch(final InterruptedException ie)
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Error joining sizing thread: " + ie.toString());
				}
			}
		}

		if (expiryThread != null)
		{
			//log.debug( "About to stop pool expiry thread.");
			expiryThread.halt();
 			expiryThread.interrupt();
 			try
 			{
	 			expiryThread.join();
			}
			catch(final InterruptedException ie)
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Error joining sizing thread: " + ie.toString());
				}
			}
		}

		poolLock.lock();
		try
		{

			// Get all resources in the pool structure, and
			// call removeResource on them.
			final Collection<Resource> allRes = structure.getAllResources();
			final Iterator<Resource> iter = allRes.iterator();
			while (iter.hasNext())
			{
				final Resource res = iter.next();
				removeResource(res);
			}
			// We call a notify all on the pool semaphore to wake up any threads waiting
			// on obtaining a resource.
			notEmpty.signal();
		}
		finally
		{
			poolLock.unlock();
		}
		log.info( "Pool shutdown complete.");
	}

	public abstract void init() throws FactoryProductionException;

	public int arbitrateCreation(final Resource res)

	{
		return (doArbitration(creationArbiters, res));
	}

	public int arbitratePreUse(final Resource res)
	{
		return (doArbitration(preUseArbiters, res));
	}

	public int arbitratePostUse(final Resource res)
	{
		return (doArbitration(postUseArbiters, res));
	}

	public int arbitratePreRelease(final Resource res)
	{
		return (doArbitration(preReleaseArbiters, res));
	}

	public int arbitratePostRelease(final Resource res)
	{
		return (doArbitration(postReleaseArbiters, res));
	}

	public int arbitrateExpiration(final Resource res)
	{
		return (doArbitration(expirationArbiters, res));
	}

	public int arbitrateRemoval(final Resource res)
	{
		return (doArbitration(removalArbiters, res));
	}

	@Override
	public String toString()
	{
		final String retString;
		poolLock.lock();
		try
		{
			retString = structure.toString();
		}
		finally
		{
			poolLock.unlock();
		}
		return (retString);
	}

	protected int doArbitration(final LinkedList<Arbiter> arbiters, final Resource res)
	{
		int retVal = Arbiter.CONTINUE;
		final Iterator<Arbiter> iter = arbiters.iterator();
		// Now while we have more arbiters, and the return code is CONT
		// check em.
		while ((retVal == Arbiter.CONTINUE) && (iter.hasNext()))
		{
			final Arbiter arb = iter.next();
			retVal = arb.arbitrateOnResource(this, structure, res);
		}
		return (retVal);
	}

	private final LinkedList<Arbiter> creationArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> preUseArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> postUseArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> preReleaseArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> postReleaseArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> expirationArbiters = new LinkedList<Arbiter>();

	private final LinkedList<Arbiter> removalArbiters = new LinkedList<Arbiter>();

	protected PoolStructure structure;
	protected PoolSizingThread sizingThread;
	protected Factory factory;
	protected ReentrantLock poolLock = new ReentrantLock();
	protected Condition notEmpty = poolLock.newCondition();
	protected PoolExpiryThread expiryThread;

	protected Pool( final PoolStructure structure,
			final long sizingCheckSleepMilliSeconds,
			final Factory factory,
			final long resourceCheckMilliSeconds )
	{
		this.structure = structure;
		this.factory = factory;

		if( sizingCheckSleepMilliSeconds > 0 )
		{
			sizingThread = new PoolSizingThread( factory, sizingCheckSleepMilliSeconds );
		}

		if( resourceCheckMilliSeconds > 0 )
		{
			expiryThread = new PoolExpiryThread( resourceCheckMilliSeconds );
		}
	}

	/**
	 * Returns the factory.
	 * @return Factory
	 */
	public Factory getFactory()
	{
		return factory;
	}
}
