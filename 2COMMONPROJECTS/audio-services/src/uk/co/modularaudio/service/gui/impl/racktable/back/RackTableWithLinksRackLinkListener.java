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

import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLinkEvent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLinkListener;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLinkEvent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLinkListener;
import uk.co.modularaudio.util.table.TableModelEvent;

public class RackTableWithLinksRackLinkListener implements RackLinkListener, RackIOLinkListener
{
//	private static Log log = LogFactory.getLog( NewRackTableWithLinksRackLinkListener.class.getName() );
	
	private RackTableWithLinks table = null;

	public RackTableWithLinksRackLinkListener( RackTableWithLinks table )
	{
		this.table = table;
	}

	@Override
	public void linksChanged(RackLinkEvent event)
	{
//		log.debug("Event received: " + event.toString());
		
		RackDataModel model = (RackDataModel)event.getSource();
		
		int eventType = event.getType();
		int eventFirstRow = event.getFirstRow();
		int eventLastRow = event.getLastRow();
		
		switch( eventType )
		{
		case TableModelEvent.INSERT:
			// New entries added into the table
			int iCounter = eventFirstRow;
			do
			{
				RackLink rackLink = model.getLinkAt( iCounter );
				table.createRackLinkImageForNewLink( rackLink );
				iCounter++;
			}
			while( iCounter < eventLastRow );
			break;
		case TableModelEvent.DELETE:
			int dCounter = eventFirstRow;
			int dEndRow = eventLastRow;
			
			do
			{
				table.removeRackLinkImageAt( eventFirstRow );
				dCounter++;
			}
			while( dCounter <= dEndRow );
			break;
		default:
			// Trap unexpected case fall through
			throw new IndexOutOfBoundsException();
		}

		// Now tell the table to recompute the composite rack link image and redisplay
		// Not sure this is necessary
		table.createCompositeRackLinksImageAndRedisplay();
	}

	@Override
	public void ioLinksChanged( RackIOLinkEvent event )
	{
//		log.debug("Event received: " + event.toString());
		
		RackDataModel model = (RackDataModel)event.getSource();
		
		int eventType = event.getType();
		int eventFirstRow = event.getFirstRow();
		int eventLastRow = event.getLastRow();
		
		switch( eventType )
		{
		case TableModelEvent.INSERT:
			int iCounter = eventFirstRow;
			do
			{
				RackIOLink rackIOLink = model.getIOLinkAt( iCounter );
				table.createRackIOLinkImageForNewLink( rackIOLink );
				iCounter++;
			}
			while( iCounter < eventLastRow );
			break;
		case TableModelEvent.DELETE:
			int dCounter = eventFirstRow;
			int dEndRow = eventLastRow;
			do
			{
				table.removeRackIOLinkImageAt( eventFirstRow );
				dCounter++;
			}
			while( dCounter <= dEndRow );
			break;
		default:
			// Trap unexpected case fall through
			throw new IndexOutOfBoundsException();
		}

		// Now tell the table to recompute the composite rack link image and redisplay
		table.createCompositeRackLinksImageAndRedisplay();
		
	}
}
