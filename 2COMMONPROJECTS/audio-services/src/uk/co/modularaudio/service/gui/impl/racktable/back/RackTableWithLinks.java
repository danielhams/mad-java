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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableEmptyCellPainter;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableGuiFactory;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag.DndWireDragDecorations;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.exception.DatastoreException;

public class RackTableWithLinks extends RackTable
{
	private static final long serialVersionUID = -2571436979816263062L;

//	private static Log log = LogFactory.getLog( RackTableWithLinks.class.getName() );

	private final RackLinksCompositeOverlay rackLinksCompositeOverlay;

	public RackTableWithLinks( final BufferedImageAllocationService bufferedImageAllocationService,
			final RackDataModel dataModel,
			final RackTableEmptyCellPainter emptyCellPainter,
			final RackTableGuiFactory factory,
			final RackTableDndPolicy dndPolicy,
			final DndRackDragDecorations rackDecorations,
			final DndWireDragDecorations wireDecorations,
			final Dimension gridSize,
			final boolean showGrid, final Color gridColour) throws DatastoreException
	{
		super( dataModel, emptyCellPainter, factory, dndPolicy, new RackTableWithLinkDecorations(rackDecorations, wireDecorations), gridSize, showGrid, gridColour );

		rackLinksCompositeOverlay = new RackLinksCompositeOverlay( bufferedImageAllocationService,
				dataModel,
				this );

		this.setLayer( rackLinksCompositeOverlay, RackTableWithLinks.LPT_STATICWIRE_LAYER );
		this.add( rackLinksCompositeOverlay );

	}

	@Override
	public void paint(final Graphics g)
	{
		layeredTablePaint( g );
//		log.debug("RackTableWithLink paint called");
	}

	@Override
	public void setRackDataModel(final RackDataModel rackDataModel) throws DatastoreException
	{
		super.setRackDataModel( rackDataModel );

		rackLinksCompositeOverlay.setDataModel( rackDataModel );
	}

	@Override
	public void destroy()
	{
		// We are a bit special in that:
		// * we must release the composite rack links image
		// * the link painter needs to release its buffered images

		rackLinksCompositeOverlay.destroy();

		// Now do any cleanup that our parent wants to do.
		super.destroy();
	}
}
