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

package uk.co.modularaudio.util.swing.mvc.combo.idstring;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboItem;
import uk.co.modularaudio.util.swing.mvc.combo.ComboViewListCellRenderer;

public class IdStringComboViewListCellRenderer<A extends IdStringComboItem> implements ComboViewListCellRenderer<A>
{
	@SuppressWarnings("rawtypes")
	private ListCellRenderer defaultRenderer = null;
	
	public IdStringComboViewListCellRenderer()
	{
		this( new DefaultListCellRenderer() );
	}

	@SuppressWarnings({ "rawtypes" })
	public IdStringComboViewListCellRenderer( ListCellRenderer defaultRenderer )
	{
		this.defaultRenderer = defaultRenderer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getListCellRendererComponent(JList<? extends A> list, A value,
			int index, boolean isSelected, boolean cellHasFocus)
	{
		String rendererValue = ( value == null ? null : value.getDisplayString() );
		return defaultRenderer.getListCellRendererComponent( list, rendererValue, index, isSelected, cellHasFocus );
	}

}
