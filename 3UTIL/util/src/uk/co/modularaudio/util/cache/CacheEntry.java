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

package uk.co.modularaudio.util.cache;

public class CacheEntry
{
	private Object value = null;
	private java.util.Date lastSetDate = null;
	
	public CacheEntry(Object value, java.util.Date lastSetDate)
	{
		this.value = value;
		this.lastSetDate = lastSetDate;
	}
	
	/**
	 * @return
	 */
	protected java.util.Date getLastSetDate()
	{
		return lastSetDate;
	}

	/**
	 * @return
	 */
	protected Object getValue()
	{
		return value;
	}

	/**
	 * @param date
	 */
	protected void setLastSetDate(java.util.Date date)
	{
		lastSetDate = date;
	}

	/**
	 * @param object
	 */
	protected void setValue(Object object)
	{
		value = object;
	}

}
