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

package uk.co.modularaudio.util.hibernate;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.co.modularaudio.util.exception.DatastoreException;

public class HibernateQueryBuilder
{
	private Query queryResult;
	private boolean isSqlQuery = false;
	private int numParametersSet = 0;

	public HibernateQueryBuilder()
	{
	}

	public HibernateQueryBuilder( final boolean isSQLQuery )
	{
		this.isSqlQuery = isSQLQuery;
	}

	public void initQuery( final Session session, final String query )
	{
		if (isSqlQuery)
		{
			queryResult = session.createSQLQuery( query );
		}
		else
		{
			queryResult = session.createQuery( query );
		}

	}

	public Query buildQuery() throws DatastoreException
	{
		if( queryResult.getNamedParameters().length != numParametersSet )
		{
			throw new DatastoreException( "The number of declared arguments is different form the number of argument passed to the query" );
		}

		return queryResult;
	}

	public void setString( final String fieldName, final String value ) throws DatastoreException
	{
		if( queryResult == null )
		{
			throw new DatastoreException( "Init your query before trying to build with parameters" );
		}

		numParametersSet++;
		queryResult.setString( fieldName, value );
	}

	public void setLong( final String fieldName, final long value ) throws DatastoreException
	{
		if( queryResult == null )
		{
			throw new DatastoreException( "Init your query before trying to build with parameters" );
		}

		numParametersSet++;
		queryResult.setLong( fieldName, value );
	}

	public void setDate( final String fieldName, final Date value ) throws DatastoreException
	{
		if( queryResult == null )
		{
			throw new DatastoreException( "Init your query before trying to build with parameters" );
		}

		numParametersSet++;
		queryResult.setDate( fieldName, value );
	}

	public void setInt( final String fieldName, final int value ) throws DatastoreException
	{
		if( queryResult == null )
		{
			throw new DatastoreException( "Init your query before trying to build with parameters" );
		}

		numParametersSet++;
		queryResult.setInteger( fieldName, value );
	}
}
