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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public abstract class AbstractLinkImage
{
	private static Log log = LogFactory.getLog( RackLinkImage.class.getName() );

	private final String allocationSource;
	private final BufferedImageAllocationService bufferImageAllocationService;

	protected Rectangle rectangle = new Rectangle();
	protected BufferedImage bufferedImage;
	protected static final float WIRE_HIGHLIGHT_WIDTH = 3.0f;

	protected static final double WIRE_SHADOW_X_OFFSET = 2.0d;

	protected static final double WIRE_SHADOW_Y_OFFSET = 2.0d;

	protected static final double WIRE_HIGHLIGHT_X_OFFSET = -2.0d;

	protected static final double WIRE_HIGHLIGHT_Y_OFFSET = -2.0d;

	protected static final float WIRE_SHADOW_WIDTH = 3.0f;

	protected static final boolean DRAW_HIGHTLIGHT_AND_SHADOW = false;

	protected final static boolean DRAW_WIRE_BOUNDING_BOX = false;

	//	private static final float WIRE_STROKE_WIDTH = 8.0f;
	protected static final float WIRE_STROKE_WIDTH = 10.0f;

	protected static final int LINK_IMAGE_DIST_TO_CENTER = 10;

	protected static final int LINK_IMAGE_PADDING_FOR_WIRE_RADIUS = LINK_IMAGE_DIST_TO_CENTER * 2;

	protected static final int WIRE_DIP_PIXELS = 30;

	private TiledBufferedImage tiledBufferedImage;
	private final AllocationMatch allocationMatchToUse = new AllocationMatch();

	private static BasicStroke wireBodyStroke = new BasicStroke( WIRE_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
	private static CompositeStroke wireStroke = new CompositeStroke( wireBodyStroke, new BasicStroke( 2.0f ) );
	private static BasicStroke basicStrokeOfOne = new BasicStroke( 1.0f );

	public AbstractLinkImage( final String allocationSource, final BufferedImageAllocationService bufferImageAllocationService, final Point sourcePoint, final Point sinkPoint )
	{
		this.allocationSource = allocationSource;
		this.bufferImageAllocationService = bufferImageAllocationService;
		if( sourcePoint != null && sinkPoint != null )
		{
			drawLinkWireIntoImage( sourcePoint, sinkPoint );
		}
	}

	private void drawLinkWireIntoImage( final Point sourcePoint, final Point sinkPoint )
	{
		//		log.debug("Drawing link from " + sourcePoint + " to " + sinkPoint);

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
		final Rectangle cubicCurveBounds = cubicCurve.getBounds();

		final int imageWidthToUse = cubicCurveBounds.width + LINK_IMAGE_PADDING_FOR_WIRE_RADIUS;
//		int imageHeightToUse = cubicCurveBounds.height + WIRE_DIP_PIXELS;
		int imageHeightToUse = cubicCurveBounds.height;
		// If the wire is close to vertical (little Y difference) we make the image a little bigger to account for the wire "dip"
		if( Math.abs( sinkPoint.y - sourcePoint.y) <= WIRE_DIP_PIXELS )
		{
			imageHeightToUse += (WIRE_DIP_PIXELS / 2);
		}

//		bufferedImage = new BufferedImage( imageWidthToUse, imageHeightToUse, BufferedImage.TYPE_INT_ARGB );
		try
		{
			tiledBufferedImage = bufferImageAllocationService.allocateBufferedImage( allocationSource,
					allocationMatchToUse,
					AllocationLifetime.SHORT,
					AllocationBufferType.TYPE_INT_ARGB,
					imageWidthToUse,
					imageHeightToUse );
		}
		catch ( final Exception e)
		{
			final String msg = "Exception caught allocating buffered image: " + e.toString();
			log.error( msg, e );
		}
		bufferedImage = tiledBufferedImage.getUnderlyingBufferedImage();
		final Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		f1 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.x;
		f2 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.y;
		f3 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.x;
		f4 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.y;
		f5 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.x;
		f6 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.y;
		f7 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.x;
		f8 += LINK_IMAGE_DIST_TO_CENTER - cubicCurveBounds.y;

		final CubicCurve2D offSetCubicCurve = new CubicCurve2D.Float( f1, f2, f3, f4, f5, f6, f7, f8 );

		// Draw the highlight and shadow
		if( DRAW_HIGHTLIGHT_AND_SHADOW )
		{
			final Graphics2D sG2d = (Graphics2D)g2d.create();
			sG2d.translate(WIRE_SHADOW_X_OFFSET, WIRE_SHADOW_Y_OFFSET);
			sG2d.setColor( Color.BLUE.darker());
			sG2d.setStroke( new BasicStroke( WIRE_SHADOW_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
			sG2d.draw( offSetCubicCurve );

			final Graphics2D hG2d = (Graphics2D)g2d.create();
			hG2d.translate(WIRE_HIGHLIGHT_X_OFFSET, WIRE_HIGHLIGHT_Y_OFFSET);
			hG2d.setColor( Color.WHITE );
			hG2d.setStroke( new BasicStroke( WIRE_HIGHLIGHT_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
			hG2d.draw( offSetCubicCurve );
		}

		g2d.setColor( Color.BLACK );
		g2d.setStroke( wireStroke );
		g2d.draw( offSetCubicCurve );

		g2d.setColor( Color.BLUE );
		g2d.setStroke( wireBodyStroke );
		g2d.draw( offSetCubicCurve );

		// For debugging, draw a green line around the outside of this image.
		if( DRAW_WIRE_BOUNDING_BOX )
		{
			g2d.setStroke( basicStrokeOfOne );
			g2d.setColor( Color.GREEN );
			g2d.drawRect( 0, 0, imageWidthToUse - 1, imageHeightToUse - 1);
		}

		rectangle.x = cubicCurveBounds.x - LINK_IMAGE_DIST_TO_CENTER;
		rectangle.y = cubicCurveBounds.y - LINK_IMAGE_DIST_TO_CENTER;
		rectangle.width = imageWidthToUse;
		rectangle.height = imageHeightToUse;
	}

	public void redrawWireWithNewPoints( final Point sourcePoint, final Point sinkPoint )
	{
		try
		{
			if( tiledBufferedImage != null )
			{
				bufferImageAllocationService.freeBufferedImage( tiledBufferedImage );
			}
			drawLinkWireIntoImage( sourcePoint, sinkPoint );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught redrawing wire with new points: " + e.toString();
			log.error( msg, e );
		}
	}

	public Rectangle getRectangle()
	{
		return rectangle;
	}

	public BufferedImage getBufferedImage()
	{
		return bufferedImage;
	}

	public void destroy()
	{
		try
		{
			if( tiledBufferedImage != null )
			{
				bufferImageAllocationService.freeBufferedImage( tiledBufferedImage );
				tiledBufferedImage = null;
			}
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught freeing buffered image: " + e.toString();
			log.error( msg, e );
		}
	}
}
