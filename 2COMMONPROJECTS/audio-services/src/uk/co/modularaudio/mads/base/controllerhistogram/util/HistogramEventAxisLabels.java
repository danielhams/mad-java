/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;


public class HistogramEventAxisLabels extends JPanel
{
	private static final long serialVersionUID = 8751186681559836915L;

	private int pixelsPerMarker = 10;
	private final FontMetrics fm;

	public HistogramEventAxisLabels( final Histogram histogram )
	{
		setBackground( Color.BLACK );
		setMinimumSize( new Dimension( HistogramDisplay.EVENTS_LABELS_WIDTH, 5 ) );

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );
		fm = getFontMetrics( getFont() );
	}

	@Override
	public void paint( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		final int width = getWidth();
		final int height = getHeight();
		final int heightMinusOne = height - 1;

		g2d.setColor( HistogramColours.AXIS_LINES );

		for( int y = 0 ; y < HistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = (heightMinusOne - ((y * pixelsPerMarker) + HistogramDisplay.AXIS_MARKER_LENGTH));

//			log.debug("Drawing marker at y=" + yPixelOffset );
//			g.drawLine( 0, yPixelOffset, width, yPixelOffset );

			final int percent = (int)((y / (float)(HistogramDisplay.NUM_EVENT_MARKERS-1))*100.0f);
			drawCenteredText( g, width, yPixelOffset, percent );
		}
	}

	private void drawCenteredText( final Graphics g2d, final int width, final int yOffset, final int value )
	{
		final String asString = Integer.toString(value) + "%";
		final char[] chars = asString.toCharArray();
		final float textHeight = fm.getAscent(); // + fm.getDescent();

		final int charsWidth = fm.charsWidth( chars, 0, chars.length );

		final int xOffset = (width-1) - charsWidth - 2;
		g2d.drawChars( chars, 0, chars.length, xOffset, yOffset + (int)(textHeight / 2 ) );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( (height - HistogramDisplay.AXIS_MARKER_LENGTH) );
	}
}
