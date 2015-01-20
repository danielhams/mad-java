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

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class RawImage
{
//	private static Log log = LogFactory.getLog( RawImage.class.getName() );
	private BufferedImage rawImage = null;
	private Graphics2D rawGraphics = null;
	private long rawImageId = -1;
	
	public RawImage( BufferedImage rawImage, Graphics2D rawGraphics, long rawImageId )
	{
		this.rawImage = rawImage;
		this.rawGraphics = rawGraphics;
		this.rawImageId = rawImageId;
	}

	public BufferedImage getRootBufferedImage()
	{
		return rawImage;
	}
	
	public Graphics getRootGraphics()
	{
		return rawGraphics;
	}

	public long getRawImageId()
	{
		return rawImageId;
	}
	
	public void clearRegion( int x, int y, int width, int height )
	{
//		log.debug("Clearing region (" + x + ", " + y + "-" + width + ", " + height + ")");
		rawGraphics.setComposite( AlphaComposite.Clear );
		rawGraphics.fillRect( x, y, width, height );
	}
}
