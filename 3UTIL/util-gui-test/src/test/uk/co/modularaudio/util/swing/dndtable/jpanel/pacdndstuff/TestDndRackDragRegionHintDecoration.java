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

package test.uk.co.modularaudio.util.swing.dndtable.jpanel.pacdndstuff;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTableDecorationHint;

public class TestDndRackDragRegionHintDecoration extends JPanelDndTableDecorationHint
{
	private static Log log = LogFactory.getLog( TestDndRackDragRegionHintDecoration.class.getName() );

	private int x = 0;
	private int y = 0;
	private BufferedImage regionHintImage = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
	private Rectangle currentDamageRectangle = new Rectangle(0,0,-1,-1);

	public TestDndRackDragRegionHintDecoration()
	{
	}

	@Override
	public void paint(final Graphics g)
	{
		if( regionHintImage != null )
		{
			g.drawImage( regionHintImage, x, y, null );
		}
	}

	public void setRegionHint( final BufferedImage regionHintImage, final int x, final int y )
	{
		if( regionHintImage == null )
		{
			log.error("This shouldn't get set to null!");
		}
		boolean isUpdated = false;
		if( regionHintImage != null )
		{
			if( this.x != x || this.y != y  || regionHintImage != this.regionHintImage)
			{
				this.x = x;
				this.y = y;
				this.regionHintImage = regionHintImage;
				isUpdated = true;
			}
		}
		else if( regionHintImage == null && this.regionHintImage != null )
		{
			this.x = x;
			this.y = y;
			this.regionHintImage = regionHintImage;
			isUpdated = true;
		}
		if( isUpdated )
		{
//			log.debug("Will ask for a repaint due to hint image change");
			final Rectangle newDamageRectangle = new Rectangle( x, y, regionHintImage.getWidth(), regionHintImage.getHeight() );
			final Rectangle emitDamageRectangle = (currentDamageRectangle == null ? newDamageRectangle :
					newDamageRectangle.union( currentDamageRectangle ));
			currentDamageRectangle = newDamageRectangle;
			this.emitNeedsRepaintEvent( this, emitDamageRectangle );
		}
	}

	@Override
	public boolean isMouseRelative()
	{
		return false;
	}

	@Override
	public Rectangle getCurrentDamageRectangle()
	{
		return currentDamageRectangle;
	}

	@Override
	public void setMousePosition(final Point mousePosition)
	{
	}

	@Override
	public void signalAnimation()
	{
		// Not animated
	}
}
