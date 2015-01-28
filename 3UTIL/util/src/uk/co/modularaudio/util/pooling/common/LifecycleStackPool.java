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

/**
 * <P>A resource pool that has lower bounds for the number of resources that should be available in it, and upper bounds on the total resource utilisation.</P>
 * <P>The pool utilises a seperate thread for the creation of resources.</P>
 * <P>When a resource is used more than maxResourceUses times, it is destroyed, and a replacement is created.</P>
 * <P>Each stage of the resource life cycle is programmable by adding Arbiters to the appropriate stage chain.</P>
 * <P>The LifecycleStackPool utilises a Stack as the internal data structure
 * for resource tracking. That means LIFO for used/free resources.</P>
 * @author dan
 * @version 1.0
 * @see Pool
 * @see Resource
 * @see ResourceNotAvailableException
 * @see StackPoolStructure
 * @see Factory
 * @see Arbiter
 * @see AddResourcesArbiter
 * @see RemoveResourcesArbiter
 **/
public class LifecycleStackPool
	extends Pool
	implements IDynamicSizedPool
{
    public LifecycleStackPool( final int lowTide,
        final int highTide,
        final int allocationStep,
        final int minResources,
        final int maxResources,
        final int maxResourceUses,
        final long sizingCheckSleepMilliSeconds,
        final Factory factory)
    {
    	super( new StackPoolStructure(), sizingCheckSleepMilliSeconds, factory, 0 );
    	poolLock.lock();
    	try
        {
            this.minResources = minResources;
            this.factory = factory;

            // Pop in the resource creation thread. This thread sits waiting for
            // things to tell it to create new ones. We will use arbiters to
            // actually call the creation thread.

            // This arbiter is called when we need to (maybe) add new reasources
            // into the pool. It will check the free size of the pool (+ any
            // outstanding creates in the creation thread), and ask the creation
            // thread to create any more that are needed.
            // Its called just before attempting to get a resource out of
            // the pool.
            final Arbiter addArbiter =
                new AddResourcesArbiter(
                    highTide,
                    allocationStep,
                    maxResources,
                    poolLock );

            this.addPreUseArbiter(addArbiter);

            // Now an arbiter to remove resources when there are too many.
            // This is called just after releasing a resource.
            final Arbiter removeResourcesArbiter =
                new RemoveResourcesArbiter(
                    lowTide,
                    allocationStep,
                    minResources,
                    poolLock);

            this.addPostReleaseArbiter(removeResourcesArbiter);

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
            sizingThread.startSizingThread( this, poolLock, notEmpty );

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

    @Override
	public void addSizingArbiter( final Arbiter arb )
    {
        // Do nothing.
    }

    @Override
	public int arbitrateSize( final Resource res )
    {
        return Arbiter.FAIL;
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
