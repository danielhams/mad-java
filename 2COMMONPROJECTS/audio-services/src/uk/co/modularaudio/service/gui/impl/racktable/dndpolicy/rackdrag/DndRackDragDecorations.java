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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag;

import java.util.ArrayList;

import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorationHint;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorations;

public class DndRackDragDecorations extends LayeredPaneDndTableDecorations
{
	// Something to draw the "red" or "green" box around the accepted region
	private final DndRackDragGhostHintDecoration ghostHintDecorator = new DndRackDragGhostHintDecoration();
	private final DndRackDragRegionHintDecoration regionHintDecorator = new DndRackDragRegionHintDecoration();

	public DndRackDragDecorations()
	{
		hints = new ArrayList<LayeredPaneDndTableDecorationHint>();
		hints.add( regionHintDecorator );
		hints.add( ghostHintDecorator );
	}

	public DndRackDragGhostHintDecoration getGhostHintDecorator()
	{
		return ghostHintDecorator;
	}

	public DndRackDragRegionHintDecoration getRegionHintDecorator()
	{
		return regionHintDecorator;
	}
}
