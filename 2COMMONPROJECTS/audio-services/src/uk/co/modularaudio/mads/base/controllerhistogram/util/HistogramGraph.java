package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.Graphics;

import javax.swing.JPanel;

public class HistogramGraph extends JPanel
{
	private static final long serialVersionUID = -2219255959276577110L;

//	private static Log log = LogFactory.getLog( HistogramGraph.class.getName() );

	private int pixelsPerBinMarker = 10;
	private int pixelsPerEventMarker = 10;

	private final Histogram histogram;

	private final static int NUM_COORDS_FOR_LINE = HistogramDisplay.NUM_HISTOGRAM_BUCKETS * 4;

	private final int[] histogramLineXValues = new int[ NUM_COORDS_FOR_LINE ];
	private final int[] histogramLineYValues = new int[ NUM_COORDS_FOR_LINE ];

	private int mouseX = -1;

	public HistogramGraph( final Histogram histogram )
	{
		this.histogram = histogram;

		setLayout( null );

		setOpaque( false );
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

		final int availableHeight = ((HistogramDisplay.NUM_EVENT_MARKERS-1) * pixelsPerEventMarker);
//		log.debug("AvailableHeight=" + availableHeight );
		final int maxHeight = heightMinusOne - availableHeight;
		final int maxWidth = ((HistogramDisplay.NUM_BIN_MARKERS-1) * pixelsPerBinMarker);
		for( int x = 0 ; x < HistogramDisplay.NUM_BIN_MARKERS ; ++x )
		{
			final int xPixelOffset = x * pixelsPerBinMarker;
			g.drawLine( xPixelOffset, heightMinusOne, xPixelOffset, maxHeight );
		}
		for( int y = 0 ; y < HistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = heightMinusOne - (y * pixelsPerEventMarker);

			g.drawLine( 0, yPixelOffset, maxWidth, yPixelOffset );
		}

		final int numTotalEvents = histogram.getNumTotalEvents();
		final HistogramBucket[] buckets = histogram.getBuckets();
		final int numBuckets = buckets.length;
		final HistogramBucket lastBucket = buckets[numBuckets-1];
		final int framesLastBucketEnd = lastBucket.getBucketEndDiff();

		for( int b = 0 ; b < buckets.length ; ++b )
		{
			final HistogramBucket bucket = buckets[b];
			final int bucketStartFrames = bucket.getBucketStartDiff();
			final int bucketEndFrames = bucket.getBucketEndDiff();
			final int numInBucket = bucket.getBucketCount();

			final int bucketStartPixel = framesToPixel( framesLastBucketEnd, maxWidth, bucketStartFrames );
			final int bucketEndPixel = framesToPixel( framesLastBucketEnd, maxWidth, bucketEndFrames );

//			log.debug( "Bucket " + b + " pixels(" + bucketStartPixel + "->" + bucketEndPixel + ")");

			final float normBarHeight = (float)numInBucket / numTotalEvents;
			final int barHeightThisBucket = (int)(availableHeight * normBarHeight);

			// Fill in line coords
			final int bucketCoordsStartIndex = (b*4);
			// axis line to bucket start
			histogramLineXValues[bucketCoordsStartIndex] = bucketStartPixel+1;
			histogramLineYValues[bucketCoordsStartIndex] = heightMinusOne;
			// Line up to height
			histogramLineXValues[bucketCoordsStartIndex+1] = bucketStartPixel+1;
			histogramLineYValues[bucketCoordsStartIndex+1] = heightMinusOne - barHeightThisBucket;
			// Across the top
			histogramLineXValues[bucketCoordsStartIndex+2] = bucketEndPixel-1;
			histogramLineYValues[bucketCoordsStartIndex+2] = heightMinusOne - barHeightThisBucket;
			// And back down
			histogramLineXValues[bucketCoordsStartIndex+3] = bucketEndPixel-1;
			histogramLineYValues[bucketCoordsStartIndex+3] = heightMinusOne;
		}

		g.setColor( HistogramColours.GRAPH_CONTENT );

		g.drawPolyline( histogramLineXValues, histogramLineYValues, NUM_COORDS_FOR_LINE );

		if( mouseX != -1 )
		{
			g.setColor( HistogramColours.AXIS_LINES);
			g.drawLine( mouseX, 0, mouseX, height );
		}
	}

	private final static int framesToPixel( final int framesLastBucketEnd, final int availableWidth, final int frames )
	{
		final float normPosition = (float)frames / framesLastBucketEnd;
		final int pixelPosition = (int)(normPosition * availableWidth);

		return pixelPosition;
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerBinMarker = HistogramSpacingCalculator.calculateBinMarkerSpacing( width );
		pixelsPerEventMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( height );
	}

	public int getGraphWidth()
	{
		return (HistogramDisplay.NUM_BIN_MARKERS-1) * pixelsPerBinMarker;
	}

	public int getGraphHeight()
	{
		return (HistogramDisplay.NUM_EVENT_MARKERS-1) * pixelsPerEventMarker;
	}

	public void setMousePosition( final int mouseX )
	{
		if( mouseX != this.mouseX )
		{
			this.mouseX = mouseX;
			repaint();
		}
	}

	public void unsetMousePosition()
	{
		setMousePosition( -1 );
	}
}
