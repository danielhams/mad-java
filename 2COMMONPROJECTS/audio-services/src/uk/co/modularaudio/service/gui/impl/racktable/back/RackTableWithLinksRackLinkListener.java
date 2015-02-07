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

	private RackLinkPainter linkPainter;

	public RackTableWithLinksRackLinkListener( final RackLinkPainter linkPainter )
	{
		this.linkPainter = linkPainter;
	}

	@Override
	public void linksChanged(final RackLinkEvent event)
	{
//		log.debug("Event received: " + event.toString());

		final RackDataModel model = (RackDataModel)event.getSource();

		final int eventType = event.getType();
		final int eventFirstRow = event.getFirstRow();
		final int eventLastRow = event.getLastRow();

		switch( eventType )
		{
		case TableModelEvent.INSERT:
			// New entries added into the table
			int iCounter = eventFirstRow;
			do
			{
				final RackLink rackLink = model.getLinkAt( iCounter );
				linkPainter.drawOneRackLinkImageAndAdd( rackLink );
				iCounter++;
			}
			while( iCounter < eventLastRow );
			break;
		case TableModelEvent.DELETE:
			int dCounter = eventFirstRow;
			final int dEndRow = eventLastRow;

			do
			{
				linkPainter.removeRackLinkImageAt( eventFirstRow );
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
		linkPainter.createCompositeRackLinksImageAndRedisplay();
	}

	@Override
	public void ioLinksChanged( final RackIOLinkEvent event )
	{
//		log.debug("Event received: " + event.toString());

		final RackDataModel model = (RackDataModel)event.getSource();

		final int eventType = event.getType();
		final int eventFirstRow = event.getFirstRow();
		final int eventLastRow = event.getLastRow();

		switch( eventType )
		{
		case TableModelEvent.INSERT:
			int iCounter = eventFirstRow;
			do
			{
				final RackIOLink rackIOLink = model.getIOLinkAt( iCounter );
				linkPainter.drawOneRackIOLinkImageAndAdd( rackIOLink );
				iCounter++;
			}
			while( iCounter < eventLastRow );
			break;
		case TableModelEvent.DELETE:
			int dCounter = eventFirstRow;
			final int dEndRow = eventLastRow;
			do
			{
				linkPainter.removeRackIOLinkImageAt( eventFirstRow );
				dCounter++;
			}
			while( dCounter <= dEndRow );
			break;
		default:
			// Trap unexpected case fall through
			throw new IndexOutOfBoundsException();
		}

		// Now tell the table to recompute the composite rack link image and redisplay
		linkPainter.createCompositeRackLinksImageAndRedisplay();

	}
}
