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

package uk.co.modularaudio.service.gui.impl.racktable.back;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TableModelListener;

public class RackTableWithLinksRackModelListener implements TableModelListener<RackComponent, RackComponentProperties>
{
	private static Log log = LogFactory.getLog( RackTableWithLinksRackModelListener.class.getName() );

	private final RackLinkPainter linkPainter;

	public RackTableWithLinksRackModelListener( final RackLinkPainter linkPainter )
	{
		this.linkPainter = linkPainter;
	}

	@Override
	public void tableChanged(final TableModelEvent<RackComponent, RackComponentProperties> event)
	{
//		log.debug("tableChanged received");
		final RackDataModel dataModel = (RackDataModel) event.getSource();
		final int eventType = event.getType();
		final int startRow = event.getFirstRow();
		final int endRow = event.getLastRow();
		if( startRow == 0 && endRow == Integer.MAX_VALUE )
		{
			log.error("NOT IMPLEMENTED!");
		}
		else
		{
			// Individual rows
			switch( eventType )
			{
			case TableModelEvent.INSERT:
				// New entries added into the table
				// We don't care as they won't have any links to start with and we'll receive the
				// "new link" event in the link event listener
				break;
			case TableModelEvent.UPDATE:
				// Entries moved in the table
				int uCounter = startRow;
				do
				{
					final RackComponent componentToFindLinksFor = dataModel.getEntryAt( uCounter );
					final List<RackLink> linksToRefresh = linkPainter.getLinksForComponent( componentToFindLinksFor );
					for( final RackLink oneLink : linksToRefresh )
					{
						linkPainter.updateLink( oneLink );
					}
					final List<RackIOLink> ioLinksToRefresh = linkPainter.getIOLinksForComponent( componentToFindLinksFor );
					for( final RackIOLink oneIOLink : ioLinksToRefresh )
					{
						linkPainter.updateIOLink( oneIOLink );
					}
					uCounter++;
				}
				while( uCounter <= endRow );
				break;
			case TableModelEvent.DELETE:
				// Entries removed from the table
				// Again, we don't care as we'll receive the link delete event in the rack link listener.
				break;
			}
		}
		linkPainter.createCompositeRackLinksImageAndRedisplay();
	}

}
