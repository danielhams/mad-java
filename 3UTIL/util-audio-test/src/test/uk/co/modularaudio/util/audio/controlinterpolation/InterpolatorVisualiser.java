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

package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class InterpolatorVisualiser extends JPanel
{
	private static final long serialVersionUID = 335854680655068226L;

	private static Log log = LogFactory.getLog( InterpolatorVisualiser.class.getName() );

	private final ControlValueInterpolator valueInterpolator;
	private final InterpolatorVisualiser controlSrcVisualiser;

	private final BufferedImage bi;
	private final Graphics2D g2d;

	public InterpolatorVisualiser( final ControlValueInterpolator valueInterpolator,
			final InterpolatorVisualiser controlSrcVisualiser )
	{
		this.valueInterpolator = valueInterpolator;
		this.controlSrcVisualiser = controlSrcVisualiser;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 10" );
//		msh.addLayoutConstraint( "debug" );

		setLayout( msh.createMigLayout() );

		final Dimension size = new Dimension( SwingControlInterpolatorAnalyser.VIS_WIDTH + 1,
				SwingControlInterpolatorAnalyser.VIS_HEIGHT + 1 );

		setPreferredSize( size );

		// Create a buffered image that we'll plot the values in
		bi = new BufferedImage( SwingControlInterpolatorAnalyser.VIS_WIDTH+1, SwingControlInterpolatorAnalyser.VIS_HEIGHT+1,
				BufferedImage.TYPE_INT_ARGB );
		g2d = bi.createGraphics();
		log.debug("Created BI for visualisation");
	}

	public void interpolateEvents( final TestEvent[] events )
	{
		int lastEventIndex = events[events.length - 1].getOffsetInSamples();
		final float[] vals = new float[lastEventIndex];

		final int numEvents = events.length;

		int curOutputOffset = 0;

		for( int i = 1 ; i < numEvents ; ++i )
		{
			final int eventOffset = events[i].getOffsetInSamples();

			// Generate using the interpolator up to this event
			final int numForThisEvent = eventOffset - curOutputOffset;
			valueInterpolator.generateControlValues( vals, curOutputOffset, numForThisEvent );

			final float newValue = events[i].getEventValue();
//			log.debug("Using newValue " + newValue );
			valueInterpolator.notifyOfNewValue( newValue );

			curOutputOffset += numForThisEvent;
		}

		if( curOutputOffset < lastEventIndex )
		{
			valueInterpolator.generateControlValues( vals, curOutputOffset, SwingControlInterpolatorAnalyser.VIS_WIDTH - curOutputOffset );
		}
		g2d.setComposite( AlphaComposite.Clear );
		g2d.setColor( Color.WHITE );
		g2d.fillRect( 0, 0, SwingControlInterpolatorAnalyser.VIS_WIDTH+1, SwingControlInterpolatorAnalyser.VIS_HEIGHT+1 );

		g2d.setComposite( AlphaComposite.SrcOver );
		// If we are the src signal,
		// draw some lines where we have events
		if( controlSrcVisualiser == null )
		{
			g2d.setColor( new Color( 0.6f, 0.6f, 1.0f ) );
			for( int i = 1 ; i < numEvents ; ++i )
			{
				final int eventOffset = events[i].getOffsetInSamples();
				final int eventPosInPixels = eventOffset / SwingControlInterpolatorAnalyser.VIS_SAMPLES_PER_PIXEL;

				g2d.drawLine( eventPosInPixels, 0, eventPosInPixels, SwingControlInterpolatorAnalyser.VIS_HEIGHT + 1 );
			}
		}

		if( controlSrcVisualiser == null )
		{
			g2d.setColor( Color.RED );
		}
		else
		{
			g2d.setColor( Color.BLACK );
		}

		int previousY = (int)(vals[0] * SwingControlInterpolatorAnalyser.VIS_HEIGHT);
		for( int i = 1 ; i < lastEventIndex ; ++i )
		{
			final float val = vals[i];
			final float asYValue = val * SwingControlInterpolatorAnalyser.VIS_HEIGHT;
			final int asYInt = (int)asYValue;
			final int x1 = (i-1) / SwingControlInterpolatorAnalyser.VIS_SAMPLES_PER_PIXEL;
			final int y1 = previousY;
			final int x2 = i / SwingControlInterpolatorAnalyser.VIS_SAMPLES_PER_PIXEL;
			final int y2 = asYInt;
//			log.debug("Drawing line from " + x1 + ", " + y1 + " to " + x2 + ", " + y2 );
			g2d.drawLine( x1, SwingControlInterpolatorAnalyser.VIS_HEIGHT - y1,
					x2, SwingControlInterpolatorAnalyser.VIS_HEIGHT - y2 );

			previousY = y2;
		}

		repaint();
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, SwingControlInterpolatorAnalyser.VIS_WIDTH + 1, SwingControlInterpolatorAnalyser.VIS_HEIGHT + 1 );
		if( controlSrcVisualiser != null )
		{
			controlSrcVisualiser.paint( g );
		}
		g.drawImage( bi, 0, 0, null );
	}

}
