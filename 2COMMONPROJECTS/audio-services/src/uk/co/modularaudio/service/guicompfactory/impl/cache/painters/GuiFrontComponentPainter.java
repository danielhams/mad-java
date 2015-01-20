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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.service.guicompfactory.impl.components.ColorDefines;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

public class GuiFrontComponentPainter implements GuiComponentPainter
{
//	private static Log log = LogFactory.getLog( GuiFrontComponentPainter.class.getName() );

	private static final int DRAG_BAR_WIDTH = 20;
	private final static int INSET = 3;
	private final static float ARC = 10;

//	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.6f;
	public final static float EMPTY_COMPONENT_GREY_LEVEL = 0.35f;
	private static final Color BLANK_FRONT_COLOR = new Color( EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL, EMPTY_COMPONENT_GREY_LEVEL );
	
	private Composite opaqueComposite = AlphaComposite.getInstance( AlphaComposite.SRC );
	private Composite eraseComposite = AlphaComposite.getInstance( AlphaComposite.CLEAR );
	
	public GuiFrontComponentPainter()
	{
	}

	@Override
	public void drawComponentImage( RackComponent rackComponent,
			BufferedImage bufferedImage,
			int imageWidth,
			int imageHeight )
	{
		Graphics2D g2d = bufferedImage.createGraphics();
		// Draw the things we want seen
		g2d.setComposite( opaqueComposite );
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor( ColorDefines.HIGHLIGHT_COLOR );
		int x = INSET;
		int y = 0;
		int width = imageWidth - ( 2 * INSET) - 1;
		int height = imageHeight - 1;
		float arcWidth = ARC;
		float arcHeight = ARC;
		RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float( x, y, width, height, arcWidth, arcHeight );
		g2d.fill( roundedRectangle );
		g2d.setColor( ColorDefines.CONTENTS_COLOR );
		g2d.draw( roundedRectangle );
		// Now switch to erase mode to paint the holes
		g2d.setComposite( eraseComposite );
		// Draw the holes a little too large.
		int holeXOffset = 7;
		int holeYOffset = 12;
		int holeRadius = 2;
		int eraseHoleXRadius = holeRadius + 2;
		int eraseHoleYRadius = holeRadius + 1;
		drawHole( g2d, x + holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + holeXOffset , height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + width - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHole( g2d, x + width - holeXOffset, height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		g2d.setComposite( opaqueComposite );
		g2d.setColor( ColorDefines.LOWLIGHT_COLOR );
		drawHoleOutline( g2d, x + holeXOffset , holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + holeXOffset , height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + width - holeXOffset, holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		drawHoleOutline( g2d, x + width - holeXOffset, height - holeYOffset, eraseHoleXRadius, eraseHoleYRadius );
		
		MadUiDefinition<?,?> uiDefinition = rackComponent.getUiDefinition();
		BufferedImage rackComponentFrontImage = uiDefinition.getFrontBufferedImage();
		int frontStartX = DRAG_BAR_WIDTH;
		int frontStartY = 2;
		int frontWidth = imageWidth - (2*DRAG_BAR_WIDTH);
		int frontHeight = imageHeight - 4;
		if( rackComponentFrontImage != null )
		{
			g2d.drawImage( rackComponentFrontImage, frontStartX, frontStartY, frontWidth, frontHeight, null );
		}
		else
		{
			g2d.setColor( BLANK_FRONT_COLOR );
			g2d.fillRect( frontStartX, frontStartY, frontWidth, frontHeight );
		}
	}

	private void drawHole( Graphics2D g2d, int xCenter, int yCenter, int xRadius, int yRadius )
	{
		int startX = xCenter - xRadius;
		int startY = yCenter - yRadius;
		int width = xRadius * 2;
		int height = yRadius * 2;
		g2d.fillOval( startX, startY, width, height );
	}

	private void drawHoleOutline( Graphics2D g2d, int xCenter, int yCenter, int xRadius, int yRadius )
	{
		int startX = xCenter - xRadius;
		int startY = yCenter - yRadius;
		int width = xRadius * 2;
		int height = yRadius * 2;
		g2d.drawOval( startX, startY, width, height );
	}

}
