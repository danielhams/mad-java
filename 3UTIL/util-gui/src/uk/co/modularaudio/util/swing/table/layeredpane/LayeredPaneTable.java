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

package uk.co.modularaudio.util.swing.table.layeredpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLayeredPane;

import uk.co.modularaudio.util.swing.table.GuiTable;
import uk.co.modularaudio.util.swing.table.GuiTableComponentTableDataModelListener;
import uk.co.modularaudio.util.swing.table.GuiTableComponentToGuiFactory;
import uk.co.modularaudio.util.swing.table.GuiTableEmptyCellPainter;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.table.TablePosition;

public class LayeredPaneTable<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties, C extends Component & LayeredPaneTableComponent>
	extends JLayeredPane
	implements GuiTable<A, B, C>
{
	private static final long serialVersionUID = 3381860590625793596L;

	// Layered pane defines:
	// DEFAULT, PALETTE, MODAL, POPUP, DRAG
	// Background lives in DEFAULT
	// Position hints live in PALETTE
	// Components live in MODAL
	// Static wires live in Components + 1
	// Dragged components live in Static Wires + 1
	// Dragged wires lines in dragged components + 1
	public static final int LPT_BACKGROUND_LAYER = 1;
	public static final int LPT_COMPONENTS_LAYER = 50;
	public static final int LPT_POSITIONHINT_LAYER = 100;
	public static final int LPT_STATICWIRE_LAYER = 150;
	public static final int LPT_DRAGGEDCOMPONENTS_LAYER = 200;
	public static final int LPT_DRAGGEDWIRE_LAYER = 250;
	public static final int LPT_TOOLTIP_LAYER = 300;

//	private static Log log = LogFactory.getLog( LayeredPaneTable.class.getName() );

	protected TableInterface<A, B> dataModel;
	protected final Dimension gridSize;
	protected int numCols = -1;
	protected int numRows = -1;
	protected final boolean showGrid;
	protected final Color gridColour;
	protected GuiTableComponentTableDataModelListener<A, B, C> listener;
	protected GuiTableComponentToGuiFactory<A,C> factory;

	private final GuiTableEmptyCellPainter emptyCellPainter;

	public LayeredPaneTable( final TableInterface<A, B> dataModel,
			final GuiTableComponentToGuiFactory<A,C> factory,
			final Dimension gridSize,
			final boolean showGrid,
			final Color gridColour,
			final GuiTableEmptyCellPainter emptyCellPainter )
	{
		this.dataModel = dataModel;
		this.gridSize = gridSize;
		this.showGrid = showGrid;
		this.gridColour = gridColour;
		this.factory = factory;
		this.listener = new GuiTableComponentTableDataModelListener<A, B, C>( this );
		dataModel.addListener( listener );
		// Use an absolute layout
		setLayout( null );
		// We are opaque
		this.setOpaque( true );
		// Set the empty cell painter we will use
		this.emptyCellPainter = emptyCellPainter;

		fullRefreshFromModel();
	}

	private void fullRefreshFromModel()
	{
		// Reset metadata
		numCols = dataModel.getNumCols();
		numRows = dataModel.getNumRows();
		this.setPreferredSize( new Dimension( (numCols * gridSize.width) + 1, (numRows * gridSize.height ) + 1 ) );

		// Remove the component we placed as the background
		installBackgroundComponent();

		// Clear up the internal structures
		clearUpInternalStructures();

		// Iterate over all the components in the data model setting them into their appropriate positions
		final List<A> modelEntries = dataModel.getEntriesAsList();
		for( int i = 0 ; i < modelEntries.size() ; i++ )
		{
			final A modelEntry = modelEntries.get( i );
			final TablePosition tablePosition = dataModel.getContentsOriginReturnNull( modelEntry );
			final C swingComponent = factory.generateSwingComponent( modelEntry );
			final Span span = modelEntry.getCellSpan();
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

	private void clearUpInternalStructures()
	{
		// Remove swing components in the right layer
		final Component[] comps = this.getComponentsInLayer( LPT_COMPONENTS_LAYER );
		for( final Component c : comps )
		{
			this.remove( c );
		}

		for( final C guiComponent : guiComponentListMirrorOfModel )
		{
			guiComponent.destroy();
		}
		guiComponentListMirrorOfModel.clear();
		guiComponentToTableModelComponentMap.clear();
		tableModelComponentToGuiComponentMap.clear();
	}

	private void installBackgroundComponent()
	{
		// Remove swing components in the right layer
		final Component[] comps = this.getComponentsInLayer( LPT_BACKGROUND_LAYER );
		for( final Component c : comps )
		{
			this.remove( c );
		}

		final LayeredPaneTableBackgroundComponent backgroundComponent = new LayeredPaneTableBackgroundComponent( emptyCellPainter,
				gridSize, numCols, numRows );
		this.setLayer( backgroundComponent, LPT_BACKGROUND_LAYER );
		backgroundComponent.setBounds( 0, 0, gridSize.width * numCols, gridSize.height * numRows );
		this.add( backgroundComponent );
	}

	// Internal caches
	private final ArrayList<C> guiComponentListMirrorOfModel = new ArrayList<C>();
	private final Map<C, A> guiComponentToTableModelComponentMap = new HashMap<C, A>();
	private final Map<A, C> tableModelComponentToGuiComponentMap = new HashMap<A, C>();

	public A getTableModelComponentFromGui( final C guiComponent )
	{
		return guiComponentToTableModelComponentMap.get( guiComponent );
	}

	public C getGuiComponentFromTableModel( final A tableModelComponent )
	{
		return tableModelComponentToGuiComponentMap.get( tableModelComponent );
	}

	@Override
	public void addGuiComponentAtGrid( final A tableModelComponent,
			final C guiComponent,
			final int indexInModel,
			final int gridStartX, final int gridStartY, final int gridEndX, final int gridEndY )
	{
		// Set the bounds
		final int startX = gridStartX * gridSize.width;
		final int startY = gridStartY * gridSize.height;
		final int endX = ((gridEndX + 1) * gridSize.width);
		final int endY = ((gridEndY + 1) * gridSize.height);
		guiComponent.setBounds( new Rectangle( startX, startY, endX - startX, endY - startY ) );
		this.setLayer( guiComponent, LPT_COMPONENTS_LAYER );
		this.add( guiComponent );
		guiComponentToTableModelComponentMap.put( guiComponent, tableModelComponent );
		guiComponentListMirrorOfModel.add( guiComponent );
		tableModelComponentToGuiComponentMap.put( tableModelComponent, guiComponent );
//		log.debug("Added gui component " + guiComponent.toString() + " at index " + indexInModel + " into grid at " + gridStartX + ", " + gridStartY );
	}

	protected void moveGuiComponentInGrid( final C guiComponent,
		final int gridStartX, final int gridStartY, final int gridEndX, final int gridEndY )
	{
		final Rectangle previousBounds = guiComponent.getBounds();
		this.repaint( previousBounds );
		// Set the bounds
		final int startX = gridStartX * gridSize.width;
		final int startY = gridStartY * gridSize.height;
		guiComponent.setLocation( startX, startY );
	}

	@Override
	public void insertGuiComponentFromModelIndex(final int index)
	{
		// We don't have a gui component yet, so fetch the table model component at that index,
		// create a new gui component for it, and add it in the appropriate place.
		final A tableModelContents = dataModel.getEntryAt( index );
		final C guiComponent = factory.generateSwingComponent( tableModelContents );
		final TablePosition tp = dataModel.getContentsOriginReturnNull( tableModelContents );
		final Span span = tableModelContents.getCellSpan();
		final int gridStartX = tp.x;
		final int gridStartY = tp.y;
		final int gridEndX = tp.x + (span.x - 1);
		final int gridEndY = tp.y + (span.y - 1);
		this.addGuiComponentAtGrid( tableModelContents, guiComponent, index,
				gridStartX, gridStartY, gridEndX, gridEndY );
//		log.debug("Inserted gui component " + guiComponent.toString() + " at index " + index );
	}

	@Override
	public void updateGuiComponentFromModelIndex(final int index)
	{
		final A tableModelContents = dataModel.getEntryAt( index );
		final C guiComponent = tableModelComponentToGuiComponentMap.get( tableModelContents );
		final TablePosition tp = dataModel.getContentsOriginReturnNull( tableModelContents );
		final Span span = tableModelContents.getCellSpan();
		final int gridStartX = tp.x;
		final int gridStartY = tp.y;
		final int gridEndX = tp.x + (span.x - 1);
		final int gridEndY = tp.y + (span.y - 1);
		this.moveGuiComponentInGrid( guiComponent,
				gridStartX, gridStartY, gridEndX, gridEndY );
	}

	@Override
	public void removeGuiComponentFromModelIndex(final int index)
	{
//		log.debug("Removing gui component at index " + index );
		final C guiComponent = guiComponentListMirrorOfModel.get( index );
		guiComponent.destroy();
//		log.debug("It is " + guiComponent.toString() );
		// Doing this remove doesn't seem to schedule a repaint - so do it manually
		final Rectangle removedComponentBounds = guiComponent.getBounds();
		this.remove( guiComponent );
		this.repaint( removedComponentBounds );
		guiComponentListMirrorOfModel.remove( guiComponent );
		guiComponentToTableModelComponentMap.remove( guiComponent );
	}

	@Override
	public void paint(final Graphics g)
	{
		layeredTablePaint(g);
	}

	public void layeredTablePaint(final Graphics g)
	{
		// Graphics2D g2d = (Graphics2D) g.create();
		super.paint(g);
		if (showGrid)
		{
			paintTableGrid(g);
		}
	}

	private void paintTableGrid(final Graphics g)
	{
		// Now paint our grid (simple loop over X and then Y)
		final Graphics gridG = g.create();
		gridG.setColor(gridColour);
		for (int i = 0; i <= numCols; i++)
		{
			final int lineStartY = 0;
			final int lineX = i * gridSize.width;
			final int lineEndY = numRows * gridSize.height;
			gridG.drawLine(lineX, lineStartY, lineX, lineEndY);
		}
		for (int j = 0; j <= numRows; j++)
		{
			final int lineStartX = 0;
			final int lineY = j * gridSize.height;
			final int lineEndX = numCols * gridSize.width;
			gridG.drawLine(lineStartX, lineY, lineEndX, lineY);
		}
	}

	public void pointToTableIndexes( final int[] tablePoint, final int[] outputPoint)
	{
		int colNum = tablePoint[0] / gridSize.width;
		int rowNum = tablePoint[1] / gridSize.height;
		if (colNum < 0)
		{
			colNum = 0;
		}
		if (colNum > numCols - 1)
		{
			colNum = numCols - 1;
		}
		if (rowNum < 0)
		{
			rowNum = 0;
		}
		if (rowNum > numRows - 1)
		{
			rowNum = numRows - 1;
		}

		outputPoint[0] = colNum;
		outputPoint[1] = rowNum;
	}

	@Override
	public void contentsChangeBegin()
	{
		// Do nothing, component _should_ take care of any redraws necessary
	}

	@Override
	public void contentsChangeEnd()
	{
		// Do nothing, component _should_ take care of any redraws necessary
		// Force a repaint
//		this.invalidate();
//		this.repaint();
	}

	public Dimension getGridSize()
	{
		return gridSize;
	}

	public void setDataModel( final TableInterface<A, B> dataModel )
	{
		if( this.dataModel != null )
		{
			this.dataModel.removeListener(listener);
		}
		this.dataModel = dataModel;
		fullRefreshFromModel();
		this.dataModel.addListener( listener );
	}

	@Override
	public void destroy()
	{
		dataModel.removeListener( listener );
		clearUpInternalStructures();
		listener = null;
		factory = null;
		dataModel = null;
	}
}
