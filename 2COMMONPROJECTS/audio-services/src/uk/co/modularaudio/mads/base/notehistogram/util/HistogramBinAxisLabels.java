package uk.co.modularaudio.mads.base.notehistogram.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class HistogramBinAxisLabels extends JPanel
{
	private static final long serialVersionUID = -7832095706567222837L;

//	private static Log log = LogFactory.getLog( HistogramBinAxisLabels.class.getName() );

	private int sampleRate = DataRate.CD_QUALITY.getValue();
	private int pixelsPerMarker = 10;

	private final NoteHistogram histogram;

	public HistogramBinAxisLabels( final NoteHistogram histogram )
	{
		setBackground( Color.BLACK );
		setOpaque( true );
		this.setMinimumSize( new Dimension( 5, NoteHistogramDisplay.BINS_LABELS_HEIGHT ) );

		setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		this.histogram = histogram;
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();

		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		g2d.setColor( HistogramColours.GRAPH_BACKGROUND );
		g2d.fillRect( 0, 0, width, height );

		g2d.setColor( HistogramColours.AXIS_LINES );

		g2d.setFont( LWTCControlConstants.LABEL_SMALL_FONT );

		final FontMetrics fm = getFontMetrics( LWTCControlConstants.LABEL_SMALL_FONT );

		final NoteHistogramBucket[] buckets = histogram.getBuckets();
		final int numBuckets = buckets.length;

		final int lastBucketStart = buckets[numBuckets-1].getBucketEndDiff();
//		log.debug( "Last bucket end is "  + lastBucketStart );

		final float incrementPerBucket = lastBucketStart / (NoteHistogramDisplay.NUM_BIN_MARKERS-1);

		final int yOffset = fm.getAscent() + 2;

		for( int b = 0 ; b < NoteHistogramDisplay.NUM_BIN_MARKERS ; ++b )
		{
			final int xOffset = NoteHistogramDisplay.EVENTS_LABELS_WIDTH +
					NoteHistogramDisplay.AXIS_MARKER_LENGTH +
					(b * pixelsPerMarker);

			final int bucketStartDifference = (int)(b * incrementPerBucket);
//			log.debug( "Bucket start diff is "  + bucketStartDifference );

			final long asNanos = AudioTimingUtils.getNumNanosecondsForBufferLength( sampleRate, bucketStartDifference );
			final float asMillis = (asNanos / 1000000.0f);

			final String labelString = MathFormatter.fastFloatPrint( asMillis, 1, false ) + "ms";

//			log.debug( "Drawing bin text " + labelString + " at " + xOffset + ", " + yOffset );
			drawCenteredText( g2d, fm, xOffset, yOffset, labelString );
		}
	}

	private void drawCenteredText( final Graphics g2d,
			final FontMetrics fm,
			final int xOffset,
			final int yOffset,
			final String labelString )
	{
		final char[] chars = labelString.toCharArray();
		final int charsWidth = fm.charsWidth( chars, 0, chars.length );
		g2d.drawChars( chars, 0, chars.length, xOffset - (charsWidth / 2), yOffset );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerMarker = HistogramSpacingCalculator.calculateBinMarkerSpacing(
				(width - NoteHistogramDisplay.AXIS_MARKER_LENGTH - NoteHistogramDisplay.EVENTS_LABELS_WIDTH) );
	}

	public void setSampleRate( final int sampleRate )
	{
		this.sampleRate = sampleRate;
		// We force a repaint so that even when stopped the labels get updated.
		repaint();
	}
}
