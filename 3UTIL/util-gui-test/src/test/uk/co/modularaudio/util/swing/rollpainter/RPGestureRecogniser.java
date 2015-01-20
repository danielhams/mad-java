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

package test.uk.co.modularaudio.util.swing.rollpainter;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RPGestureRecogniser implements MouseMotionListener, MouseListener
{
	private static Log log = LogFactory.getLog( RPGestureRecogniser.class.getName() );
	
	private final JComponent componentToWatch;
	
	public RPGestureRecogniser( JComponent componentToWatch )
	{
		this.componentToWatch = componentToWatch;
		
		componentToWatch.addMouseListener( this );
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		log.debug("mouseDragged");
		// Get these whilst during a "mouse down" event
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
//		log.debug("mouseMoved");
		// Get these all the time the mouse is moving over the canvas
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
//		log.debug("mouseClicked");
		int clickCount = e.getClickCount();
		if( clickCount > 0 )
		{
			log.debug("Have click count " + clickCount );
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
//		log.debug("mousePressed");
		// Specifically on mouse down
		componentToWatch.addMouseMotionListener( this );
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
//		log.debug("mouseReleased");
		// Specifically on mouse up
		componentToWatch.removeMouseMotionListener( this );
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
//		log.debug("mouseEntered");
		// Boundary event as expected
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
//		log.debug("mouseExited");
		// Boundary event as expected
	}

}
