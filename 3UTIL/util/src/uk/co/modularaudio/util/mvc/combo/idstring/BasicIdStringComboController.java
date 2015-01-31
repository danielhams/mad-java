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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.MvcListenerEvent.EventType;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;

public class BasicIdStringComboController<A extends IdStringComboItem> implements IdStringComboController<A>
{
	private static Log log = LogFactory.getLog( BasicIdStringComboController.class.getName());

	private IdStringComboModel<A> model = null;

	public BasicIdStringComboController( final IdStringComboModel<A> model )
	{
		this.model = model;
	}

	@Override
	public void setSelectedElement( final A selectedElement )
		throws RecordNotFoundException
	{
		// do some checks, update the necessary bits of the model
		// and propogate a "refresh" event
		final int index = model.getItemIndex( selectedElement );
		if( index == -1 )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Unable to find element: " + selectedElement.toString() + " in combo model");
			}
			throw new RecordNotFoundException();
		}
		else
		{
			// This doesn't necessarily have to update the model.
			// Could do something somewhere else and force a refresh
			model.setSelectedItemByIndex( index );
			final ComboModelListenerEvent event = new ComboModelListenerEvent( EventType.SELECTION_CHANGED, index, -1 );
			final Collection<ComboModelListener<A>> listeners = model.getListeners();
			for( final ComboModelListener<A> l : listeners )
			{
				l.selectionChanged( event );
			}
		}

	}

	@Override
	public void setSelectedElementById( final String selectedElementId ) throws RecordNotFoundException
	{
		final A elementToSelect = model.getElementById( selectedElementId );
		this.setSelectedElement( elementToSelect );
	}

	public void setModel( final IdStringComboModel<A> newModel )
	{
		this.model = newModel;
	}
}
