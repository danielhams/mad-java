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
import java.awt.Graphics;

import javax.swing.JPanel;

public class HistogramEventAxisMarkers extends JPanel
{
	private static final long serialVersionUID = -1935561737815623735L;

//	private static Log log = LogFactory.getLog( HistogramEventAxisMarkers.class.getName() );

	private int pixelsPerMarker = 10;

	public HistogramEventAxisMarkers( final Histogram histogram )
	{
		this.setBackground( Color.CYAN );
		setMinimumSize( new Dimension( HistogramDisplay.AXIS_MARKER_LENGTH, 5 ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
		final int heightMinusOne = height - 1;

		g.setColor( HistogramColours.AXIS_LINES );

		for( int y = 0 ; y < HistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = heightMinusOne - (y * pixelsPerMarker);

//			log.debug("Drawing marker at y=" + yPixelOffset );
			g.drawLine( 0, yPixelOffset, width, yPixelOffset );
		}
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( height );
	}
}
