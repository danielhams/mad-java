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

import uk.co.modularaudio.util.pooling.common.Arbiter;
import uk.co.modularaudio.util.pooling.common.Pool;
import uk.co.modularaudio.util.pooling.common.PoolStructure;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * @author dan
 *
 */
public class NumTimesUsedArbiter implements Arbiter
{
	private final int maxUses;

	public NumTimesUsedArbiter( final int maxTimes )
	{
		this.maxUses = maxTimes;
	}

	/**
	 * @see uk.co.modularaudio.util.pooling.common.Arbiter#arbitrateOnResource(Pool,
	 *      PoolStructure, Resource)
	 */
	@Override
	public int arbitrateOnResource( final Pool pool, final PoolStructure data, final Resource res )
	{
		int retVal = Arbiter.CONTINUE;
		final MultishotProcessResource procRes = (MultishotProcessResource) res;
		if (procRes.getNumTimesUsed() > maxUses)
		{
			retVal = Arbiter.FAIL;
		}
		return retVal;
	}

}
