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

package uk.co.modularaudio.util.swing.scrollpane;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

public class AutoScrollingMouseListener extends MouseAdapter implements MouseMotionListener
{
	// 30hz updates
	private final static int SCROLL_CALLBACK_MILLIS = 1000 / 30;
	
	// If the mouse is further away that FAST_OFFSET_LIMIT, we scroll by FAST_SCROLL_OFFSET
	// otherwise we scroll by SLOW_SCROLL_OFFSET
	private static final int FAST_OFFSET_DISTANCE = 20;
	private static final int FAST_SCROLL_AMOUNT = 10;
	private static final int SLOW_SCROLL_AMOUNT = 3;

	private static final int MOUSE_EDGE_SCROLL_WIDTH = 20;

//	private static Log log = LogFactory.getLog( AutoScrollingMouseListener.class.getName() );
	
	private AutoScrollingTimerTask timerTask = null;
	private Timer autoScrollTime = null;
	
	public AutoScrollingMouseListener()
	{
	}
	
	public void start( JComponent compToScroll )
	{
		if( autoScrollTime == null )
		{
			timerTask = new AutoScrollingTimerTask( this, compToScroll);
			autoScrollTime = new Timer( SCROLL_CALLBACK_MILLIS, timerTask );
			autoScrollTime.start();
//			log.debug("Started.");
		}
	}
	
	public void stop()
	{
		if( autoScrollTime != null )
		{
			autoScrollTime.stop();
			autoScrollTime = null;
//			log.debug("Stopped.");
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		// For any mouse drag event events that end up outside of our target component visible rectangle, call
		// the scrollRectToVisible on it
		JComponent sourceComponent = (JComponent)e.getComponent();
		Rectangle origSourceComponentVisibleRect = sourceComponent.getVisibleRect();
		// Reduce the test rectangle we use to determine if we are at the edge of the component.
		// This will allow auto-scrolling behaviour to begin before the mouse actually exits the component.
		Rectangle sourceComponentVisibleRect = new Rectangle( origSourceComponentVisibleRect.x + MOUSE_EDGE_SCROLL_WIDTH,
				origSourceComponentVisibleRect.y + MOUSE_EDGE_SCROLL_WIDTH,
				origSourceComponentVisibleRect.width - (2 * MOUSE_EDGE_SCROLL_WIDTH ),
				origSourceComponentVisibleRect.height - ( 2 * MOUSE_EDGE_SCROLL_WIDTH ) );

//		log.debug("Bounds are " + sourceComponentVisibleRect);
		Point dragPoint = e.getPoint();
//		Point dragPoint = e.getLocationOnScreen();
		// Convert this screen point to one relative to the source component
//		log.debug("Screen point is  " + dragPoint);
//		SwingUtilities.convertPointFromScreen( dragPoint, sourceComponent );
//		log.debug("Local point relative to component is  " + dragPoint);
		if( sourceComponentVisibleRect.contains( dragPoint ) )
		{
			stop();
			// Do nothing
//			log.debug("Source component contains drag point, will ignore");
		}
		else
		{
			start( sourceComponent );
//			log.debug("Point not contained in source component, will attempt a scroll");
			// Work out the offset from the visible region
			int xOffset = 0;
			int yOffset = 0;
			
			int visibleRectLeftOffset = sourceComponentVisibleRect.x;
			int visibleRectRightOffset = visibleRectLeftOffset + sourceComponentVisibleRect.width;
			int visibleRectTopOffset = sourceComponentVisibleRect.y;
			int visibleRectBottomOffset = visibleRectTopOffset + sourceComponentVisibleRect.height;
			
			if( dragPoint.x < visibleRectLeftOffset )
			{
				xOffset = dragPoint.x - visibleRectLeftOffset;
			}
			else if( dragPoint.x > visibleRectRightOffset )
			{
				xOffset = dragPoint.x - visibleRectRightOffset;
			}
			else
			{
				xOffset = 0;
			}

			if( dragPoint.y < visibleRectTopOffset )
			{
				yOffset = dragPoint.y - visibleRectTopOffset;
			}
			else if( dragPoint.y > visibleRectBottomOffset )
			{
				yOffset = dragPoint.y - visibleRectBottomOffset;
			}
			else
			{
				yOffset = 0;
			}
			// Now clamp any scroll offset to a max value
			int xOffsetAbs = Math.abs( xOffset );
			int xOffsetSign = Integer.signum( xOffset );
			if( xOffsetAbs > FAST_OFFSET_DISTANCE )
			{
				xOffset = xOffsetSign * FAST_SCROLL_AMOUNT;
			}
			else
			{
				xOffset = xOffsetSign * SLOW_SCROLL_AMOUNT;
			}
			int yOffsetAbs = Math.abs( yOffset );
			int yOffsetSign = Integer.signum( yOffset );
			if( yOffsetAbs > FAST_OFFSET_DISTANCE )
			{
				yOffset = yOffsetSign * FAST_SCROLL_AMOUNT;
			}
			else
			{
				yOffset = yOffsetSign * SLOW_SCROLL_AMOUNT;
			}
			scrollingAmount = new Point( xOffset, yOffset );
			e.consume();
		}
	}
	
	private Point scrollingAmount = null;

	public Point getScrollingAmount()
	{
		return scrollingAmount;
	}

}
