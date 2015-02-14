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

package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class MemReducedComponentFactory
{
//	private static Log log = LogFactory.getLog( MemReducedComponentFactory.class.getName() );

	private final ContainerImages frontDecorationImages;
	private final ContainerImages backDecorationImages;

//	private final SwingDebugger swingDebugger;

	public MemReducedComponentFactory( final BufferedImageAllocationService bias ) throws MadProcessingException, DatastoreException
	{
		frontDecorationImages = drawFrontDecorations();
		backDecorationImages = drawBackDecorations();

//		swingDebugger = new SwingDebugger( bias, this );
//		swingDebugger.setVisible( true );
	}

	private final ContainerImages drawFrontDecorations()
	{
//		log.trace( "Allocating and drawing decorations" );

		final BufferedImage frontDecorations = new BufferedImage( PaintedComponentDefines.FRONT_MIN_WIDTH+1, PaintedComponentDefines.FRONT_MIN_HEIGHT+1, BufferedImage.TYPE_INT_ARGB );
//		log.trace( "Allocated BI (" + PaintedComponentDefines.FRONT_MIN_WIDTH + ", " + PaintedComponentDefines.FRONT_MIN_HEIGHT + ")");

		final Graphics2D g2d = frontDecorations.createGraphics();

		g2d.setComposite( PaintedComponentDefines.OPAQUE_COMPOSITE );

//		g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
//		g2d.fillRect( 0, 0, FRONT_MIN_WIDTH, FRONT_MIN_HEIGHT );
//		log.trace( "Filled rect in BI (" + FRONT_MIN_WIDTH + ", " + FRONT_MIN_HEIGHT + ")");

		doActualFrontPaint( g2d );

		final BufferedImage aaFrontDecorations = new BufferedImage( PaintedComponentDefines.FRONT_MIN_WIDTH+1, PaintedComponentDefines.FRONT_MIN_HEIGHT+1, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D aaG2d = aaFrontDecorations.createGraphics();
		aaG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

		doActualFrontPaint( aaG2d );

		final BufferedImage frontLeftTopDecoration = aaFrontDecorations.getSubimage( 0, 0, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT );
		final BufferedImage frontLeftInsetDecoration = frontDecorations.getSubimage( 0, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 1, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, 1 );
		final BufferedImage frontLeftBottomDecoration = aaFrontDecorations.getSubimage( 0, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 2, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT );

		final BufferedImage frontTopInsetDecoration = frontDecorations.getSubimage( PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 1, 0, 1, PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET );

		final BufferedImage frontRightTopDecoration = aaFrontDecorations.getSubimage( PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 2, 0, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT );
		final BufferedImage frontRightInsetDecoration = frontDecorations.getSubimage( PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 2, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 1, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, 1 );
		final BufferedImage frontRightBottomDecoration = aaFrontDecorations.getSubimage( PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 2, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 2, PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH, PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT );

		final BufferedImage frontBottomInsetDecoration = frontDecorations.getSubimage( PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 2, (PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT * 2) + 2 - PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET, 1, PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET );

		return new ContainerImages( frontDecorations,
				aaFrontDecorations,
				frontLeftTopDecoration,
				frontLeftInsetDecoration,
				frontLeftBottomDecoration,
				frontTopInsetDecoration,
				frontRightTopDecoration,
				frontRightInsetDecoration,
				frontRightBottomDecoration,
				frontBottomInsetDecoration );

	}

	private void doActualFrontPaint( final Graphics2D g2d )
	{
		final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
				0, 0,
				PaintedComponentDefines.FRONT_MIN_WIDTH,
				PaintedComponentDefines.FRONT_MIN_HEIGHT,
				PaintedComponentDefines.OUTSIDE_BORDER_ARC,
				PaintedComponentDefines.OUTSIDE_BORDER_ARC );

		g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
		g2d.fill( roundedRectangle );
		g2d.setColor( PaintedComponentDefines.CONTENTS_COLOR );
		g2d.draw( roundedRectangle );

		// Now switch to erase mode to paint the holes
		g2d.setComposite( PaintedComponentDefines.ERASE_COMPOSITE );
		// Draw the holes a little too large.
		final int holeXOffset = PaintedComponentDefines.HOLE_POS_X_OFFSET;
		final int holeYOffset = PaintedComponentDefines.HOLE_POS_Y_OFFSET;
		final int holeRadius = PaintedComponentDefines.HOLE_BASE_RADIUS;
		final int eraseHoleXRadius = holeRadius + PaintedComponentDefines.HOLE_SURROUND_RADIUS_X;
		final int eraseHoleYRadius = holeRadius + PaintedComponentDefines.HOLE_SURROUND_RADIUS_Y;
		drawHole( g2d, holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, holeXOffset , PaintedComponentDefines.FRONT_MIN_HEIGHT - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, PaintedComponentDefines.FRONT_MIN_WIDTH - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, PaintedComponentDefines.FRONT_MIN_WIDTH - holeXOffset, PaintedComponentDefines.FRONT_MIN_HEIGHT - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		g2d.setComposite( PaintedComponentDefines.OPAQUE_COMPOSITE );
		g2d.setColor( PaintedComponentDefines.LOWLIGHT_COLOR );
		drawHoleOutline( g2d, holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, holeXOffset , PaintedComponentDefines.FRONT_MIN_HEIGHT - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, PaintedComponentDefines.FRONT_MIN_WIDTH - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, PaintedComponentDefines.FRONT_MIN_WIDTH - holeXOffset, PaintedComponentDefines.FRONT_MIN_HEIGHT - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
	}

	private void drawHole( final Graphics2D g2d, final int xCenter, final int yCenter, final int xRadius, final int yRadius )
	{
		final int startX = xCenter - xRadius;
		final int startY = yCenter - yRadius;
		final int width = xRadius * 2;
		final int height = yRadius * 2;
		g2d.fillOval( startX, startY, width, height );
	}

	private void drawHoleOutline( final Graphics2D g2d, final int xCenter, final int yCenter, final int xRadius, final int yRadius )
	{
		final int startX = xCenter - xRadius;
		final int startY = yCenter - yRadius;
		final int width = xRadius * 2;
		final int height = yRadius * 2;
		g2d.drawOval( startX, startY, width, height );
	}

	private final ContainerImages drawBackDecorations()
	{
//		log.trace( "Allocating and drawing decorations" );

		final BufferedImage backDecorations = new BufferedImage( PaintedComponentDefines.BACK_MIN_WIDTH+1, PaintedComponentDefines.BACK_MIN_HEIGHT+1, BufferedImage.TYPE_INT_ARGB );
//		log.trace( "Allocated BI (" + PaintedComponentDefines.BACK_MIN_WIDTH + ", " + PaintedComponentDefines.BACK_MIN_HEIGHT + ")");

		final Graphics2D g2d = backDecorations.createGraphics();

		g2d.setComposite( PaintedComponentDefines.OPAQUE_COMPOSITE );

		doActualBackPaint( g2d );

		final BufferedImage aaBackDecorations = new BufferedImage( PaintedComponentDefines.BACK_MIN_WIDTH+1, PaintedComponentDefines.BACK_MIN_HEIGHT+1, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D aaG2d = aaBackDecorations.createGraphics();

		aaG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

		doActualBackPaint( aaG2d );

		final BufferedImage backLeftTopDecoration =
				aaBackDecorations.getSubimage( 0, 0,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT );
		final BufferedImage backLeftInsetDecoration =
				backDecorations.getSubimage( 0, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 1,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, 1 );
		final BufferedImage backLeftBottomDecoration =
				aaBackDecorations.getSubimage( 0, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 2,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT );

		final BufferedImage backTopInsetDecoration =
				backDecorations.getSubimage( PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 1, 0,
						1, PaintedComponentDefines.BACK_BOTTOM_TOP_INSET );

		final BufferedImage backRightTopDecoration =
				aaBackDecorations.getSubimage( PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 2, 0,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT );
		final BufferedImage backRightInsetDecoration =
				backDecorations.getSubimage( PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 2,
						PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 1,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, 1 );
		final BufferedImage backRightBottomDecoration =
				aaBackDecorations.getSubimage( PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 2,
						PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 2,
						PaintedComponentDefines.BACK_ONE_CORNER_WIDTH, PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT );

		final BufferedImage backBottomInsetDecoration =
				backDecorations.getSubimage( PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 2,
						(PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT * 2) + 2 - PaintedComponentDefines.BACK_BOTTOM_TOP_INSET,
						1, PaintedComponentDefines.BACK_BOTTOM_TOP_INSET );

		return new ContainerImages( backDecorations,
				aaBackDecorations,
				backLeftTopDecoration,
				backLeftInsetDecoration,
				backLeftBottomDecoration,
				backTopInsetDecoration,
				backRightTopDecoration,
				backRightInsetDecoration,
				backRightBottomDecoration,
				backBottomInsetDecoration );
	}

	private void doActualBackPaint( final Graphics2D g2d )
	{
		final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
				0, 0,
				PaintedComponentDefines.BACK_MIN_WIDTH,
				PaintedComponentDefines.BACK_MIN_HEIGHT,
				PaintedComponentDefines.OUTSIDE_BORDER_ARC,
				PaintedComponentDefines.OUTSIDE_BORDER_ARC );

		g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
		g2d.fill( roundedRectangle );
		g2d.setColor( PaintedComponentDefines.CONTENTS_COLOR );
		g2d.draw( roundedRectangle );

//		g2d.setColor( Color.pink );
//		g2d.fillRect( 0, 0, PaintedComponentDefines.BACK_MIN_WIDTH+1, PaintedComponentDefines.BACK_MIN_HEIGHT+1 );
	}
	public void paint( final Graphics g )
	{
		// Debugging stub to output what we are doing
		final Graphics2D g2d = (Graphics2D)g;

		g2d.setComposite( PaintedComponentDefines.SRCOVER_COMPOSITE );

		// Draw the bits of the front

		g2d.drawImage( frontDecorationImages.ltbi,
				10, 10, null );
		g2d.drawImage( frontDecorationImages.libi,
				10, 10 + PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 1, null );
		g2d.drawImage( frontDecorationImages.lbbi,
				10, 10 + PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 3, null );

		g2d.drawImage( frontDecorationImages.tibi,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 1, 10, null );

		g2d.drawImage( frontDecorationImages.rtbi,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 3, 10, null );
		g2d.drawImage( frontDecorationImages.ribi,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 3,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 1, null );
		g2d.drawImage( frontDecorationImages.rbbi,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 3,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT + 3, null );

		g2d.drawImage( frontDecorationImages.bibi,
				10 + PaintedComponentDefines.FRONT_ONE_CORNER_WIDTH + 1,
				10 + (PaintedComponentDefines.FRONT_ONE_CORNER_HEIGHT * 2) + 3 -
					PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET, null );

		g2d.drawImage( frontDecorationImages.src,
				10, 100, null );

		// And the bits of the back
		g2d.drawImage( backDecorationImages.ltbi,
				200, 10, null );
		g2d.drawImage( backDecorationImages.libi,
				200, 10 + PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 1, null );
		g2d.drawImage( backDecorationImages.lbbi,
				200, 10 + PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 3, null );

		g2d.drawImage( backDecorationImages.tibi,
				200 + PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 1, 10, null );

		g2d.drawImage( backDecorationImages.rtbi,
				200 + PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 3, 10, null );
		g2d.drawImage( backDecorationImages.ribi,
				200 + PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 3,
				10 + PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 1, null );
		g2d.drawImage( backDecorationImages.rbbi,
				200 + PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 3,
				10 + PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT + 3, null );

		g2d.drawImage( backDecorationImages.bibi,
				200 + PaintedComponentDefines.BACK_ONE_CORNER_WIDTH + 1,
				10 + (PaintedComponentDefines.BACK_ONE_CORNER_HEIGHT * 2) + 3 -
					PaintedComponentDefines.BACK_BOTTOM_TOP_INSET, null );


		g2d.drawImage( backDecorationImages.src,
				200, 100, null );
	}

	public ContainerImages getFrontDecorationImages()
	{
		return frontDecorationImages;
	}

	public ContainerImages getBackDecorationImages()
	{
		return backDecorationImages;
	}

	public AbstractGuiAudioComponent createFrontGuiComponent( final RackComponent rc )
	{
		return new ResizableFrontContainer( frontDecorationImages, rc );
	}

	public AbstractGuiAudioComponent createBackGuiComponent( final RackComponent rc )
	{
		return new ResizableBackContainer( backDecorationImages, rc );
	}
}
