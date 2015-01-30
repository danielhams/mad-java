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

public class TestDndRackDragGhostHintDecoration extends JPanelDndTableDecorationHint
{
	private static Log log = LogFactory.getLog( TestDndRackDragGhostHintDecoration.class.getName() );

	private BufferedImage image = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
	private Point mouseOffset = new Point(0,0);
	private Rectangle currentDamageRectangle = new Rectangle( 0, 0, -1, -1);

	@Override
	public boolean isMouseRelative()
	{
		return true;
	}

	@Override
	public void paint(final Graphics g)
	{
		if( image != null )
		{
			g.drawImage( image, mouseOffset.x, mouseOffset.y, null );
		}
	}

	public void setImageAndOffset(final BufferedImage image, final Point mouseOffset )
	{
		if( image == null || mouseOffset == null )
		{
			// Stop
			log.error("Oi, nutter!");
		}
		boolean isUpdated = false;
		if( mouseOffset != null && !mouseOffset.equals( this.mouseOffset ) )
		{
			this.image = image;
			this.mouseOffset = mouseOffset;
			isUpdated = true;
		}
		else if( mouseOffset == null && this.mouseOffset != null )
		{
			this.image = image;
			this.mouseOffset = mouseOffset;
			isUpdated = true;
		}
		if( isUpdated )
		{
			final Rectangle newDamangeRectangle = new Rectangle( curMousePosition.x + mouseOffset.x,
					curMousePosition.y + mouseOffset.y,
					image.getWidth(),
					image.getHeight() );
			final Rectangle emitDamageRectangle = ( currentDamageRectangle == null ? newDamangeRectangle :
				newDamangeRectangle.union( currentDamageRectangle ) );
			currentDamageRectangle = newDamangeRectangle;
			this.emitNeedsRepaintEvent( this, emitDamageRectangle );
		}
	}


	@Override
	public Rectangle getCurrentDamageRectangle()
	{
		return currentDamageRectangle;
	}

	private Point curMousePosition = new Point(0,0);

	@Override
	public void setMousePosition( final Point mousePosition )
	{
		curMousePosition = mousePosition;
		// this means our damage rectangle has changed
		final Rectangle newDamangeRectangle = new Rectangle( curMousePosition.x + mouseOffset.x,
				curMousePosition.y + mouseOffset.y,
				image.getWidth(),
				image.getHeight() );
		final Rectangle emitDamageRectangle = ( currentDamageRectangle == null ? newDamangeRectangle :
			newDamangeRectangle.union( currentDamageRectangle ) );
		currentDamageRectangle = newDamangeRectangle;
		this.emitNeedsRepaintEvent( this, emitDamageRectangle );
	}

	@Override
	public void signalAnimation()
	{
		// Not animated
	}

}
