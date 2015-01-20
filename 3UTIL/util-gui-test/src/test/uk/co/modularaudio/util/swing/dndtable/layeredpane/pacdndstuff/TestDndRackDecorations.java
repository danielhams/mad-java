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

import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorations;

public class TestDndRackDecorations extends LayeredPaneDndTableDecorations
{
	// Something to draw the "red" or "green" box around the accepted region
	private TestDndRackDragGhostHintDecoration ghostHintDecorator = new TestDndRackDragGhostHintDecoration();
	private TestDndRackDragRegionHintDecoration regionHintDecorator = new TestDndRackDragRegionHintDecoration();

	public TestDndRackDecorations()
	{
		hints.add( regionHintDecorator );
		hints.add( ghostHintDecorator );
	}

	public TestDndRackDragGhostHintDecoration getGhostHintDecorator()
	{
		return ghostHintDecorator;
	}

	public TestDndRackDragRegionHintDecoration getRegionHintDecorator()
	{
		return regionHintDecorator;
	}
}
