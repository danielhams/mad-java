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

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellVisitor;

public class InternalTableResizingVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> implements TableCellVisitor<A, B>
{
	private Span sourceTableSpan = null;
	private Span targetTableSpan = null;
	private Object[][] targetTableCells = null;
	private Object[][] targetTableProperties = null;

	private boolean wasSizingProblems = false;

	public InternalTableResizingVisitor( final Span sourceTableSpan,
			final Span targetTableSpan,
			final Object[][] targetTableCells,
			final Object[][] targetTableProperties )
	{
		this.sourceTableSpan = sourceTableSpan;
		this.targetTableSpan = targetTableSpan;
		this.targetTableCells = targetTableCells;
		this.targetTableProperties = targetTableProperties;
	}

	@Override
	public boolean isAllDone()
	{
		// If we've already found a problem we can finish
		return wasSizingProblems;
	}

	@Override
	public void visit(final TableCell<A> a, final TableCellProperties<B> b, final int indexInModel )
	{
		final A contents = a.getCellContents();
		final int cellX = a.getCellX();
		final int cellY = a.getCellY();

		if( contents != null || !wasSizingProblems )
		{
			// Check there is a place for this cell in the new table
			if( cellX < targetTableSpan.x && cellY < targetTableSpan.y )
			{
				targetTableCells[cellX][cellY] = a;
				targetTableProperties[cellX][cellY] = b;
			}
			else
			{
				wasSizingProblems = true;
			}
		}

	}

	@Override
	public void begin()
	{
		// Check the destination is as big as we need
		if( targetTableSpan.x < sourceTableSpan.x ||
				targetTableSpan.y < sourceTableSpan.y )
		{
			wasSizingProblems = true;
		}
	}

	@Override
	public void cleanup()
	{
		return;
//		// Do a pass over the target table creating any necessary new TableCell and TableCellProperties objects
//		if( !wasSizingProblems )
//		{
//			for( int i = 0 ; i < targetTableSpan.x ; i++ )
//			{
//				for( int j = 0 ; j < targetTableSpan.y ; j++ )
//				{
//					TableCell<A> c = (TableCell<A>)targetTableCells[i][j];
//					if( c == null )
//					{
//						targetTableCells[i][j] = new TableCell<A>( i, j);
//					}
//					TableCellProperties<B> p = (TableCellProperties<B>)targetTableProperties[i][j];
//					if( p == null )
//					{
//						targetTableProperties[i][j] = new TableCellProperties<B>();
//					}
//				}
//			}
//		}
//		throw new RuntimeException("NOT FINISHED");
	}

	public boolean wasSizingProblems()
	{
		return wasSizingProblems;
	}

}
