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

import java.util.concurrent.locks.ReentrantLock;


/**
 * <P>This is a re-usable arbiter for checking the size of the pool, and if necessary removing existing free resources.</P>
 * <P>For an example of its usage, check out the ExpiringDynamicStackPool where it is used.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Arbiter
 * @see uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool*/
public class RemoveResourcesArbiter implements Arbiter
{
//	protected static Log log = LogFactory.getLog(RemoveResourcesArbiter.class.getName());

	public RemoveResourcesArbiter(
		final int lowTide,
		final int allocationStep,
		final int minResources,
		final ReentrantLock poolLock )
	{
		this.lowTide = lowTide;
		this.allocationStep = allocationStep;
		this.minResources = minResources;
		this.poolLock = poolLock;
	}

	@Override
	public int arbitrateOnResource(final Pool pool, final PoolStructure data, final Resource res)
	{
        final IDynamicSizedPool dsp = (IDynamicSizedPool)pool;
		// Check the size of the pool
		poolLock.lock();
		try
		{
			boolean removeResources = false;
			final int freeSize = data.freeSize();
			final int busySize = data.busySize();

//            if (busySize == 10)
//            {
//                log.debug("Found all busy.");
//            }

//    		int totalSize = freeSize + busySize;
//    		log.debug( "RRA RemoveResourceArbiter arbitrate called.");
//    		log.debug( "RRA Free size is:" + freeSize);
//    		log.debug( "RRA Busy size is:" + busySize);
//    		log.debug( "RRA Total size is:" + totalSize);

    		// See what the thread is up to too
    		final int numberChange = dsp.getNumNeeded();
//    		log.debug( "RRA SizingThread num: " + numberChange);

    		// Check to see if we have stepped over our limits in the
    		// pool size, and add appropriately.
    		final int totalNumFree = freeSize + numberChange;

    		//log.debug( "RRA totalnumfree: " + totalNumFree);
    		final int totalWithChange = totalNumFree + busySize;

    		// Here check to see if the num free >= hightide (i.e. too many free)
    		// If so, check that taking away allocstep still gives us our min resources
    		// Special case - if minresources = 0 and totalnumfree = allocationstep
    		// then remove remaining resources.
    		if ((minResources == 0 && totalNumFree == allocationStep)
    			|| (( totalNumFree > allocationStep + lowTide)
    				&& (totalWithChange - allocationStep) >= minResources))
    		{
    			removeResources = true;
    		}

    		if (removeResources)
    		{
    			// Remove allocation step resources.
    		    //log.debug( "RRA asking for step removal on pool " + pool.getName());
    			dsp.addToNumNeeded( -allocationStep );
    		}
        }
		finally
		{
			poolLock.unlock();
		}

		return (Arbiter.CONTINUE);
	}

	private final int lowTide;
	private final int allocationStep;
	private final int minResources;
	private final ReentrantLock poolLock;
}
