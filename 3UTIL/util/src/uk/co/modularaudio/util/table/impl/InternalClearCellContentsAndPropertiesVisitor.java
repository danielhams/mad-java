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

import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellVisitor;

public class InternalClearCellContentsAndPropertiesVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties> implements TableCellVisitor<A, B>
{
	
	private Set<A> contentsToRemove = new HashSet<A>();
	private Set<B> propertiesToRemove = new HashSet<B>();
	public InternalClearCellContentsAndPropertiesVisitor()
	{
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
		A a = htc.getCellContents();
		if( a != null )
		{
			contentsToRemove.add( a );
		}
		htc.setCellContents( null, -1, -1 );
		B b = htcp.getCellProperties();
		if( b != null )
		{
			propertiesToRemove.add( b );
		}
		htcp.setCellProperties( null );
	}

	@Override
	public void begin()
	{
	}

	@Override
	public void cleanup()
	{
		for( A a : contentsToRemove )
		{
			a.removalFromTable();
		}
		for( B b : propertiesToRemove )
		{
			b.removalFromTable();
		}
	}
}
