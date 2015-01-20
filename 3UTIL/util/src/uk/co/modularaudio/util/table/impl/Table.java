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

package uk.co.modularaudio.util.table.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableCellVisitor;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TableModelListener;
import uk.co.modularaudio.util.table.TablePosition;
import uk.co.modularaudio.util.table.TableResizeException;


public class Table<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> implements TableInterface<A, B>
{
//	private static Log log = LogFactory.getLog( Table.class.getName() );
	
	private Span tableSpan = null;
	
	private Object[][] tableCells;
	private Object[][] tableProperties;
	
	private List<A> objectsInTable = new ArrayList<A>();
//	private FastList<A> objectsInTableFastList = new FastList<A>();
	private Map<A, TablePosition> objectToPositionMap = new HashMap<A, TablePosition>();
	
	private List<TableModelListener<A,B>> listeners = new ArrayList<TableModelListener<A,B>>();
	
	protected TableModelEvent<A, B> outEvent = new TableModelEvent<A, B>( this, 0, 0, 0 );
	
	@SuppressWarnings("unchecked")
	protected void visitTableCellsForSpan( int startX,
			int startY,
			Span span,
			TableCellVisitor<A,B> visitor,
			boolean visitAllCells,
			boolean visitEmptyCells )
	{
		int xSpan = span.x;
		int ySpan = span.y;
		
		visitor.begin();
		
		boolean allDone = visitor.isAllDone();
		
		for( int i = startX; !allDone && i < startX + xSpan ; i++ )
		{
			for( int j = startY ; !allDone && j < startY + ySpan ; j++ )
			{
				Object tco = tableCells[i][j];
				TableCell<A> c = (TableCell<A>) tco;

				// If we shouldn't visit all cells we skip table entries where
				// the cell isn't marked as the origin cell
				boolean shouldVisit = false;
				if( visitAllCells )
				{
					shouldVisit = true;
				}
				else
				{
					if( !visitEmptyCells && c.getCellContents() == null )
					{
						shouldVisit = false;
					}
					else
					{
						shouldVisit = ( c.getCellX() == c.getOriginCellX() && c.getCellY() == c.getOriginCellY() );
					}
					
				}
				
				if( shouldVisit )
				{
					Object tcp = tableProperties[i][j];
					TableCellProperties<B> p = (TableCellProperties<B>)tcp;
					visitor.visit( c, p, getEntryIndexReturnMinusOne( c.getCellContents() ) );
					allDone = visitor.isAllDone();
				}
			}
		}
		visitor.cleanup();
	}

	public Table(int x, int y)
	{
		tableSpan = new Span( x, y );
		// Java doesn't let us allocate generic arrays
		tableCells = new Object[x][y];
		tableProperties = new Object[x][y];
		
		// Fill it up
		for( int i = 0 ; i < x ; i++ )
		{
			for( int j = 0 ; j < y ; j++ )
			{
				tableCells[i][j] = new TableCell<A>( i, j );
				tableProperties[i][j] = new TableCellProperties<B>();
			}
		}
	}

	@Override
	public void addContentsAtPosition( A contents, int x, int y)
		throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException
	{
		this.addContentsAndPropertiesAtPosition(contents, null, x, y);
	}
	
	@Override
	public void addContentsAndPropertiesAtPosition(A contents, B properties, int x, int y)
			throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException
	{
		int newContentsIndex = noEventFireAddContentsAndProperties(contents, properties, x, y);

		// Finally emit a "contents inserted" for this objects index in the internal contents list
		outEvent.setValues( this, newContentsIndex, newContentsIndex, TableModelEvent.INSERT );
		fireTableChangedEvent( outEvent );
	}

	protected int noEventFireAddContentsAndProperties(A contents, B properties, int x, int y)
			throws ContentsAlreadyAddedException, TableIndexOutOfBoundsException, TableCellFullException
	{
		// Check if all cells are free
		if( !canStoreContentsAtPosition(contents, x, y) )
		{
			String msg = "Unable to find sufficient space for contents " + contents.toString() + " at cell (" + x + ", " + y + ")";
			throw new TableCellFullException( msg );
		}
		Span contentCellSpan = contents.getCellSpan();

		// Ok, object is new and all the table cells are free.
		InternalSetCellContentsAndPropertiesVisitor<A,B> sccv = 
			new InternalSetCellContentsAndPropertiesVisitor<A,B>( contents, properties, x, y );
		
		visitTableCellsForSpan( x, y, contentCellSpan, sccv, true, true );
		
		// Now add this object into the set of objects already in the table
		int newContentsIndex = objectsInTable.size();
		objectsInTable.add( contents );
		objectToPositionMap.put( contents, new TablePosition( x, y ) );
		return newContentsIndex;
	}

