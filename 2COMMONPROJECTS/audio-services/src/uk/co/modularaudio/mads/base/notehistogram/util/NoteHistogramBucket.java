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
