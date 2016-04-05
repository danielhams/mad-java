package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class HistogramGraph extends JPanel
{
	private static final long serialVersionUID = -2219255959276577110L;

	private static final int BUCKET_INFO_DATA_WIDTH = 150;
	private static final int BUCKET_INFO_DATA_HEIGHT = 70;
	private static final int BUCKET_INFO_INDICATOR_WIDTH = 15;
	private static final int BUCKET_INFO_TOTAL_WIDTH = BUCKET_INFO_DATA_WIDTH + BUCKET_INFO_INDICATOR_WIDTH;
	private static final int BUCKET_INFO_TOTAL_HEIGHT = BUCKET_INFO_DATA_HEIGHT;

	private final static int NUM_BUBBLE_POINTS = 7;

//	private static Log log = LogFactory.getLog( HistogramGraph.class.getName() );

	private int pixelsPerBucketMarker = 10;
	private int pixelsPerEventMarker = 10;

	private final Histogram histogram;

	private int mouseX = -1;

	public HistogramGraph( final Histogram histogram )
	{
		this.histogram = histogram;

		setLayout( null );

		setOpaque( false );
	}

	@SuppressWarnings("boxing")
	@Override
	public void paint( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		final int width = getWidth();
		final int height = getHeight();
//		if( log.isDebugEnabled() )
//		{
//			log.debug("GraphWidthHeight(" + getGraphWidth() + ", " + getGraphHeight() + ")" );
//		}
		final int heightMinusOne = height-1;

		g2d.setColor( HistogramColours.GRAPH_BACKGROUND );
		g2d.fillRect( 0, 0, width, height );

		g2d.setColor( HistogramColours.AXIS_LINES );

		final int availableHeight = ((HistogramDisplay.NUM_EVENT_MARKERS-1) * pixelsPerEventMarker);
//		log.debug("AvailableHeight=" + availableHeight );
		final int maxHeight = heightMinusOne - availableHeight;
		final int maxWidth = ((HistogramDisplay.NUM_BUCKET_MARKERS-1) * pixelsPerBucketMarker);
		for( int x = 0 ; x < HistogramDisplay.NUM_BUCKET_MARKERS ; ++x )
		{
			final int xPixelOffset = x * pixelsPerBucketMarker;
			g2d.drawLine( xPixelOffset, heightMinusOne, xPixelOffset, maxHeight );
		}
		for( int y = 0 ; y < HistogramDisplay.NUM_EVENT_MARKERS ; ++y )
		{
			final int yPixelOffset = heightMinusOne - (y * pixelsPerEventMarker);

			g2d.drawLine( 0, yPixelOffset, maxWidth, yPixelOffset );
		}

		final int numTotalEvents = histogram.getNumTotalEvents();
		final HistogramBucket[] buckets = histogram.getBuckets();
		final int numBuckets = buckets.length;
		final HistogramBucket lastBucket = buckets[numBuckets-1];
		final long nanosLastBucketEnd = lastBucket.getBucketEndNanos();

		g2d.setColor( HistogramColours.GRAPH_CONTENT );

		for( int b = 0 ; b < buckets.length ; ++b )
		{
			final HistogramBucket bucket = buckets[b];
			final long bucketStartNanos = bucket.getBucketStartNanos();
			final long bucketEndNanos = bucket.getBucketEndNanos();
			final int numInBucket = bucket.getBucketCount();

			final int bucketStartPixel = nanosToPixel( nanosLastBucketEnd, maxWidth, bucketStartNanos );
			final int bucketEndPixel = nanosToPixel( nanosLastBucketEnd, maxWidth, bucketEndNanos );

			final float normBarHeight = (float)numInBucket / numTotalEvents;
			final int barHeightThisBucket = (int)(availableHeight * normBarHeight);

			final int barStartX = bucketStartPixel + 1;
			final int barEndX = bucketEndPixel - 1;
			int barBottomY = heightMinusOne - barHeightThisBucket;
			final int barTopY = heightMinusOne;

			if( barBottomY == barTopY )
			{
				barBottomY -= 1;
			}

			g2d.fillRect( barStartX, barBottomY, barEndX - barStartX, barTopY - barBottomY );
		}

		if( mouseX != -1 )
		{
			g2d.setColor( HistogramColours.AXIS_LINES);
			g2d.drawLine( mouseX, heightMinusOne, mouseX, heightMinusOne - availableHeight );

			g2d.setFont( LWTCControlConstants.LABEL_FONT );

			final HistogramBucket b = getBucketForXPos( mouseX );
			final TwoTuple<Integer,Integer> bucketCoords = getBucketTopCoords( b );
			final int bucketTopX = bucketCoords.getHead();
			final int bucketTopY = bucketCoords.getTail();

			displayBucketInfo( g2d, b, maxWidth, availableHeight, bucketTopX, bucketTopY );
		}
	}

	private void displayBucketInfo( final Graphics2D g2d,
			final HistogramBucket bucket,
			final int availableWidth,
			final int availableHeight,
			final int bucketTopX,
			final int bucketTopY )
	{
		final long bucketStartNanos = bucket.getBucketStartNanos();
		final long bucketEndNanos = bucket.getBucketEndNanos();
		final float bucketStartMillis = bucketStartNanos / 1000000.0f;
		final float bucketEndMillis = bucketEndNanos / 1000000.0f;
		final int numEvents = bucket.getBucketCount();

		final float numSecondsAtStart = bucketStartMillis / 1000.0f;
		final float numSecondsAtEnd = bucketEndMillis / 1000.0f;
		final float freqAtStart = (numSecondsAtStart == 0 ? Float.POSITIVE_INFINITY : 1.0f / numSecondsAtStart );
		final float freqAtEnd = (numSecondsAtEnd == 0 ? Float.POSITIVE_INFINITY : 1.0f / numSecondsAtEnd );

		g2d.setColor( HistogramColours.AXIS_LINES );

		final boolean isToRight = bucketTopX < (availableWidth - BUCKET_INFO_TOTAL_WIDTH);

		final int actualY = HistogramDisplay.MARKER_PADDING + availableHeight - bucketTopY;
		final boolean isDownwards = actualY < (availableHeight - BUCKET_INFO_TOTAL_HEIGHT);
		final int adjustedY = (isDownwards
				?
				actualY
				:
				actualY - BUCKET_INFO_TOTAL_HEIGHT );

//		log.debug("Drawing rect at " + adjustedX + " " + adjustedY );

		final int[] xPoints = new int[NUM_BUBBLE_POINTS];
		final int[] yPoints = new int[NUM_BUBBLE_POINTS];
		// Origin of bucket
		xPoints[0] = bucketTopX;
		yPoints[0] = actualY;
		// bubble top join
		xPoints[1] = isToRight
				?
				bucketTopX + BUCKET_INFO_INDICATOR_WIDTH
				:
				bucketTopX - BUCKET_INFO_INDICATOR_WIDTH;
		yPoints[1] = (adjustedY + (BUCKET_INFO_DATA_HEIGHT/4));
		// top closest corner of rectangle
		xPoints[2] = xPoints[1];
		yPoints[2] = adjustedY;
		// top furthest corner of rectangle
		xPoints[3] = isToRight
				?
				xPoints[2] + BUCKET_INFO_DATA_WIDTH
				:
				xPoints[2] - BUCKET_INFO_DATA_WIDTH;
		yPoints[3] = yPoints[2];
		// bottom furthest corner of rectangle
		xPoints[4] = xPoints[3];
		yPoints[4] = yPoints[3] + BUCKET_INFO_DATA_HEIGHT;
		// bottom closest corner of rectangle
		xPoints[5] = xPoints[2];
		yPoints[5] = yPoints[4];
		// bubble bottom join
		xPoints[6] = xPoints[1];
		yPoints[6] = adjustedY + BUCKET_INFO_DATA_HEIGHT - (BUCKET_INFO_DATA_HEIGHT/4);

//		log.debug( "IsToRight " + isToRight );
//		log.debug( "IsDownards " + isDownwards );
//		log.debug( "Plotting points: " + Arrays.toString(xPoints) + " " + Arrays.toString(yPoints) );

		g2d.fillPolygon( xPoints, yPoints, NUM_BUBBLE_POINTS );

		g2d.setColor( HistogramColours.GRAPH_CONTENT );
		g2d.drawPolygon( xPoints, yPoints, NUM_BUBBLE_POINTS );

		// And draw the text
		final int textStartX = (isToRight ? xPoints[2] : xPoints[3]) + 10;

		StringBuilder sb = new StringBuilder(30);
		sb.append( "Millis: [" );
		sb.append( MathFormatter.fastFloatPrint( bucketStartMillis, 2, false ) );
		sb.append( ',' );
		sb.append( MathFormatter.fastFloatPrint( bucketEndMillis, 2, false ) );
		sb.append( ')' );

		drawString( g2d, sb.toString(), textStartX, yPoints[3] + 20 );

		sb = new StringBuilder(30);
		sb.append( "Freq: [" );
		sb.append( MathFormatter.fastFloatPrint( freqAtStart, 2, false ) );
		sb.append( ',' );
		sb.append( MathFormatter.fastFloatPrint( freqAtEnd, 2, false ) );
		sb.append( ')' );

		drawString( g2d, sb.toString(), textStartX, yPoints[3] + 40 );

		sb = new StringBuilder(30);
		sb.append( "Num Events: " );
		sb.append( numEvents );

		drawString( g2d, sb.toString(), textStartX, yPoints[3] + 60 );
	}

	private void drawString( final Graphics2D g2d, final String s, final int x, final int y )
	{
		final char[] charData = s.toCharArray();
		g2d.drawChars( charData, 0, charData.length, x, y );
	}

	private final static int nanosToPixel( final long nanosLastBucketEnd, final int availableWidth, final long nanos )
	{
		final double normPosition = (double)nanos / nanosLastBucketEnd;
		final int pixelPosition = (int)(normPosition * availableWidth);

		return pixelPosition;
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		pixelsPerBucketMarker = HistogramSpacingCalculator.calculateBucketMarkerSpacing( width );
		pixelsPerEventMarker = HistogramSpacingCalculator.calculateEventMarkerSpacing( height );
	}

	public int getGraphWidth()
	{
		return (HistogramDisplay.NUM_BUCKET_MARKERS-1) * pixelsPerBucketMarker;
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

	@SuppressWarnings("boxing")
	public TwoTuple<Integer, Integer> getBucketTopCoords( final HistogramBucket hb )
	{
		final HistogramBucket[] buckets = histogram.getBuckets();
		final int numBuckets = buckets.length;
		final HistogramBucket lastBucket = buckets[numBuckets-1];
		final long nanosLastBucketEnd = lastBucket.getBucketEndNanos();

		final int availableWidth = ((HistogramDisplay.NUM_BUCKET_MARKERS-1) * pixelsPerBucketMarker);

		final long startNanos = hb.getBucketStartNanos();
		final long endNanos = hb.getBucketEndNanos();
		final long midNanos = (endNanos + startNanos) / 2;
		final int bucketX = nanosToPixel( nanosLastBucketEnd, availableWidth, midNanos );

		final int numInBucket = hb.getBucketCount();
		final int numTotal = histogram.getNumTotalEvents();
		final float normNumInBucket = (float)numInBucket / numTotal;
		final int availableHeight = ((HistogramDisplay.NUM_EVENT_MARKERS-1) * pixelsPerEventMarker);
		final int bucketY = (int)(availableHeight * normNumInBucket);
		return new TwoTuple<Integer, Integer>( bucketX, bucketY );
	}

	public HistogramBucket getBucketForXPos( final int mouseXPosInGraph )
	{
		final int bucketNumber = (int)(((float)mouseXPosInGraph / getGraphWidth() ) * HistogramDisplay.NUM_HISTOGRAM_BUCKETS);
		final HistogramBucket[] buckets = histogram.getBuckets();
		if( bucketNumber >=0 && bucketNumber < buckets.length )
		{
//			log.debug( "For pos " + mouseXPosInGraph + " returning bucket " + bucketNumber );
			return buckets[bucketNumber];
		}
		return null;
	}
}
