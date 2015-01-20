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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

public class AutoScrollingTimerTask implements ActionListener
{
//	private static Log log = LogFactory.getLog( AutoScrollingTimerTask.class.getName() );
	
	private AutoScrollingMouseListener mouseListener = null;
	private JComponent compToScroll = null;
	
	public AutoScrollingTimerTask( AutoScrollingMouseListener mouseListener, JComponent compToScroll )
	{
		this.mouseListener = mouseListener;
		this.compToScroll = compToScroll;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
//		log.debug("Autoscroll timer action fired.");
		Rectangle compToScrollVisibleRect = compToScroll.getVisibleRect();
		// Get the amount by which to offset from the auto scrolling mouse listener and call the set visible rect
		Point scrollingAmount = mouseListener.getScrollingAmount();
		if( scrollingAmount != null )
		{
			int xOffset = scrollingAmount.x;
			int yOffset = scrollingAmount.y;
			
			int visibleRectLeftOffset = compToScrollVisibleRect.x;
			int visibleRectTopOffset = compToScrollVisibleRect.y;
			
			Rectangle scrollRect = new Rectangle( visibleRectLeftOffset + xOffset, 
					visibleRectTopOffset + yOffset, 
					compToScrollVisibleRect.width, 
					compToScrollVisibleRect.height );
	
			compToScroll.scrollRectToVisible( scrollRect );
		}
	}
}
