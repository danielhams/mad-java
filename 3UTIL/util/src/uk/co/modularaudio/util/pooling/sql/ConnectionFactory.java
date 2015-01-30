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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.pooling.common.Factory;
import uk.co.modularaudio.util.pooling.common.FactoryProductionException;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * <P>
 * A Resource Factory that creates database connections matching the supplied
 * arguements.
 * <P>
 *
 * @author D.Hams
 * @see FactoryProductionException
 * @see DatabaseConnectionResource
 */
public class ConnectionFactory implements Factory
{
	protected static Log log = LogFactory.getLog( ConnectionFactory.class.getName() );

	/**
	 * <P>
	 * Setup the factory parameters.
	 * </P>
	 *
	 * @param classname
	 * @param connectionUrl
	 * @param username
	 * @param password
	 */
	public ConnectionFactory( final String classname, final String connectionUrl, final String username, final String password )
	{
		this.classname = classname;
		this.connectionURL = connectionUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * <P>
	 * Create a database connection resource using the configured parameters.
	 * </P>
	 * <P>
	 * A FactoryProductionException is thrown if any problems occur.
	 * </P>
	 *
	 * @exception FactoryProductionException
	 */
	@Override
	public Resource createResource() throws FactoryProductionException
	{
		Resource res = null;
		try
		{
			// log.debug("About to ask to create a database connection.");
			res = createDBConnection();
			// log.debug("Just created a database connection.");
		}
		catch (final SQLException sqle)
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Error creating DBConnection inside factory." );
				log.error( "SQLE: " + sqle.toString() );
			}
			throw new FactoryProductionException( sqle.toString() );
		}
		if (res == null)
		{
			log.error( "Passing back (for some reason) a null resource from the factory." );
		}
		return (res);
	}

	/**
	 * <P>
	 * Internal method to create the java.sql.Connection, and place it inside a
	 * DatabaseConnectionResource.
	 * </P>
	 *
	 * @return DatabaseConnectionResource
	 * @exception SQLException
	 */
	private DatabaseConnectionResource createDBConnection() throws SQLException
	{
		// Create the connection
		final Connection c = getConnection();

		// Force the application to explicitly set its commit points.
		c.setAutoCommit( false );

		String setupFunctionIndexes = "ALTER SESSION SET OPTIMIZER_MODE = FIRST_ROWS";
		final PreparedStatement som = c.prepareStatement( setupFunctionIndexes );
		som.execute();
		som.close();

		setupFunctionIndexes = "ALTER SESSION SET QUERY_REWRITE_ENABLED = TRUE";
		final PreparedStatement sqre = c.prepareStatement( setupFunctionIndexes );
		sqre.execute();
		sqre.close();

		setupFunctionIndexes = "ALTER SESSION SET QUERY_REWRITE_INTEGRITY = TRUSTED";
		final PreparedStatement sqri = c.prepareStatement( setupFunctionIndexes );
		sqri.execute();
		sqri.close();

		// log.debug("Altered session to use function indexes.");

		final DatabaseConnectionResource dbcr = new DatabaseConnectionResource( c );
		return (dbcr);
	}

	/**
	 * <P>
	 * Ask the JDBC driver manager to get a connection to the database.
	 * </P>
	 *
	 * @exception SQLException
	 * @return java.sql.Connection
	 */
	private Connection getConnection() throws SQLException
	{
		// log.debug("About to getConnection of '" + connectionURL + "' (" +
		// username + " -> " + password + ")");
		final Connection c = DriverManager.getConnection( connectionURL, username, password );
		// log.debug("Successful in obtaining a connection.");
		return (c);
	}

	/**
	 * <P>
	 * Initialise the factory by attempting to get the classes required for the
	 * driver.
	 * </P>
	 *
	 * @exception FactoryProductionException
	 */
	@Override
	public void init() throws FactoryProductionException
	{
		if (classname == null || classname.equals( "" ))
		{
			throw new FactoryProductionException( "No driver class specified." );
		}

		try
		{
			Class.forName( classname );
			// Test creating a connection
			final Connection c = getConnection();
			c.close();
		}
		catch (final ClassNotFoundException cnfe)
		{
			throw new FactoryProductionException( cnfe.toString() );
		}
		catch (final SQLException sqle)
		{
			throw new FactoryProductionException( sqle.toString() );
		}
	}

	private String classname = "";
	private String connectionURL = "";
	private String username = "";
	private String password = "";
}
