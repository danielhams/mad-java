package uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenLongIntHashMap;
import org.apache.mahout.math.set.OpenLongHashSet;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluatorAbstract.Accumulator;

public class RecalculatingAccumulator implements Accumulator
{
	// Map from int bits to num
	private final OpenLongIntHashMap valueHashMap = new OpenLongIntHashMap();

	private long maxIntForBits;
	
	private long numTotalKeys;
	private long numKeysAdded;

	@Override
	public void reset( final int numSignificantBits )
	{
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow(  numSignificantBits );
		maxIntForBits = bMaxIntForBits.longValue() - 1;

		valueHashMap.clear();
		
		numTotalKeys = 0;
		numKeysAdded = 0;
	}

	@Override
	public void addValue( final long sv )
	{
		if( valueHashMap.containsKey( sv ) )
		{
			final int num = valueHashMap.get( sv );
			valueHashMap.put( sv, num+1 );
		}
		else
		{
			valueHashMap.put( sv, 1 );
		}
		numTotalKeys++;
		numKeysAdded++;
	}

	@Override
	public int getNumSourceValues()
	{
		return valueHashMap.size();
	}

	@Override
	public LongArrayList getAllSourceValues()
	{
		return valueHashMap.keys();
	}

	@Override
	public NumDeltasAndIterator getSortedDeltaIterator()
	{
		final LongArrayList keysArrayList = valueHashMap.keys();
		final int numOrigKeys = keysArrayList.size();
//		log.trace( "NumOrigKeys=" + numOrigKeys );
		final LongArrayList noBoundariesKeysList = new LongArrayList();
		for( int i = 0 ; i < numOrigKeys ; ++i )
		{
			final long oneKey = keysArrayList.get( i );
			if( oneKey != 0 &&
					oneKey != maxIntForBits)
			{
				noBoundariesKeysList.add( oneKey );
			}
//			else if( log.isTraceEnabled() )
//			{
//				log.trace( "Eliminated boundary value " + oneKey );
//				log.trace( "This is bitset " + longToBitsetString( oneKey ) );
//			}
		}
		final int numNoBoundariesKeys = noBoundariesKeysList.size();
//		log.trace( "NumNoBoundariesKeys=" + numNoBoundariesKeys );

		final long[] noBoundariesKeys = noBoundariesKeysList.toArray( new long[numNoBoundariesKeys] );

//		log.trace( "Sorting values" );

		final long[] sortedVals = new long[numNoBoundariesKeys];
		System.arraycopy( noBoundariesKeys, 0, sortedVals, 0, numNoBoundariesKeys );
		Arrays.sort( sortedVals );

		final int numDeltas = numNoBoundariesKeys - 1;

		final OpenLongHashSet uniqDeltas = new OpenLongHashSet( numDeltas );

//		log.trace( "Computing deltas" );

		for( int f = 0 ; f < numDeltas ; ++f )
		{
			final long testDelta = sortedVals[f+1] - sortedVals[f];
			uniqDeltas.add( testDelta );
		}

//		log.trace( "Min delta is " + minDelta );

		final LongArrayList deltas = uniqDeltas.keys();
		deltas.sort();

		final Iterator<Long> di = new Iterator<Long>()
		{
			int curIndex = 0;
			int size = deltas.size();
			LongArrayList ds = deltas;

			@Override
			public Long next()
			{
				return deltas.get( curIndex++ );
			}

			@Override
			public boolean hasNext()
			{
				return curIndex < size;
			}
		};
		return new NumDeltasAndIterator( deltas.size(), di );
	}

	@Override
	public long getNumKeysAdded()
	{
		return numKeysAdded;
	}

	@Override
	public long getNumTotalKeys()
	{
		return numTotalKeys;
	}

}
