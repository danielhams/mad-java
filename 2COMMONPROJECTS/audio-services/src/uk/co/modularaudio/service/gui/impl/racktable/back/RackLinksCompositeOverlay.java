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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLinkEvent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLinkListener;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLinkEvent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLinkListener;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TableModelListener;

public class RackLinksCompositeOverlay extends JComponent
{
	private static final long serialVersionUID = 3441350163975600834L;

	private static Log log = LogFactory.getLog( RackLinksCompositeOverlay.class.getName() );

	private static final int WIRE_DIP_PIXELS = 30;

	private static final float WIRE_STROKE_WIDTH = 10.0f;

	private static final BasicStroke WIRE_BODY_STROKE = new BasicStroke( WIRE_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
	private static final CompositeStroke WIRE_STROKE = new CompositeStroke( WIRE_BODY_STROKE, new BasicStroke( 2.0f ) );
//	private static final BasicStroke BASIC_STROKE_OF_ONE = new BasicStroke( 1.0f );

	private final static float GENERAL_WIRE_TRANSPARENCY = 0.7f;
	private final static float ONE_WIRE_TRANSPARENCY = 0.7f;

	private static final AlphaComposite GENERAL_WIRE_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			GENERAL_WIRE_TRANSPARENCY);
	private static final AlphaComposite ONE_WIRE_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			ONE_WIRE_TRANSPARENCY);

	// Instance stuff

	private final BufferedImageAllocationService bias;

	private final AllocationMatch allocationMatch = new AllocationMatch();

	private final RackTableWithLinks rackTable;

	private RackDataModel dataModel;

	private final OverlayModelListener overlayModelListener;

	private TiledBufferedImage tbi;
	private BufferedImage bi;
	private Graphics2D g2d;
	private int biWidth;
	private int biHeight;

	private class OverlayModelListener
		implements TableModelListener<RackComponent, RackComponentProperties>, RackLinkListener, RackIOLinkListener
	{
		private final RackLinksCompositeOverlay rlco;
		public OverlayModelListener( final RackLinksCompositeOverlay rlco )
		{
			this.rlco = rlco;
		}

		@Override
		public void tableChanged( final TableModelEvent<RackComponent, RackComponentProperties> event )
		{
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
					rlco.redrawAllLinks();
					repaint();
					break;
				case TableModelEvent.DELETE:
					// Entries removed from the table
					// Again, we don't care as we'll receive the link delete event in the rack link listener.
					break;
				}
			}
		}

		@Override
		public void ioLinksChanged( final RackIOLinkEvent outEvent )
		{
			rlco.redrawAllLinks();
			repaint();
		}

		@Override
		public void linksChanged( final RackLinkEvent outEvent )
		{
			rlco.redrawAllLinks();
			repaint();
		}
	};

	public RackLinksCompositeOverlay( final BufferedImageAllocationService bias,
			final RackDataModel dataModel,
			final RackTableWithLinks rackTable ) throws DatastoreException
	{
		this.bias = bias;
		this.rackTable = rackTable;
		this.dataModel = dataModel;
		setOpaque( false );

		// Work out how big the buffered image needs to be and allocate one
		final Span span = dataModel.getSpan();
		final Dimension rackGridSize = rackTable.getGridSize();
		biWidth = span.x * rackGridSize.width;
		biHeight = span.y * rackGridSize.height;

		tbi = bias.allocateBufferedImage( this.getClass().getSimpleName(),
				allocationMatch,
				AllocationLifetime.SHORT,
				AllocationBufferType.TYPE_INT_ARGB,
				biWidth,
				biHeight );
		bi = tbi.getUnderlyingBufferedImage();
		g2d = bi.createGraphics();
		g2d.setComposite(GENERAL_WIRE_COMPOSITE);

		overlayModelListener = new OverlayModelListener( this );

		startListening();

		setBounds( 0, 0, biWidth, biHeight );
	}

	private final void startListening()
	{
		dataModel.addListener( overlayModelListener );
		dataModel.addRackLinksListener( overlayModelListener );
		dataModel.addRackIOLinksListener( overlayModelListener );
	}

	private final void stopListening()
	{
		dataModel.removeRackIOLinksListener( overlayModelListener );
		dataModel.removeRackLinksListener( overlayModelListener );
		dataModel.removeListener( overlayModelListener );
	}

	public void setDataModel( final RackDataModel dataModel ) throws DatastoreException
	{
		stopListening();

		this.dataModel = dataModel;

		final Span span = dataModel.getSpan();
		final Dimension rackGridSize = rackTable.getGridSize();
		final int newBiWidth = span.x * rackGridSize.width;
		final int newBiHeight = span.y * rackGridSize.height;

		if( biWidth != newBiWidth || biHeight != newBiHeight )
		{
//			log.debug("Reallocating changed buffered image due to dimensions diff");
//			log.debug("(" + biWidth + "," + biHeight + ")->(" + newBiWidth + "," + newBiHeight + ")");
			bias.freeBufferedImage( tbi );

			tbi = bias.allocateBufferedImage( this.getClass().getSimpleName(),
					allocationMatch,
					AllocationLifetime.SHORT,
					AllocationBufferType.TYPE_INT_ARGB,
					newBiWidth,
					newBiHeight );
			bi = tbi.getUnderlyingBufferedImage();
			g2d = bi.createGraphics();

			setBounds( 0, 0, newBiWidth, newBiHeight );
			biWidth = newBiWidth;
			biHeight = newBiHeight;
		}

		redrawAllLinks();

		startListening();
	}

	@Override
	public void paint( final Graphics g )
	{
//		log.debug("Paint was called with rect " + g.getClipBounds());
		final Graphics2D pg2d = (Graphics2D)g;
		pg2d.setComposite(GENERAL_WIRE_COMPOSITE);
		pg2d.drawImage( bi, 0, 0, null );
	}

	public final void destroy()
	{
		stopListening();

		try
		{
			g2d = null;
			bi = null;
			bias.freeBufferedImage( tbi );
			tbi = null;
		}
		catch (final DatastoreException e)
		{
			final String msg = "DatastoreException caught returning composite links master buffer: " + e.toString();
			log.error( msg, e );
		}
	}

	void redrawAllLinks()
	{
		g2d.setComposite( AlphaComposite.Clear );
		g2d.fillRect( 0, 0, biWidth, biHeight );

		g2d.setComposite( ONE_WIRE_COMPOSITE );

		final int numLinks = this.dataModel.getNumLinks();
		for( int i = 0 ; i < numLinks ; ++i )
		{
			final RackLink rl = this.dataModel.getLinkAt( i );
			drawOneLink( rl );
		}

		final int numIOLinks = this.dataModel.getNumIOLinks();
		for( int i = 0 ; i < numIOLinks ; ++i )
		{
			final RackIOLink ril = this.dataModel.getIOLinkAt( i );
			drawOneIOLink( ril );
		}
	}

	private void drawOneLink( final RackLink rl )
	{
		// For now lets assume some magic about how the end points of the cable are computed.
		final RackComponent sourceRackComponent = rl.getProducerRackComponent();
		final AbstractGuiAudioComponent sourceGuiComponent = rackTable.getGuiComponentFromTableModel( sourceRackComponent );
		final MadChannelInstance sourceRackComponentChannel = rl.getProducerChannelInstance();
		final GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );
		final Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				rackTable,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent,
				sourceGuiPlug );

		final RackComponent sinkRackComponent = rl.getConsumerRackComponent();
		final AbstractGuiAudioComponent sinkGuiComponent = rackTable.getGuiComponentFromTableModel( sinkRackComponent );
		final MadChannelInstance sinkRackComponentChannel = rl.getConsumerChannelInstance();
		final GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		final Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				rackTable,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );
		drawLine( sourcePoint, sinkPoint );
	}

	private void drawOneIOLink( final RackIOLink ril )
	{
		// Bit of a hack....
		final RackComponent masterIOComponent = dataModel.getContentsAtPosition( 0,  0 );
		final MadChannelInstance ioChannelInstance = ril.getRackChannelInstance();
		final RackComponent rackComponent = ril.getRackComponent();
		final MadChannelInstance rackComponentChannelInstance = ril.getRackComponentChannelInstance();
		final MadChannelDirection direction = rackComponentChannelInstance.definition.direction;

		// For now lets assume some magic about how the end points of the cable are computed.
		final RackComponent sourceRackComponent = (direction == MadChannelDirection.PRODUCER ? rackComponent : masterIOComponent );
		final AbstractGuiAudioComponent sourceGuiComponent = rackTable.getGuiComponentFromTableModel( sourceRackComponent );
		final MadChannelInstance sourceRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? rackComponentChannelInstance : ioChannelInstance );
		final GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );

		final Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				rackTable,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent,
				sourceGuiPlug );

		final RackComponent sinkRackComponent = (direction == MadChannelDirection.PRODUCER ? masterIOComponent : rackComponent );
		final AbstractGuiAudioComponent sinkGuiComponent = rackTable.getGuiComponentFromTableModel( sinkRackComponent );
		final MadChannelInstance sinkRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? ioChannelInstance : rackComponentChannelInstance );
		final GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		final Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				rackTable,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );

		drawLine( sourcePoint, sinkPoint );
	}

	private final void drawLine( final Point sourcePoint, final Point sinkPoint )
	{
		final int fromX = sourcePoint.x;
		final int fromY = sourcePoint.y;
		final int toX = sinkPoint.x;
		final int toY = sinkPoint.y;

		float f1, f2, f3, f4, f5, f6, f7, f8 = 0.0f;
		f1 = fromX;
		f2 = fromY;
		f3 = fromX;
		f4 = fromY + WIRE_DIP_PIXELS;
		f5 = toX;
		f6 = toY + WIRE_DIP_PIXELS;
		f7 = toX;
		f8 = toY;
		final CubicCurve2D cubicCurve = new CubicCurve2D.Float( f1, f2, f3, f4, f5, f6, f7, f8 );

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor( Color.BLACK );
		g2d.setStroke( WIRE_STROKE );
		g2d.draw( cubicCurve );

		g2d.setColor( Color.BLUE );
//		g2d.setColor( Color.YELLOW );
		g2d.setStroke( WIRE_BODY_STROKE );
		g2d.draw( cubicCurve );

//		g2d.drawLine( sourcePoint.x, sourcePoint.y, sinkPoint.x, sinkPoint.y );
	}

}
