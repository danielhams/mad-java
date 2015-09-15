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

package uk.co.modularaudio.util.swing.texttoggle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TextToggleMouseListener implements MouseListener
{
	private final TextToggle originTt;

	public TextToggleMouseListener( final TextToggle originTt )
	{
		this.originTt = originTt;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		originTt.receiveClick();
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
	}

}
