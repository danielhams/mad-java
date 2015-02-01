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

package uk.co.modularaudio.service.gui.impl.racktable.back;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.exception.DatastoreException;

public class RackIOLinkImage extends AbstractLinkImage
{
//	private static Log log = LogFactory.getLog( NewRackLinkImage.class.getName() );

	private final RackIOLink rackIOLink;

	public RackIOLinkImage( final BufferedImageAllocationService bufferedImageAllocationService, final RackIOLink rackIOLink, final Point sourcePoint, final Point sinkPoint )
		throws DatastoreException
	{
		super( "NewRackIOLinkImage", bufferedImageAllocationService, sourcePoint, sinkPoint );
		this.rackIOLink = rackIOLink;
	}

	@Override
	public Rectangle getRectangle()
	{
		return rectangle;
	}

	@Override
	public BufferedImage getBufferedImage()
	{
		return bufferedImage;
	}

	public RackIOLink getRackIOLink()
	{
		return rackIOLink;
	}
}
