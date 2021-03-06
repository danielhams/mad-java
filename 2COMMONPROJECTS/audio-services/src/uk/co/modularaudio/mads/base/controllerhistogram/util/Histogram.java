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

package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Histogram
{
	private static Log log = LogFactory.getLog( Histogram.class.getName() );

	private final int numBuckets;

	private int totalNumEvents;

	private final HistogramBucket[] buckets;
	private int nonBucketCount;
	private long lowestNanos = Long.MAX_VALUE;
	private long highestNanos = Long.MIN_VALUE;

	public interface HistogramListener
	{
		void receiveHistogramUpdate();
	};

	private final ArrayList<HistogramListener> listeners = new ArrayList<HistogramListener>();

	public Histogram( final int numBuckets,
			final long lastBucketUpperNanos )
	{
		this.numBuckets = numBuckets;

		buckets = new HistogramBucket[numBuckets];

		final double nanosPerBucket = lastBucketUpperNanos / numBuckets;

		for( int b = 0 ; b < numBuckets ; ++b )
		{
			final long bucketStartNanos = (long)(b * nanosPerBucket);
			final long bucketEndNanos = (long)((b+1) * nanosPerBucket);
			buckets[b] = new HistogramBucket( bucketStartNanos,
					bucketEndNanos );
		}
	}

	public void addNoteDiffNanos( final long noteDiffFrames )
	{
		if( noteDiffFrames < lowestNanos )
		{
			lowestNanos = noteDiffFrames;
		}
		if( noteDiffFrames > highestNanos )
		{
			highestNanos = noteDiffFrames;
		}

		boolean matchedBucket = false;
		for( int b = 0 ; b < numBuckets ; ++b )
		{
			if( buckets[b].addCountForBucket( noteDiffFrames ) )
			{
				matchedBucket = true;
				break;
			}
		}

		if( matchedBucket )
		{
			totalNumEvents++;
		}
		else
		{
			nonBucketCount++;
		}

		for( final HistogramListener hl : listeners )
		{
			hl.receiveHistogramUpdate();
		}
	}

	public void reset()
	{
		for( int b = 0 ; b < numBuckets ; ++b )
		{
			buckets[b].reset();
		}
		nonBucketCount = 0;
		lowestNanos = Long.MAX_VALUE;
		highestNanos = Long.MIN_VALUE;
		totalNumEvents = 0;
	}

	public void dumpStats()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Have " + totalNumEvents + " total events");
			for( final HistogramBucket b : buckets )
			{
				log.debug( "Bucket (" + b.getBucketStartNanos() + "->" + b.getBucketEndNanos() +
						") has " + b.getBucketCount() + " entries" );
			}
			log.debug("Nonbucket has " + nonBucketCount );
			log.debug("The lowest seen was " + lowestNanos );
			log.debug("The highest seen was " + highestNanos );
		}
	}

	public void addHistogramListener( final HistogramListener listener )
	{
		listeners.add( listener );
	}

	public int getNumTotalEvents()
	{
		return totalNumEvents;
	}

	public HistogramBucket[] getBuckets()
	{
		return buckets;
	}

	public HistogramBucket getBucketForNanos( final long nanos )
	{
		for( final HistogramBucket b : buckets )
		{
			if( b.getBucketStartNanos() <= nanos && b.getBucketEndNanos() > nanos )
			{
				return b;
			}
		}
		return null;
	}
}
