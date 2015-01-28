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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.impl.racktable.RackTable;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableDndPolicy;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableEmptyCellPainter;
import uk.co.modularaudio.service.gui.impl.racktable.RackTableGuiFactory;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.rackdrag.DndRackDragDecorations;
import uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.wiredrag.DndWireDragDecorations;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;

public class RackTableWithLinks extends RackTable
{
	private static final long serialVersionUID = -2571436979816263062L;

//	private static Log log = LogFactory.getLog( NewRackTableWithLinks.class.getName() );

	private RackDataModel dataModel;

	private final RackTableWithLinksRackModelListener rackModelListener;
	private final RackTableWithLinksRackLinkListener rackLinkListener;

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
		super( dataModel, emptyCellPainter, factory, dndPolicy, new LinksDecorations(rackDecorations, wireDecorations), gridSize, showGrid, gridColour );

		this.dataModel = dataModel;

		this.linkPainter = new RackLinkPainter( bufferedImageAllocationService, dataModel, this );

		// Register a custom rack model listener so that when a component moves we recalculate the link images
		rackModelListener = new RackTableWithLinksRackModelListener( this );
		dataModel.addListener( rackModelListener );

		// Also add a listener to track the rack link changes
		rackLinkListener = new RackTableWithLinksRackLinkListener( this );
		dataModel.addRackLinksListener( rackLinkListener );
		dataModel.addRackIOLinksListener( rackLinkListener );

		fullLinksRefreshFromModel();
	}

	@Override
	public void paint(final Graphics g)
	{
		layeredTablePaint( g );
//		swingTablePaint( g );
		if( compositeRackLinksImage != null )
		{
			final Graphics2D g2d = (Graphics2D) g.create();
			final Composite alphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.65f );
			g2d.setComposite( alphaComposite );
			g2d.drawImage( compositeRackLinksImage, compositeImageRectangle.x, compositeImageRectangle.y, null );
		}
//		swingDndTablePaint( g );
	}

	private Rectangle compositeImageRectangle = null;
	private BufferedImage compositeRackLinksImage = null;

	public final void fullLinksRefreshFromModel()
	{
		// Make sure we are starting from a clean slate.
		compositeImageRectangle = null;
		compositeRackLinksImage = null;

		linkPainter.clear();

		// Produce the individual rackLinkImages from each rack link
		linkPainter.createIndividualRackLinkImages();
		linkPainter.createIndividualRackIOLinkImages();

		// Now create the composite image with all of them on
		linkPainter.createCompositeRackLinksImageAndRedisplay();
	}

	public void createRackLinkImageForNewLink( final RackLink rackLink )
	{
		linkPainter.drawOneRackLinkImageAndAdd( rackLink );
	}

	public void updateRackLinkImageForLink( final RackLink rl )
	{
		linkPainter.updateRackLinkImageForLink( rl );
	}

	public void removeRackLinkImageAt( final int modelIndex )
	{
		linkPainter.removeRackLinkImageAt( modelIndex );
	}

	public List<RackLink> getLinksForComponent(final RackComponent componentToFindLinksFor)
	{
		return linkPainter.getLinksForComponent( componentToFindLinksFor );
	}

	public List<RackIOLink> getIOLinksForComponent( final RackComponent componentToFindLinksFor )
	{
		return linkPainter.getIOLinksForComponent( componentToFindLinksFor );
	}

	public void updateLink( final RackLink oneLink )
	{
		linkPainter.updateLink(oneLink);
	}

	public void updateIOLink( final RackIOLink oneLink )
	{
		linkPainter.updateIOLink(oneLink);
	}

	@Override
	public void setRackDataModel(final RackDataModel rackDataModel)
	{
		removeListenersFromModel();
		linkPainter.clear();

		super.setRackDataModel(rackDataModel);
		this.dataModel = rackDataModel;
		linkPainter.setDataModel( dataModel );

		dataModel.addListener( rackModelListener );
		dataModel.addRackLinksListener( rackLinkListener );
		dataModel.addRackIOLinksListener( rackLinkListener );
		fullLinksRefreshFromModel();
	}

	private void removeListenersFromModel()
	{
		dataModel.removeListener( rackModelListener );
		dataModel.removeRackLinksListener( rackLinkListener );
		dataModel.removeRackIOLinksListener( rackLinkListener );
	}

	public void createCompositeRackLinksImageAndRedisplay()
	{
		linkPainter.createCompositeRackLinksImageAndRedisplay();
	}

	public void createRackIOLinkImageForNewLink( final RackIOLink rackIOLink )
	{
		linkPainter.drawOneRackIOLinkImageAndAdd( rackIOLink );
	}

	public void removeRackIOLinkImageAt( final int modelIndex )
	{
		linkPainter.removeRackIOLinkImageAt( modelIndex );
	}

	@Override
	public void destroy()
	{
		// We are a bit special in that:
		// * we must release the composite rack links image
		// * the link painter needs to release it's buffered images
		linkPainter.destroy();

		// Now do any cleanup that our parent wants to do.
		super.destroy();

		dataModel = null;
	}
}
