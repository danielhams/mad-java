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
    public LifecycleStackPool(
        int lowTide,
        int highTide,
        int allocationStep,
        int minResources,
        int maxResources,
        int maxResourceUses,
        long sizingCheckSleepMilliSeconds,
        Factory factory)
    {
        synchronized (poolSemaphore)
        {
            // Use a stack structure to store our resources. This makes recently
            // used resources the ones that are used next.
            poolStructure = new StackPoolStructure();

//            this.lowTide = lowTide;
//            this.highTide = highTide;
//            this.allocationStep = allocationStep;
            this.minResources = minResources;
//            this.maxResources = maxResources;
//            this.maxResourceUses = maxResourceUses;
            this.factory = factory;

            // Pop in the resource creation thread. This thread sits waiting for
            // things to tell it to create new ones. We will use arbiters to
            // actually call the creation thread.
            poolSizingThread = new PoolSizingThread(this, this.poolSemaphore, factory, sizingCheckSleepMilliSeconds);

            // This arbiter is called when we need to (maybe) add new reasources
            // into the pool. It will check the free size of the pool (+ any
            // outstanding creates in the creation thread), and ask the creation
            // thread to create any more that are needed.
            // Its called just before attempting to get a resource out of
            // the pool.
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
            Arbiter removeResourcesArbiter =
                new RemoveResourcesArbiter(
                    lowTide,
                    highTide,
                    allocationStep,
                    minResources,
                    maxResources,
                    poolSizingThread,
                    poolSemaphore);

            this.addPostReleaseArbiter(removeResourcesArbiter);

        }
    }

    @Override
	public void init() throws FactoryProductionException
    {
        synchronized (poolSemaphore)
        {
            // Set the factory up.
            factory.init();

            // Start off the threads for creation and expiry.
            Thread pst = new Thread(poolSizingThread);
            pst.start();

            if (minResources != 0)
            {
                numNeeded = minResources;
            }
        }
    }

//    private int lowTide = 0;
//    private int highTide = 0;
//    private int allocationStep = 0;
    private int minResources = 0;
//    private int maxResources = 0;
//    private int maxResourceUses = 0;

    protected int numNeeded = 0;

    @Override
	public void addSizingArbiter( Arbiter arb )
    {
        // Do nothing.
    }

    @Override
	public int arbitrateSize( Resource res )
    {
        return Arbiter.FAIL;
    }

    @Override
	public void addToNumNeeded(int numToAdd)
    {
        numNeeded += numToAdd;
    }

    @Override
	public int getNumNeeded()
    {
        return numNeeded;
    }
}
