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

package uk.co.modularaudio.util.table;

import java.util.List;
import java.util.Set;

import uk.co.modularaudio.util.exception.DatastoreException;


public interface TableInterface<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties>
{
	// Contents in the table
	public A getContentsAtPosition( int x, int y );
	public B getPropertiesAtPosition( int x, int y );
	
	public void addContentsAtPosition( A contents, int x, int y)
		throws DatastoreException, ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException;

	public void addContentsAndPropertiesAtPosition( A contents, B properties, int x, int y)
		throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException;
	
	public boolean canStoreContentsAtPosition( A contents, int x, int y )
		throws ContentsAlreadyAddedException, TableIndexOutOfBoundsException;

	public boolean canMoveContentsToPosition( A contents, int x, int y )
		throws NoSuchContentsException, TableIndexOutOfBoundsException;
	
	public void moveContentsToPosition( A contents, int x, int y )
		throws DatastoreException, NoSuchContentsException, TableIndexOutOfBoundsException, ContentsAlreadyAddedException, TableCellFullException;


	public void removeContents( A contents )
		throws NoSuchContentsException;

	// Table dimensions
	public int getNumCols();
	public int getNumRows();
	public int getNumEntries();
	
	// Unique entries in the table
	public Set<A> getEntriesAsSet();
	public List<A> getEntriesAsList();
	// Useful for rapid iterating on things in the table as iterating with FastList is "malloc" free
//	public FastList<A> getEntriesAsFastList();
	public A getEntryAt(int index);
	public int getEntryIndexReturnMinusOne( A contents );
	
	// Visiting cells (and properties )
	public void visitCells( TableCellVisitor<A, B> visitor, boolean visitAllCells );
	public void visitCells( TableCellVisitor<A, B> visitor, int x, int y, int xSpan, int ySpan, boolean visitAllCells );
	public void visitFilledCells( TableCellVisitor<A, B> visitor );
	public void visitFilledCells( TableCellVisitor<A, B> visitor, int x, int y, int xSpan, int ySpan );
	public void visitEmptyCells( TableCellVisitor<A, B> visitor );
	
	// Finding where an object is actually stored (in fact, it's origin (top left) in the table)
	public TablePosition getContentsOriginReturnNull( A contents );
	
	public void addListener( TableModelListener<A,B> listener );
	public void removeListener( TableModelListener<A,B> listener );

	// Table dimensions manipulation
	public void resize(int i, int j) throws TableResizeException;
	public void insertColumn( int columnToInsert ) throws TableResizeException;
	public void insertRow( int rowToInsert ) throws TableResizeException;
	public void deleteColumn( int columnToDelete ) throws TableResizeException;
	public void deleteRow( int rowToDelete ) throws TableResizeException;

	// Cleaning up any circular references that may occur
	public void dirtyFixToCleanupReferences();
}
