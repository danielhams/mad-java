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

package uk.co.modularaudio.service.bufferedimageallocation.impl.cache;

import java.awt.image.BufferedImage;

public class UsedEntry
{
	private final String allocationSource;
	private final RawImage rawImage;
	private final long assignedId;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final BufferedImage bufferedImage;
	
	public UsedEntry( String allocationSource,
			RawImage rawImage,
			long assignedId,
			int x,
			int y,
			int width,
			int height,
			BufferedImage bufferedImage )
	{
		this.allocationSource = allocationSource;
		this.rawImage = rawImage;
		this.assignedId = assignedId;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.bufferedImage = bufferedImage;
	}
	
	public String getAllocationSource()
	{
		return allocationSource;
	}

	public RawImage getRawImage()
	{
		return rawImage;
	}

	public long getAssignedId()
	{
		return assignedId;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public BufferedImage getBufferedImage()
	{
		return bufferedImage;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "AllocationSource(");
		sb.append( allocationSource );
		sb.append( ") xy(" );
		sb.append(x);
		sb.append(",");
		sb.append(y);
		sb.append(")wh(");
		sb.append(width);
		sb.append(",");
		sb.append(height);
		sb.append(")");
		
		return sb.toString();	
	}
}
