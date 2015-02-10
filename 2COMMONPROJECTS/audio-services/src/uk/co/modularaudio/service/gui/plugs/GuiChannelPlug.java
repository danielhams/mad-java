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

package uk.co.modularaudio.service.gui.plugs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;

public abstract class GuiChannelPlug extends JPanel implements GuiPlugImageCreator
{
//	private static Log log = LogFactory.getLog( GuiChannelPlug.class.getName() );

	private static final long serialVersionUID = -9176621611479002996L;

	protected final static boolean DRAWN_PLUGS = true;

	protected MadUiChannelInstance uiChannelInstance;

	protected Point center;

	protected static final int PLUG_DIAMETER = 10;
	protected static final int SOCKET_DIAMETER = 10;

	protected static final Color PLUG_INSIDE_COLOR = Color.DARK_GRAY;
	protected static final Color PLUG_OUTLINE_COLOR = Color.LIGHT_GRAY;

	protected Rectangle plugBounds;
	protected BufferedImage plugImage;

	protected static GuiPlugImageCache plugImageCache = new GuiPlugImageCache();

	public GuiChannelPlug( final MadUiChannelInstance sacd )
	{
		this.setOpaque( false );
		this.uiChannelInstance = sacd;
		center = sacd.getCenter();
		final int startX = center.x - (SOCKET_DIAMETER / 2);
		final int startY = center.y - (SOCKET_DIAMETER / 2);
		plugBounds = new Rectangle( startX, startY, SOCKET_DIAMETER + 2, SOCKET_DIAMETER + 2);
		this.setBounds( plugBounds );
	}

	@Override
	public abstract void paint( Graphics g );

	public MadUiChannelInstance getUiChannelInstance()
	{
		return uiChannelInstance;
	}

	public void destroy()
	{
		uiChannelInstance = null;
		plugImage = null;
	}
}
