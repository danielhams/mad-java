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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableCellVisitor;

public class TablePrinterVisitor<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties>
	implements TableCellVisitor<A, B>
{
	private static Log log = LogFactory.getLog( TablePrinterVisitor.class.getName() );

	@Override
	public boolean isAllDone()
	{
		return false;
	}

	@Override
	public void visit(final TableCell<A> htc, final TableCellProperties<B> htcp, final int indexInModel )
	{
		final A contents = htc.getCellContents();
		final B properties = htcp.getCellProperties();
		if( contents != null )
		{
			final String positionStr = "indexInModel(" + indexInModel + ") position(" + htc.getCellX() + "," + htc.getCellY() + ") - origin(" +
				htc.getOriginCellX() + "," + htc.getOriginCellY() + ")";
			if( log.isDebugEnabled() )
			{
				log.debug("The cell " + positionStr + " contains " + contents.toString());
				if( properties != null )
				{
					log.debug("Properties: " + properties.toString());
				}
			}
		}
		else
		{
			if( properties != null )
			{
				log.error( "Found properties with no associated table cell set!");
			}
		}
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
