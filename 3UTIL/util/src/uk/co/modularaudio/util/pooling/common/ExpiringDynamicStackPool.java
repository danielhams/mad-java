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

	public ExpiringDynamicStackPool( final int lowTide,
		final int highTide,
		final int allocationStep,
		final int minResources,
		final int maxResources,
		final long resourceCheckMilliSeconds,
		final long sizingCheckSleepMilliSeconds,
		final Factory factory)
	{
		super( new StackPoolStructure(), sizingCheckSleepMilliSeconds, factory, resourceCheckMilliSeconds );
		assert( sizingCheckSleepMilliSeconds > 0 );
		assert( resourceCheckMilliSeconds > 0 );
		poolLock.lock();
		try
		{
			this.minResources = minResources;


			final Arbiter addArbiter =
				new AddResourcesArbiter(
					highTide,
					allocationStep,
					maxResources,
					poolLock );

			this.addPreUseArbiter(addArbiter);

			final Arbiter removeArbiter =
				new RemoveResourcesArbiter(
					lowTide,
					allocationStep,
					minResources,
					poolLock );

			this.addPostReleaseArbiter(removeArbiter);

			// Now add both these arbiters into the sizingArbiters list.
			this.addSizingArbiter( addArbiter );
			this.addSizingArbiter( removeArbiter );

			// Finally we need an arbiter to actually expire things. But since that
			// is resource specific, its left up to the implementation to add
			// one in.
		}
		finally
		{
			poolLock.unlock();
		}
	}

	@Override
	public void init() throws FactoryProductionException
	{
		poolLock.lock();
		try
		{
			// Set the factory up.
			factory.init();

			// Start off the threads for creation and expiry.
			//log.debug( "EDSP starting creation thread.");
			sizingThread.start();

			//log.debug( "EDSP starting expiry thread.");
			expiryThread.start();

			if (minResources != 0)
			{
				numNeeded = minResources;
			}
		}
		finally
		{
			poolLock.unlock();
		}
	}

	private int minResources = 0;

    protected int numNeeded = 0;

	private final LinkedList<Arbiter> sizingArbiters = new LinkedList<Arbiter>();

	@Override
	public void addSizingArbiter(final Arbiter arb)
	{
		sizingArbiters.addLast( arb );
	}

	@Override
	public int arbitrateSize(final Resource res)
	{
		return (doArbitration(sizingArbiters, res));
	}

	// Call the sizing arbiters when a resource is removed from the pool
	@Override
	public Resource removeResource( final Resource res )
	{
        final Resource retVal = super.removeResource( res );
        poolLock.lock();
        try
		{
        	notEmpty.notifyAll();
		}
        finally
        {
        	poolLock.unlock();
        }
		this.arbitrateSize( res );
		poolLock.lock();
		try
		{
			notEmpty.notifyAll();
		}
		finally
		{
			poolLock.unlock();
		}
        return( retVal );
	}

    @Override
	public void addToNumNeeded(final int numToAdd)
    {
        numNeeded += numToAdd;
    }

    @Override
	public int getNumNeeded()
    {
        return numNeeded;
    }

}
