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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class FixedXTransparentBorder extends JPanel
{
	private static final long serialVersionUID = -721733939455076637L;
//	private static Log log = LogFactory.getLog( FixedXTransparentBorder.class.getName() );

	private final BufferedImage bi;

	public FixedXTransparentBorder( final BufferedImage bi )
	{
		setOpaque( true );
		this.bi = bi;

		final Dimension size = new Dimension( bi.getWidth(), 1 );

		this.setSize( size );
		this.setMinimumSize( size );
		this.setPreferredSize( size );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		// Ugly + 10 hack to get around bug where size passed isn't actual screen sizze :-(
		g.drawImage( bi, 0, 0, width, height + 10, null );
	}
}
