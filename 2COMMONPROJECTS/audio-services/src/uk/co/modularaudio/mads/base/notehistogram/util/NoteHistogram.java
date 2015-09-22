package uk.co.modularaudio.mads.base.notehistogram.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NoteHistogram
{
	private static Log log = LogFactory.getLog( NoteHistogram.class.getName() );

	private final int numBuckets;
	private final int framesPerBucket;

	private final NoteHistogramBucket[] buckets;
	private int nonBucketCount;
	private int lowestDiff = Integer.MAX_VALUE;
	private int highestDiff = Integer.MIN_VALUE;

	public NoteHistogram( final int numBuckets,
			final int framesPerBucket )
	{
		this.numBuckets = numBuckets;
		this.framesPerBucket = framesPerBucket;

		buckets = new NoteHistogramBucket[numBuckets];

		for( int b = 0 ; b < numBuckets ; ++b )
		{
			buckets[b] = new NoteHistogramBucket( framesPerBucket * b,
					(framesPerBucket * (b + 1)) );
		}
	}

	public void addNoteDiff( final int noteDiffFrames )
	{
		if( noteDiffFrames < lowestDiff )
		{
			lowestDiff = noteDiffFrames;
		}
		if( noteDiffFrames > highestDiff )
		{
			highestDiff = noteDiffFrames;
		}
		for( int b = 0 ; b < numBuckets ; ++b )
		{
			if( buckets[b].addCountForBucket( noteDiffFrames ) )
			{
				return;
			}
		}

		nonBucketCount++;
	}

	public void reset()
	{
		for( int b = 0 ; b < numBuckets ; ++b )
		{
			buckets[b].reset();
		}
		nonBucketCount = 0;
		lowestDiff = Integer.MAX_VALUE;
		highestDiff = Integer.MIN_VALUE;
	}

	public void dumpStats()
	{
		for( final NoteHistogramBucket b : buckets )
		{
			log.debug( "Bucket (" + b.getBucketStartDiff() + "->" + b.getBucketEndDiff() +
					") has " + b.getBucketCount() + " entries" );
		}
		log.debug("Nonbucket has " + nonBucketCount );
		log.debug("The lowest seen was " + lowestDiff );
		log.debug("The highest seen was " + highestDiff );
	}
}
