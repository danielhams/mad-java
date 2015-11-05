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

package uk.co.modularaudio.mads.base.notehistogram.util;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NoteHistogram
{
	private static Log log = LogFactory.getLog( NoteHistogram.class.getName() );

	private final int numBuckets;

	private int totalNumEvents;

	private final NoteHistogramBucket[] buckets;
	private int nonBucketCount;
	private int lowestDiff = Integer.MAX_VALUE;
	private int highestDiff = Integer.MIN_VALUE;

	public interface HistogramListener
	{
		void receiveHistogramUpdate();
	};

	private final ArrayList<HistogramListener> listeners = new ArrayList<HistogramListener>();

	public NoteHistogram( final int numBuckets,
			final int framesPerBucket )
	{
		this.numBuckets = numBuckets;

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
		lowestDiff = Integer.MAX_VALUE;
		highestDiff = Integer.MIN_VALUE;
		totalNumEvents = 0;
	}

	public void dumpStats()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Have " + totalNumEvents + " total events");
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

	public void addHistogramListener( final HistogramListener listener )
	{
		listeners.add( listener );
	}

	public int getNumTotalEvents()
	{
		return totalNumEvents;
	}

	public NoteHistogramBucket[] getBuckets()
	{
		return buckets;
	}
}
