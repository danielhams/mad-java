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

import uk.co.modularaudio.util.pooling.common.Arbiter;
import uk.co.modularaudio.util.pooling.common.ExpiringDynamicStackPool;
import uk.co.modularaudio.util.pooling.common.FactoryProductionException;
import uk.co.modularaudio.util.pooling.common.Resource;
import uk.co.modularaudio.util.pooling.common.ResourceNotAvailableException;

/**
 * <P>
 * This is an implementation of a database connection pool using the
 * ExpiringDynamicStackPool.
 * </P>
 * <P>
 * The DatabaseConnectionPool uses arbiters to perform all database connection
 * specific logic during adding/removing/creation/destruction of connections.
 * </P>
 * <P>
 * The Connection Pool has the following features:
 * <UL>
 * <LI>Dynamic sizing using step-sizes and high/low tide markings.
 * <LI>Testing the connection is valid both before releasing to a client, and
 * before putting a freed connection back into the pool.
 * <LI>Connections may have a time limit set on them.This expiry causes the pool
 * to close connections that are not given back to the pool after some
 * configured time.
 * <LI>Callers to the connection pool may chose either a blocking or
 * non-blocking wait for a connection.
 * </UL>
 *
 * @author D.Hams
 * @version 1.0
 * @see ExpiringDynamicStackPool
 */
public class DatabaseConnectionPool extends ExpiringDynamicStackPool
{
	/**
	 * <P>
	 * Create the connection pool using the supplied parameters for pool size,
	 * expiry time, and the db connection itself.
	 * </P>
	 * <P>
	 * Additonally, the caller can specify if the connection is to be tested,
	 * and when the test occurs.
	 * </P>
	 *
	 * @param classname
	 * @param connectionURL
	 * @param username
	 * @param password
	 * @param lowTide
	 * @param highTide
	 * @param allocationStep
	 * @param minCons
	 * @param maxCons
	 * @param connectionLifetime
	 * @param expiryCheckMilliSeconds
	 * @param sizingCheckSleepMillis
	 * @throws FactoryProductionException
	 * @see ExpiringDynamicStackPool
	 * @see DatabaseConnectionResource
	 * @see ConnectionFactory
	 * @see ExpiryArbiter
	 * @see RemovalArbiter
	 * @see ConnectionTesterArbiter
	 * @see SetLastActiveDateArbiter
	 **/
	public DatabaseConnectionPool( final String classname, final String connectionURL, final String username, final String password,
			final int lowTide, final int highTide, final int allocationStep, final int minCons, final int maxCons, final int connectionLifetime,
			final long expiryCheckMilliSeconds, final long sizingCheckSleepMillis ) throws FactoryProductionException
	{
		// Call our superclass with the appropriate constructors
		super( lowTide, highTide, allocationStep, minCons, maxCons, expiryCheckMilliSeconds, sizingCheckSleepMillis,
		// Check every n seconds for expired connections
				new ConnectionFactory( classname, connectionURL, username, password ) );

		setupArbiters( connectionLifetime, true, false );

		// Now call init to see if all is well
		// Log.debug(className, "Calling pool init.");
		super.init();
	}

	/**
	 * <P>
	 * Create the connection pool using the supplied parameters for pool size,
	 * expiry time, and the db connection itself.
	 * </P>
	 *
	 * @param classname
	 * @param connectionURL
	 * @param username
	 * @param password
	 * @param lowTide
	 * @param highTide
	 * @param allocationStep
	 * @param minCons
	 * @param maxCons
	 * @param connectionLifetime
	 * @param expiryCheckMilliSeconds
	 * @param sizingCheckSleepMillis
	 * @param testConnectionBeforeUse
	 * @param testConnectionAfterUse
	 * @throws FactoryProductionException
	 * @see ExpiringDynamicStackPool
	 * @see DatabaseConnectionResource
	 * @see ConnectionFactory
	 * @see ExpiryArbiter
	 * @see RemovalArbiter
	 * @see ConnectionTesterArbiter
	 * @see SetLastActiveDateArbiter
	 **/
	public DatabaseConnectionPool( final String classname, final String connectionURL, final String username, final String password,
			final int lowTide, final int highTide, final int allocationStep, final int minCons, final int maxCons, final int connectionLifetime,
			final long expiryCheckMilliSeconds, final long sizingCheckSleepMillis, final boolean testConnectionBeforeUse,
			final boolean testConnectionAfterUse ) throws FactoryProductionException
	{
		// Call our superclass with the appropriate constructors
		super( lowTide, highTide, allocationStep, minCons, maxCons, expiryCheckMilliSeconds, sizingCheckSleepMillis,
		// Check every n seconds for expired connections
				new ConnectionFactory( classname, connectionURL, username, password ) );

		setupArbiters( connectionLifetime, testConnectionBeforeUse, testConnectionAfterUse );

		// Now call init to see if all is well
		// Log.debug(className, "Calling pool init.");
		super.init();
	}

