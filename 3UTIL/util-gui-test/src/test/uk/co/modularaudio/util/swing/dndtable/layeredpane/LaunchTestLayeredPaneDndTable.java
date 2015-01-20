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

package test.uk.co.modularaudio.util.swing.dndtable.layeredpane;

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

import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff.TestDndRackDecorations;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff.TestDndRackDragPolicy;
import test.uk.co.modularaudio.util.swing.dndtable.layeredpane.pacdndstuff.TestDndRackTargetRegion;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.table.GuiTableDataModel;
import uk.co.modularaudio.util.table.Span;

public class LaunchTestLayeredPaneDndTable extends JFrame
{
	private static final long serialVersionUID = -2122530652398178239L;
	
	private static Log log = LogFactory.getLog( LaunchTestLayeredPaneDndTable.class.getName() );

	private LayoutManager layoutManager = null;
	
	private JScrollPane frontScrollpane = null;

	private JComponent frontAudioComponentTable = null;
	
	private TestPopupMenu popupMenu = null;
	public LaunchTestLayeredPaneDndTable() throws RecordNotFoundException, DatastoreException
	{
		this.setTitle("Test Layered Pane Dnd Table.");
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		this.setSize( new Dimension( 800, 600 ) );
		this.setLayout( getLayoutManager() );
		
		// The scrollpane contains the front view
		this.add( getFrontScrollpane(), "growx, growy 100");
		this.add( new TestDndRackTargetRegion() );
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
			GuiTableDataModel<TestTC, TestTP> tableModel = new GuiTableDataModel<TestTC, TestTP>(100, 100);
			try
			{
				TestIOBar ioBar = new TestIOBar();
				tableModel.addContentsAtPosition( ioBar, 0, 0 );
				
				int num = 0;
				for( int x = 3 ; x < 30 ; x+=4 )
				{
					for( int y = 3 ; y < 40 ; y+=2 )
					{
						TestTC contents = new TestTC( num );
						if( y % 3 == 0 )
						{
							contents.span = new Span( 1,1 );
						}						
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
			boolean showGrid = false;
			Color gridColor = new Color( 80, 80, 80);
			TestECP emptyCellPainter = new TestECP();
			TestDndCTGF componentToGuiFactory = new TestDndCTGF();
			TestDndRackDecorations dndDecorations = new TestDndRackDecorations();
			TestDndRackDragPolicy dndTablePolicy = new TestDndRackDragPolicy( tableModel, dndDecorations );
			LayeredPaneDndTable<TestTC, TestTP, TestGC> testDndTable = new LayeredPaneDndTable<TestTC, TestTP, TestGC>( tableModel,
					componentToGuiFactory,
					dndTablePolicy,
					dndDecorations,
					gridSize,
					showGrid,
					gridColor,
					emptyCellPainter );
			
			frontAudioComponentTable = testDndTable;
		}
		return frontAudioComponentTable;
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

	public TestPopupMenu getPopupMenu()
	{
		if( popupMenu == null )
		{
			popupMenu = new TestPopupMenu();
		}
		return popupMenu;
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

					LaunchTestLayeredPaneDndTable launcher = new LaunchTestLayeredPaneDndTable();
					log.debug("Off we go");
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
