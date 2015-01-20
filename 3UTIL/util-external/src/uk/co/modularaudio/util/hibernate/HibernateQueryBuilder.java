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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.hibernate.Query;
import org.hibernate.Session;

import uk.co.modularaudio.util.exception.DatastoreException;

public class HibernateQueryBuilder
{
	private Map<String,String> parameters=new HashMap<String,String>();
	private Log log;
	private Query queryResult;
	private boolean isSqlQuery = false;
	public HibernateQueryBuilder(Log log)  {
		this.log = log;
		if (log == null) 
		{
			throw new NullPointerException("Logger is null unable to log things");
		}
	}
	public HibernateQueryBuilder(Log log,boolean isSQLQuery)  {
		this.log = log;
		if (log == null) 
		{
			throw new NullPointerException("Logger is null unable to log things");
		}
		this.isSqlQuery = isSQLQuery;
	}
	public void initQuery(Session session,String query) {
		if (isSqlQuery) 
		{
			queryResult = session.createSQLQuery(query);
		} else
		{
			queryResult = session.createQuery(query);
		}
		
	}
	public Query buildQuery() throws DatastoreException {
		if (parameters != null && parameters.size() >0 && queryResult.getNamedParameters().length != parameters.size()) {
			throw new DatastoreException("The number of declared arguments is different form the number of argument passed to the query");
		}
		
		HibernateDebugger.translateQuery(queryResult.getQueryString(),log,parameters);
		return queryResult;
	}
	public void setString(String fieldName,String value) throws DatastoreException {
		// if (value != null)
		{
			parameters.put(fieldName,value);
			if (queryResult != null) {
				queryResult.setString(fieldName,value);
			} else {
				throw new DatastoreException("Init your query before trying to build with parameters");
			}
		}
			
	}
	public void setLong(String fieldName,Long value) throws DatastoreException {
		// if (value != null)
		{
			parameters.put(fieldName,value.toString());
			if (queryResult != null) {
				queryResult.setLong(fieldName,value);
			} else {
				throw new DatastoreException("Init your query before trying to build with parameters");
			}
		}
			
	}

	public void setDate(String fieldName,Date value) throws DatastoreException {
		// if (value != null)
		{
			parameters.put(fieldName,value.toString());
			if (queryResult != null) {
				queryResult.setDate(fieldName,value);
			} else {
				throw new DatastoreException("Init your query before trying to build with parameters");
			}
		}
			
	}
}
