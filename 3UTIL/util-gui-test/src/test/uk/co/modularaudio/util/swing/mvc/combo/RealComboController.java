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

package test.uk.co.modularaudio.util.swing.mvc.combo;

import java.util.Collection;

import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.mvc.MvcListenerEvent.EventType;
import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;
import uk.co.modularaudio.util.mvc.combo.idstring.BasicIdStringComboController;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboItem;

public class RealComboController extends BasicIdStringComboController<IdStringComboItem>
{
//	private static Log log = LogFactory.getLog( RealComboController.class.getName());
	
	private RealComboModel model = null;
	
	public RealComboController( RealComboModel model )
	{
		super( model );
		this.model = model;
	}

	@Override
	public void setSelectedElement( IdStringComboItem selectedElement )
		throws RecordNotFoundException
	{
		// do some checks, update the necessary bits of the model
		// and propogate a "refresh" event
		int index = model.getItemIndex( selectedElement );
		if( index == -1 )
		{
			throw new RecordNotFoundException();
		}
		else
		{
			// This doesn't necessarily have to update the model.
			// Could do something somewhere else and force a refresh
			model.setSelectedItemByIndex( index );
			ComboModelListenerEvent event = new ComboModelListenerEvent( EventType.SELECTION_CHANGED, index, -1 );
			Collection<ComboModelListener<IdStringComboItem>> listeners = model.getListeners();
			for( ComboModelListener<IdStringComboItem> l : listeners )
			{
				l.selectionChanged(event);
			}
			
			try
			{
				IdStringComboItem newElement = new IdStringComboItem("perfect", "The perfect gentle, durable arse wipe" );
				model.addNewElement( newElement );
				int newElementIndex = model.getItemIndex( newElement );
				
				ComboModelListenerEvent newElementEvent = new ComboModelListenerEvent( EventType.INTERVAL_ADDED, newElementIndex - 1, newElementIndex );
				listeners = model.getListeners();
				for( ComboModelListener<IdStringComboItem> l : listeners )
				{
					l.intervalAdded( newElementEvent );
				}
			}
			catch(MAConstraintViolationException ecve)
			{
//				String msg = "MAConstraintViolationException caught adding new element: " + ecve.toString();
//				log.error( msg, ecve );
			}
		}
		
	}

}
