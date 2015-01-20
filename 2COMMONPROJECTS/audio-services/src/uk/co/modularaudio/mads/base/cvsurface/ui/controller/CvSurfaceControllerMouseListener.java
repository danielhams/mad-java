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

package uk.co.modularaudio.mads.base.cvsurface.ui.controller;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import uk.co.modularaudio.mads.base.cvsurface.ui.CvSurfaceMadUiInstance;

public class CvSurfaceControllerMouseListener implements MouseListener, MouseMotionListener
{
//	private static Log log = LogFactory.getLog( CvSurfaceControllerMouseListener.class.getName() );

	private enum ClickState
	{
		NOT_CLICKED,
		CLICKED
	}

	private ClickState clickState = ClickState.NOT_CLICKED;
	private Point lastPoint = null;

	private CvSurfaceMadUiInstance uiInstance = null;
	private CvSurfaceControllerUiJComponent uiComponent = null;
	private Rectangle bounds = null;
	private int maxX = -1;
	private int maxY = -1;

	public CvSurfaceControllerMouseListener( CvSurfaceMadUiInstance uiInstance, CvSurfaceControllerUiJComponent uiComponent )
	{
		this.uiInstance = uiInstance;
		this.uiComponent = uiComponent;
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
		lastPoint = e.getPoint();
		if( lastPoint != null )
		{
			boundPoint();
		}
		sendPositionChange( lastPoint );
	}

	@Override
	public void mouseMoved( MouseEvent e )
	{
	}

	public Point getCurrentSelectionPoint()
	{
		// Return null if we are not dragging!
		if( clickState == ClickState.CLICKED )
		{
			return lastPoint;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
	}

	@Override
	public void mousePressed( MouseEvent e )
	{
		clickState = ClickState.CLICKED;
		lastPoint =  e.getPoint();
		if( lastPoint != null )
		{
			boundPoint();
		}
		sendPositionChange( lastPoint );
	}

	@Override
	public void mouseReleased( MouseEvent e )
	{
		clickState = ClickState.NOT_CLICKED;
		lastPoint = null;

		sendPositionChange( null );
	}

	@Override
	public void mouseEntered( MouseEvent e )
	{
	}

	@Override
	public void mouseExited( MouseEvent e )
	{
	}

	private void sendPositionChange( Point lastPoint )
	{
		float newX = 0.0f;
		float newY = 0.0f;
		if( lastPoint != null )
		{
			// Normalise the point from -1 -> 1 on each axis
			newX = normalise( lastPoint.x, bounds.width - 1 );
			newY = normalise( lastPoint.y, bounds.height - 1 );
			// Flip the Y
			newY = -newY;
//			log.debug("Sending position change: " + lastPoint.x + ", " + lastPoint.y + " " + MathFormatter.floatPrint( newX,  2 ) + ", " + MathFormatter.floatPrint( newY, 2 ) );
		}
		uiInstance.sendPositionChange( newX, newY );
	}

	private float normalise( int value, int maxValue )
	{
		// make it 0 -> 1
		float monoPole = ((float)value) / maxValue;
		// Now expand to -1 -> 1
		float retVal = (monoPole - 0.5f) * 2.0f;

		return retVal;
	}

	private void boundPoint()
	{
		if( bounds == null )
		{
			bounds = uiComponent.getBounds();
			maxX = bounds.width - 1;
			maxY = bounds.height - 1;
		}
		lastPoint.x = ( lastPoint.x < 0 ? 0 : (lastPoint.x > maxX ? maxX : lastPoint.x ) );
		lastPoint.y = ( lastPoint.y < 0 ? 0 : (lastPoint.y > maxY ? maxY : lastPoint.y ) );
	}

}
