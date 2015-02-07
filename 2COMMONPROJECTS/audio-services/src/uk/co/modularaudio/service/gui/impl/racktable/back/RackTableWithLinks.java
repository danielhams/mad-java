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

public class RackTableWithLinks extends RackTable
{
	private static final long serialVersionUID = -2571436979816263062L;

//	private static Log log = LogFactory.getLog( NewRackTableWithLinks.class.getName() );

	private RackDataModel dataModel;

	private final RackTableWithLinksListener rackWithLinksListener;

	private final RackLinkPainter linkPainter;

	public RackTableWithLinks( final BufferedImageAllocationService bufferedImageAllocationService,
			final RackDataModel dataModel,
			final RackTableEmptyCellPainter emptyCellPainter,
			final RackTableGuiFactory factory,
			final RackTableDndPolicy dndPolicy,
			final DndRackDragDecorations rackDecorations,
			final DndWireDragDecorations wireDecorations,
			final Dimension gridSize,
			final boolean showGrid, final Color gridColour)
	{
		super( dataModel, emptyCellPainter, factory, dndPolicy, new RackTableWithLinkDecorations(rackDecorations, wireDecorations), gridSize, showGrid, gridColour );

		this.dataModel = dataModel;

		this.linkPainter = new RackLinkPainter( bufferedImageAllocationService, dataModel, this );

		// Add a listener to track changes to the model and links
		rackWithLinksListener = new RackTableWithLinksListener( linkPainter );

		addListenersToModel();

		fullLinksRefreshFromModel();
	}

	@Override
	public void paint(final Graphics g)
	{
		layeredTablePaint( g );
	}

	public final void fullLinksRefreshFromModel()
	{
		// Make sure we are starting from a clean slate.
		linkPainter.clear();

		// Produce the individual rackLinkImages from each rack link
		linkPainter.fullRefreshIndividualLinkImages();

		// Now create the composite image with all of them on
		linkPainter.createCompositeRackLinksImageAndRedisplay();
	}

	@Override
	public void setRackDataModel(final RackDataModel rackDataModel)
	{
		removeListenersFromModel();
		linkPainter.clear();

		super.setRackDataModel(rackDataModel);
		this.dataModel = rackDataModel;
		linkPainter.setDataModel( dataModel );

		addListenersToModel();
		fullLinksRefreshFromModel();
	}

	private final void addListenersToModel()
	{
		dataModel.addListener( rackWithLinksListener );
		dataModel.addRackLinksListener( rackWithLinksListener );
		dataModel.addRackIOLinksListener( rackWithLinksListener );
	}

	private final void removeListenersFromModel()
	{
		dataModel.removeListener( rackWithLinksListener );
		dataModel.removeRackLinksListener( rackWithLinksListener );
		dataModel.removeRackIOLinksListener( rackWithLinksListener );
	}

	@Override
	public void destroy()
	{
		// We are a bit special in that:
		// * we must release the composite rack links image
		// * the link painter needs to release its buffered images
		linkPainter.destroy();

		// Now do any cleanup that our parent wants to do.
		super.destroy();

		dataModel = null;
	}
}
