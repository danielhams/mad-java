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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.exception.DatastoreException;



public class ReflectionUtils
{
	private static Log log = LogFactory.getLog(ReflectionUtils.class.getName());


	public static void copyOverGetAttributes(Class<?> interfaceListingAttributes, Object src, Object dest)
			throws DatastoreException
	{
		try
		{
			Method methods[] = interfaceListingAttributes.getMethods();

			for (int i = 0; i < methods.length; i++)
			{
				Method getMethod = methods[i];
				String methodName = getMethod.getName();

				if (methodName.startsWith("get"))
				{
					Class<?> returnType = getMethod.getReturnType();
					
					Object result = getMethod.invoke(src);

					String setMethodName = methodName.replaceFirst("get", "set");

					Method setMethod = interfaceListingAttributes.getDeclaredMethod(setMethodName, new Class[]
					{ returnType });

					setMethod.invoke(dest, result);
				}
			}
		}
		catch (IllegalAccessException iae)
		{
			throw new DatastoreException("iae: " + iae.toString());
		}
		catch (InvocationTargetException ite)
		{
			throw new DatastoreException("ite: " + ite.toString());
		}
		catch (NoSuchMethodException nsme)
		{
			throw new DatastoreException("nsme: " + nsme.toString());
		}
	}

	/*
	 * Return a string to debug the values of an object
	 */
	@SuppressWarnings("rawtypes")
	public static String debugObjectValues(Object src) throws DatastoreException
	{
		StringBuilder debugString = new StringBuilder(src.getClass().getName()+" :");
		StringBuilder collectionString = new StringBuilder();
		String prefix="get";
		boolean firstGetter = true;
		try
		{
			Method methods[] = src.getClass().getMethods();

			for (int i = 0; i < methods.length; i++)
			{
				Method getMethod = methods[i];
				String methodName = getMethod.getName();
				
				if (methodName.startsWith(prefix) && !methodName.equalsIgnoreCase("getclass"))
				{
					String propertyName = methodName.substring(prefix.length(),methodName.length());
					if (getMethod.getParameterTypes().length>0) 
					{
						continue;
					}
					if (!firstGetter) {
						debugString.append(",");
					}
					
					Object result = getMethod.invoke(src);
					if (result instanceof Map) {
						collectionString.append(propertyName).append("=");
						collectionString.append("(");
						Map map = (Map)result;
						for (Iterator iter = map.entrySet().iterator(); iter.hasNext();)
						{
							Map.Entry entry = (Map.Entry) iter.next();
							collectionString.append(" ").append(entry.getKey()).append("|").append(entry.getValue());
							if (iter.hasNext()) {
								collectionString.append(",");
							}
						}
						collectionString.append(")");
					} else {
						debugString.append(propertyName).append("=");
						debugString.append(result!=null?result.toString():"null");
					}
					firstGetter = false;
				}
			}
			if (collectionString.length()>0) {
				debugString.append(",").append(collectionString);
			}
		}
		catch (Throwable thr)
		{
			log.debug("Throwable problem in debugObjectValues: " + thr.toString(),thr);
		}
		
		return debugString.toString();
	}
	
	public static String getClassPackageAsPath( Object o )
	{
		Class<?> theObject = o.getClass();
		Package thePackage = theObject.getPackage();
		String packageAsString = thePackage.toString();
		String tmpVal = packageAsString.replaceAll("package\\s","");
		String retVal = tmpVal.replaceAll( "\\.", "/");
		return retVal;
	}

}
