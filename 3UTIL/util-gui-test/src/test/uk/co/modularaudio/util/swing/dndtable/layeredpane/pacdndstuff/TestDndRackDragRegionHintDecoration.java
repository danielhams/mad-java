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

import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;

public class TestDndRackDragRegionHintDecoration extends LayeredPaneDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( TestDndRackDragRegionHintDecoration.class.getName() );

	private RegionHintType hintType = null;
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;

	private boolean isActive = false;

	private final TestDndRackTargetRegion targetRegionComponent = new TestDndRackTargetRegion();

	public TestDndRackDragRegionHintDecoration()
	{
	}

	public void setRegionHint( final RegionHintType hintType, final int x, final int y, final int width, final int height )
	{
		boolean isUpdated = false;

		if( this.x != x || this.y != y  || this.width != width || this.height != height )
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			isUpdated = true;
		}

		if( this.hintType != hintType )
		{
			this.hintType = hintType;
			isUpdated = true;
		}

		if( isUpdated )
		{
			// TODO actually implement the hint
//			log.debug("RegionHint notices an update to be done!");
			targetRegionComponent.setRegionType( hintType );
			targetRegionComponent.setSize( width, height );
			targetRegionComponent.setLocation( x, y );
		}
	}

	@Override
	public boolean isMouseRelative()
	{
		return false;
	}

	@Override
	public void setMousePosition(final Point mousePosition)
	{
	}

	public enum RegionHintType
	{
		SOURCE, VALID, INVALID
	}

	@Override
	public void setActive( final boolean activeBool )
	{
		if( isActive != activeBool )
		{
//			log.debug("setActive called with " + activeBool + " so will change state..");
			isActive = activeBool;
			if( !isActive )
			{
				table.remove( targetRegionComponent );
				table.repaint( targetRegionComponent.getBounds() );
			}
			else
			{
				table.add( targetRegionComponent, LayeredPaneDndTable.LPT_POSITIONHINT_LAYER, 0 );
				targetRegionComponent.setLocation( x, y );
				table.repaint( targetRegionComponent.getBounds() );
			}
		}

	}

	@Override
	public void signalAnimation()
	{
		// No animation
	}
}
