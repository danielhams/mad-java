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
import java.util.ArrayList;

import uk.co.modularaudio.util.swing.dndtable.GuiDndTableState;

public class LayeredPaneDndTableDecorator
{
//	private static Log log = LogFactory.getLog( LayeredPaneDndTableDecorator.class.getName() );

	private final LayeredPaneDndTableDecorations decorations;

	public LayeredPaneDndTableDecorator( final LayeredPaneDndTable<?,?,?> table,
			final GuiDndTableState state,
			final LayeredPaneDndTableDecorations decorations )
	{
		this.decorations = decorations;
		for( final LayeredPaneDndTableDecorationHint hint : decorations.hints )
		{
			hint.setTable( table );
		}
	}

	public void setMousePosition( final Point mousePosition )
	{
		final ArrayList<LayeredPaneDndTableDecorationHint> hints = decorations.getHints();
		for( final LayeredPaneDndTableDecorationHint hint : hints )
		{
			hint.setMousePosition( mousePosition );
		}
	}

	public void signalAnimation()
	{
		final ArrayList<LayeredPaneDndTableDecorationHint> hints = decorations.getHints();
		for( final LayeredPaneDndTableDecorationHint hint : hints )
		{
			hint.signalAnimation();
		}
	}

}
