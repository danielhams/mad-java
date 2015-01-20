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

package test.uk.co.modularaudio.util.mvc.combo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboItem;

public class Tester
{
	private static Log log = LogFactory.getLog( Tester.class.getName() );
	
	public Tester()
	{
	}
	
	public void go() throws Exception
	{
		List<IdStringComboItem> sampleItems = new ArrayList<IdStringComboItem>();
		sampleItems.add( new IdStringComboItem( "thin", "Nasty, thin, toilet paper" ) );
		sampleItems.add( new IdStringComboItem( "sand", "Harsh, sandpaper like bog roll" ) );

		RealComboModel rcm = new RealComboModel( sampleItems );
		RealComboController rcc = new RealComboController( rcm );
		
		debugModel( rcm );
		
		IdStringComboItem itemToSelect = rcm.getElementAt( 1 );
		ComboModelListener<IdStringComboItem> myListener = new ComboModelListener<IdStringComboItem>()
		{
			@Override
			public void intervalAdded(ComboModelListenerEvent e)
			{
				log.debug("Interval added event: " + e.toString() );
			}

			@Override
			public void intervalRemoved(ComboModelListenerEvent e)
			{
				log.debug("Interval removed event:: " + e.toString() );
			}

			@Override
			public void contentsChanged(ComboModelListenerEvent e)
			{
				log.debug("Contents changed event: " + e.toString() );
			}

			@Override
			public void selectionChanged(ComboModelListenerEvent e)
			{
				log.debug("Selection changed event: " + e.toString() );
			}

		};
		rcm.addListener( myListener );
		rcc.setSelectedElement( itemToSelect );
		
		// Now check the model is indeed updated
		debugModel( rcm );
	}
	
	public void debugModel( RealComboModel rcm )
	{
		int numElements = rcm.getNumElements();
		for( int i = 0 ; i < numElements ; i++ )
		{
			IdStringComboItem eci = rcm.getElementAt( i );
			log.debug("Got an item: " + eci.getId() + " " + eci.getDisplayString() );
		}
		int selectedItemIndex = rcm.getSelectedItemIndex();
		if( selectedItemIndex != -1 )
		{
			IdStringComboItem selectedItem = rcm.getElementAt( selectedItemIndex );
			log.debug("And the currently selected item is " + selectedItem.getId() + " " + selectedItem.getDisplayString() );
		}
		else
		{
			log.debug("No item is currently selected");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
		throws Exception
	{
		Tester t = new Tester();
		t.go();
	}

}
