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
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class LayeredPaneDndTableMouseListener<A extends RackModelTableSpanningContents,
B extends SpanningContentsProperties, C extends Component & LayeredPaneTableComponent & GuiDndTableComponent>
	implements MouseMotionListener, MouseListener
{
	private static final Point OFFSCREEN_MOUSE_MOVE_POINT = new Point( -1, -1 );

	private static Log log = LogFactory.getLog( LayeredPaneDndTableMouseListener.class.getName());

	private final LayeredPaneDndTable<A, B, C> table;
	private final GuiDndTableState dndState;
	private final LayeredPaneDndTablePolicy<A,B,C> dndPolicy;
	private final LayeredPaneDndTableDecorationManager<A,B,C> decorationManager;

	private final TwoTuple<Point, C> componentAndLocalPoint = new TwoTuple<Point, C>( new Point(), null );

	public LayeredPaneDndTableMouseListener( final LayeredPaneDndTable<A, B, C> table,
			final GuiDndTableState dndState,
			final LayeredPaneDndTablePolicy<A,B,C> dndPolicy,
			final LayeredPaneDndTableDecorationManager<A,B,C> decorationManager )
	{
		this.table = table;
		this.dndState = dndState;
		this.dndPolicy = dndPolicy;
		this.decorationManager = decorationManager;
	}

	@Override
	public void mouseDragged(final MouseEvent e)
	{
		try
		{
			final State curState = dndState.getCurrentState();
			final Point tablePoint = e.getPoint();
//			log.debug("Mouse dragged point: " + tablePoint);
			final boolean foundIt = table.getComponentAtWithLocalPoint( tablePoint, componentAndLocalPoint );

			Point localPoint = null;
			C component = null;
			if( foundIt )
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
		catch(final Exception e1)
		{
			final String msg = "Exception caught during mouse drag processing: " + e1.toString();
			log.error( msg, e1 );
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e)
	{
		try
		{
			final State curState = dndState.getCurrentState();
			final Point tablePoint = e.getPoint();
//			log.debug("Mouse moved point: " + tablePoint);
			decorationManager.setMouseLocation( tablePoint );
			final boolean foundIt = table.getComponentAtWithLocalPoint( tablePoint, componentAndLocalPoint );
			Point point = null;
			C component = null;
			if( foundIt )
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
				// Probably a bit of mouse movement after releasing a drag
				// but we haven't processed the drag end yet.
				if( !point.equals( OFFSCREEN_MOUSE_MOVE_POINT ) )
				{
					log.error("Got a 'during_drag' state but we shouldn't receive these.");
					// Reset the state
					dndState.changeTo( State.BROWSING );
				}
				else
				{
					// It's okay, expected "clearUpDecorations"
				}
				break;
			default:
				break;
			}
		}
		catch(final Exception e1)
		{
			final String msg = "Exception caught during mouse movement processing: " + e1.toString();
			log.error( msg, e1 );
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
//		log.debug(e);
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
//		log.debug(e);
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
//		log.debug(e);
		final State currentState = dndState.getCurrentState();
		switch( currentState )
		{
		case MOUSE_OVER_DRAGGABLE_AREA:
			try
			{
				// Hack to clear up any outstanding selection
				dndPolicy.isMouseOverDndSource( table, null, null, null );
				dndState.changeTo( State.BROWSING );
			}
			catch (final BadStateTransitionException e1)
			{
				final String msg = "Error transitioning to browsing on mouse exit.";
				log.error( msg, e1 );
			}
			break;
		default:
			break;
		}
		// Hack to clear up any selection
		clearUpDecorations();
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		// Popup test
		if( e.isPopupTrigger() )
		{
			final Point dragStartPoint = e.getPoint();
			final boolean foundIt = table.getComponentAtWithLocalPoint( dragStartPoint, componentAndLocalPoint );
			Point localPoint = null;
			C component = null;
			if( foundIt )
			{
				localPoint = componentAndLocalPoint.getHead();
				component = componentAndLocalPoint.getTail();
			}

			if( dndPolicy.isMouseOverPopupSource( table, component, localPoint, dragStartPoint ) )
			{
				try
				{
					dndState.changeTo( State.POPUP );
					dndPolicy.doPopup( table, component, localPoint, dragStartPoint );
					dndState.changeTo( State.BROWSING );
				}
				catch( final BadStateTransitionException bste )
				{
					final String msg = "Bad state transition attempting to perform popup";
					log.error( msg, bste );
				}
			}
		}
		else
		{
	//		log.debug(e);
			try
			{
				final State currentState = dndState.getCurrentState();
				switch( currentState )
				{
				case MOUSE_OVER_DRAGGABLE_AREA:
					// Begin a drag
					final Point dragStartPoint = e.getPoint();
					final boolean foundIt = table.getComponentAtWithLocalPoint( dragStartPoint, componentAndLocalPoint );
					Point localPoint = null;
					C component = null;
					if( foundIt )
					{
						localPoint = componentAndLocalPoint.getHead();
						component = componentAndLocalPoint.getTail();
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
			catch(final Exception e1)
			{
				final String msg = "Exception caught processing mouse press: " + e1.toString();
				log.error( msg, e1 );
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
//		log.debug("Mouse released.");
		// Popup test
		if( e.isPopupTrigger() )
		{
			final Point dragStartPoint = e.getPoint();
			final boolean foundIt = table.getComponentAtWithLocalPoint( dragStartPoint, componentAndLocalPoint );
			Point localPoint = null;
			C component = null;
			if( foundIt )
			{
				localPoint = componentAndLocalPoint.getHead();
				component = componentAndLocalPoint.getTail();
			}

			if( dndPolicy.isMouseOverPopupSource( table, component, localPoint, dragStartPoint ) )
			{
				dndPolicy.doPopup( table, component, localPoint, dragStartPoint );
			}
		}
		else
		{
	//		log.debug(e);
			try
			{
				final State curState = dndState.getCurrentState();
				final Point tablePoint = e.getPoint();
				final boolean foundIt = table.getComponentAtWithLocalPoint( tablePoint, componentAndLocalPoint );
				Point point = null;
				C component = null;
				if( foundIt )
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
//						log.debug("Ended drag.");
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
//				log.debug("Synthesising mouse move.");
				synthesiseMouseMove( e );
			}
			catch(final Exception e1)
			{
				final String msg = "Exception caught during mouse release processing: " + e1.toString();
				log.error( msg, e1 );
			}
		}
	}

	private void synthesiseMouseMove( final MouseEvent e )
	{
		// Synthesise a mouse moved event so that hover criteria etc are setup correctly
		this.mouseMoved( e );
	}

	private void clearUpDecorations()
	{
		decorationManager.setMouseLocation( OFFSCREEN_MOUSE_MOVE_POINT );
	}
}
