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

package test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff;

import java.awt.Point;
import java.awt.Rectangle;

import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.TestGC;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;

public class TestDndRackDragGhostHintDecoration extends LayeredPaneDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( TestDndRackDragGhostHintDecoration.class.getName() );
	
	private Point mouseOffset = new Point(0,0);
	
	private boolean isActive = false;

	private TestGC ghostGuiComponent = null;

	@Override
	public boolean isMouseRelative()
	{
		return true;
	}

	public void setComponentAndOffset( TestGC dragSourceGuiComponent, Point dragSourceMouseOffset)
	{
		boolean isUpdated = false;
		if( mouseOffset != null && !mouseOffset.equals( this.mouseOffset ) )
		{
			isUpdated = true;
		}
		else if( mouseOffset == null && this.mouseOffset != null )
		{
			isUpdated = true;
		}
	
		this.ghostGuiComponent = dragSourceGuiComponent;
		this.mouseOffset = dragSourceMouseOffset;
		
		if( isUpdated )
		{
			// Not sure I need to do anything in here...
		}
	}

	private Point curMousePosition = new Point(0,0);

	@Override
	public void setMousePosition( Point mousePosition )
	{
		curMousePosition = mousePosition;
	}

	@Override
	public void setActive( boolean activeBool )
	{
		if( isActive != activeBool )
		{
			isActive = activeBool;

			int layer;
			if( isActive )
			{
				layer = LayeredPaneDndTable.LPT_DRAGGEDCOMPONENTS_LAYER;
			}
			else
			{
				layer = LayeredPaneDndTable.LPT_COMPONENTS_LAYER;
			}
//			log.debug("setActive called with " + activeBool + " and is different from existing value. Will re-initialise the ghost into layer " + layer);

			table.setLayer( ghostGuiComponent, layer );

		}
	}
	
	@Override
	public void signalAnimation()
	{
		if( isActive )
		{
//			log.debug("Attempting to move it ");
			// Work out the new position for the component and update it
			Rectangle previousBounds = ghostGuiComponent.getBounds();
			// Necessary?
//			ghostGuiComponent.setOpaque( false );
			int newX = curMousePosition.x + mouseOffset.x;
			int newY = curMousePosition.y + mouseOffset.y;
			if( previousBounds.x != newX || previousBounds.y != newY )
			{
				ghostGuiComponent.setLocation( newX, newY );
				Rectangle newBounds = ghostGuiComponent.getBounds();
				previousBounds.width++;
				previousBounds.height++;
				newBounds.width++;
				newBounds.height++;
//				log.debug("Doing repaints of pb:(" + previousBounds.toString() + ") and nb:(" + newBounds + ")");
				table.repaint( previousBounds );
				table.repaint( newBounds );
			}
		}
	}

}
