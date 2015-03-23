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

package uk.co.modularaudio.util.mvc.combo.idstring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.modularaudio.util.mvc.MvcListenerEvent.EventType;
import uk.co.modularaudio.util.mvc.combo.ComboModel;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;

public class IdStringComboModel<A extends IdStringComboItem> implements ComboModel<A>
{
//	private static Log log = LogFactory.getLog( IdStringComboModel.class.getName() );

	protected List<A> theList = new ArrayList<A>();
	protected int currentlySelectedItemIndex;

	protected List<ComboModelListener<A>> listeners = new ArrayList<ComboModelListener<A>>();
	protected Map<String, A> idToElementMap = new HashMap<String, A> ();

	public IdStringComboModel( final Collection<A> startupItems )
	{
		theList.addAll( startupItems );
		for( final A ci : startupItems )
		{
			idToElementMap.put( ci.getId(), ci );
		}
	}

	@Override
	public int getNumElements()
	{
		return theList.size();
	}

	@Override
	public A getElementAt(final int i)
	{
		return theList.get( i );
	}

	@Override
	public int getSelectedItemIndex()
	{
		return currentlySelectedItemIndex;
	}

	@Override
	public A getSelectedElement()
	{
		if( currentlySelectedItemIndex != -1 )
		{
			return theList.get( currentlySelectedItemIndex );
		}
		else
		{
			return null;
		}
	}

	@Override
	public void addListener(final ComboModelListener<A> listener)
	{
		listeners.add( listener );
	}

	@Override
	public void removeListener(final ComboModelListener<A> listener)
	{
		listeners.remove( listener );
	}

	@Override
	public int getItemIndex(final IdStringComboItem item)
	{
		return theList.indexOf( item );
	}

	public void setSelectedItemByIndex( final int index )
		throws IndexOutOfBoundsException
	{
		if( index >= 0 && index < theList.size() )
		{
			if( currentlySelectedItemIndex != index )
			{
				final ComboModelListenerEvent listChangedEvent = new ComboModelListenerEvent(
						EventType.SELECTION_CHANGED, currentlySelectedItemIndex, index );

				currentlySelectedItemIndex = index;

				for( final ComboModelListener<A> l : listeners )
				{
					l.selectionChanged( listChangedEvent );
				}
			}
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public Collection<ComboModelListener<A>> getListeners()
	{
		return listeners;
	}

	@Override
	public A getElementById(final String selectedElementId)
	{
		return idToElementMap.get( selectedElementId );
	}

}
