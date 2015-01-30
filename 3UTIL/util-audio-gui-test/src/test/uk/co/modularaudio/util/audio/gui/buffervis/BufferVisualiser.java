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

package test.uk.co.modularaudio.util.audio.gui.buffervis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BufferVisualiser extends JPanel
{
	private static final int PLOT_WIDTH = 800;
	private static final int PLOT_HEIGHT = 600;

	private static final int AXES_INSET = 100;

	private static final int GRAPH_WIDTH = PLOT_WIDTH + AXES_INSET;
	private static final int GRAPH_HEIGHT = PLOT_HEIGHT + AXES_INSET;


	private static final long serialVersionUID = 5165617914357047279L;

	private static Log log = LogFactory.getLog( BufferVisualiser.class.getName() );

	private final Dimension size;

	private final BufferedImage bi;
	private final Graphics2D biG2d;

	private float minVal = Float.MAX_VALUE;
	private float maxVal = Float.MIN_VALUE;
	private float diff = 0.0f;

	private final AffineTransform origTransform;
	private final AffineTransform yFlipTransform;

	public BufferVisualiser()
	{
		size = new Dimension( GRAPH_WIDTH, GRAPH_HEIGHT );
		this.setSize( size );
		this.setPreferredSize( size );

		bi = new BufferedImage( GRAPH_WIDTH, GRAPH_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		biG2d = bi.createGraphics();

		origTransform = biG2d.getTransform();

		biG2d.scale( 1, -1 );
		biG2d.translate( 0, -(GRAPH_HEIGHT ) );

		yFlipTransform = biG2d.getTransform();

	}

	public void regenerateFromBuffers( final String labelOne, final float[] buffer, final int numSamples, final String labelTwo, final float[] bufferTwo )
	{
		biG2d.setColor( Color.BLACK );
		biG2d.fillRect( 0,  0, GRAPH_WIDTH, GRAPH_HEIGHT );

//		biG2d.setColor( Color.RED );
//		biG2d.drawRect( 1, 1, GRAPH_WIDTH - 2, GRAPH_HEIGHT - 2 );

		computeMinMax( bufferTwo, numSamples );

		drawAxes( labelOne, Color.orange, labelTwo, Color.pink );

		plotCurve( Color.orange, buffer, numSamples );
		plotCurve( Color.pink, bufferTwo, numSamples );
	}

	@Override
	public void paint( final Graphics g )
	{
		if( bi != null )
		{
			g.drawImage( bi, 0, 0, null );
		}
	}

	private void computeMinMax( final float[] buffer, final int numSamples )
	{
		minVal = Float.MAX_VALUE;
		maxVal = Float.MIN_VALUE;

		for( int i = 0 ; i < numSamples ; i++ )
		{
			final float v = buffer[ i ];
			if( Float.isNaN( v ) || Float.isInfinite( v ) )
			{
				continue;
			}
			if( v < minVal ) minVal = v;
			if( v > maxVal ) maxVal = v;
		}
		diff = maxVal - minVal;
	}

	private void drawAxes(final String labelOne, final Color colorOne, final String labelTwo, final Color colorTwo)
	{
		biG2d.setTransform( yFlipTransform );

		final int tickInset = (int)(AXES_INSET * 2.5/3);
		biG2d.setColor( Color.white );
		// X axis
		biG2d.drawLine( AXES_INSET, AXES_INSET, AXES_INSET + PLOT_WIDTH, AXES_INSET );
		biG2d.drawLine( tickInset, AXES_INSET, AXES_INSET, AXES_INSET );
		biG2d.drawLine( tickInset, AXES_INSET + PLOT_HEIGHT, AXES_INSET, AXES_INSET + PLOT_HEIGHT );

		// Y axis
		biG2d.drawLine( AXES_INSET, AXES_INSET, AXES_INSET, AXES_INSET + PLOT_HEIGHT );
		biG2d.drawLine( AXES_INSET, tickInset, AXES_INSET, AXES_INSET );
		biG2d.drawLine( AXES_INSET + PLOT_WIDTH - 1, tickInset, AXES_INSET + PLOT_WIDTH - 1, AXES_INSET );

		biG2d.setTransform( origTransform );

		final String minYLabel = "Min Y(" + minVal + ")";
		biG2d.drawString( minYLabel, tickInset - 60, PLOT_HEIGHT - (tickInset / 4) );

		final String maxYLabel = "Max Y(" + maxVal + ")";
		biG2d.drawString( maxYLabel, tickInset - 60, tickInset / 4 );

		biG2d.setColor( colorOne );
		biG2d.drawString( labelOne, AXES_INSET + 20,  50 );

		biG2d.setColor( colorTwo );
		biG2d.drawString( labelTwo, AXES_INSET + 20, 100 );
	}

	private void plotCurve( final Color color, final float[] buffer, final int numSamples )
	{
		biG2d.setColor( color );
		biG2d.setTransform( yFlipTransform );

		final int startXOffset = AXES_INSET;
		final int startYOffset = AXES_INSET;

		int previousXPixel = -1;
		int previousYPixel = -1;


		for( int i = 0 ; i < numSamples ; i++ )
		{
			final int currentX = (int)(( (float)i / (numSamples - 1)) * PLOT_WIDTH);
			final float val = buffer[ i ];
			final float normalisedVal = (val - minVal) / diff;
			final int currentY = (int)(normalisedVal * PLOT_HEIGHT );

			if( log.isDebugEnabled() )
			{
				log.debug("Value is " + val );
				log.debug("Normalised is " + normalisedVal );
				log.debug("Translated to (X,Y) is (" + currentX + ", " + currentY + ")");
			}
			final int pixelX = startXOffset + currentX;
			final int pixelY = startYOffset + currentY;
			if( previousXPixel != -1 )
			{
				if( previousXPixel != pixelX || previousYPixel != pixelY )
				{
					biG2d.drawLine( previousXPixel, previousYPixel, pixelX, pixelY );
				}
			}
			previousXPixel = pixelX;
			previousYPixel = pixelY;
		}
	}
}
