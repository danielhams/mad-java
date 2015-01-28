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

package uk.co.modularaudio.util.swing.table;

import java.awt.Component;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;
import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TableModelListener;

public class GuiTableComponentTableDataModelListener<A extends RackModelTableSpanningContents,
	B extends SpanningContentsProperties,
	C extends Component>
	implements TableModelListener<A, B>
{
//	private static Log log = LogFactory.getLog( GuiTableComponentTableDataModelListener.class.getName() );

	private final GuiTable<A, B, C> table;

	public GuiTableComponentTableDataModelListener( final GuiTable<A, B, C> table )
	{
		this.table = table;
	}

	@Override
	public void tableChanged(final TableModelEvent<A, B> event)
	{
//		log.debug("Event received: " + event.toString());

		table.contentsChangeBegin();

		final int eventType = event.getType();
		final int startRow = event.getFirstRow();
		final int endRow = event.getLastRow();

		if( startRow == 0 && endRow == Integer.MAX_VALUE )
		{
			// Complete table action
			// TODO trap this baby
			throw new IndexOutOfBoundsException();
		}
		else
		{
			// Individual rows
			switch( eventType )
			{
			case TableModelEvent.INSERT:
				// New entries added into the table
				int iCounter = startRow;
				do
				{
					table.insertGuiComponentFromModelIndex( iCounter );
					iCounter++;
				}
				while( iCounter < endRow );
				break;
			case TableModelEvent.UPDATE:
				// Entries moved in the table
				int uCounter = startRow;
				do
				{
					table.updateGuiComponentFromModelIndex( uCounter );
					uCounter++;
				}
				while( uCounter < endRow );
				break;
			case TableModelEvent.DELETE:
				// Entries removed from the table
				int dCounter = startRow;
				do
				{
					table.removeGuiComponentFromModelIndex( startRow );
					dCounter++;
				}
				while( dCounter <= endRow );
				break;
			}
		}

		// TODO implementation should do these
		//		table.validate();
		//		table.repaint();
		table.contentsChangeEnd();
	}
}
