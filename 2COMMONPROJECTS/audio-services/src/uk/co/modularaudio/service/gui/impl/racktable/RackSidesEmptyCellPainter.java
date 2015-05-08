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

package uk.co.modularaudio.service.gui.impl.racktable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;



public class RackSidesEmptyCellPainter implements RackTableEmptyCellPainter
{
	public static final boolean USE_SINGLE_BLIT = false;
	public static final boolean PAINT_IT_SCALED = false;

	public static final Color CONTENTS_COLOR = new Color( 0.2f, 0.2f, 0.2f );
//	public static final Color CONTENTS_COLOR = new Color( 0.6f, 0.6f, 0.6f );
	public static final Color HIGHLIGHT_COLOR;
	public static final Color LOWLIGHT_COLOR;

	static
	{
		HIGHLIGHT_COLOR = CONTENTS_COLOR.brighter();
		LOWLIGHT_COLOR = CONTENTS_COLOR.darker();
	}

	private static final float POS_FRAME_INSET_MULTIPLIER = 5.0f / 300.0f;
	private static final float POS_FRAME_BAR_MULTIPLIER = 10.0f / 300.0f;
	private static final float POS_CONTENTS_MULTIPLIER = 1.0f - ( 2 * (( 2 * POS_FRAME_INSET_MULTIPLIER ) + POS_FRAME_BAR_MULTIPLIER) );
	private static final float POS_MOUNTING_HOLE_RADIUS_MULTIPLIER = 0.7f;

	private static final int POS_FRAME_INSET_WIDTH = 5;
	private static final int POS_FRAME_BAR_WIDTH = 10;

	private Dimension cachedImageDimension;
	private BufferedImage cachedImage;

	private BufferedImage oneGridBufferedImage;
	private BufferedImage singleBlutBufferedImage;

	@Override
	public void paintEmptyCell(final Graphics g, final int x, final int y, final int width, final int height)
	{
		if( cachedImageDimension == null )
		{
			cachedImageDimension = new Dimension( width, height );
		}
		generateOneGridBufferedImage( cachedImageDimension );
		g.drawImage( oneGridBufferedImage, x, y, null );
	}

	private void internalPaintOne( final Graphics parentG, final int x, final int y, final int width, final int height )
	{
		final Graphics2D emptyG2d = (Graphics2D)parentG.create();
//		emptyG2d.setClip(x, y, width, height);
		emptyG2d.translate( x, y );
		// Only need to paint 0 -> width and 0 -> height
		final Dimension testDimension = new Dimension(width, height);
		if( cachedImageDimension == null || cachedImageDimension != testDimension )
		{
			cachedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB);
			final Graphics2D g = cachedImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			/**/
			if( PAINT_IT_SCALED )
			{
				reallyPaintItScaled(g, width, height);
			}
			else
			{
				reallyPaintItAbsolute(g, width, height);
			}
			/**/
		}

