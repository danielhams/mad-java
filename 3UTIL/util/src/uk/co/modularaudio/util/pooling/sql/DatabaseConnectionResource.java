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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * <P>
 * An implementation of the generic Resource class containing
 * java.sql.Connection.
 * </P>
 * <P>
 * Extra parameters supported over the standard Resource class are
 * <UL>
 * <LI>LastActiveDate - the time at which the resource was last release from the
 * connection pool. This is used to determine if a connection resource has
 * expired its lease from the pool.
 * </UL>
 *
 * @author D.Hams
 * @see DatabaseConnectionPool
 */
public class DatabaseConnectionResource implements Resource, Connection
{
	DatabaseConnectionPool dcp = null;

	/**
	 * <P>
	 * Create a new database connection resource from the supplied java sql
	 * connection.
	 * </P>
	 *
	 * @param connection
	 */
	public DatabaseConnectionResource( Connection connection )
	{
		this.connection = connection;
		lastActiveDate = new Date();
	}

	public void setDatabaseConnectionPool( DatabaseConnectionPool dcp )
	{
		this.dcp = dcp;
	}

	/**
	 * <P>
	 * Get the date on which this resource was last leased from the connection
	 * pool.
	 * </P>
	 *
	 * @return java.util.Date
	 */
	public Date getLastActiveDate()
	{
		return (lastActiveDate);
	}

	/**
	 * <P>
	 * Set the last time on which the resource was leased from the connection
	 * pool.
	 * </P>
	 *
	 * @param date
	 */
	public void setLastActiveDate( Date date )
	{
		lastActiveDate = date;
	}

	/**
	 * <P>
	 * Get the java.sql.Connection from inside the resource.
	 * </P>
	 *
	 * @return Connection
	 */
	public Connection getConnection()
	{
		return connection;
	}

	/**
	 * <P>
	 * Set the java.sql.Connection inside the resource.
	 * </P>
	 *
	 * @param connection
	 */
	public void setConnection( Connection connection )
	{
		this.connection = connection;
	}

	/**
	 * <P>
	 * Return some debugging String information about the resource.
	 * </P>
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return (connection.toString() + "->lmd(" + lastActiveDate + ")");
	}

	/**
	 * <P>
	 * The standard java sql connection.
	 * </P>
	 */
	private Connection connection;

	/**
	 * <P>
	 * The time the connection was last leased from the connection pool.
	 * </P>
	 */
	private Date lastActiveDate;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException
	{
		connection.clearWarnings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException
	{
		// Return us to the pool.
		if (dcp != null)
		{
			dcp.releaseConnection( this );
		}
		else
		{
			connection.close();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException
	{
		connection.commit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException
	{
		return connection.createStatement();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability )
			throws SQLException
	{
		return connection.createStatement( resultSetType, resultSetConcurrency, resultSetHoldability );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement( int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return connection.createStatement( resultSetType, resultSetConcurrency );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
	public boolean getAutoCommit() throws SQLException
	{
		return connection.getAutoCommit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException
	{
		return connection.getCatalog();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException
	{
		return connection.getHoldability();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException
	{
		return connection.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
	public int getTransactionIsolation() throws SQLException
	{
		return connection.getTransactionIsolation();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		return connection.getTypeMap();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		return connection.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException
	{
		return connection.isClosed();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() throws SQLException
	{
		return connection.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL( String sql ) throws SQLException
	{
		return connection.nativeSQL( sql );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return connection.prepareCall( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException
	{
		return connection.prepareCall( sql, resultSetType, resultSetConcurrency );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall( String sql ) throws SQLException
	{
		return connection.prepareCall( sql );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
	 * int)
	 */
	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability ) throws SQLException
	{
		return connection.prepareStatement( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
			throws SQLException
	{
		return connection.prepareStatement( sql, resultSetType, resultSetConcurrency );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys ) throws SQLException
	{
		return connection.prepareStatement( sql, autoGeneratedKeys );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) throws SQLException
	{
		return connection.prepareStatement( sql, columnIndexes );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement( String sql, String[] columnNames ) throws SQLException
	{
		return connection.prepareStatement( sql, columnNames );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement( String sql ) throws SQLException
	{
		return connection.prepareStatement( sql );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint( Savepoint savepoint ) throws SQLException
	{
		connection.releaseSavepoint( savepoint );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException
	{
		connection.rollback();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback( Savepoint savepoint ) throws SQLException
	{
		connection.rollback( savepoint );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit( boolean autoCommit ) throws SQLException
	{
		connection.setAutoCommit( autoCommit );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
	public void setCatalog( String catalog ) throws SQLException
	{
		connection.setCatalog( catalog );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
	public void setHoldability( int holdability ) throws SQLException
	{
		connection.setHoldability( holdability );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly( boolean readOnly ) throws SQLException
	{
		connection.setReadOnly( readOnly );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException
	{
		return connection.setSavepoint();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint( String name ) throws SQLException
	{
		return connection.setSavepoint();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
	public void setTransactionIsolation( int level ) throws SQLException
	{
		connection.setTransactionIsolation( level );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap( Map<String, Class<?>> map ) throws SQLException
	{
		connection.setTypeMap( map );
	}

	@Override
	public Array createArrayOf( String typeName, Object[] elements ) throws SQLException
	{
		return connection.createArrayOf( typeName, elements );
	}

	@Override
	public Blob createBlob() throws SQLException
	{
		return connection.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException
	{
		return connection.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException
	{
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException
	{
		return connection.createSQLXML();
	}

	@Override
	public Struct createStruct( String typeName, Object[] attributes ) throws SQLException
	{
		return connection.createStruct( typeName, attributes );
	}

	@Override
	public Properties getClientInfo() throws SQLException
	{
		return connection.getClientInfo();
	}

	@Override
	public String getClientInfo( String name ) throws SQLException
	{
		return connection.getClientInfo( name );
	}

	@Override
	public boolean isValid( int timeout ) throws SQLException
	{
		return connection.isValid( timeout );
	}

	@Override
	public void setClientInfo( Properties properties ) throws SQLClientInfoException
	{
		connection.setClientInfo( properties );
	}

	@Override
	public void setClientInfo( String name, String value ) throws SQLClientInfoException
	{
		connection.setClientInfo( name, value );
	}

	@Override
	public boolean isWrapperFor( Class<?> iface ) throws SQLException
	{
		return connection.isWrapperFor( iface );
	}

	@Override
	public <T> T unwrap( Class<T> iface ) throws SQLException
	{
		return connection.unwrap( iface );
	}

	@Override
	public void setSchema( String schema ) throws SQLException
	{
		connection.setSchema( schema );
	}

	@Override
	public String getSchema() throws SQLException
	{
		return connection.getSchema();
	}

	@Override
	public void abort( Executor executor ) throws SQLException
	{
		connection.abort( executor );
	}

	@Override
	public void setNetworkTimeout( Executor executor, int milliseconds ) throws SQLException
	{
		connection.setNetworkTimeout( executor, milliseconds );
	}

	@Override
	public int getNetworkTimeout() throws SQLException
	{
		return connection.getNetworkTimeout();
	}
}
