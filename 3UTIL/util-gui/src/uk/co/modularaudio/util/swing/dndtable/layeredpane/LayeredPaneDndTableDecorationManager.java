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

package uk.co.modularaudio.util.swing.dndtable.layeredpane;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.GuiDndTableStateTransitionListener;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.State;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public class LayeredPaneDndTableDecorationManager<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties,
C extends Component & LayeredPaneTableComponent & GuiDndTableComponent> implements GuiDndTableStateTransitionListener
{
//	private static Log log = LogFactory.getLog( LayeredPaneDndTableDecorationManager.class.getName() );

	private static final Cursor OVER_DRAG_AREA_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR );
	private static final Cursor DURING_DRAG_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR );
	private final LayeredPaneDndTable<A, B, C> table;

	private final LayeredPaneDndTableDecorator tableDecorator;
	private final LayeredPaneDndTableMouseFollowAnimationTimer mouseFollowThread;

	public LayeredPaneDndTableDecorationManager( final LayeredPaneDndTable<A,B,C> table,
			final GuiDndTableState state,
			final LayeredPaneDndTablePolicy<A,B,C> dndPolicy,
			final LayeredPaneDndTableDecorations decorations )
	{
		this.table = table;

		// Aggregate of decorators used at runtime
		tableDecorator = new LayeredPaneDndTableDecorator( table, state, decorations );

		mouseFollowThread = new LayeredPaneDndTableMouseFollowAnimationTimer( tableDecorator );
	}

	@Override
	public void receiveTransition(final State stateBefore, final State stateAfter)
	{
		switch( stateAfter )
		{
		case BROWSING:
//			log.debug("Setting cursor to null");
			table.setCursor( null );
			mouseFollowThread.stop();
			break;
		case MOUSE_OVER_DRAGGABLE_AREA:
//			log.debug("Setting cursor to over drag");
			table.setCursor( OVER_DRAG_AREA_CURSOR );
			mouseFollowThread.stop();
			break;
		case DURING_DRAG:
//			log.debug("Setting cursor to during drag");
			table.setCursor( DURING_DRAG_CURSOR );
			// Start our thread that loops around checking to see if we need to do a repaint every Xms
			// and calls repaint on the table if need be.
			mouseFollowThread.start();
			break;
		default:
			{
			}
		}
	}

	public void setMouseLocation(final Point tablePoint)
	{
		tableDecorator.setMousePosition( tablePoint );
	}
}
