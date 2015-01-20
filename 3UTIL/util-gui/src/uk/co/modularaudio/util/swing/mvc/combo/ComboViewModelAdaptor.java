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

package uk.co.modularaudio.util.swing.mvc.combo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.combo.ComboController;
import uk.co.modularaudio.util.mvc.combo.ComboItem;
import uk.co.modularaudio.util.mvc.combo.ComboModel;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;

final class ComboViewModelAdaptor<A extends ComboItem> implements ComboBoxModel<A>
{
	protected static Log log = LogFactory.getLog( ComboViewModelAdaptor.class.getName() );
	
	private final ComboModel<A> cm;
	private final ComboController<A> cc;

	private Map<ListDataListener, ComboModelListener<A>> ldlToCmlListenerMap = new HashMap<ListDataListener, ComboModelListener<A>>();

	ComboViewModelAdaptor(ComboModel<A> cm, ComboController<A> cc)
	{
		this.cm = cm;
		this.cc = cc;
	}

	@Override
	public int getSize()
	{
		return cm.getNumElements();
	}

	@Override
	public A getElementAt(int index)
	{
		return cm.getElementAt( index );
	}

	@Override
	public void addListDataListener(final ListDataListener l)
	{
		ComboModelListener<A> decoration = new ComboViewModelListenerAdaptor<A>(l);
		cm.addListener( decoration );
		ldlToCmlListenerMap.put( l, decoration );
	}

	@Override
	public void removeListDataListener(ListDataListener l)
	{
		ComboModelListener<A> decoration = ldlToCmlListenerMap.get( l );
		if( decoration != null )
		{
			cm.removeListener( decoration );
			ldlToCmlListenerMap.remove( l );
		}
	}

	@Override
	public void setSelectedItem(Object anItem)
	{
		@SuppressWarnings("unchecked")
		A realItem = (A)anItem;
		try
		{
			cc.setSelectedElement( realItem );
		}
		catch (RecordNotFoundException e)
		{
			String msg = "RecordNotFoundException caught adapting combo box model set selected item: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public Object getSelectedItem()
	{
//		int index = cm.getSelectedItemIndex();
//		log.debug("ComboViewModelAdaptor selected item index is " + index );
		return cm.getSelectedElement();
	}
}
