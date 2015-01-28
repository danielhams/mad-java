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

package uk.co.modularaudio.util.pooling.forkexec.pool;

import uk.co.modularaudio.util.pooling.common.FactoryProductionException;
import uk.co.modularaudio.util.pooling.common.LifecycleStackPool;
import uk.co.modularaudio.util.pooling.common.ResourceNotAvailableException;

/**
 * @author dan
 *
 */
public class MultishotProcessPool extends LifecycleStackPool
{
	public MultishotProcessPool( final String[] cmdArray, final String outputTerminator, final int lowTide, final int highTide,
			final int allocationStep, final int minProcesses, final int maxProcesses, final int maxTimesUsed, final long sizingCheckSleepTime )
			throws FactoryProductionException
	{
		super( lowTide, highTide, allocationStep, minProcesses, maxProcesses, maxTimesUsed, sizingCheckSleepTime,
				new ProcessResourceFactory( cmdArray, outputTerminator ) );

		// Now add our multishotprocess specific arbiters
		final UpdateTimesUsedArbiter updateTimesUsedArbiter = new UpdateTimesUsedArbiter();
		this.addPreReleaseArbiter( updateTimesUsedArbiter );

		final NumTimesUsedArbiter checkTimesUsedArbiter = new NumTimesUsedArbiter( maxTimesUsed );
		this.addPreReleaseArbiter( checkTimesUsedArbiter );

		// Something to make sure that the processes are terminated and closed
		// when removed from the pool.
		final RemovalArbiter closeProcessArbiter = new RemovalArbiter();
		this.addRemovalArbiter( closeProcessArbiter );
		super.init();
	}

	public MultishotProcessResource leaseResource() throws ResourceNotAvailableException
	{
		return ((MultishotProcessResource) super.useResource());
	}

	public void returnResource( final MultishotProcessResource resource )
	{
		super.releaseResource( resource );
	}

	public MultishotProcessResource leaseResourceWait() throws InterruptedException
	{
		return ((MultishotProcessResource) super.useResourceWait());
	}
}
