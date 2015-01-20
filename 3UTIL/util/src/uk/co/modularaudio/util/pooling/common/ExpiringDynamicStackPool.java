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

import java.util.LinkedList;

/**
 * <P>A resource pool that has lower bounds for the number of resources that should be available in it, and upper bounds on the total resource utilisation.</P>
 * <P>The pool utilises a seperate thread for the creation of resources, and has a thread that periodically checks to see if a resource has expired.</P>
 * <P>Each stage of the resource life cycle is programmable by adding Arbiters to the appropriate stage chain.</P>
 * <P>The ExpiringDynamicStackPool utilises a Stack as the internal data structure
 * for resource tracking. That means LIFO for used/free resources.</P>
 * <P>An example database connection pool is given in the test subpackage.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Pool
 * @see uk.co.modularaudio.util.pooling.common.Resource
 * @see uk.co.modularaudio.util.pooling.common.ResourceNotAvailableException
 * @see uk.co.modularaudio.util.pooling.common.StackPoolStructure
 * @see uk.co.modularaudio.util.pooling.common.Factory
 * @see uk.co.modularaudio.util.pooling.common.Arbiter
 * @see uk.co.modularaudio.util.pooling.common.AddResourcesArbiter
 * @see uk.co.modularaudio.util.pooling.common.RemoveResourcesArbiter*/
public class ExpiringDynamicStackPool 
	extends Pool
	implements IDynamicSizedPool
{
//	private static Log log = LogFactory.getLog(ExpiringDynamicStackPool.class.getName());
	
	public ExpiringDynamicStackPool(
		int lowTide,
		int highTide,
		int allocationStep,
		int minResources,
		int maxResources,
		long resourceCheckMilliSeconds,
		long sizingCheckSleepMilliSeconds,
		Factory factory)
	{
		synchronized (poolSemaphore)
		{
			// Use a stack structure to store our resources. This makes recently
			// used resources the ones that are used next.
			//log.debug( "EDSP creating stack pool structure");
			poolStructure = new StackPoolStructure();

//			this.lowTide = lowTide;
//			this.highTide = highTide;
//			this.allocationStep = allocationStep;
			this.minResources = minResources;
//			this.maxResources = maxResources;
//			this.resourceCheckMilliSeconds = resourceCheckMilliSeconds;
			this.factory = factory;
//			this.sizingCheckSleepMilliSeconds = sizingCheckSleepMilliSeconds;

			// Pop in the resource creation thread. This thread sits waiting for
			// things to tell it to create new ones. We will use arbiters to
			// actually call the creation thread.
			//log.debug( "EDSP creating resource creation thread");
			poolSizingThread = new PoolSizingThread(this, this.poolSemaphore, factory, sizingCheckSleepMilliSeconds);

			// Create an expiry thread. This thread looks to see if any resources
			// have expired by calling the expiry arbiters on all the busy
			// resources. If the arbiters say FAIL, then the expiry thread removes
			// them from the pool.
			poolExpiryThread =
				new PoolExpiryThread(
					resourceCheckMilliSeconds,
					poolSemaphore,
					this,
					poolStructure);

			// This arbiter is called when we need to (maybe) add new reasources
			// into the pool. It will check the free size of the pool (+ any 
			// outstanding creates in the creation thread), and ask the creation
			// thread to create any more that are needed.
			// Its called just before attempting to get a resource out of
			// the pool.
			//log.debug( "EDSP creating add resources arbiter.");
			Arbiter addArbiter =
				new AddResourcesArbiter(
					lowTide,
					highTide,
					allocationStep,
					minResources,
					maxResources,
					poolSizingThread,
					poolSemaphore);

			this.addPreUseArbiter(addArbiter);

			// Now an arbiter to remove resources when there are too many.
			// This is called just after releasing a resource.
			//log.debug( "EDSP creating remove resources arbiter.");
			Arbiter removeArbiter =
				new RemoveResourcesArbiter(
					lowTide,
					highTide,
					allocationStep,
					minResources,
					maxResources,
					poolSizingThread,
					poolSemaphore);

			this.addPostReleaseArbiter(removeArbiter);
			
			// Now add both these arbiters into the sizingArbiters list.
			this.addSizingArbiter( addArbiter );
			this.addSizingArbiter( removeArbiter );

			// Finally we need an arbiter to actually expire things. But since that
			// is resource specific, its left up to the implementation to add
			// one in.
		}
	}

	public void init() throws FactoryProductionException
	{
		synchronized (poolSemaphore)
		{
			// Set the factory up.
			factory.init();

			// Start off the threads for creation and expiry.
			//log.debug( "EDSP starting creation thread.");
			poolSizingThread.start();
			
			//log.debug( "EDSP starting expiry thread.");
			poolExpiryThread.start();
			
			if (minResources != 0)
			{
				numNeeded = minResources;
			}
		}
	}

//	private int lowTide = 0;
//	private int highTide = 0;
//	private int allocationStep = 0;
	private int minResources = 0;
//	private int maxResources = 0;
//	private long resourceCheckMilliSeconds = 0;
//	private long sizingCheckSleepMilliSeconds = 0;
	
    protected int numNeeded = 0;
    
	private LinkedList<Arbiter> sizingArbiters = new LinkedList<Arbiter>();
	
	public void addSizingArbiter(Arbiter arb)
	{
		sizingArbiters.addLast( arb );
	}
	
	public int arbitrateSize(Resource res)
	{
		return (doArbitration(sizingArbiters, res));
	}
	
	// Call the sizing arbiters when a resource is removed from the pool
	public Resource removeResource( Resource res )
	{
        Resource retVal = super.removeResource( res );
		synchronized(poolSemaphore)
		{
			this.poolSemaphore.notifyAll();
		}
		this.arbitrateSize( res );
		synchronized(poolSemaphore)
		{
			this.poolSemaphore.notifyAll();
		}
        return( retVal );
	}
    
    public void addToNumNeeded(int numToAdd)
    {
        numNeeded += numToAdd;
    }

    public int getNumNeeded()
    {
        return numNeeded;
    }

}
