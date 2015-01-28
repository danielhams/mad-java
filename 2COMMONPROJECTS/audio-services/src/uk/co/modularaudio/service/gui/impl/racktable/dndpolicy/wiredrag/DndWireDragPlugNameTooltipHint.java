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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;

public class DndWireDragPlugNameTooltipHint extends LayeredPaneDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( NewDndWireDragPlugNameTooltipHint.class.getName() );

	private final JLabel nameLabel;

	private final Rectangle myBounds = new Rectangle();

	public DndWireDragPlugNameTooltipHint()
	{
		final Color fgColor = Color.WHITE;
		final Color bgColor = Color.BLACK;
		nameLabel = new JLabel();
		nameLabel.setOpaque( true );
		nameLabel.setBackground( bgColor );
		nameLabel.setForeground( fgColor );
		nameLabel.setBorder( new LineBorder( fgColor, 1 ) );

		setPlugName( "Tooltip thingy" );
	}

	public void setPlugName( final String plugName )
	{
		nameLabel.setText( plugName );

		final int xBefore = myBounds.x;
		final int yBefore = myBounds.y;
		nameLabel.revalidate();
		final Dimension size = nameLabel.getPreferredSize();
		nameLabel.setBounds( xBefore, yBefore, size.width, size.height );

		nameLabel.getBounds( myBounds );
		myBounds.x = xBefore;
		myBounds.y = yBefore;
//		log.debug("Got bounds during plug name set of " + myBounds.toString() );
	}

	@Override
	public void setActive( final boolean activeBool )
	{
//		log.debug("SetActive called with " + activeBool );
		active = activeBool;
		if( activeBool )
		{
			nameLabel.setBounds( myBounds );
			table.setLayer( nameLabel, LayeredPaneDndTable.LPT_TOOLTIP_LAYER );
			table.add( nameLabel );
			table.repaint( myBounds );
		}
		else
		{
			table.remove( nameLabel );
			table.repaint( myBounds );
		}
	}

	@Override
	public boolean isMouseRelative()
	{
		return false;
	}

	@Override
	public void setMousePosition( final Point mousePosition )
	{
//		log.debug("Received mouse position " + mousePosition.toString() );

		myBounds.x = mousePosition.x + 10;
		myBounds.y = mousePosition.y + 10;
		if( isActive() )
		{
			nameLabel.setBounds( myBounds );
		}
//		myBounds.x = 300;
//		myBounds.y = 300;
	}

	@Override
	public void signalAnimation()
	{
		// No animation
	}

}
