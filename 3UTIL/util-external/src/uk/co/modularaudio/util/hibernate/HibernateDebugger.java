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
	 * Take the HQL and provide a HQL with all the parameters expanded
	 * @param query
	 * @param log
	 * @param parameters
	 * @return
	 */
	public static String translateQuery(String query,Log log,Map<String,String> parameters  ) {
		String result = null;
		if (parameters != null && parameters.size()>0) {
			for (Iterator<Entry<String,String>> iter = parameters.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,String> element = (Map.Entry<String,String>) iter.next();
				Object value = element.getValue(); 
				result=query.replaceAll(":"+element.getKey().toString(), (value!=null) ? value.toString() : "NULL");
				query = result;
			
			}
		} else {
			result = query;
		}
		
		log.debug(result);
		return result;
	}
	
	/**
	 * Take the Object and provide a HQL with all the parameters expanded
	 * @param query
	 * @param log
	 * @param parameters
	 * @return
	 */
	public static String showObjectProperties(Object sourceObject,Log log,String message ) {
		String result = null;
		try
		{
			if (message != null) {
				result = message + " : ";
			}
			result += ReflectionUtils.debugObjectValues(sourceObject);
		}
		catch (DatastoreException e)
		{
			log.debug("Problem while trying to debug an object properties",e);
		}
		
		log.debug(result);
		return result;
	}
}
