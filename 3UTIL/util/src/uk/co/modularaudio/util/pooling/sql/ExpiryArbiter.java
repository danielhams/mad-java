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

package uk.co.modularaudio.util.pooling.sql;

import java.util.Date;

import uk.co.modularaudio.util.pooling.common.Arbiter;
import uk.co.modularaudio.util.pooling.common.Pool;
import uk.co.modularaudio.util.pooling.common.PoolStructure;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * <P>
 * Used to determine if a DatabaseConnectionResource lease has expired.
 * </P>
 */
public class ExpiryArbiter implements Arbiter
{
	public ExpiryArbiter( int secs )
	{
		this.expirySeconds = secs;
	}

	/**
	 * <P>
	 * Decide if the supplied (DBConnection)Resource lease has expired.
	 * </P>
	 * <P>
	 * The expiry date is used.
	 * </P>
	 */
	@Override
	public int arbitrateOnResource( Pool pool, PoolStructure data, Resource res )
	{
		int retVal = Arbiter.CONTINUE;

		if (this.expirySeconds == -1)
		{
			retVal = Arbiter.CONTINUE;
		}
		else
		{

			DatabaseConnectionResource dbres = (DatabaseConnectionResource) res;

			Date expiryDate = new Date();
			long secsSince = expiryDate.getTime();

			secsSince -= (1000 * expirySeconds);
			expiryDate.setTime( secsSince );

			Date activeDate = dbres.getLastActiveDate();

			// Log.debug(className, "LAD(" + activeDate.toString() + ")<(" +
			// expiryDate.toString() + ")");

			if (activeDate.before( expiryDate ))
			{
				retVal = Arbiter.FAIL;
			}
		}
		return (retVal);
	}

	private int expirySeconds = 0;
}
