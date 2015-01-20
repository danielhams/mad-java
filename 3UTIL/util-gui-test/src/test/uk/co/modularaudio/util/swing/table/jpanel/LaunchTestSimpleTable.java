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

package test.uk.co.modularaudio.util.swing.table.jpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.swing.table.TestCTGF;
import test.uk.co.modularaudio.util.swing.table.TestECP;
import test.uk.co.modularaudio.util.swing.table.TestGC;
import test.uk.co.modularaudio.util.swing.table.TestTC;
import test.uk.co.modularaudio.util.swing.table.TestTP;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.table.jpanel.JPanelTable;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.table.impl.Table;

public class LaunchTestSimpleTable extends JFrame
{
	private static final long serialVersionUID = -2122530652398178239L;
	
	private static Log log = LogFactory.getLog( LaunchTestSimpleTable.class.getName() );

	private LayoutManager layoutManager = null;
	
	private JScrollPane frontScrollpane = null;

	private JComponent frontAudioComponentTable = null;
	
	private TestPopupMenu popupMenu = null;
	private TestPopupListener popupListener = null;

	public LaunchTestSimpleTable() throws RecordNotFoundException, DatastoreException
	{
		this.setTitle("Test Swing Table.");
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		this.setSize( new Dimension( 800, 600 ) );
		this.setLayout( getLayoutManager() );
		
		// The scrollpane contains the front view
		this.add( getFrontScrollpane(), "growx, growy 100");
	}
	
	public JScrollPane getFrontScrollpane()
	{
		if( frontScrollpane == null )
		{
			frontScrollpane = new JScrollPane();
			frontScrollpane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			frontScrollpane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
			frontScrollpane.getVerticalScrollBar().setUnitIncrement( 16 );
			frontScrollpane.setAutoscrolls( true );
			JViewport viewport = frontScrollpane.getViewport();
			JComponent fact = getFrontAudioComponentTable();
			viewport.add( fact, "grow" );
		}
		return frontScrollpane;
	}
	
	public JComponent getFrontAudioComponentTable()
	{
		if( frontAudioComponentTable == null )
		{
			TableInterface<TestTC, TestTP> tableModel = new Table<TestTC, TestTP>(100, 100);
			try
			{
				int num = 0;
				for( int x = 0 ; x < 20 ; x+=2 )
				{
					for( int y = 0 ; y < 20 ; y+=2 )
					{
						TestTC contents = new TestTC( num );
						num++;
						tableModel.addContentsAtPosition(contents, x, y );
					}
				}
//				TestTC contentsOne = new TestTC( 1 );
//				tableModel.addContentsAtPosition( contentsOne, 0, 0 );
//				TestTC contentsTwo = new TestTC( 2 );
//				tableModel.addContentsAtPosition( contentsTwo, 0, 2 );
			}
			catch (Exception e)
			{
				log.error( e );
			}
			
			Dimension gridSize = new Dimension( 50, 50 );
			boolean showGrid = true;
			Color gridColor = Color.GREEN;
			TestECP emptyCellPainter = new TestECP();
			TestCTGF componentToGuiFactory = new TestCTGF();
			JPanelTable<TestTC, TestTP, TestGC> testSwingTable = new JPanelTable<TestTC, TestTP, TestGC>( tableModel,
					emptyCellPainter,
					componentToGuiFactory,
					gridSize,
					showGrid,
					gridColor );
			
			TestPopupListener popupListener = getPopupListener();
			
			testSwingTable.addMouseListener( popupListener );
			
			frontAudioComponentTable = testSwingTable;
		}
		return frontAudioComponentTable;
	}
	
	private TestPopupListener getPopupListener()
	{
		if( popupListener == null )
		{
			TestPopupMenu popupMenu = getPopupMenu();
			popupListener = new TestPopupListener( popupMenu );
		}
		return popupListener;
	}
	
	private TestPopupMenu getPopupMenu()
	{
		if( popupMenu == null )
		{
			popupMenu = new TestPopupMenu();
		}
		return popupMenu;
	}
	
	private LayoutManager getLayoutManager()
	{
		if( layoutManager == null )
		{
			//layoutManager = new MigLayout( "inset 0, gap 0, flowy", "", "[fill][]");
			layoutManager = new MigLayout( "flowy", "[fill, grow]", "[fill, grow][]");
		}
		return layoutManager;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
//					LookAndFeel newLookAndFeel = new SubstanceBusinessLookAndFeel();
//					UIManager.setLookAndFeel( newLookAndFeel );
					UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

					LaunchTestSimpleTable launcher = new LaunchTestSimpleTable();
					launcher.setVisible( true );
				}
				catch (Exception e)
				{
					log.error( e );
				}
			}
		} );
	}
}