		emptyG2d.drawImage( cachedImage, 0, 0, width, height, null );

	}

	public void reallyPaintItScaled( final Graphics2D g, final int width, final int height )
	{
		// Set up the background
		g.setColor( CONTENTS_COLOR );
		g.fillRect(0, 0, width, height);
		// Draw the rack bar highlights like:
		// <--------------------------------------------------------------------------------------------------> 300
		// <-> frameinset 5 (1.67 %)
		//      <---> actual bar 20 (6.67 %)
		//              <->frameoutset 5 (1.67 %)
		//                   <------------------------------------------------------------------------> content 240 (80%)
		final float frameInsetWidth = width * POS_FRAME_INSET_MULTIPLIER;
		final float frameBarWidth = width * POS_FRAME_BAR_MULTIPLIER;
		final float contentWidth = width * POS_CONTENTS_MULTIPLIER;

		float startXPos = 0;
		float endXPos  = 0 ;

		// Draw left frame (inset, bar, inset)
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameLeft( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameBarWidth;
		drawScaledBar( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameRight( g, startXPos, 0, endXPos - 1, height );

		// Contents go here, just paint a darker gray for now
		startXPos = endXPos;
		endXPos += contentWidth;
		drawScaledContents( g, startXPos, 0, endXPos - 1, height );

		// Finally the end bar too
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameLeft( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameBarWidth;
		drawScaledBar( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameRight( g, startXPos, 0, endXPos - 1, height );

	}

	public void reallyPaintItAbsolute( final Graphics2D g, final int width, final int height )
	{
		// Set up the background
		g.setColor( CONTENTS_COLOR );
		g.fillRect(0, 0, width, height);
		// Draw the rack bar highlights like:
		// <--------------------------------------------------------------------------------------------------> 300
		// <-> frameinset 5 (1.67 %)
		//      <---> actual bar 20 (6.67 %)
		//              <->frameoutset 5 (1.67 %)
		//                   <------------------------------------------------------------------------> content 240 (80%)
		final float frameInsetWidth = POS_FRAME_INSET_WIDTH;
		final float frameBarWidth = POS_FRAME_BAR_WIDTH;
		final float contentWidth = width - ( 2 * frameBarWidth  + ( 4 * frameInsetWidth) );

		float startXPos = 0;
		float endXPos  = 0 ;

		// Draw left frame (inset, bar, inset)
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameLeft( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameBarWidth;
		drawScaledBar( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameRight( g, startXPos, 0, endXPos - 1, height );

		// Contents go here, just paint a darker gray for now
		startXPos = endXPos;
		endXPos += contentWidth +1;
		drawScaledContents( g, startXPos, 0, endXPos - 1, height );

		// Finally the end bar too
		startXPos = endXPos;
		endXPos += frameInsetWidth -1;
		drawScaledFrameLeft( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameBarWidth;
		drawScaledBar( g, startXPos, 0, endXPos -1, height );
		startXPos = endXPos;
		endXPos += frameInsetWidth;
		drawScaledFrameRight( g, startXPos, 0, endXPos - 1, height );

	}

	private void drawScaledContents(final Graphics2D g, final float startXPos, final int i, final float endXPos, final int height)
	{
		final int leftPixel = (int)startXPos;
		final int rightPixel = (int)endXPos;
		g.setColor( CONTENTS_COLOR );
		g.fill3DRect( leftPixel, 0, rightPixel - leftPixel, height, false );
	}

	private void drawScaledFrameLeft(final Graphics2D g, final float startXPos, final int i, final float endXPos, final int height)
	{
		final int rightPixel = (int)endXPos;
		g.setColor( HIGHLIGHT_COLOR );
		g.drawLine( rightPixel, 0, rightPixel, height );
	}

	private void drawScaledBar(final Graphics2D g, final float startXPos, final int i, final float endXPos, final int height)
	{
		final int leftPixel = (int)startXPos;
		final int rightPixel = ((int)endXPos);
		// Draw lowlight from the frame left
		g.setColor( LOWLIGHT_COLOR );
		g.drawLine( leftPixel, 0, leftPixel, height );

		// Now draw the two mounting holes
		final int diameter = (int)((rightPixel - leftPixel) * POS_MOUNTING_HOLE_RADIUS_MULTIPLIER);
		final int holeStartXPixel = leftPixel + (diameter / 2) - 1;

		// One for the top
		int holeStartYPixel = 9;
		g.fillOval( holeStartXPixel, holeStartYPixel, diameter, diameter );

		// One for the bottom
		holeStartYPixel = height - (diameter + 10);
		g.fillOval( holeStartXPixel, holeStartYPixel, diameter, diameter );

		// Finally the right highlight begins
		g.setColor( HIGHLIGHT_COLOR );
		g.drawLine( rightPixel, 0, rightPixel, height );
	}

	private void drawScaledFrameRight(final Graphics2D g, final float startXPos, final int i, final float endXPos, final int height)
	{
		final int leftPixel = (int)startXPos;
		g.setColor( LOWLIGHT_COLOR );
		g.drawLine( leftPixel, 0, leftPixel, height );
	}

	@Override
	public BufferedImage getSingleBlitBufferedImage(final Dimension gridSize, final int numCols, final int numRows)
	{
		generateOneGridBufferedImage(gridSize);

		generateSingleBlitBufferedImage(gridSize, numCols, numRows);
		return singleBlutBufferedImage;
	}

	private void generateSingleBlitBufferedImage(final Dimension gridSize,
			final int numCols, final int numRows)
	{
		if( singleBlutBufferedImage == null )
		{
			singleBlutBufferedImage = new BufferedImage( gridSize.width * numCols, gridSize.height * numRows, BufferedImage.TYPE_INT_RGB );
			final Graphics g = singleBlutBufferedImage.createGraphics();
			for( int i = 0 ; i < numCols ; i ++ )
			{
				for( int j = 0 ; j < numRows ; j++ )
				{
					g.drawImage( oneGridBufferedImage, i*gridSize.width, j*gridSize.height, null );
//					paintEmptyCell( g, i * gridSize.width, j * gridSize.height, gridSize.width, gridSize.height);
				}
			}
		}
	}

	private void generateOneGridBufferedImage(final Dimension gridSize)
	{
		if( oneGridBufferedImage == null )
		{
			oneGridBufferedImage = new BufferedImage( gridSize.width, gridSize.height, BufferedImage.TYPE_INT_RGB );
			final Graphics g = oneGridBufferedImage.createGraphics();
			internalPaintOne( g, 0, 0, gridSize.width, gridSize.height );
		}
	}

	@Override
	public boolean needSingleBlit()
	{
		return USE_SINGLE_BLIT;
	}

}
