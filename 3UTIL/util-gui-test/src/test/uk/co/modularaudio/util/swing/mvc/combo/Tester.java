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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.mvc.combo.ComboModelListener;
import uk.co.modularaudio.util.mvc.combo.ComboModelListenerEvent;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboItem;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.combo.ComboView;
import uk.co.modularaudio.util.swing.mvc.combo.idstring.IdStringComboViewListCellRenderer;

public class Tester
{
	private static Log log = LogFactory.getLog( Tester.class.getName() );

	private JFrame frame = null;

	public Tester()
	{
		frame = new JFrame();
		frame.setMinimumSize( new Dimension( 300, 100 ) );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}

	public void go() throws Exception
	{
		final List<IdStringComboItem> sampleItems = new ArrayList<IdStringComboItem>();
		sampleItems.add( new IdStringComboItem( "thin", "Nasty, thin, toilet paper" ) );
		sampleItems.add( new IdStringComboItem( "sand", "Harsh, sandpaper like bog roll" ) );

		final RealComboModel rcm = new RealComboModel( sampleItems );
		final RealComboController rcc = new RealComboController( rcm );
		// Now check the model is indeed updated
		debugModel( rcm );

		final ComboModelListener<IdStringComboItem> myListener = new ComboModelListener<IdStringComboItem>()
		{
			@Override
			public void contentsChanged(final ComboModelListenerEvent e)
			{
				log.debug("Contents changed event: " + e.toString() );
				// Now debug it
				debugModel(rcm);
			}

			@Override
			public void intervalAdded(final ComboModelListenerEvent e)
			{
				log.debug("Interval added event: " + e.toString() );
			}

			@Override
			public void intervalRemoved(final ComboModelListenerEvent e)
			{
				log.debug("Interval removed event: " + e.toString() );
			}

			@Override
			public void selectionChanged(final ComboModelListenerEvent e)
			{
				log.debug("Selection changed event: " + e.toString() );
				final int selectionIndex = e.getIndex0();
				final IdStringComboItem item = rcm.getElementAt( selectionIndex );
				log.debug("The id of the selection is now: " + item.getId() );
			}
		};
		rcm.addListener( myListener );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		frame.setLayout( msh.createMigLayout() );

		final IdStringComboViewListCellRenderer<IdStringComboItem> cr = new IdStringComboViewListCellRenderer<IdStringComboItem>( new DefaultListCellRenderer() );
		final ComboView<IdStringComboItem> comboView = new ComboView<IdStringComboItem>( rcm, rcc, cr );
		comboView.setEditable( false );
		frame.add( comboView, "grow" );
		final JLabel testLabel = new JLabel("Hmmphg");
		frame.add( testLabel, "" );
		frame.setVisible( true );
	}

	public void debugModel( final RealComboModel rcm )
	{
		final int numElements = rcm.getNumElements();
		for( int i = 0 ; i < numElements ; i++ )
		{
			final IdStringComboItem eci = rcm.getElementAt( i );
			log.debug("Got an item: " + eci.getId() + " " + eci.getDisplayString() );
		}
		final int selectedItemIndex = rcm.getSelectedItemIndex();
		if( selectedItemIndex != -1 )
		{
			final IdStringComboItem selectedItem = rcm.getElementAt( selectedItemIndex );
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
	public static void main(final String[] args)
		throws Exception
	{
		final Tester t = new Tester();
		t.go();
	}

}
