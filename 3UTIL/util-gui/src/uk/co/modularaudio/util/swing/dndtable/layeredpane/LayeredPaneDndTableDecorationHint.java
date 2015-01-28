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

import java.awt.Point;

public abstract class LayeredPaneDndTableDecorationHint
{
//	private static Log log = LogFactory.getLog( LayeredPaneDndTableDecorationHint.class.getName() );

	protected boolean active;

	protected LayeredPaneDndTable<?, ?, ?> table;

	public boolean isActive()
	{
		return active;
	}

	public void setTable( final LayeredPaneDndTable<?, ?, ?> table )
	{
		this.table = table;
	}

	public abstract void setActive( boolean activeBool);

	public abstract boolean isMouseRelative();

	public abstract void setMousePosition(Point mousePosition);

	public abstract void signalAnimation();

}
