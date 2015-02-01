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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import uk.co.modularaudio.util.exception.DatastoreException;

public class HibernateDebugger
{
	/**
	 * Expand the HQL and return a completed string with replaced parameters
	 *
	 * @param query The source HQL query
	 * @param log the logger which the output will be written
	 * @param parameters a map of parameters to the query
	 * @return the completed HQL query with parameters inlined
	 */
	public static String translateQuery( final String query, final Log log, final Map<String, String> parameters )
	{
		final StringBuilder result = new StringBuilder();
		if (parameters != null && parameters.size() > 0)
		{
			for (final Iterator<Entry<String, String>> iter = parameters.entrySet().iterator(); iter.hasNext();)
			{
				final Map.Entry<String, String> element = iter.next();
				final Object value = element.getValue();
				result.append( query.replaceAll( ":" + element.getKey().toString(), (value != null) ? value.toString()
						: "NULL" ) );
			}
		}
		else
		{
			result.append( query );
		}

		final String retString = result.toString();
		log.debug( retString );
		return retString;
	}

	/**
	 * Take the Object and dump it's fields to the provided logger
	 *
	 * @param sourceObject the object to debug
	 * @param log the logger which the output will be written
	 * @param message the prefix to add to the log output
	 * @return A combined string containing the property names and values for
	 *         the source object
	 */
	public static String showObjectProperties( final Object sourceObject, final Log log, final String message )
	{
		final StringBuilder result = new StringBuilder();
		try
		{
			if (message != null)
			{
				result.append( message );
				result.append( " : " );
			}
			result.append( ReflectionUtils.debugObjectValues( sourceObject ) );
		}
		catch (final DatastoreException e)
		{
			log.debug( "Problem while trying to debug an object properties", e );
		}

		final String retString = result.toString();
		log.debug( retString );
		return retString;
	}
}
