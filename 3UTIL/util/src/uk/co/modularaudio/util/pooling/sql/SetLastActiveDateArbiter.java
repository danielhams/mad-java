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
 * Set the last active date of the supplied (DBConnection)Resource.
 * </P>
 * <P>
 * This arbiter is used by the DatabaseConnectionPool before leasing a
 * DatabaseConnectionResource out to a client.
 * </P>
 *
 * @author D. Hams
 * @see Arbiter
 * @see DatabaseConnectionResource
 */
public class SetLastActiveDateArbiter implements Arbiter
{
	// private static Log log = LogFactory.getLog(
	// SetLastActiveDateArbiter.class.getName());

	/**
	 * <P>
	 * Empty constructor.
	 * </P>
	 */
	public SetLastActiveDateArbiter()
	{
	}

	/**
	 * <P>
	 * Set the last active date of the DatabaseConnectionResource.
	 * </P>
	 */
	@Override
	public int arbitrateOnResource( Pool pool, PoolStructure data, Resource resource )
	{
		// log.debug("Setting last active date.");
		DatabaseConnectionResource dbres = (DatabaseConnectionResource) resource;
		dbres.setLastActiveDate( new Date() );
		// log.debug("Set last active date.");
		return (Arbiter.CONTINUE);
	}

}
