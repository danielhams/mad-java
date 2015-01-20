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

		synchronized (poolSemaphore)
		{
			// Before we call use, arbirate the pool size
			// We don't care what the return is.

			if (poolStructure.freeSize() > 0)
			{
				retResource = poolStructure.useFreeResource();
			}
			else
			{
				throw new ResourceNotAvailableException();
			}
		}
		// Do any arbitration we need before handing it back.
		ar = arbitratePostUse(retResource);

		if (ar != Arbiter.CONTINUE)
		{
			log.warn( "PostUse arbiter failure inside useResource");
			// If the resource failed arbitration, remove it
			this.removeResource(retResource);
			throw new ResourceNotAvailableException();
		}

		return (retResource);
	}

	public void releaseResource(Resource res)
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
			synchronized (poolSemaphore)
			{
				poolStructure.releaseUsedResource(res);
				poolSemaphore.notifyAll();
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
			this.removeResource(res);
			// If the resource failed arbitration, remove it.
			log.warn("(Pre/Post)Release arbitration of " + res.toString() + " failed.");
		}
	}
	
	public Resource useResourceWait() throws InterruptedException
	{
		return(useResourceWait(0));
	}

	public Resource useResourceWait( long waitTime) throws InterruptedException
	{
	    // Need a flag to indicate we should wait forever, or take our time.
	    boolean waitForever = (waitTime == 0 ? true : false);
	    boolean doneWaiting = false;
	    
		Resource retResource = null;
		
		while (retResource == null && !doneWaiting)
		{
			int ar = arbitratePreUse(retResource);

			synchronized (poolSemaphore)
			{
				// Before we call use, arbirate the pool size
				// We don't care what the return is.

				while (poolStructure.freeSize() < 1 && !doneWaiting)
				{
				    // If the user specified a maximum time to wait, we need to find out how long we waited for
				    // in this wait (since we can be woken up when there are no free resources).
				    long beforeWaitTime = 0;
				    if (!waitForever)
				        {
				        	beforeWaitTime = System.currentTimeMillis();
				        }
				    
					poolSemaphore.wait( waitTime );
					
					if (!waitForever && poolStructure.freeSize() < 1)
					{
					    long afterWaitTime = System.currentTimeMillis();
					    waitTime = waitTime - (afterWaitTime - beforeWaitTime);
					    if (waitTime <= 0)
					    {
					        // Its too late - the client has already waiting too long.
					        return(null);
					    }
					}
				}
				
			    retResource = poolStructure.useFreeResource();
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

	public void addResource(Resource res)
	{
		// Before we add it it, arbitrate its creation
		int ar = arbitrateCreation(res);

		if (ar == Arbiter.CONTINUE)
		{
			// Now look the pool, and add it into the structure.
			synchronized (poolSemaphore)
			{
				poolStructure.addResource(res);
				poolSemaphore.notifyAll();
			}
		}
		else
		{
			log.warn("Create arbitration of " + res.toString() + " failed.");
		}
	}

	public Resource removeAnyFreeResource() throws ResourceNotAvailableException
	{
        Resource retVal = null;
		synchronized (poolSemaphore)
		{
			if (poolStructure.freeSize() > 0)
			{
				retVal = poolStructure.removeAnyFreeResource();
				arbitrateRemoval( retVal );
			}
			else
			{
				throw new ResourceNotAvailableException();
			}
		}
        return( retVal );
	}

	public Resource removeResource(Resource res)
	{
        Resource retVal = null;
		//log.debug( "Remove resource called.");
		// Before we delete it, arbitrate its deletion
		int ar = arbitrateRemoval(res);
		if (ar == Arbiter.CONTINUE)
		{
			//log.debug( "Arbitrate returned continue");
			synchronized (poolSemaphore)
			{
			    retVal = poolStructure.removeResource(res);
			}
		}
		else
		{
			log.warn("Removal arbitration  of " + res.toString() + " failed.");
		}
        return( retVal );
	}

	public void addCreationArbiter(Arbiter arb)
	{
		creationArbiters.addLast(arb);
	}

	public void addPreUseArbiter(Arbiter arb)
	{
		preUseArbiters.addLast(arb);
	}

	public void addPostUseArbiter(Arbiter arb)
	{
		postUseArbiters.addLast(arb);
	}

	public void addPreReleaseArbiter(Arbiter arb)
	{
		preReleaseArbiters.addLast(arb);
	}

	public void addPostReleaseArbiter(Arbiter arb)
	{
		postReleaseArbiters.addLast(arb);
	}

	public void addExpirationArbiter(Arbiter arb)
	{
		expirationArbiters.addLast(arb);
	}

	public void addRemovalArbiter(Arbiter arb)
	{
		removalArbiters.addLast(arb);
	}

	public void shutdown()
	{
        log.trace("Pool shutdown commencing.");

		// First stop the threads.
		if (poolSizingThread != null)
		{
			//log.debug( "About to stop pool sizing thread.");
			poolSizingThread.halt();
			poolSizingThread.interrupt();
			try
			{
				poolSizingThread.join();
			}
			catch(InterruptedException ie)
			{
				log.error( "Error joining sizing thread: " + ie.toString());
			}
		}

		if (poolExpiryThread != null)
		{
			//log.debug( "About to stop pool expiry thread.");
			poolExpiryThread.halt();
 			poolExpiryThread.interrupt();
 			try
 			{
	 			poolExpiryThread.join();
			}
			catch(InterruptedException ie)
			{
				log.error( "Error joining sizing thread: " + ie.toString());
			}
		}

		synchronized (poolSemaphore)
		{

			// Get all resources in the pool structure, and
			// call removeResource on them.
			Collection<Resource> allRes = poolStructure.getAllResources();
			Iterator<Resource> iter = allRes.iterator();
			while (iter.hasNext())
			{
				Resource res = (Resource) iter.next();
				removeResource(res);
			}
			// We call a notify all on the pool semaphore to wake up any threads waiting
			// on obtaining a resource.
			poolSemaphore.notifyAll();
		}
		log.info( "Pool shutdown complete.");
	}

	public abstract void init() throws FactoryProductionException;

	public int arbitrateCreation(Resource res)
	
	{
		return (doArbitration(creationArbiters, res));
	}

	public int arbitratePreUse(Resource res)
	{
		return (doArbitration(preUseArbiters, res));
	}

	public int arbitratePostUse(Resource res)
	{
		return (doArbitration(postUseArbiters, res));
	}

	public int arbitratePreRelease(Resource res)
	{
		return (doArbitration(preReleaseArbiters, res));
	}

	public int arbitratePostRelease(Resource res)
	{
		return (doArbitration(postReleaseArbiters, res));
	}

	public int arbitrateExpiration(Resource res)
	{
		return (doArbitration(expirationArbiters, res));
	}

	public int arbitrateRemoval(Resource res)
	{
		return (doArbitration(removalArbiters, res));
	}

	public String toString()
	{
		String retString = "";
		synchronized (poolSemaphore)
		{
			retString = poolStructure.toString();
		}
		return (retString);
	}

	protected int doArbitration(LinkedList<Arbiter> arbiters, Resource res)
	{
		int retVal = Arbiter.CONTINUE;
		Iterator<Arbiter> iter = arbiters.iterator();
		// Now while we have more arbiters, and the return code is CONT
		// check em.
		while ((retVal == Arbiter.CONTINUE) && (iter.hasNext()))
		{
			Arbiter arb = (Arbiter) iter.next();
			retVal = arb.arbitrateOnResource(this, poolStructure, res);
		}
		return (retVal);
	}

	private LinkedList<Arbiter> creationArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> preUseArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> postUseArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> preReleaseArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> postReleaseArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> expirationArbiters = new LinkedList<Arbiter>();

	private LinkedList<Arbiter> removalArbiters = new LinkedList<Arbiter>();

    protected String poolName = null;
	protected PoolStructure poolStructure = null;
	protected PoolSizingThread poolSizingThread = null;
	protected Factory factory = null;
	protected Integer poolSemaphore = new Integer(0);
	protected PoolExpiryThread poolExpiryThread;
    
	/**
	 * Returns the factory.
	 * @return Factory
	 */
	public Factory getFactory()
	{
		return factory;
	}

    public String getName()
    {
        return poolName;
    }

}
