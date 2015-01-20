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

import java.util.EventObject;

/**
 * Shameless stolen from TableModelEvent which I can't subclass as Table
 * isn't a subclass of table model....
 * 
 * @author dan
 * 
 * @param <A>
 * @param <B>
 */
public class TableModelEvent<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6214316660381045148L;

	public TableInterface<A, B> mySource = null;

	/** Identifies the addtion of new rows or columns. */
	public static final int INSERT = 1;

	/** Identifies a change to existing data. */
	public static final int UPDATE = 0;

	/** Identifies the removal of rows or columns. */
	public static final int DELETE = -1;

	/** Identifies the header row. */
	public static final int HEADER_ROW = -1;

	protected int type;

	protected int firstRow;

	protected int lastRow;

	//
	// Constructors
	//

	public TableModelEvent(TableInterface<A, B> source)
	{
		// Use Integer.MAX_VALUE instead of getRowCount() in case rows were
		// deleted.
		this(source, 0, Integer.MAX_VALUE, UPDATE);
	}

	public TableModelEvent(TableInterface<A, B> source, int row)
	{
		this(source, row, row, UPDATE);
	}

	public TableModelEvent(TableInterface<A, B> source, int firstRow, int lastRow )
	{
		this(source, firstRow, lastRow, UPDATE);
	}

	public TableModelEvent(TableInterface<A, B> source, int firstRow, int lastRow, int type)
	{
		super(source);
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.type = type;
	}

	//
	// Querying Methods
	//

	/**
	 * Returns the first row that changed. HEADER_ROW means the meta data, ie.
	 * names, types and order of the columns.
	 */
	public int getFirstRow()
	{
		return firstRow;
	};

	/** Returns the last row that changed. */
	public int getLastRow()
	{
		return lastRow;
	};

	/**
	 * Returns the type of event - one of: INSERT, UPDATE and DELETE.
	 */
	public int getType()
	{
		return type;
	}
	
	public String toString()
	{
		return("Type(" + type + ") firstRow(" + firstRow + ") lastRow(" + lastRow + ")");
	}

	public void setValues( TableInterface<A,B> source, int firstRow, int lastRow, int type )
	{
		this.source = source;
		this.mySource = source;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.type = type;		
	}

}
