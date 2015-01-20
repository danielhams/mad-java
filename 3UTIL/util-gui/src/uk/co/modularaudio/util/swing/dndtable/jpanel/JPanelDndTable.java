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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTable;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponentToGuiFactory;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;
import uk.co.modularaudio.util.swing.table.jpanel.JPanelTable;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class JPanelDndTable<A extends RackModelTableSpanningContents,
	B extends SpanningContentsProperties,
	C extends Component & GuiDndTableComponent>
	extends JPanelTable<A, B, C>
	implements GuiDndTable<A, B, C>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8519000108823657929L;

//	private static Log log = LogFactory.getLog( SwingDndTable.class.getName() );
	
	private GuiDndTableState dndState = null;
//	private SwingDndTablePolicy<A,B,C> dndPolicy = null;
	private JPanelDndTableMouseListener<A,B,C> dndMouseListener = null;
	private JPanelDndTableDecorationManager<A,B,C> dndDecorationManager = null;

	public JPanelDndTable( TableInterface<A, B> dataModel,
			GuiTableEmptyCellPainter emptyCellPainter,
			GuiDndTableComponentToGuiFactory<A,C> factory,
			JPanelDndTablePolicy<A,B,C> dndPolicy,
			JPanelDndTableDecorations dndDecorations,
			Dimension gridSize,
			boolean showGrid,
			Color gridColour)
	{
		super(dataModel, emptyCellPainter, factory, gridSize, showGrid, gridColour);
		
		// For DnD we use
		// (1) A state tracking object that notified interested parties
		// (2) A mouse listener that takes mouse co-ordinates and allows the policy object to determine how / if to call the
		//       SwingDnDComponent the mouse is over
		// (3) Custom paint logic so that when a "drag" is underway we can over-paint with ticks / crosses type thing
//		this.dndPolicy = dndPolicy;
		this.dndState = new GuiDndTableState(GuiDndTableState.State.BROWSING);
		this.dndDecorationManager = new JPanelDndTableDecorationManager<A,B,C>( this, dndState, dndPolicy, dndDecorations );
		this.dndMouseListener = new JPanelDndTableMouseListener<A,B,C>( this, dndState, dndPolicy, dndDecorationManager );
		this.addMouseListener( dndMouseListener );
		this.addMouseMotionListener( dndMouseListener );
		dndState.addTransitionListener( dndDecorationManager );
	}

	public TwoTuple<Point, C> getComponentAtWithLocalPoint( Point rsp )
	{
		TwoTuple<Point, C> retVal = null;
		Point tableIndexes = pointToTableIndexes( rsp );
		if( tableIndexes != null )
		{
			A tableComponent = dataModel.getContentsAtPosition( tableIndexes.x, tableIndexes.y );
			if( tableComponent != null )
			{
				C swingComponent = getGuiComponentFromTableModel( tableComponent );
				if( swingComponent != null )
				{
					// Work out the remained from the component width
					Point remainder = new Point( rsp.x - swingComponent.getX(), rsp.y - swingComponent.getY() );
					retVal = new TwoTuple<Point,C>( remainder, swingComponent );
				}
			}
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public TwoTuple<Point, C> oldgetComponentAtWithLocalPoint( Point rsp )
	{
		TwoTuple<Point, C> retVal = null;
		C c = null;
		int localX = -1;
		int localY = -1;
		Point p = null;

		synchronized (getTreeLock())
		{
			// Two passes: see comment in sun.awt.SunGraphicsCallback
			for (int i = 0; i < this.getComponentCount() ; i++)
			{
				Component comp = this.getComponent( i );
				if (comp != null )
				{
					int tmpX = rsp.x - comp.getX();
					int tmpY = rsp.y - comp.getY();
					if (comp.contains(tmpX, tmpY))
					{
						localX = tmpX;
						localY = tmpY;
						c = (C) comp;
					}
				}
			}
			for (int i = 0; i < this.getComponentCount() ; i++)
			{
				Component comp = this.getComponent( i );
				if (comp != null )
				{
					int tmpX = rsp.x - comp.getX();
					int tmpY = rsp.y - comp.getY();
					if (comp.contains( tmpX, tmpY))
					{
						localX = tmpX;
						localY = tmpY;
						c = (C) comp;
					}
				}
			}
		}
		
		if( c != null )
		{
			p = new Point( localX, localY );
			retVal = new TwoTuple<Point, C>( p, c);
		}

		return retVal;
	}


	@Override
	public void paint(Graphics g)
	{
		super.swingTablePaint( g );
		
		swingDndTablePaint( g );
	}
	
	public void swingDndTablePaint( Graphics g )
	{
		dndDecorationManager.paint( g );
	}
}
