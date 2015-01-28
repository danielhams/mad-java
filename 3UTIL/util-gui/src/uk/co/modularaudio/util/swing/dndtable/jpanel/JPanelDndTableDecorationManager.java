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

package uk.co.modularaudio.util.swing.dndtable.jpanel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.GuiDndTableStateTransitionListener;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.State;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public class JPanelDndTableDecorationManager<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties,
C extends Component & GuiDndTableComponent> implements GuiDndTableStateTransitionListener
{
	private static final Cursor OVER_DRAG_AREA_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR );
	private static final Cursor DURING_DRAG_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR );
	private final JPanelDndTable<A, B, C> table;
	private final JPanelDndTableDecorator tableDecorator;
	private final JPanelDndTableMouseFollowAnimationTimer mouseFollowThread;

	public JPanelDndTableDecorationManager( final JPanelDndTable<A,B,C> table,
			final GuiDndTableState state,
			final JPanelDndTablePolicy<A,B,C> dndPolicy,
			final JPanelDndTableDecorations decorations )
	{
		this.table = table;

		// Fetch the image we will be "dragging" along with it's offset from the mouse pointer position
		tableDecorator = new JPanelDndTableDecorator( table, state, decorations );

		mouseFollowThread = new JPanelDndTableMouseFollowAnimationTimer( tableDecorator );
	}

	@Override
	public void receiveTransition(final State stateBefore, final State stateAfter)
	{
		switch( stateAfter )
		{
		case BROWSING:
			table.setCursor( null );
			mouseFollowThread.stop();
			break;
		case MOUSE_OVER_DRAGGABLE_AREA:
			table.setCursor( OVER_DRAG_AREA_CURSOR );
			mouseFollowThread.stop();
			break;
		case DURING_DRAG:
			table.setCursor( DURING_DRAG_CURSOR );
			// Start our thread that loops around checking to see if we need to do a repaint every 100ms
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

	public void paint(final Graphics g)
	{
		tableDecorator.paint( g );
	}
}
