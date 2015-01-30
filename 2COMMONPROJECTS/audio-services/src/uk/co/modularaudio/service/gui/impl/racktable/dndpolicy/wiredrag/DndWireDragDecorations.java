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

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorations;

public class DndWireDragDecorations extends LayeredPaneDndTableDecorations
{
	private final DndWireDragCurrentWireHint currentWireDecorationHint;
	private final DndWireDragStaticRegionDecorationHint sourcePlugDecorationHint;
	private final DndWireDragPlugNameTooltipHint plugNameTooltipHint;

	public DndWireDragDecorations( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		sourcePlugDecorationHint = new DndWireDragStaticRegionDecorationHint();
		currentWireDecorationHint = new DndWireDragCurrentWireHint( bufferedImageAllocationService );
		plugNameTooltipHint = new DndWireDragPlugNameTooltipHint();

		hints.add( sourcePlugDecorationHint );
		hints.add( currentWireDecorationHint );
		hints.add( plugNameTooltipHint );
	}

	public DndWireDragCurrentWireHint getCurrentWireDecorationHint()
	{
		return currentWireDecorationHint;
	}

	public DndWireDragStaticRegionDecorationHint getSourcePlugDecorationHint()
	{
		return sourcePlugDecorationHint;
	}

	public DndWireDragPlugNameTooltipHint getPlugNameTooltipHint()
	{
		return plugNameTooltipHint;
	}

}
