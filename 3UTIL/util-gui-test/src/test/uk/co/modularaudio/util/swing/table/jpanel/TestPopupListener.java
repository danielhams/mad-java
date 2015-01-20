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

package test.uk.co.modularaudio.util.swing.table.jpanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;

public class TestPopupListener implements MouseListener
{
	private JPopupMenu popupMenu = null;

	public TestPopupListener( JPopupMenu popupMenu )
	{
		this.popupMenu = popupMenu;
	}
	
	private void popup( MouseEvent e )
	{
		popupMenu.show(e.getComponent(), e.getX(), e.getY() );
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if( e.isPopupTrigger() )
		{
			popup( e );
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if( e.isPopupTrigger() )
		{
			popup( e );
		}
	}
}
