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

public class FreeEntry
{
	private final RawImage sourceRawImage;
	private final int x;
	private final int y;
	private final int width;
	private final int height;

	public FreeEntry( final RawImage sourceRawImage, final int x, final int y, final int width, final int height )
	{
		this.sourceRawImage = sourceRawImage;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public RawImage getSourceRawImage()
	{
		return sourceRawImage;
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

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("sri(");
		sb.append(sourceRawImage.getRawImageId());
		sb.append(")xy(");
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
