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

package uk.co.modularaudio.util.audio.gui.madswingcontrols;

import java.awt.Canvas;
import java.awt.Rectangle;


public abstract class PacCanvas extends Canvas
{
	private static final long serialVersionUID = -437811233199546479L;

	protected Rectangle realBounds = new Rectangle();

	public abstract String getControlValue();

	public abstract void receiveControlValue( final String value );

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );
		realBounds.x = x;
		realBounds.y = y;
		realBounds.width = width;
		realBounds.height = height;
	}

	@Override
	public void setBounds( final Rectangle r )
	{
		super.setBounds( r );
		realBounds.x = r.x;
		realBounds.y = r.y;
		realBounds.width = r.width;
		realBounds.height = r.height;
	}
}
