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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.BadStateTransitionException;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState.State;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class JPanelDndTableMouseListener<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties, C extends Component & GuiDndTableComponent>
	implements MouseMotionListener, MouseListener
{
	private static Log log = LogFactory.getLog( JPanelDndTableMouseListener.class.getName());
	
	private JPanelDndTable<A, B, C> table;
	private GuiDndTableState dndState = null;
	private JPanelDndTablePolicy<A,B,C> dndPolicy = null;
	private JPanelDndTableDecorationManager<A,B,C> decorationManager = null;

	public JPanelDndTableMouseListener(JPanelDndTable<A, B, C> table,
			GuiDndTableState dndState,
			JPanelDndTablePolicy<A,B,C> dndPolicy,
			JPanelDndTableDecorationManager<A,B,C> decorationManager )
	{
		this.table = table;
		this.dndState = dndState;
		this.dndPolicy = dndPolicy;
		this.decorationManager = decorationManager;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		try
		{
			State curState = dndState.getCurrentState();
			Point tablePoint = e.getPoint();
//			log.debug("Mouse dragged point: " + tablePoint);
			TwoTuple<Point, C> componentAndLocalPoint = table.getComponentAtWithLocalPoint( tablePoint );
			Point localPoint = null;
			C component = null;
			if( componentAndLocalPoint != null )
			{
				localPoint = componentAndLocalPoint.getHead();
				component = componentAndLocalPoint.getTail();
			}
			switch( curState )
			{
			case DURING_DRAG:
				decorationManager.setMouseLocation( tablePoint );
				if( dndPolicy.isValidDragTarget( table, component, localPoint, tablePoint ) )
				{
//					log.debug("Is over valid drag!");
				}
				else
				{
//					log.debug("Is not over valid drag!");
				}
				break;
			case MOUSE_OVER_DRAGGABLE_AREA:
				log.error("Found browsing or over draggable area during a drag - but we shouldn't receive these.");
				// Reset the state
				dndState.changeTo( State.BROWSING );
				break;
			case BROWSING:
				// Do nothing.
			default:
				break;
			}
		}
		catch(Exception e1)
		{
			String msg = "Exception caught during mouse drag processing: " + e1.toString();
			log.error( msg, e1 );
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		try
		{
			State curState = dndState.getCurrentState();
			Point tablePoint = e.getPoint();
//			log.debug("Mouse moved point: " + tablePoint);
			TwoTuple<Point, C> componentAndLocalPoint = table.getComponentAtWithLocalPoint( tablePoint );
			Point point = null;
			C component = null;
			if( componentAndLocalPoint != null )
			{
				point = componentAndLocalPoint.getHead();
				component = componentAndLocalPoint.getTail();
			}
			switch( curState )
			{
			case BROWSING:
				if( dndPolicy.isMouseOverDndSource( table, component, point, tablePoint ) )
				{
					dndState.changeTo( State.MOUSE_OVER_DRAGGABLE_AREA);
				}
				break;
			case MOUSE_OVER_DRAGGABLE_AREA:
				if( !dndPolicy.isMouseOverDndSource( table, component, point, tablePoint ) )
				{
					dndState.changeTo( State.BROWSING );
				}
				break;
			case DURING_DRAG:
				log.error("Got a 'during_drag' state but we shouldn't receive these.");
				// Reset the state
				dndState.changeTo( State.BROWSING );
				break;
			default:
				break;
			}
		}
		catch(Exception e1)
		{
			String msg = "Exception caught during mouse movement processing: " + e1.toString();
			log.error( msg, e1 );
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
//		log.debug(e);
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
//		log.debug(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
//		log.debug(e);
		State currentState = dndState.getCurrentState();
		switch( currentState )
		{
		case MOUSE_OVER_DRAGGABLE_AREA:
			try
			{
				// Hack to clear up any outstanding selection
				dndPolicy.isMouseOverDndSource( table, null, null, null );
				dndState.changeTo( State.BROWSING );
			}
			catch (BadStateTransitionException e1)
			{
				String msg = "Error transitioning to browsing on mouse exit.";
				log.error( msg, e1 );
			}
			break;
		default:
			break;
		}
		// Hack to clear up any selection
		decorationManager.setMouseLocation( new Point( -1, -1 ) );
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
//		log.debug(e);
		try
		{
			State currentState = dndState.getCurrentState();
			switch( currentState )
			{
			case MOUSE_OVER_DRAGGABLE_AREA:
				// Begin a drag
				Point dragStartPoint = e.getPoint();
				TwoTuple<Point, C> pointAndComponent = table.getComponentAtWithLocalPoint( dragStartPoint );
				Point localPoint = null;
				C component = null;
				if( pointAndComponent != null )
				{		
					localPoint = pointAndComponent.getHead();
					component = pointAndComponent.getTail();
					dndPolicy.startDrag( table, component, localPoint, dragStartPoint );
					dndState.changeTo( State.DURING_DRAG );
					decorationManager.setMouseLocation( dragStartPoint );
				}
				else
				{
					log.error( "Failed to find drag source when clicked over a drag area.");
					dndState.changeTo( State.BROWSING );
				}
				break;
			default:
				break;
			}
		}
		catch(Exception e1)
		{
			String msg = "Exception caught processing mouse press: " + e1.toString();
			log.error( msg, e1 );
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
//		log.debug(e);
		try
		{
			State curState = dndState.getCurrentState();
			Point tablePoint = e.getPoint();
			TwoTuple<Point, C> componentAndLocalPoint = table.getComponentAtWithLocalPoint( tablePoint );
			Point point = null;
			C component = null;
			if( componentAndLocalPoint != null )
			{
				point = componentAndLocalPoint.getHead();
				component = componentAndLocalPoint.getTail();
			}
			switch( curState )
			{
			case DURING_DRAG:
				if( dndPolicy.isValidDragTarget( table, component, point, tablePoint ) )
				{
					dndPolicy.endDrag( table, component, point, tablePoint );
				}
				else
				{
					dndPolicy.endInvalidDrag( table, component, point, tablePoint );
//					log.debug("Is not over valid drag!");
				}
				// Now fall back into the browsing state
			case BROWSING:
			case MOUSE_OVER_DRAGGABLE_AREA:
				// Reset the state
				dndState.changeTo( State.BROWSING );
				break;
			default:
				break;
			}
//			decorationManager.setMouseLocation( tablePoint );
			// Synthesise a mouse moved event so that hover criteria etc are setup correctly
			this.mouseMoved( e );
		}
		catch(Exception e1)
		{
			String msg = "Exception caught during mouse release processing: " + e1.toString();
			log.error( msg, e1 );
		}
	}
}
