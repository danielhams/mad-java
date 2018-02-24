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

public class HistogramBucketAxisMarkers extends JPanel
{
	private static final long serialVersionUID = -6203154278089521511L;
//	private static Log log = LogFactory.getLog( HistogramBinAxisMarkers.class.getName() );

	private int pixelsPerMarker = 10;

	public HistogramBucketAxisMarkers( final Histogram histogram )
	{
		setBackground( Color.LIGHT_GRAY );
		setMinimumSize( new Dimension( 5, HistogramDisplay.AXIS_MARKER_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int height = getHeight();

		g.setColor( HistogramColours.AXIS_LINES );

		for( int x = 0 ; x < HistogramDisplay.NUM_BUCKET_MARKERS ; ++x )
		{
			final int xPixelOffset = HistogramDisplay.AXIS_MARKER_LENGTH + (x * pixelsPerMarker);
			g.drawLine( xPixelOffset, 0, xPixelOffset, height );
		}
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerMarker = HistogramSpacingCalculator.calculateBucketMarkerSpacing( width - HistogramDisplay.AXIS_MARKER_LENGTH );
	}
}
