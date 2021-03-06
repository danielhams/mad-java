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
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellVisitor;

public class InternalEmptyCellsOnlyVisitorWrapper<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties>
	implements TableCellVisitor<A, B>
{
	
	private TableCellVisitor<A, B> realVisitor = null;

	public InternalEmptyCellsOnlyVisitorWrapper(TableCellVisitor<A, B> visitor)
	{
		this.realVisitor = visitor;
	}

	@Override
	public void begin()
	{
		realVisitor.begin();
	}

	@Override
	public void cleanup()
	{
		realVisitor.cleanup();
	}

	@Override
	public boolean isAllDone()
	{
		return realVisitor.isAllDone();
	}

	@Override
	public void visit(TableCell<A> a, TableCellProperties<B> b, int indexInModel )
	{
		if( a.getCellContents() == null )
		{
			realVisitor.visit( a, b, indexInModel );
		}
	}
}
