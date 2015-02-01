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

import java.awt.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;

public class DndWireDragCurrentWireHint extends LayeredPaneDndTableDecorationHint
{
	private static Log log = LogFactory.getLog( DndWireDragCurrentWireHint.class.getName() );

	private Point sourceLocation = new Point(-1,-1);
	private Point curMousePosition = new Point(-1,-1);

	private Point previousSourceLocation = new Point( -1, -1 );
	private Point previousMouseLocation = new Point( -1, -1 );

	private final DndCurrentWireImage currentWireImage;

	public DndWireDragCurrentWireHint( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		currentWireImage = new DndCurrentWireImage( bufferedImageAllocationService );
	}

	@Override
	public boolean isMouseRelative()
	{
		// We don't want it to move with the mouse, we'll use the animation timer to prompt re-drawings
		return false;
	}

	@Override
	public void setMousePosition(final Point mousePosition)
	{
		curMousePosition = mousePosition;
	}

	public void setDragStartPosition( final Point sourceLocation )
	{
		this.sourceLocation = sourceLocation;
	}

	@Override
	public void setActive( final boolean activeBool )
	{
		try
		{
			if( active != activeBool )
			{
//				log.debug("setActive called with " + activeBool + " so will change state..");
				active = activeBool;
				if( !active )
				{
//					log.debug("Removing current wire in ");
					table.remove( currentWireImage );
					table.repaint( currentWireImage.getBounds() );
					currentWireImage.setWirePosition( null, null );
				}
				else
				{
//					log.debug("Adding current wire in ");
					currentWireImage.setWirePosition( sourceLocation, curMousePosition );

					table.setLayer( currentWireImage, LayeredPaneDndTable.LPT_DRAGGEDWIRE_LAYER );
					table.add( currentWireImage );

					table.repaint( currentWireImage.getBounds() );
				}
			}
		}
		catch ( final Exception e)
		{
			final String msg ="Exception caught setting wire drag hint active: " + e.toString();
			log.error( msg, e );
		}

	}

	@Override
	public void signalAnimation()
	{
		try
		{
			// If the positions are different to when we last generated the wire image, regenerate it and request a repaint
			if( curMousePosition != previousMouseLocation ||
					sourceLocation  != previousSourceLocation )
			{
				if( active )
				{
//					log.debug("Animation noticed a movement - will update current wire");
					currentWireImage.setWirePosition( sourceLocation, curMousePosition );
					previousMouseLocation = curMousePosition;
					previousSourceLocation = sourceLocation;
					currentWireImage.repaint();
				}
			}
		}
		catch ( final Exception e )
		{
			final String msg ="Exception caught setting wire drag hint active: " + e.toString();
			log.error( msg, e );
		}
	}

}
