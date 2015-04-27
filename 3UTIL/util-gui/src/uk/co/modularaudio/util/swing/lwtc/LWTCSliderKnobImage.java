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

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;

public class LWTCSliderKnobImage
{
	private static final int OUTLINE_ARC_WIDTH = 8;
	private static final int OUTLINE_ARC_HEIGHT = OUTLINE_ARC_WIDTH;

	private static final int INSIDE_ARC_WIDTH = 4;
	private static final int INSIDE_ARC_HEIGHT = INSIDE_ARC_WIDTH;

	private static final Composite COMPOSITE_SHADOW =
			AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.1f );

	private static final Composite COMPOSITE_OPAQUE =
			AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 1.0f );

	public final static int H_KNOB_WIDTH = 29;
	public final static int H_KNOB_HEIGHT = 16;

	public final static int V_KNOB_WIDTH = 16;
	public final static int V_KNOB_HEIGHT = 29;

	public final static Dimension H_KNOB_SIZE = new Dimension( H_KNOB_WIDTH, H_KNOB_HEIGHT );
	public final static Dimension H_SLIDER_MIN_SIZE = new Dimension( H_KNOB_WIDTH + 6,
			H_KNOB_HEIGHT + 6 );
	public final static Dimension V_KNOB_SIZE = new Dimension( V_KNOB_WIDTH, V_KNOB_HEIGHT );
	public final static Dimension V_SLIDER_MIN_SIZE = new Dimension( V_KNOB_WIDTH + 6,
			V_KNOB_HEIGHT + 6);

//	private static Log log = LogFactory.getLog( LWTCSliderKnobImage.class.getName() );

	private final BufferedImage bi;

	public LWTCSliderKnobImage( final LWTCSliderColours colours, final int orientation )
	{
		bi = generateImageForKnob( colours, orientation );
	}

	public BufferedImage getKnobImage()
	{
		return bi;
	}

	private BufferedImage generateImageForKnob( final LWTCSliderColours sliderColours,
			final int orientation )
	{
		int iwidth, iheight, width, height;

		if( orientation == SwingConstants.HORIZONTAL )
		{
			iwidth = H_KNOB_WIDTH;
			iheight = H_KNOB_HEIGHT;
		}
		else
		{
			iwidth = V_KNOB_WIDTH;
			iheight = V_KNOB_HEIGHT;
		}
		width = iwidth - 1;
		height = iheight - 1;

		final BufferedImage ti = new BufferedImage(iwidth, iheight, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = ti.createGraphics();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		// Shadow
		g2d.setComposite( COMPOSITE_SHADOW );
		g2d.setColor( Color.BLACK );
		g2d.fillRoundRect( 1, 1, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		g2d.setComposite( COMPOSITE_OPAQUE );

		// outline
		g2d.setColor( sliderColours.getControlOutline() );
		g2d.fillRoundRect( 0, 0, width, height, OUTLINE_ARC_WIDTH, OUTLINE_ARC_HEIGHT );

		// Background with gradient
		final GradientPaint gp;
		if( orientation == SwingConstants.HORIZONTAL )
		{
			gp = new GradientPaint( 0, 1, sliderColours.getBackgroundGradStart(),
					0, height-2, sliderColours.getBackgroundGradEnd() );
		}
		else
		{
			gp = new GradientPaint( 1, 0, sliderColours.getBackgroundGradStart(),
					width-2, 0, sliderColours.getBackgroundGradEnd() );
		}
		g2d.setPaint( gp );
		g2d.fillRoundRect( 1, 1, width-2, height-2, INSIDE_ARC_WIDTH, INSIDE_ARC_HEIGHT );

		// Two barrel lines
		final GradientPaint bgp;
		if( orientation == SwingConstants.HORIZONTAL )
		{
			bgp = new GradientPaint( 0, 1, sliderColours.getBarrelGradStart(),
					0, height-2, sliderColours.getBarrelGradEnd() );
		}
		else
		{
			bgp = new GradientPaint( 1, 0, sliderColours.getBarrelGradStart(),
					width-2, 0, sliderColours.getBarrelGradEnd() );
		}
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		g2d.setPaint( bgp );

		if( orientation == SwingConstants.HORIZONTAL )
		{
			g2d.fillRect( 6, 1, 1, height - 2 );
			g2d.fillRect( 21, 1, 1, height - 2 );
		}
		else
		{
			g2d.fillRect( 1, 6, width - 2, 1 );
			g2d.fillRect( 1, 21, width - 2, 1 );
		}

		// Center dial
		final GradientPaint cdgp;
		if( orientation == SwingConstants.HORIZONTAL )
		{
			cdgp = new GradientPaint( 0, 1, sliderColours.getDialGradStart(),
					0, height-2, sliderColours.getDialGradEnd() );
		}
		else
		{
			cdgp = new GradientPaint( 1, 0, sliderColours.getDialGradStart(),
					width-2, 0, sliderColours.getDialGradEnd() );
		}
		g2d.setPaint( cdgp );

		if( orientation == SwingConstants.HORIZONTAL )
		{
			g2d.fillRect( 7, 1, 14, height - 2 );
		}
		else
		{
			g2d.fillRect( 1, 7, width - 2, 14 );
		}

		int startX, startY;
		if( orientation == SwingConstants.HORIZONTAL )
		{
			startX = 7 + 4;
			startY = 4;
		}
		else
		{
			startX = 4;
			startY = 7 + 4;
		}
		for( int i = 0 ; i < 3 ; ++i )
		{
			for( int j = 0 ; j < 3 ; ++j )
			{
				final int x = startX + (j * 3);
				final int y = startY + (i * 3);
				g2d.setColor( sliderColours.getDimpleDark() );
				g2d.drawLine( x, y, x+1, y );
				g2d.setColor( sliderColours.getDimpleLight() );
				g2d.drawLine( x + 1, y, x+1, y+1 );
				g2d.drawLine( x, y + 1, x+1, y+1 );
			}
		}

		// If an indicator color is set, draw the indicator line.
		final Color indicatorColor = sliderColours.getIndicatorColor();
		if( indicatorColor != null )
		{
			g2d.setColor( indicatorColor );
			if( orientation == SwingConstants.VERTICAL )
			{
				final int x = startX + 5;
				final int y = startY + 3;
				g2d.drawLine( x, y, x+3, y );
			}
			else
			{
				final int x = startX + 3;
				final int y = startY + 5;
				g2d.drawLine( x, y, x, y+3 );
			}
		}

		return ti;
	}
}
