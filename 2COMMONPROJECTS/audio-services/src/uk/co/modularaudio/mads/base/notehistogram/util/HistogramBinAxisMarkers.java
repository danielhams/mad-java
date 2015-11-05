package uk.co.modularaudio.mads.base.notehistogram.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class HistogramBinAxisMarkers extends JPanel
{
	private static final long serialVersionUID = -6203154278089521511L;
//	private static Log log = LogFactory.getLog( HistogramBinAxisMarkers.class.getName() );

	private int pixelsPerMarker = 10;

	public HistogramBinAxisMarkers( final NoteHistogram histogram )
	{
		setBackground( Color.LIGHT_GRAY );
		setMinimumSize( new Dimension( 5, NoteHistogramDisplay.AXIS_MARKER_LENGTH ) );
	}

	@Override
	public void paint( final Graphics g )
	{
		final int height = getHeight();

		g.setColor( HistogramColours.AXIS_LINES );

		for( int x = 0 ; x < NoteHistogramDisplay.NUM_BIN_MARKERS ; ++x )
		{
			final int xPixelOffset = NoteHistogramDisplay.AXIS_MARKER_LENGTH + (x * pixelsPerMarker);
			g.drawLine( xPixelOffset, 0, xPixelOffset, height );
		}
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerMarker = HistogramSpacingCalculator.calculateBinMarkerSpacing( width - NoteHistogramDisplay.AXIS_MARKER_LENGTH );
	}
}