	@Override
	public void removeContents(A contents) throws NoSuchContentsException
	{
		int indexOfRemovedObject = noEventFireRemoveContents(contents);
		
		// Now fire a rows deleted event
		outEvent.setValues( this, indexOfRemovedObject, indexOfRemovedObject, TableModelEvent.DELETE );
		fireTableChangedEvent( outEvent );
	}

	@SuppressWarnings("unchecked")
	protected int noEventFireRemoveContents(A contents) throws NoSuchContentsException
	{
		TablePosition position = objectToPositionMap.get( contents );
		// Check it's in the table first
		if( position == null )
		{
			throw new NoSuchContentsException();
		}
		// delete it from inside the table
		// We fetch the origin and span info, then loop over it
		TableCell<A> cell = (TableCell<A>)tableCells[ position.x ][ position.y ];
		
		int originCellX = cell.getOriginCellX();
		int originCellY = cell.getOriginCellY();
		
		Span cellContentsSpan = cell.getCellContents().getCellSpan();
		
		InternalClearCellContentsAndPropertiesVisitor<A, B> cellAndPropertiesClearVisitor = new InternalClearCellContentsAndPropertiesVisitor<A, B>();
	
		visitTableCellsForSpan(originCellX, originCellY, cellContentsSpan, cellAndPropertiesClearVisitor, true, true );
		
		// Remove it from the table of objects
		int indexOfRemovedObject = objectsInTable.indexOf( contents );
		objectsInTable.remove( indexOfRemovedObject );
		objectToPositionMap.remove( contents );
		return indexOfRemovedObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void moveContentsToPosition(A contents, int x, int y) throws DatastoreException, NoSuchContentsException,
			TableIndexOutOfBoundsException, TableCellFullException
	{
		// Use the internal no event fire methods to remove and then add the contents
		if( !objectToPositionMap.containsKey( contents ) )
		{
			throw new NoSuchContentsException();
		}
		int indexOfUpdatedContents = objectsInTable.indexOf( contents );
		TablePosition currentPosition = getContentsOriginReturnNull( contents );
		B properties = getPropertiesAtPosition( currentPosition.x, currentPosition.y );

		// Removing from cells
		TableCell<A> cell = (TableCell<A>)tableCells[ currentPosition.x ][ currentPosition.y ];
		
		int originCellX = cell.getOriginCellX();
		int originCellY = cell.getOriginCellY();
		
		Span cellContentsSpan = cell.getCellContents().getCellSpan();
		
		InternalClearCellContentsAndPropertiesVisitor<A, B> cellAndPropertiesClearVisitor = new InternalClearCellContentsAndPropertiesVisitor<A, B>();
	
		visitTableCellsForSpan(originCellX, originCellY, cellContentsSpan, cellAndPropertiesClearVisitor, true, true );
		
		// Adding it back at the new position
		InternalSetCellContentsAndPropertiesVisitor<A,B> sccv = 
			new InternalSetCellContentsAndPropertiesVisitor<A,B>( contents, properties, x, y );
		
		visitTableCellsForSpan( x, y, cellContentsSpan, sccv, true, true );
		
		TablePosition positionToUpdate = objectToPositionMap.get( contents );
		positionToUpdate.x = x;
		positionToUpdate.y = y;

		// Now fire a content updated event
		outEvent.setValues( this, indexOfUpdatedContents, indexOfUpdatedContents, TableModelEvent.UPDATE );
		fireTableChangedEvent( outEvent );
	}

	protected void fireTableChangedEvent(TableModelEvent<A, B> outEvent)
	{
		for( TableModelListener<A, B> lis : listeners )
		{
			lis.tableChanged( outEvent );
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public A getContentsAtPosition(int i, int j)
	{
		TableCell<A> cell = (TableCell<A>) tableCells[i][j];
		return cell.getCellContents();
	}

	@SuppressWarnings("unchecked")
	@Override
	public B getPropertiesAtPosition(int i, int j)
	{
		TableCellProperties<B> cellProperties = (TableCellProperties<B>)tableProperties[i][j];
		return cellProperties.getCellProperties();
	}

	@Override
	public int getNumCols()
	{
		return tableSpan.x;
	}

	@Override
	public int getNumRows()
	{
		return tableSpan.y;
	}
	
	@Override
	public int getNumEntries()
	{
		return objectsInTable.size();
	}
	
	@Override
	public Set<A> getEntriesAsSet()
	{
		return objectToPositionMap.keySet();
	}
	
	@Override
	public List<A> getEntriesAsList()
	{
		return objectsInTable;
	}
	
//	@Override
//	public FastList<A> getEntriesAsFastList()
//	{
//		return objectsInTableFastList;
//	}
	
	@Override
	public A getEntryAt( int index )
	{
		return objectsInTable.get( index );
	}
	
	@Override
	public int getEntryIndexReturnMinusOne( A contents )
	{
		if( objectsInTable.contains( contents ) )
		{
			return objectsInTable.indexOf( contents );
		}
		else
		{
			return -1;
		}
	}

	@Override
	public void visitCells(TableCellVisitor<A, B> visitor, boolean visitAllCells )
	{
		visitTableCellsForSpan( 0, 0, tableSpan, visitor, visitAllCells, true );
	}

	@Override
	public void visitCells(TableCellVisitor<A, B> visitor, int x, int y, int xSpan, int ySpan, boolean visitAllCells )
	{
		visitTableCellsForSpan( x, y, new Span( xSpan, ySpan ), visitor, visitAllCells, true );
	}
	
	@Override
	public void visitFilledCells(TableCellVisitor<A, B> visitor )
	{
		visitTableCellsForSpan( 0, 0, tableSpan, visitor, false, false );
	}
	
	@Override
	public void visitFilledCells(TableCellVisitor<A, B> visitor, int x, int y, int xSpan, int ySpan )
	{
		visitTableCellsForSpan( x, y, new Span( xSpan, ySpan ), visitor, false, false );
	}
	
	@Override
	public void visitEmptyCells( TableCellVisitor<A, B> visitor )
	{
		InternalEmptyCellsOnlyVisitorWrapper<A, B> wrapper = new InternalEmptyCellsOnlyVisitorWrapper<A, B>( visitor );
		visitTableCellsForSpan( 0, 0, tableSpan, wrapper, true, true );
	}
	
	
	@Override
	public TablePosition getContentsOriginReturnNull(A contents)
	{
		TablePosition retVal = objectToPositionMap.get( contents );
		
		return retVal;
	}

	@Override
	public void addListener(TableModelListener<A, B> listener)
	{
		this.listeners.add( listener );
	}

	@Override
	public void removeListener(TableModelListener<A,B> listener)
	{
		this.listeners.remove( listener );
	}

	@Override
	public boolean canStoreContentsAtPosition(A contents, int x, int y) throws ContentsAlreadyAddedException, TableIndexOutOfBoundsException
	{
		InternalCheckEmptyCellVisitor<A,B> visitorToUse = new InternalCheckEmptyCellVisitor<A,B>();
		canStoreContentsAtPosition( visitorToUse, contents, x, y, true);
		return visitorToUse.wasEmpty();
	}
	
	@Override
	public boolean canMoveContentsToPosition( A contents, int x, int y ) throws NoSuchContentsException, TableIndexOutOfBoundsException
	{
		if( !objectToPositionMap.keySet().contains( contents ) )
		{
			throw new NoSuchContentsException();
		}
		
//		try
//		{
//			InternalCheckEmptyCellVisitorIgnoreContents<A, B> visitorToUse = new InternalCheckEmptyCellVisitorIgnoreContents<A, B>( contents );
//			canStoreContentsAtPosition( visitorToUse, contents, x, y, false);
//			return visitorToUse.wasEmpty();
//		}
//		catch (ContentsAlreadyAddedException e)
//		{
//			// Really shouldn't happen.
//			e.printStackTrace();
//			return false;
//		}
		// optimised version
		boolean canAdd = true;
		Span span = contents.getCellSpan();
		for( int i = x ; canAdd && i < x + span.x ; i++ )
		{
			for( int j = y ; canAdd && j < y + span.y ; j++ )
			{
				if( i >= 0 && (i < tableSpan.x) && j >= 0 && j < (tableSpan.y) )
				{
					Object o = tableCells[i][j];
					@SuppressWarnings("unchecked")
					RackModelTableSpanningContents cell = ((TableCell<RackModelTableSpanningContents>)o).getCellContents();
					if( cell != null && cell != contents )
					{
						canAdd = false;
					}
				}
				else
				{
					canAdd = false;
				}
			}
		}
		return canAdd;
	}
	
	protected void canStoreContentsAtPosition( TableCellVisitor<A,B> visitorToUse, A contents, int x, int y, boolean throwContentsAlreadyAdded )
		throws ContentsAlreadyAddedException, TableIndexOutOfBoundsException
	{
		if( throwContentsAlreadyAdded )
		{
			// Check if object already in the table
			if( objectToPositionMap.keySet().contains( contents ) )
			{
				throw new ContentsAlreadyAddedException();
			}
		}
		
		// Check it is within the bounds of the table span
		Span contentCellSpan = contents.getCellSpan();
		if( ( x < 0 || x + (contentCellSpan.x - 1) >= tableSpan.x ) ||
				( y < 0 || y + (contentCellSpan.y - 1) >= tableSpan.y ) )
		{
			String msg = "Unable to store " + contents.toString() + " at (" + x + ", " + y + ") - component violates table boundaries";
			throw new TableIndexOutOfBoundsException( msg );
		}

		// Use the provided visitor and let the caller determine what's correct
		visitTableCellsForSpan( x, y, contentCellSpan, visitorToUse, true, true );
	}

	@Override
	public void resize( int x, int y )
		throws TableResizeException
	{
		// First check that resizing doesn't cause problems
		boolean sizingProblems = false;
		
		if( x < 1 || y < 1 )
		{
			sizingProblems = true;
		}

		if( sizingProblems )
		{
			// For now just throw the exception
			throw new TableResizeException();
		}
		else
		{
			// Allocate a new backing array then iterate over the cells moving them across to the new array
			// adding any necessary new TableCell and TableCellProperties objects
			// then replace the internal array and update the tableSpan object
			Span newTableSpan = new Span( x, y );
			Object[][] newTableCells = new Object[x][y];
			Object[][] newTableProperties = new Object[x][y];
			InternalTableResizingVisitor<A, B> resizingVisitor = new InternalTableResizingVisitor<A, B>( tableSpan, 
					newTableSpan,
					newTableCells, 
					newTableProperties);
			visitTableCellsForSpan( 0, 0, tableSpan, resizingVisitor, true, true );
			
			if( resizingVisitor.wasSizingProblems() )
			{
				throw new TableResizeException();
			}
			else
			{
				tableSpan = newTableSpan;
				tableCells = newTableCells;
				tableProperties = newTableProperties;
			}
		}
	}

	@Override
	public void insertColumn(int columnToInsert) throws TableResizeException
	{
		// Here we use a visitor to create a list of all the contents that fills the column that will be inserted that is an origin cell
		// We create a list from these cells with new co-ordinates + 1 col and remove them from the existing table.
		// We then resize the table to have one more column, and then we re-insert all of the contents at their new positions.
		
		throw new TableResizeException();		
	}

	@Override
	public void deleteColumn(int columnToDelete) throws TableResizeException
	{
		throw new TableResizeException();		
	}

	@Override
	public void deleteRow(int rowToDelete) throws TableResizeException
	{
		throw new TableResizeException();		
	}

	@Override
	public void insertRow(int rowToInsert) throws TableResizeException
	{
		throw new TableResizeException();		
	}
	
	public void dirtyFixToCleanupReferences()
	{
		for( int row = 0 ; row < tableSpan.y ; row++ )
		{
			for( int col = 0 ; col < tableSpan.x ; col++ )
			{
				tableCells[ col ][ row ] = null;
				tableProperties[ col ][ row ] = null;
			}
		}
		objectsInTable.clear();
		objectToPositionMap.clear();
		listeners.clear();
		outEvent.setValues( null,  -1, -1, -1 );
	}

}
