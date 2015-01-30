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

package uk.co.modularaudio.util.audio.gui.mad.rack;

import java.util.EventObject;

/**
 * Shameless stolen from TableModelEvent which I can't subclass as HamTable
 * isn't a subclass of table model....
 *
 * @author dan
 *
 */
public class RackIOLinkEvent extends EventObject
{
	/**
	 *
	 */
	private static final long serialVersionUID = 6214316660381045148L;

	public RackDataModel mySource;

	/** Identifies the addtion of new rows or columns. */
	public static final int INSERT = 1;

	/** Identifies a change to existing data. */
	public static final int UPDATE = 0;

	/** Identifies the removal of rows or columns. */
	public static final int DELETE = -1;

	/** Identifies the header row. */
	public static final int HEADER_ROW = -1;

	//
	// Instance Variables
	//

	protected int type;

	protected int firstRow;

	protected int lastRow;

	//
	// Constructors
	//

	/**
	 * All row data in the table has changed, listeners should discard any state
	 * that was based on the rows and requery the <code>TableModel</code> to get
	 * the new row count and all the appropriate values. The <code>JTable</code>
	 * will repaint the entire visible region on receiving this event, querying
	 * the model for the cell values that are visible. The structure of the
	 * table ie, the column names, types and order have not changed.
	 */
	public RackIOLinkEvent( final RackDataModel source )
	{
		// Use Integer.MAX_VALUE instead of getRowCount() in case rows were
		// deleted.
		this(source, 0, Integer.MAX_VALUE, UPDATE);
	}

	/**
	 * This row of data has been updated. To denote the arrival of a completely
	 * new table with a different structure use <code>HEADER_ROW</code> as the
	 * value for the <code>row</code>. When the <code>JTable</code> receives
	 * this event and its <code>autoCreateColumnsFromModel</code> flag is set it
	 * discards any TableColumns that it had and reallocates default ones in the
	 * order they appear in the model. This is the same as calling
	 * <code>setModel(TableModel)</code> on the <code>JTable</code>.
	 */
	public RackIOLinkEvent( final RackDataModel source, final int row)
	{
		this(source, row, row, UPDATE );
	}

	/**
	 * The data in rows [<I>firstRow</I>, <I>lastRow</I>] have been updated.
	 */
	public RackIOLinkEvent(final RackDataModel source, final int firstRow, final int lastRow)
	{
		this(source, firstRow, lastRow, UPDATE);
	}

	/**
	 * The cells from (firstRow, column) to (lastRow, column) have been changed.
	 * The <I>column</I> refers to the column index of the cell in the model's
	 * co-ordinate system. When <I>column</I> is ALL_COLUMNS, all cells in the
	 * specified range of rows are considered changed.
	 * <p>
	 * The <I>type</I> should be one of: INSERT, UPDATE and DELETE.
	 */
	public RackIOLinkEvent(final RackDataModel source, final int firstRow, final int lastRow, final int type)
	{
		super(source);
		this.mySource = source;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.type = type;
	}

	//
	// Querying Methods
	//

	/**
	 * @return Returns the first row that changed. HEADER_ROW means the meta data, ie.
	 * names, types and order of the columns.
	 */
	public int getFirstRow()
	{
		return firstRow;
	};

	/**
	 * @return Returns the last row that changed.
	 */
	public int getLastRow()
	{
		return lastRow;
	};

	/**
	 * @return Returns the type of event - one of: INSERT, UPDATE and DELETE.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param source The table the event is for
	 * @param firstRow The start index of the change
	 * @param lastRow The end index (non-inclusive) of the change
	 * @param type The type of the change
	 */
	public void setValues( final RackDataModel source, final int firstRow, final int lastRow, final int type )
	{
		this.source = source;
		this.mySource = source;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.type = type;
	}
}
