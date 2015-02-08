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

package uk.co.modularaudio.service.guicompfactory.impl.cache.painters;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

public class GuiFrontComponentPainter implements GuiComponentPainter
{
//	private static Log log = LogFactory.getLog( GuiFrontComponentPainter.class.getName() );


	private final Composite opaqueComposite = AlphaComposite.getInstance( AlphaComposite.SRC );
	private final Composite eraseComposite = AlphaComposite.getInstance( AlphaComposite.CLEAR );

	public GuiFrontComponentPainter()
	{
	}

	@Override
	public void drawComponentImage( final RackComponent rackComponent,
			final BufferedImage bufferedImage,
			final boolean useCustomImages,
			final int imageWidth,
			final int imageHeight )
	{
		final Graphics2D g2d = bufferedImage.createGraphics();
		// Draw the things we want seen
		g2d.setComposite( opaqueComposite );
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor( PaintedComponentDefines.HIGHLIGHT_COLOR );
		final int x = PaintedComponentDefines.INSET;
		final int y = 0;
		final int width = imageWidth - ( 2 * PaintedComponentDefines.INSET) - 1;
		final int height = imageHeight - 1;
		final float arcWidth = PaintedComponentDefines.ARC;
		final float arcHeight = PaintedComponentDefines.ARC;
		final RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( x, y, width, height, arcWidth, arcHeight );
		g2d.fill( roundedRectangle );
		g2d.setColor( PaintedComponentDefines.CONTENTS_COLOR );
		g2d.draw( roundedRectangle );
		// Now switch to erase mode to paint the holes
		g2d.setComposite( eraseComposite );
		// Draw the holes a little too large.
		final int holeXOffset = 7;
		final int holeYOffset = 12;
		final int holeRadius = 2;
		final int eraseHoleXRadius = holeRadius + 2;
		final int eraseHoleYRadius = holeRadius + 1;
		drawHole( g2d, x + holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + holeXOffset , height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + width - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + width - holeXOffset, height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		g2d.setComposite( opaqueComposite );
		g2d.setColor( PaintedComponentDefines.LOWLIGHT_COLOR );
		drawHoleOutline( g2d, x + holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + holeXOffset , height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + width - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + width - holeXOffset, height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );

		final MadUiDefinition<?,?> uiDefinition = rackComponent.getUiDefinition();
		final BufferedImage rackComponentFrontImage = uiDefinition.getFrontBufferedImage();
		final int frontStartX = PaintedComponentDefines.DRAG_BAR_WIDTH;
		final int frontStartY = 2;
		final int frontWidth = imageWidth - (2*PaintedComponentDefines.DRAG_BAR_WIDTH);
		final int frontHeight = imageHeight - 4;
		if( rackComponentFrontImage != null && useCustomImages )
		{
			g2d.drawImage( rackComponentFrontImage, frontStartX, frontStartY, frontWidth, frontHeight, null );
		}
		else
		{
			g2d.setColor( PaintedComponentDefines.BLANK_FRONT_COLOR );
			g2d.fillRect( frontStartX, frontStartY, frontWidth, frontHeight );
		}
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

}
