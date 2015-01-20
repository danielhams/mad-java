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

public class InternalCheckEmptyCellVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> implements TableCellVisitor<A, B>
{
	private boolean wasEmpty = true;
	private boolean isDone = false;

	@Override
	public boolean isAllDone()
	{
		return isDone;
	}

	@Override
	public void visit(TableCell<A> htc, TableCellProperties<B> htcp, int indexInModel )
	{
		A contents = htc.getCellContents();
		if( contents != null )
		{
			wasEmpty = false;
			isDone = true;
		}
	}
	
	public boolean wasEmpty()
	{
		return wasEmpty;
	}

	@Override
	public void begin()
	{
	}

	@Override
	public void cleanup()
	{
	}

}
