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

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.pooling.common.Arbiter;
import uk.co.modularaudio.util.pooling.common.Pool;
import uk.co.modularaudio.util.pooling.common.PoolStructure;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * <P>
 * Given the supplied (DBConnection)Resource, check if the connection is closed
 * or null.
 * </P>
 * <P>
 * If the connection is closed or null, this Arbiter returns Arbiter.FAIL.
 * </P>
 *
 * @author D. Hams
 * @see Arbiter
 * @see DatabaseConnectionPool
 */
public class ConnectionTesterArbiter implements Arbiter
{
	private static Log log = LogFactory.getLog( ConnectionTesterArbiter.class.getName() );

	/**
	 * <P>
	 * Empty constructor.
	 * </P>
	 */
	public ConnectionTesterArbiter()
	{
	}

	/**
	 * <P>
	 * Decide if the connection is still valid or not.
	 * </P>
	 *
	 * @param pool
	 * @param data
	 * @param resource
	 * @see Arbiter
	 */
	@Override
	public int arbitrateOnResource( final Pool pool, final PoolStructure data, final Resource resource )
	{
		// log.debug("Connection tester arbiter called.");
		int retVal = Arbiter.FAIL;

		final DatabaseConnectionResource dbres = (DatabaseConnectionResource) resource;
		try
		{
			if (dbres != null && dbres.isClosed() == false)
			{

				// Do a select from DUAL to see if things are still up
				// (BADUM is meant to be a heartbeat :-)
				final String testSql = "SELECT 'BADUM' FROM DUAL";
				// log.debug("about to run");
				final Statement stat = dbres.createStatement();
				if (stat.execute( testSql ))
				{
					retVal = Arbiter.CONTINUE;
				}

				// log.debug("run");
				stat.close();
				// log.debug("Connection tester ran.");

				retVal = Arbiter.CONTINUE;
			}
		}
		catch (final SQLException sqle)
		{
			if( log.isWarnEnabled() )
			{
				log.warn( "SQLException caught testing DB connection: " + sqle.toString() );
			}
		}

		if (retVal == Arbiter.FAIL)
		{
			log.warn( "Database connection test failed." );
		}

		// log.debug("Connection tester arbiter done.");
		return (retVal);
	}
}
