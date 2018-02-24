/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class HistogramDisplayMouseListener implements MouseListener, MouseMotionListener
{
//	private static Log log = LogFactory.getLog( HistogramDisplayMouseListener.class.getName() );

	private final static int NO_BUTTONS_MASK = MouseEvent.BUTTON1_DOWN_MASK |
			MouseEvent.BUTTON2_DOWN_MASK |
			MouseEvent.BUTTON3_DOWN_MASK;

	private final HistogramGraph graph;

	public HistogramDisplayMouseListener( final HistogramGraph graph )
	{
		this.graph = graph;
	}

	@Override
	public void mouseDragged( final MouseEvent me )
	{
	}

	@Override
	public void mouseMoved( final MouseEvent me )
	{
		if( (me.getModifiersEx() & NO_BUTTONS_MASK) == 0 )
		{
//			log.debug( "MouseMoved " + me.toString() );
			final int curX = me.getX();
			final int adjustedX = curX - (HistogramDisplay.EVENTS_LABELS_WIDTH + HistogramDisplay.AXIS_MARKER_LENGTH);
			final int curY = me.getY();
			final int adjustedY = curY - (HistogramDisplay.BUCKETS_LABELS_HEIGHT + HistogramDisplay.AXIS_MARKER_LENGTH);
			if( adjustedX >= 0 && adjustedX < graph.getGraphWidth() &&
					adjustedY >= 0 && adjustedY < graph.getGraphHeight() )
			{
				graph.setMousePosition( adjustedX );
			}
			else
			{
				graph.unsetMousePosition();
			}
		}
	}

	@Override
	public void mouseClicked( final MouseEvent me )
	{
	}

	@Override
	public void mousePressed( final MouseEvent me )
	{
	}

	@Override
	public void mouseReleased( final MouseEvent me )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent me )
	{
	}

	@Override
	public void mouseExited( final MouseEvent me )
	{
	}
}
