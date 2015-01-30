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

package uk.co.modularaudio.service.library;


public class CuePoint
{
	private long cuePointId;
	private long framePosition;
	private String name;
	public CuePoint()
	{
		this( 0, 0, "" );
	}

	public CuePoint( long cuePointId, long framePosition, String name )
	{
		this.cuePointId = cuePointId;
		this.framePosition = framePosition;
		this.name = name;
	}
	
	public void setName( String name )
	{
		this.name = name;
	}
	
	public void setFramePosition( long framePosition )
	{
		this.framePosition = framePosition;
	}

	public long getCuePointId()
	{
		return cuePointId;
	}

	public void setCuePointId( long cuePointId )
	{
		this.cuePointId = cuePointId;
	}

	public long getFramePosition()
	{
		return framePosition;
	}

	public String getName()
	{
		return name;
	}
	
}
