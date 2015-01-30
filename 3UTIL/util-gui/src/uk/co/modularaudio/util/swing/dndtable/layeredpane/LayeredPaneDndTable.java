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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTable;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponentToGuiFactory;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTable;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class LayeredPaneDndTable<A extends RackModelTableSpanningContents,
	B extends SpanningContentsProperties,
	C extends Component & LayeredPaneTableComponent & GuiDndTableComponent>
	extends LayeredPaneTable<A, B, C>
	implements GuiDndTable<A, B, C>
{
	private static final long serialVersionUID = -8519000108823657929L;

	private static Log log = LogFactory.getLog( LayeredPaneDndTable.class.getName() );

	protected GuiDndTableState dndState;
	protected LayeredPaneDndTableMouseListener<A,B,C> dndMouseListener;
	protected LayeredPaneDndTableDecorationManager<A,B,C> dndDecorationManager;

	protected boolean forcePaint = false;

	private final int[] tableIndexesPoint = new int[2];

	public LayeredPaneDndTable( final TableInterface<A, B> dataModel,
			final GuiDndTableComponentToGuiFactory<A,C> factory,
			final LayeredPaneDndTablePolicy<A,B,C> dndPolicy,
			final LayeredPaneDndTableDecorations decorations,
			final Dimension gridSize,
			final boolean showGrid,
			final Color gridColour,
			final GuiTableEmptyCellPainter emptyCellPainter)
	{
		super(dataModel, factory, gridSize, showGrid, gridColour, emptyCellPainter );

		// For DnD we use
		// (1) A state tracking object that notified interested parties
		// (2) A mouse listener that takes mouse co-ordinates and allows the policy object to determine how / if to call the
		//       DnDComponent the mouse is over
		// (3) Custom paint logic so that when a "drag" is underway we can over-paint with ticks / crosses type thing

		this.dndState = new GuiDndTableState( GuiDndTableState.State.BROWSING);
		this.dndDecorationManager = new LayeredPaneDndTableDecorationManager<A,B,C>( this, dndState, dndPolicy, decorations );
		this.dndMouseListener = new LayeredPaneDndTableMouseListener<A,B,C>( this, dndState, dndPolicy, dndDecorationManager );
		this.addMouseListener( dndMouseListener );
		this.addMouseMotionListener( dndMouseListener );
		dndState.addTransitionListener( dndDecorationManager );
	}

	public boolean getComponentAtWithLocalPoint( final Point rsp, final TwoTuple<Point, C> destination )
	{
		boolean retVal = false;
//		log.debug("Searching for component at " + rsp.toString() );
		final int[] compIndexes = new int[2];
		compIndexes[0] = rsp.x;
		compIndexes[1] = rsp.y;
		pointToTableIndexes( compIndexes, tableIndexesPoint );

		final A tableComponent = dataModel.getContentsAtPosition( tableIndexesPoint[0], tableIndexesPoint[1] );
		if( tableComponent != null )
		{
			final C swingComponent = getGuiComponentFromTableModel( tableComponent );
			if( swingComponent != null )
			{
				// Work out the remained from the component width
				retVal = true;
				final Point thePoint = destination.getHead();
				thePoint.x = compIndexes[0] - swingComponent.getX();
				thePoint.y = compIndexes[1] - swingComponent.getY();
				destination.setTail( swingComponent );
//				log.debug("Found one returning: " + thePoint.toString() + " " + swingComponent.getClass().getSimpleName() );
			}
		}

		return retVal;
	}

	@Override
	public void paint(final Graphics g)
	{
		final boolean showing = isShowing();
		final boolean visible = isVisible();
//		log.debug("Attempted repaint...");
		if( forcePaint || (showing && visible) )
		{
//			if( log.isDebugEnabled() )
//			{
//				Rectangle clipBounds = g.getClipBounds();
//				log.debug("Repainting..." + clipBounds.toString() );
//			}
			super.layeredTablePaint( g );
		}
		else
		{
			log.debug("Skipped repaint.");
		}
	}

}
