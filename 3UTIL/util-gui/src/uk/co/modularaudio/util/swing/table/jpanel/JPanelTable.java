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

package uk.co.modularaudio.util.swing.table.jpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.table.GuiTable;
import uk.co.modularaudio.util.swing.table.GuiTableComponentTableDataModelListener;
import uk.co.modularaudio.util.swing.table.GuiTableComponentToGuiFactory;
import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.table.TablePosition;


public class JPanelTable<A extends RackModelTableSpanningContents, 
	B extends SpanningContentsProperties,
	C extends Component>
	extends JPanel
	implements GuiTable<A, B, C >
{
	private static final long serialVersionUID = -7415174078798644069L;
	
//	private static Log log = LogFactory.getLog( SwingTable.class.getName() );

	protected TableInterface<A, B> dataModel = null;
	protected Dimension gridSize = null;
	private boolean showGrid = false;
	private Color gridColour = null;
	protected int numCols = 1;
	protected int numRows = -1;
	
	private List<C> guiComponentListMirrorOfModel = new ArrayList<C>();
	private Map<C, A> guiComponentToTableModelComponentMap = new HashMap<C, A>();
	private Map<A, C> tableModelComponentToGuiComponentMap = new HashMap<A, C>();
	
	public A getTableModelComponentFromGui( C guiComponent )
	{
		return guiComponentToTableModelComponentMap.get( guiComponent );
	}
	
	public C getGuiComponentFromTableModel( A tableModelComponent )
	{
		return tableModelComponentToGuiComponentMap.get( tableModelComponent );
	}
	
	// A listener that updates our components when the model changes
	private GuiTableComponentTableDataModelListener<A, B, C> listener = null;
	
	// A factory that generates new GUI components from the table model component definition
	private GuiTableComponentToGuiFactory<A,C> factory = null;
	
	// Something used to "paint" empty cells
	private GuiTableEmptyCellPainter emptyCellPainter = null;
	
	public JPanelTable( TableInterface<A, B> dataModel,
			GuiTableEmptyCellPainter emptyCellPainter,
			GuiTableComponentToGuiFactory<A,C> factory,
			Dimension gridSize, 
			boolean showGrid, 
			Color gridColour )
	{
		this.dataModel = dataModel;
		this.emptyCellPainter = emptyCellPainter;
		this.gridSize = gridSize;
		this.showGrid = showGrid;
		this.gridColour = gridColour;
		this.factory = factory;
		this.listener = new GuiTableComponentTableDataModelListener<A, B, C>( this, factory );
		dataModel.addListener( listener );
		// Use an absolute layout
		setLayout( null );
		// Allow us to paint the background
		this.setOpaque( false );
		fullRefreshFromModel();
	}
	
	public void fullRefreshFromModel()
	{
		// Remove swing components
		this.removeAll();
		// Clear up the internal structures
		guiComponentListMirrorOfModel.clear();
		guiComponentToTableModelComponentMap.clear();
		tableModelComponentToGuiComponentMap.clear();
		
		// Now reset everything
		numCols = dataModel.getNumCols();
		numRows = dataModel.getNumRows();
		this.setPreferredSize( new Dimension( (numCols * gridSize.width) + 1, (numRows * gridSize.height ) + 1 ) );
		// Iterate over all the components in the data model setting them into their appropriate positions
		// Iterate over all the components in the data model setting them into their appropriate positions
		List<A> modelEntries = dataModel.getEntriesAsList();
		for( int i = 0 ; i < modelEntries.size() ; i++ )
		{
			A modelEntry = modelEntries.get( i );
			TablePosition tablePosition = dataModel.getContentsOriginReturnNull( modelEntry );
			C swingComponent = factory.generateSwingComponent( modelEntry );
			Span span = modelEntry.getCellSpan();
			addGuiComponentAtGrid( modelEntry,
					swingComponent,
					i,
					tablePosition.x,
					tablePosition.y,
					tablePosition.x + (span.x - 1),
					tablePosition.y + (span.y - 1));
		}
		this.invalidate();
		this.validate();
		this.repaint();
	}

	@Override
	public void paint(Graphics g)
	{
		swingTablePaint( g );
	}
	
	public void swingTablePaint( Graphics g )
	{
//		Graphics2D g2d = (Graphics2D) g.create();
		if( emptyCellPainter != null )
		{
			paintEmptyCells( g );
		}
		super.paint(g);
		if( showGrid )
		{
			paintTableGrid( g );
		}
	}
	
	private void paintEmptyCells( Graphics g )
	{
//		Graphics emptyG = g.create();
		if( emptyCellPainter.needSingleBlit() )
		{
			// Use a cache buffered image rendered to the entire size of the table.
			BufferedImage bi = emptyCellPainter.getSingleBlitBufferedImage( gridSize, numCols, numRows );
			g.drawImage( bi, 0, 0, null );
		}
		else
		{
			// For optimisations sake we should only paint what is visible.
			// We do this by passing the current viewing region (clip)
			Rectangle clipBounds = g.getClipBounds();
			
			// Work out the span this gives us
			int spanStartX = (int)Math.floor( (double)(clipBounds.x) / gridSize.width );
			int spanEndX = Math.min( (int)Math.ceil( (double)(clipBounds.x + clipBounds.width) / gridSize.width ), numCols );
			int spanStartY = (int)Math.floor( (double)(clipBounds.y) / gridSize.height );
			int spanEndY = Math.min( (int)Math.ceil( (double)(clipBounds.y + clipBounds.height) / gridSize.height ), numRows );
			
//			SwingTableEmptyCellPaintingVisitor<A, B> visitor = new SwingTableEmptyCellPaintingVisitor<A, B>( g ,
//					gridSize,
//					clipBounds,
//					emptyCellPainter );
//
//			dataModel.visitCells(visitor,  spanStartX, spanStartY, spanEndX - spanStartX, spanEndY - spanStartY, true );
	
			// Using the visitor is expensive - just loop around the span we have
			for( int x = spanStartX ; x < spanEndX ; x++ )
			{
				for( int y = spanStartY ; y < spanEndY ; y++ )
				{
					emptyCellPainter.paintEmptyCell(g,  x * gridSize.width, y * gridSize.height, gridSize.width, gridSize.height );
				}
			}
		}
	}

	private void paintTableGrid(Graphics g)
	{
		// Now paint our grid (simple loop over X and then Y)
		Graphics gridG = g.create();
		gridG.setColor( gridColour );
		for( int i = 0 ; i <= numCols ; i++ )
		{
			int lineStartY = 0;
			int lineX = i * gridSize.width;
			int lineEndY = numRows * gridSize.height;
			gridG.drawLine(lineX, lineStartY, lineX, lineEndY );
		}
		for( int j = 0 ; j <= numRows ; j++ )
		{
			int lineStartX = 0;
			int lineY = j * gridSize.height;
			int lineEndX = numCols * gridSize.width;
			gridG.drawLine(lineStartX, lineY, lineEndX, lineY );
		}
	}

	public void addGuiComponentAtGrid( A tableModelComponent,
			C guiComponent,
			int indexInModel,
			int gridStartX, int gridStartY, int gridEndX, int gridEndY )
	{
		// Set the bounds
		int startX = gridStartX * gridSize.width;
		int startY = gridStartY * gridSize.height;
		int endX = ((gridEndX + 1) * gridSize.width);
		int endY = ((gridEndY + 1) * gridSize.height);
		guiComponent.setBounds( new Rectangle( startX, startY, endX - startX, endY - startY ) );
		this.add( guiComponent );
		guiComponentToTableModelComponentMap.put( guiComponent, tableModelComponent );
		guiComponentListMirrorOfModel.add( guiComponent );
		tableModelComponentToGuiComponentMap.put( tableModelComponent, guiComponent );
	}
	
	protected void moveGuiComponentInGrid( C guiComponent,
		int gridStartX, int gridStartY, int gridEndX, int gridEndY )
	{
		// Set the bounds
		int startX = gridStartX * gridSize.width;
		int startY = gridStartY * gridSize.height;
		int endX = ((gridEndX + 1) * gridSize.width);
		int endY = ((gridEndY + 1) * gridSize.height);
		guiComponent.setBounds( new Rectangle( startX, startY, endX - startX, endY - startY ) );
		guiComponent.invalidate();
	}

	protected Set<A> getTableModelComponentEntries()
	{
		return new HashSet<A>(tableModelComponentToGuiComponentMap.keySet());
	}

	protected void removeTableModelComponentAndGui( A tableModelComponent,
			int indexInModel )
	{
		C guiComponent = tableModelComponentToGuiComponentMap.get( tableModelComponent );
		assert( guiComponent != null);
		guiComponentListMirrorOfModel.remove( indexInModel );
		guiComponentToTableModelComponentMap.remove( guiComponent );
		tableModelComponentToGuiComponentMap.remove( tableModelComponent );
		this.remove( guiComponent );
	}
	
	public Point pointToTableIndexes( Point tablePoint )
	{
		int colNum = tablePoint.x / gridSize.width;
		int rowNum = tablePoint.y / gridSize.height;
		if( colNum < 0 )
		{
			colNum = 0;
		}
		if( colNum > numCols - 1 )
		{
			colNum = numCols - 1;
		}
		if( rowNum < 0 )
		{
			rowNum = 0;
		}
		if( rowNum > numRows - 1 )
		{
			rowNum = numRows - 1;
		}
		
		return new Point( colNum, rowNum );
	}

	public Dimension getGridSize()
	{
		return gridSize;
	}

	public void insertGuiComponentFromModelIndex(int index)
	{
		// We don't have a gui component yet, so fetch the table model component at that index,
		// create a new gui component for it, and add it in the appropriate place.
		A tableModelContents = dataModel.getEntryAt( index );
		C guiComponent = factory.generateSwingComponent( tableModelContents );
		TablePosition tp = dataModel.getContentsOriginReturnNull( tableModelContents );
		Span span = tableModelContents.getCellSpan();
		int gridStartX = tp.x;
		int gridStartY = tp.y;
		int gridEndX = tp.x + (span.x - 1);
		int gridEndY = tp.y + (span.y - 1);
		this.addGuiComponentAtGrid( tableModelContents, guiComponent, index, 
				gridStartX, gridStartY, gridEndX, gridEndY );
	}

	public void updateGuiComponentFromModelIndex(int index)
	{
		A tableModelContents = dataModel.getEntryAt( index );
		C guiComponent = tableModelComponentToGuiComponentMap.get( tableModelContents );
		TablePosition tp = dataModel.getContentsOriginReturnNull( tableModelContents );
		Span span = tableModelContents.getCellSpan();
		int gridStartX = tp.x;
		int gridStartY = tp.y;
		int gridEndX = tp.x + (span.x - 1);
		int gridEndY = tp.y + (span.y - 1);
		this.moveGuiComponentInGrid( guiComponent,
				gridStartX, gridStartY, gridEndX, gridEndY );
	}

	public void removeGuiComponentFromModelIndex(int index)
	{
		C guiComponent = guiComponentListMirrorOfModel.get( index );
		this.remove( guiComponent );
		guiComponentListMirrorOfModel.remove( guiComponent );
		guiComponentToTableModelComponentMap.remove( guiComponent );
	}
	
	public void setDataModel( TableInterface<A, B> dataModel )
	{
		this.dataModel.removeListener(listener);
		this.dataModel = dataModel;
		this.dataModel.addListener( listener );
		fullRefreshFromModel();
	}

	@Override
	public void contentsChangeBegin()
	{
	}

	@Override
	public void contentsChangeEnd()
	{
		// Force a repaint
		this.invalidate();
		this.repaint();
	}

	@Override
	public void destroy()
	{
		guiComponentListMirrorOfModel.clear();
		guiComponentToTableModelComponentMap.clear();
		tableModelComponentToGuiComponentMap.clear();
		listener = null;
		factory = null;
		dataModel.dirtyFixToCleanupReferences();		
	}
}
