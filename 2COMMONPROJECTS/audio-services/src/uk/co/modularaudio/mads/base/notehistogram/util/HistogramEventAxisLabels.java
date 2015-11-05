package uk.co.modularaudio.mads.base.notehistogram.util;

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

	public HistogramEventAxisLabels( final NoteHistogram histogram )
	{
		setBackground( Color.BLACK );
		setMinimumSize( new Dimension( NoteHistogramDisplay.EVENTS_LABELS_WIDTH, 5 ) );

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

		for( int y = 0 ; y < NoteHistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = (heightMinusOne - ((y * pixelsPerMarker) + NoteHistogramDisplay.AXIS_MARKER_LENGTH));

//			log.debug("Drawing marker at y=" + yPixelOffset );
//			g.drawLine( 0, yPixelOffset, width, yPixelOffset );

			final int percent = (int)((y / (float)(NoteHistogramDisplay.NUM_EVENT_MARKERS-1))*100.0f);
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

		pixelsPerMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( (height - NoteHistogramDisplay.AXIS_MARKER_LENGTH) );
	}
}
