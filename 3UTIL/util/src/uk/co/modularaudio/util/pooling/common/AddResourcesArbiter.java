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
 * <P>This is a re-usable arbiter for checking the size of the pool, and if necessary creating new resources.</P>
 * <P>For an example of its usage, check out the ExpiringDynamicStackPool where it is used.</P>
 * @author dan
 * @version 1.0
 * @see uk.co.modularaudio.util.pooling.common.Arbiter
 * @see uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool*/
public class AddResourcesArbiter implements Arbiter
{
//    private static Log log = LogFactory.getLog( AddResourcesArbiter.class.getName());

	public AddResourcesArbiter(
		final int highTide,
		final int allocationStep,
		final int maxResources,
		final ReentrantLock poolLock )
	{
		this.highTide = highTide;
		this.allocationStep = allocationStep;
		this.maxResources = maxResources;
		this.poolLock = poolLock;
	}

	@Override
	public int arbitrateOnResource(final Pool pool, final PoolStructure data, final Resource res)
	{
        final IDynamicSizedPool dsp = (IDynamicSizedPool)pool;

		//log.debug("ARA called.");
		poolLock.lock();
		try
		{
			// Check the size of the pool
			boolean createNewResources = false;

			final int freeSize = data.freeSize();
			final int busySize = data.busySize();

//            if (busySize == 10)
//            {
//                log.debug("Found all busy.");
//            }

			//int totalSize = freeSize + busySize;
    		//log.debug( "ARA AddResourceArbiter arbitrate called.");
    		//log.debug( "ARA Free size is:" + freeSize);
    		//log.debug( "ARA Busy size is:" + busySize);
    		//log.debug( "ARA Total size is:" + totalSize);

    		final int numberChange = dsp.getNumNeeded();

    		//log.debug( "ARA SizingThread num: " + numberChange);

    		// Check to see if we have stepped over our limits in the
    		// pool size, and add appropriately.
    		final int totalNumFree = freeSize + numberChange;

    		//log.debug( "ARA totalnumfree: " + totalNumFree);
    		final int totalWithChange = totalNumFree + busySize;

    		if (totalNumFree < highTide
    			&& (totalWithChange + allocationStep) <= maxResources)
    		{
    			createNewResources = true;
    		}

    		if (createNewResources)
    		{
    		    //log.debug( "ARA asking for step allocate on " + pool.getName());
    			// Create allocation step resources.
    			dsp.addToNumNeeded( allocationStep );
    		}
        }
		finally
		{
			poolLock.unlock();
		}

		return (Arbiter.CONTINUE);
	}

	private final int highTide;
	private final int allocationStep;
	private final int maxResources;
	private final ReentrantLock poolLock;
}
