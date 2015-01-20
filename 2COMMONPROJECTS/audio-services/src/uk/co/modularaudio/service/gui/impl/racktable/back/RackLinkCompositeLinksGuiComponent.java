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

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class RackLinkCompositeLinksGuiComponent extends JComponent
{
	private static final long serialVersionUID = 3441350163975600834L;
	
	private Rectangle compositeLinksBounds = null;
	private BufferedImage compositeLinksImage = null;
	
	private final static float GENERAL_WIRE_TRANSPARENCY = 0.7f;

	public RackLinkCompositeLinksGuiComponent()
	{
		setOpaque( false );
	}
	
	public void setBoundsAndImage( Rectangle bounds, BufferedImage image )
	{
		this.compositeLinksBounds = bounds;
		this.compositeLinksImage = image;
		if( bounds != null )
		{
			setBounds( bounds );
		}
	}
	
	public void paint( Graphics g )
	{
		if( compositeLinksBounds != null && compositeLinksImage != null )
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					GENERAL_WIRE_TRANSPARENCY));
			g.drawImage( compositeLinksImage, 0, 0, null );
		}
	}
}
