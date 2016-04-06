package uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenLongIntHashMap;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluatorAbstract.Accumulator;

public class TreeMapAccumulator implements Accumulator
{

	private long maxIntForBits;

	private long numTotalKeys;
	private long numKeysAdded;

	private final OpenLongIntHashMap allKeys = new OpenLongIntHashMap();
	private final ArrayList<Long> noBoundaryKeys = new ArrayList<Long>();

	private final TreeMap<Long,Integer> deltas = new TreeMap<Long,Integer>();

	@Override
	public void reset( final int numSignificantBits )
	{
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow(  numSignificantBits );
		maxIntForBits = bMaxIntForBits.longValue() - 1;

		allKeys.clear();
		noBoundaryKeys.clear();
		deltas.clear();

		numTotalKeys = 0;
		numKeysAdded = 0;
	}

	private long[] insertNewValue( final long newValue )
	{
		final long[] retVal = new long[2];
		retVal[0] = -1;
		retVal[1] = -1;

		final int insertionIndex = Collections.binarySearch( noBoundaryKeys, newValue );

//		log.trace( "Insertion index is " + insertionIndex );

		if( insertionIndex <= 0 )
		{
			numKeysAdded++;

			final int realInsertionIndex = -(insertionIndex+1);
			if( realInsertionIndex > 0 )
			{
				retVal[0] = noBoundaryKeys.get(realInsertionIndex-1);
			}
			final int lastIndex = noBoundaryKeys.size() - 1;
			if( realInsertionIndex <= lastIndex )
			{
				retVal[1] = noBoundaryKeys.get(realInsertionIndex);
			}
			// We'll see
			noBoundaryKeys.add( realInsertionIndex, newValue );

		}
		else
		{
			// Already in there
		}

		return retVal;
	}

	private void refDelta( final long deltaToRef )
	{
		if( deltas.containsKey( deltaToRef ) )
		{
			final int count = deltas.get( deltaToRef );
			deltas.put( deltaToRef, count+1 );
		}
		else
		{
			deltas.put( deltaToRef, 1 );
		}
	}

	private void derefDelta( final long deltaToDeref )
	{
		final int count = deltas.get( deltaToDeref );
		if( count == 1 )
		{
			deltas.remove( deltaToDeref );
		}
		else
		{
			deltas.put( deltaToDeref, count-1 );
		}
	}

	@Override
	public void addValue( final long sv )
	{
		numTotalKeys++;
		final boolean existingKey = allKeys.containsKey( sv );
		if( existingKey )
		{
			allKeys.put( sv, allKeys.get( sv ) + 1 );
		}
		else
		{
			allKeys.put( sv, 1 );

			// Avoid boundary value bias skewing results.
			if( sv != 0 && sv != maxIntForBits )
			{
				final long[] neighbours = insertNewValue( sv );

				if( neighbours[0] != -1 )
				{
					if( neighbours[1] != -1 )
					{
						// We're in the middle of two values
						// need to remove the delta between them and insert
						// two new ones
						final long deltaToRemove = neighbours[1] - neighbours[0];

						derefDelta( deltaToRemove );


						refDelta( sv - neighbours[0] );
						refDelta( neighbours[1] - sv );
					}
					else
					{
						// We are the new "last value"
						refDelta( sv - neighbours[0] );
					}
				}
				else if( neighbours[1] != -1 ) // && neighbours[0] == -1
				{
					// We are the new "first value"
					refDelta( neighbours[1] - sv );
				}
				else
				{
					// First value, no delta to insert
				}
			}
		}
	}

	@Override
	public int getNumSourceValues()
	{
		return allKeys.size();
	}

	@Override
	public LongArrayList getAllSourceValues()
	{
		return allKeys.keys();
	}

	@Override
	public NumDeltasAndIterator getSortedDeltaIterator()
	{
		final int numDeltas = deltas.size();
//		final LongArrayList retVal = new LongArrayList( numDeltas );
//		for( final long l : deltas.keySet() )
//		{
//			retVal.add( l );
//		}
//		return retVal;
		final Iterator<Long> i = deltas.keySet().iterator();
		return new NumDeltasAndIterator( numDeltas, i );
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

	@Override
	public long getNumDeltas()
	{
		return deltas.size();
	}

}
