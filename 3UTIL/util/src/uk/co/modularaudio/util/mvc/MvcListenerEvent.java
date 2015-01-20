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

package uk.co.modularaudio.util.mvc;

public class MvcListenerEvent
{
	public enum EventType
	{
		FULL_REFRESH,
		INTERVAL_ADDED,
		INTERVAL_REMOVED,
		SELECTION_CHANGED
	}
	
	protected EventType eventType;
	protected int index0 = -1;
	protected int index1 = -1;
	
	public MvcListenerEvent( EventType eventType, int index0, int index1 )
	{
		this.eventType = eventType;
		this.index0 = index0;
		this.index1 = index1;
	}
	
	public EventType getEventType()
	{
		return eventType;
	}
	
	public int getIndex0()
	{
		return index0;
	}
	
	public int getIndex1()
	{
		return index1;
	}

}
