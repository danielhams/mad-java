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

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.util.mvc.combo.ComboItem;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;

final class ComboViewModelListenerAdaptor<A extends ComboItem > implements ComboModelListener<A>
{
//	private final static Log log = LogFactory.getLog( ComboViewModelListenerAdaptor.class.getName() );
	private final ListDataListener l;

	ComboViewModelListenerAdaptor(ListDataListener l)
	{
		this.l = l;
	}

	private ListDataEvent adaptEvent( ComboModelListenerEvent event )
	{
		int index0 = event.getIndex0();
		int index1 = event.getIndex1();
		int eventType = -1;
		switch( event.getEventType() )
		{
		case INTERVAL_ADDED:
			eventType = ListDataEvent.INTERVAL_ADDED;
			break;
		case INTERVAL_REMOVED:
			eventType = ListDataEvent.INTERVAL_REMOVED;
			break;
		case FULL_REFRESH:
		case SELECTION_CHANGED:
		default:
			eventType = ListDataEvent.CONTENTS_CHANGED;
			break;
		}
		ListDataEvent e = new ListDataEvent( this, eventType, index0, index1 );
		return e;
	}

	@Override
	public void contentsChanged( ComboModelListenerEvent event )
	{
		l.contentsChanged( adaptEvent( event ) );
	}

	@Override
	public void intervalAdded( ComboModelListenerEvent event )
	{
		l.intervalAdded( adaptEvent( event ) );
	}

	@Override
	public void intervalRemoved( ComboModelListenerEvent event )
	{
		l.intervalRemoved( adaptEvent( event ) );
	}

	@Override
	public void selectionChanged( ComboModelListenerEvent event )
	{
		// Java's combo box doesn't have a "selection changed" event
		// so lets just propogate it as a contents changed
		l.contentsChanged( adaptEvent( event ) );
	}
}
