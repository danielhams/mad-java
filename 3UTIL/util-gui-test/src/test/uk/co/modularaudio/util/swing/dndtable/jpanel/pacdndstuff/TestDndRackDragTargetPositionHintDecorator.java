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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTableDecorationHint;

public class TestDndRackDragTargetPositionHintDecorator extends JPanelDndTableDecorationHint
{
	private Point targetPosition;

	private static int hintRadius = 10;

	private Rectangle currentDamageRectangle = new Rectangle(0,0,-1,-1);

	@Override
	public void paint(final Graphics g)
	{
		if( targetPosition != null )
		{
			final Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC) );

			g2d.setColor( Color.BLUE );
			g2d.fillOval( targetPosition.x - hintRadius ,targetPosition.y - hintRadius, hintRadius, hintRadius );
		}
	}

	public void setTargetPosition(final Point targetPosition)
	{
		boolean isUpdated = false;
		if( targetPosition != null && !targetPosition.equals( this.targetPosition ) )
		{
			this.targetPosition = targetPosition;
			isUpdated = true;
		}
		else if( targetPosition == null && this.targetPosition != null )
		{
			this.targetPosition = targetPosition;
			isUpdated = true;
		}

		if( isUpdated )
		{
			final Rectangle newDamageRectangle = new Rectangle( targetPosition.x - hintRadius,
					targetPosition.y - hintRadius,
					hintRadius,
					hintRadius );
			final Rectangle emitDamageRectangle = ( currentDamageRectangle == null ?
					newDamageRectangle : newDamageRectangle.union( currentDamageRectangle ) );
			currentDamageRectangle = newDamageRectangle;
			this.emitForceRepaintEvent( this, emitDamageRectangle );
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