	/**
	 * @param icString
	 * @param lowTide
	 * @param highTide
	 * @param allocationStep
	 * @param minCons
	 * @param maxCons
	 * @param connectionLifetime
	 * @param expiryCheckMilliSeconds
	 * @param sizingCheckSleepMillis
	 * @throws FactoryProductionException
	 */
	public DatabaseConnectionPool( final String icString, final int lowTide, final int highTide, final int allocationStep, final int minCons,
			final int maxCons, final int connectionLifetime, final long expiryCheckMilliSeconds, final long sizingCheckSleepMillis )
			throws FactoryProductionException
	{
		// Call our superclass with the appropriate constructors
		super( lowTide, highTide, allocationStep, minCons, maxCons, expiryCheckMilliSeconds, sizingCheckSleepMillis,
		// Check every n seconds for expired connections
				new JNDIConnectionFactory( icString ) );

		setupArbiters( connectionLifetime, true, false );

		// Now call init to see if all is well
		// Log.debug(className, "Calling pool init.");
		super.init();
	}

	protected void setupArbiters( final int connectionLifetime, final boolean testConnectionBeforeUse,
			final boolean testConnectionAfterUse )
	{
		// Add an arbiter that sets the last active date to now when the
		// resource is 'used' from the pool
		super.addPostUseArbiter( new SetLastActiveDateArbiter() );

		// Now add in an arbiter that examines the DatabaseConnectionResource to
		// see if its last used date is older than 'connectionLifetime'
		super.addExpirationArbiter( new ExpiryArbiter( connectionLifetime ) );

		// Add in a removal arbiter to close the connections
		super.addRemovalArbiter( new RemovalArbiter() );

		Arbiter connectionTester = null;

		if (testConnectionBeforeUse || testConnectionAfterUse)
		{
			connectionTester = new ConnectionTesterArbiter();
		}

		if (testConnectionBeforeUse)
		{
			super.addPostUseArbiter( connectionTester );
		}

		if (testConnectionAfterUse)
		{
			super.addPreReleaseArbiter( connectionTester );
		}
	}

	// Some wrapper functions to do casting
	/**
	 * <P>
	 * Perform a blocking wait on getting a database connection.
	 * </P>
	 * <P>
	 * The interrupted exception is thrown when the calling thread is
	 * interrupted inside the wait on the pool condition variable.
	 * </P>
	 * <P>
	 * It is just a wrapper casting function for the pools useResourceWait()
	 * call.
	 * </P>
	 *
	 * @return DatabaseConnectionResource
	 * @see DatabaseConnectionResource
	 * @exception InterruptedException
	 */
	public DatabaseConnectionResource getConnectionWait() throws InterruptedException
	{
		return ((DatabaseConnectionResource) useResourceWait());
	}

	/**
	 * <P>
	 * Perform a blocking wait on getting a database connection.
	 * </P>
	 * <P>
	 * A timeout can be provided after which the call will return.
	 * </P>
	 * <P>
	 * The interrupted exception is thrown when the calling thread is
	 * interrupted inside the wait on the pool condition variable.
	 * </P>
	 * <P>
	 * It is just a wrapper casting function for the pools useResourceWait()
	 * call.
	 * </P>
	 *
	 * @param timeout
	 * @return DatabaseConnectionResource
	 * @see DatabaseConnectionResource
	 * @throws InterruptedException
	 */
	public DatabaseConnectionResource getConnectionWait( final long timeout ) throws InterruptedException
	{
		return ((DatabaseConnectionResource) useResourceWait( timeout ));
	}

	/**
	 * <P>
	 * Attempt to get a database connection from the pool, but do not wait.
	 * </P>
	 * <P>
	 * Simply a wrapper casting call to the pools useResource() function.
	 * </P>
	 *
	 * @return DatabaseConnectionResource
	 * @see DatabaseConnectionResource
	 */
	public DatabaseConnectionResource getConnection() throws ResourceNotAvailableException
	{
		return ((DatabaseConnectionResource) useResource());
	}

	/**
	 * <P>
	 * Return a database connection back into the connection pool to be re-used.
	 * </P>
	 * <P>
	 * Simply a wrapper function on the pools releaseResource method.
	 * </P>
	 *
	 * @param resource
	 * @see DatabaseConnectionResource
	 */
	public void releaseConnection( final DatabaseConnectionResource resource )
	{
		releaseResource( resource );
	}

	/**
	 * <P>
	 * Add a new resource into the pool for use
	 * </P>
	 *
	 * @see uk.co.modularaudio.util.pooling.common.Pool#addResource(uk.co.modularaudio.util.pooling.common.Resource)
	 */
	@Override
	public void addResource( final Resource resource )
	{
		if (resource instanceof DatabaseConnectionResource)
		{
			final DatabaseConnectionResource dcr = (DatabaseConnectionResource) resource;
			dcr.setDatabaseConnectionPool( this );
		}
		super.addResource( resource );
	}
}
