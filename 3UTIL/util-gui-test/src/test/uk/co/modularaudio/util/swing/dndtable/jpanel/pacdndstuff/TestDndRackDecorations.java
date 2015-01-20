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

import java.util.ArrayList;

import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTableDecorationHint;
import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTableDecorations;

public class TestDndRackDecorations extends JPanelDndTableDecorations
{
	private static final boolean SHOW_TARGET_POSITION_HINT = false;
	
	// Something to draw the "red" or "green" box around the accepted region
	private TestDndRackDragGhostHintDecoration ghostHintDecorator = new TestDndRackDragGhostHintDecoration();
	private TestDndRackDragRegionHintDecoration regionHintDecorator = new TestDndRackDragRegionHintDecoration();
	private TestDndRackDragTargetPositionHintDecorator targetPositionHintDecorator = new TestDndRackDragTargetPositionHintDecorator();

	public TestDndRackDecorations()
	{
		hints = new ArrayList<JPanelDndTableDecorationHint>();
		hints.add( regionHintDecorator );
		if( SHOW_TARGET_POSITION_HINT )
		{
			hints.add( targetPositionHintDecorator );
		}
		hints.add( ghostHintDecorator );

	}

	public static boolean isShowTargetPositionHint()
	{
		return SHOW_TARGET_POSITION_HINT;
	}

	public TestDndRackDragGhostHintDecoration getGhostHintDecorator()
	{
		return ghostHintDecorator;
	}

	public TestDndRackDragRegionHintDecoration getRegionHintDecorator()
	{
		return regionHintDecorator;
	}

	public TestDndRackDragTargetPositionHintDecorator getTargetPositionHintDecorator()
	{
		return targetPositionHintDecorator;
	}
}
