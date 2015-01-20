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

public class InternalSetCellContentsAndPropertiesVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> implements TableCellVisitor<A, B>
{
	private A contents = null;
	private B properties = null;
	private int originCellX = -1;
	private int originCellY = -1;

	public InternalSetCellContentsAndPropertiesVisitor(A contents, B properties, int originCellX, int originCellY )
	{
		this.contents = contents;
		this.properties = properties;
		this.originCellX = originCellX;
		this.originCellY = originCellY;
	}

	@Override
	public boolean isAllDone()
	{
		// Do all cells
		return false;
	}

	@Override
	public void visit(TableCell<A> htc, TableCellProperties<B> htcp, int indexInModel )
	{
		htc.setCellContents( contents, originCellX, originCellY );
		htcp.setCellProperties( properties );
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
