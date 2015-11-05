package uk.co.modularaudio.mads.base.notehistogram.util;

import java.awt.Graphics;

import javax.swing.JPanel;

public class HistogramGraph extends JPanel
{
	private static final long serialVersionUID = -2219255959276577110L;

//	private static Log log = LogFactory.getLog( HistogramGraph.class.getName() );

	private int pixelsPerBinMarker = 10;
	private int pixelsPerEventMarker = 10;

	private final NoteHistogram histogram;

	private final int[] histogramLineXValues = new int[ NoteHistogramDisplay.NUM_HISTOGRAM_BUCKETS ];
	private final int[] histogramLineYValues = new int[ NoteHistogramDisplay.NUM_HISTOGRAM_BUCKETS ];

	public HistogramGraph( final NoteHistogram histogram )
	{
		this.histogram = histogram;
	}

	@Override
	public void paint( final Graphics g )
	{
		final int width = getWidth();
		final int height = getHeight();
//		log.debug("Height=" + height );
		final int heightMinusOne = height-1;

		g.setColor( HistogramColours.GRAPH_BACKGROUND );
		g.fillRect( 0, 0, width, height );

		g.setColor( HistogramColours.AXIS_LINES );

		final int availableHeight = ((NoteHistogramDisplay.NUM_EVENT_MARKERS-1) * pixelsPerEventMarker);
//		log.debug("AvailableHeight=" + availableHeight );
		final int maxHeight = heightMinusOne - availableHeight;
		final int maxWidth = ((NoteHistogramDisplay.NUM_BIN_MARKERS-1) * pixelsPerBinMarker);
		for( int x = 0 ; x < NoteHistogramDisplay.NUM_BIN_MARKERS ; ++x )
		{
			final int xPixelOffset = x * pixelsPerBinMarker;
			g.drawLine( xPixelOffset, heightMinusOne, xPixelOffset, maxHeight );
		}
		for( int y = 0 ; y < NoteHistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = heightMinusOne - (y * pixelsPerEventMarker);

			g.drawLine( 0, yPixelOffset, maxWidth, yPixelOffset );
		}

		final int numTotalEvents = histogram.getNumTotalEvents();
		final NoteHistogramBucket[] buckets = histogram.getBuckets();
		final int numBuckets = buckets.length;

		final float[] normalisedBuckets = new float[numBuckets];
		for( int b = 0 ; b < buckets.length ; ++b )
		{
			final float normVal = (buckets[b].getBucketCount() / (float)numTotalEvents);
			normalisedBuckets[b] = normVal;
//			log.debug("Percentage for bucket " + b + " is " + MathFormatter.fastFloatPrint( normVal, 8, true ) );
			histogramLineYValues[b] = (heightMinusOne - (int)(normVal * availableHeight));
			final int xPosition = (int)(((float)b / numBuckets) * maxWidth);
			histogramLineXValues[b] = xPosition;
		}

		g.setColor( HistogramColours.GRAPH_CONTENT );

		g.drawPolyline( histogramLineXValues, histogramLineYValues, numBuckets );
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerBinMarker = HistogramSpacingCalculator.calculateBinMarkerSpacing( width );
		pixelsPerEventMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( height );
	}
}
