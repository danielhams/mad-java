package uk.co.modularaudio.mads.base.notehistogram.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class HistogramEventAxisMarkers extends JPanel
{
	private static final long serialVersionUID = -1935561737815623735L;

//	private static Log log = LogFactory.getLog( HistogramEventAxisMarkers.class.getName() );

	private int pixelsPerMarker = 10;

	public HistogramEventAxisMarkers( final NoteHistogram histogram )
	{
		this.setBackground( Color.CYAN );
		setMinimumSize( new Dimension( NoteHistogramDisplay.AXIS_MARKER_LENGTH, 5 ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
		final int heightMinusOne = height - 1;

		g.setColor( HistogramColours.AXIS_LINES );

		for( int y = 0 ; y < NoteHistogramDisplay.NUM_EVENT_MARKERS ; ++y )
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
