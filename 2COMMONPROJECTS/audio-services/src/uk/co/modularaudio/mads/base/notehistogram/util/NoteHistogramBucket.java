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

public class NoteHistogramBucket
{
	private int bucketCount;

	private final int bucketStartDiff;
	private final int bucketEndDiff;

	public NoteHistogramBucket( final int bucketStartDiff,
			final int bucketEndDiff )
	{
		this.bucketStartDiff = bucketStartDiff;
		this.bucketEndDiff = bucketEndDiff;
	}

	public boolean addCountForBucket( final int timeDiff )
	{
		if( timeDiff >= bucketStartDiff &&
				timeDiff < bucketEndDiff )
		{
			bucketCount++;
			return true;
		}
		else
		{
			return false;
		}
	}

	public int getBucketCount()
	{
		return bucketCount;
	}

	public int getBucketStartDiff()
	{
		return bucketStartDiff;
	}

	public int getBucketEndDiff()
	{
		return bucketEndDiff;
	}

	public void reset()
	{
		bucketCount = 0;
	}
}
