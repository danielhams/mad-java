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
import java.awt.Rectangle;

import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.DndTargetRegionImageGenerator;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.RegionHintType;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;

public class DndWireDragStaticRegionDecorationHint extends LayeredPaneDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( NewDndWireDragStaticRegionDecorationHint.class.getName() );

	private RegionHintType hintType;
	private Rectangle hintRectangle = new Rectangle( 0, 0, 0, 0 );
	private Rectangle previousRenderRectangle;

	private final DndTargetRegionImageGenerator targetRegionGenerator = new DndTargetRegionImageGenerator();

	public DndWireDragStaticRegionDecorationHint()
	{
	}

	public void setRegionHint( final RegionHintType hintType, final Rectangle incomingHintRectangle, final Rectangle renderRectangle )
	{
		boolean isUpdated = false;

		if( !hintRectangle.equals( incomingHintRectangle ) )
		{
			this.hintRectangle = incomingHintRectangle;
			isUpdated = true;
		}

		if( this.hintType != hintType )
		{
			this.hintType = hintType;
			isUpdated = true;
		}

		if( previousRenderRectangle != null && !previousRenderRectangle.equals( renderRectangle ) )
		{
			isUpdated = true;
		}
		else if( previousRenderRectangle == null && renderRectangle != null ||
				previousRenderRectangle != null && renderRectangle == null )
		{
			isUpdated = true;
		}

		if( isUpdated )
		{
			this.previousRenderRectangle = renderRectangle;
			this.hintRectangle = incomingHintRectangle;

//			log.debug("RegionHint notices an update to be done!");
			targetRegionGenerator.setRegionType( this.hintType );
			targetRegionGenerator.setSize( renderRectangle.width, renderRectangle.height );
			targetRegionGenerator.setLocation( hintRectangle.x + renderRectangle.x, hintRectangle.y + renderRectangle.y );
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

	@Override
	public void setActive( final boolean activeBool )
	{
		if( active != activeBool )
		{
//			log.debug("setActive called with " + activeBool + " so will change state..");
			active = activeBool;
			if( !active )
			{
				table.remove( targetRegionGenerator );
				table.repaint( targetRegionGenerator.getBounds() );
			}
			else
			{
				table.add( targetRegionGenerator, LayeredPaneDndTable.LPT_POSITIONHINT_LAYER, 0 );
				targetRegionGenerator.setLocation( hintRectangle.x + previousRenderRectangle.x, hintRectangle.y + previousRenderRectangle.y );
				table.repaint( targetRegionGenerator.getBounds() );
			}
		}

	}

	@Override
	public void signalAnimation()
	{
		// No animation
	}
}
