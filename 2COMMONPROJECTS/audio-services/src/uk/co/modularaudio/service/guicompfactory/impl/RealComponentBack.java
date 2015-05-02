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

package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

class RealComponentBack extends JPanel
{
	private static final long serialVersionUID = 5211955307472576952L;
	private static Log log = LogFactory.getLog( RealComponentBack.class.getName() );

	private final RackComponent rc;
	private final BufferedImage backBufferedImage;

	public RealComponentBack( final ResizableBackContainer resizableBackContainer, final RackComponent rc )
	{
		this.rc = rc;
		this.backBufferedImage = rc.getUiDefinition().getBackBufferedImage();

		this.setOpaque( true );
		this.setLayout( null );

		for (final GuiChannelPlug plug : resizableBackContainer.plugsToDestroy)
		{
			this.add( plug );
		}

		final Dimension size = new Dimension( PaintedComponentDefines.BACK_MIN_WIDTH,
				PaintedComponentDefines.BACK_MIN_HEIGHT );
		setSize( size );
		setMinimumSize( size );
		setPreferredSize( size );
	}

	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
		// log.debug("Looking for plug at real position " + localPoint );
		GuiChannelPlug retVal = null;
		final Component c = this.getComponentAt( localPoint );
		if (c != null)
		{
			if (c instanceof GuiChannelPlug)
			{
				retVal = (GuiChannelPlug) c;
			}
		}
		return retVal;
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		super.paintComponent( g );

		final int imageWidth = backBufferedImage.getWidth();
		final int imageHeight = backBufferedImage.getHeight();
		final int width = getWidth();
		// Hack since I duffed up the heights due to border.
		final int height = getHeight() + 8;

		if( imageWidth != width || imageHeight != height )
		{
			final StringBuilder sb = new StringBuilder("Component ");
			sb.append( rc.getInstance().getDefinition().getId() );
			sb.append( " has badly sized back image: (" );
			sb.append( imageWidth );
			sb.append( ", " );
			sb.append( imageHeight );
			sb.append( ") - component size(" );
			sb.append( width );
			sb.append( ", " );
			sb.append( height );
			sb.append( ")" );
			final String msg = sb.toString();
			log.warn( msg );
		}

		g.drawImage( backBufferedImage, 0, 0, null );
	}
}
