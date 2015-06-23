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

package uk.co.modularaudio.util.sorteditemmodel;

import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

public class SortedItemListModel<A extends Object> extends DefaultComboBoxModel<A>
{
	private static final long serialVersionUID = -4859411511141297418L;

	private Comparator<A> comparator;

	// Can't use this - must use comparator
	@SuppressWarnings("unused")
	private SortedItemListModel()
	{
		super();
	}

	public SortedItemListModel( final Vector<A> items, final Comparator<A> comp)
	{
		super(items);
		comparator = comp;
	}

	@Override
	public void addElement( final A element )
	{
		insertElementAt( element, 0 );
	}

	@Override
	public void insertElementAt( final A element, final int iIndex )
	{
		int index = iIndex;
		final int size = getSize();

		for( index = 0 ; index < size ; index++)
		{
			final A c = getElementAt(index);
			if( comparator.compare(c, element) > 0 )
			{
				break;
			}
		}

		super.insertElementAt( element, index);
	}
}
