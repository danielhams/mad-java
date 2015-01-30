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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.impl.racktable.back.RackLinkImage;

public class DndCurrentWireImage extends JPanel
{
	private static final long serialVersionUID = 1832134641594451973L;

//	private static Log log = LogFactory.getLog( NewDndCurrentWireImage.class.getName() );

	private static final float CURRENT_WIRE_TRANSPARENCY = 0.7f;

	private final RackLinkImage currentRackLinkImage;

	public DndCurrentWireImage( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.setOpaque( false );

		currentRackLinkImage = new RackLinkImage( "NewDndCurrentWireImage", bufferedImageAllocationService, null, null, null );
	}

	public void setWirePosition( final Point sourcePoint, final Point sinkPoint )
	{
		if( sourcePoint == null || sinkPoint == null )
		{
			currentRackLinkImage.destroy();
		}
		else
		{
			currentRackLinkImage.redrawWireWithNewPoints( sourcePoint, sinkPoint );
			final Rectangle currentRectangle = currentRackLinkImage.getRectangle();
			setBounds( currentRectangle );
		}
	}

	@Override
	public void paint( final Graphics g )
	{
		final BufferedImage bi = currentRackLinkImage.getBufferedImage();
		if( bi != null )
		{
			final Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					CURRENT_WIRE_TRANSPARENCY));

//			log.debug("Painting an image");
			g2d.drawImage( bi, 0, 0, null );
		}
		else
		{
//			log.debug("Not got an image.");
		}
	}
}
