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

package uk.co.modularaudio.util.swing.dndtable.jpanel;

import java.util.ArrayList;
import java.util.List;

public class JPanelDndTableDecorations
{
//	private static Log log = LogFactory.getLog( SwingDndTableDecorations.class.getName() );

	protected List<JPanelDndTableDecorationHint> hints = new ArrayList<JPanelDndTableDecorationHint>();

	protected JPanelDndTableDecorations()
	{
	}

	public JPanelDndTableDecorations( final List<JPanelDndTableDecorationHint> hints )
	{
		if( hints != null )
		{
			this.hints = hints;
		}
	}

	public List<JPanelDndTableDecorationHint> getHints()
	{
		return hints;
	}
}
