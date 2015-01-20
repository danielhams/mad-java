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

package uk.co.modularaudio.util.mvc.combo.idstringandvalue;

import java.util.Collection;

import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.MvcListenerEvent.EventType;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;

public class BasicIdStringAndValueComboController<A extends IdStringAndValueComboItem<B>, B> implements IdStringAndValueComboController<A, B>
{
//	private static Log log = LogFactory.getLog( BasicIdStringAndValueComboController.class.getName());
	
	protected IdStringAndValueComboModel<A,B> model = null;
	
	public BasicIdStringAndValueComboController( IdStringAndValueComboModel<A,B> model )
	{
		this.model = model;
	}

	@Override
	public void setSelectedElement( A selectedElement )
		throws RecordNotFoundException
	{
		// do some checks, update the necessary bits of the model
		// and propogate a "refresh" event
		int index = model.getItemIndex( selectedElement );
		// If not null and we didn't find it, throw the RNFE
		if( selectedElement != null && index == -1 )
		{
			throw new RecordNotFoundException();
		}
		else
		{
			// This doesn't necessarily have to update the model.
			// Could do something somewhere else and force a refresh
			model.setSelectedItemByIndex( index );
			ComboModelListenerEvent event = new ComboModelListenerEvent( EventType.SELECTION_CHANGED, index, -1 );
			Collection<ComboModelListener<A>> listeners = model.getListeners();
			for( ComboModelListener<A> l : listeners )
			{
				l.selectionChanged( event );
			}
		}
		
	}

	@Override
	public void setSelectedElementById( String selectedElementId ) throws RecordNotFoundException
	{
		A elementToSelect = model.getElementById( selectedElementId );
		this.setSelectedElement( elementToSelect );
	}

	public void setModel( IdStringAndValueComboModel<A,B> newModel )
	{
		this.model = newModel;
	}

}
