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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.pooling.common.Arbiter;
import uk.co.modularaudio.util.pooling.common.Pool;
import uk.co.modularaudio.util.pooling.common.PoolStructure;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * <P>
 * Arbiter when a resource is removed from the pool.
 * </P>
 */
public class RemovalArbiter implements Arbiter
{
	protected static Log log = LogFactory.getLog( RemovalArbiter.class.getName() );

	public RemovalArbiter()
	{
	}

	/**
	 * <P>
	 * This arbiter is called before a resource is destroyed in the pool.
	 * </P>
	 * <P>
	 * It closes the java.sql.Connection.
	 * </P>
	 */
	@Override
	public int arbitrateOnResource( Pool pool, PoolStructure data, Resource res )
	{
		// Log.debug(className, "** Closing connection. **");
		DatabaseConnectionResource dbc = (DatabaseConnectionResource) res;
		Connection con = dbc.getConnection();
		try
		{
			// Log.debug(className, "Attempting to get lock");
			synchronized (con)
			{
				// Log.debug(className, "Got lock, attempting to close.");
				con.close();
				if (con.isClosed())
				{
					// Log.debug(className, "Successful in closing connection");
				}
				else
				{
					log.error( "Unable to close connection for some reason." );
				}
			}
		}
		catch (SQLException sqle)
		{
			log.error( "Unable to closed DB connection", sqle );
		}
		return (Arbiter.CONTINUE);
	}
}
