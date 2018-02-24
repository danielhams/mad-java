/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongIntHashMap;

import uk.co.modularaudio.util.math.FastMath;

public class OldNumBitsEvaluatorTreeMap implements NumBitsEvaluator
{
	private static Log log = LogFactory.getLog( OldNumBitsEvaluatorTreeMap.class.getName() );

	public final static int MAX_BITS = 63;

	private final OpenLongIntHashMap allKeys = new OpenLongIntHashMap();
	private final ArrayList<Long> noBoundaryKeys = new ArrayList<Long>();

	private final TreeMap<Long,Integer> deltas = new TreeMap<Long,Integer>();

	private int numSignificantBits;
	private long maxIntForBits;

	private long numTotalKeys;
	private long numKeysAdded;

	public OldNumBitsEvaluatorTreeMap( final int numSignificantBits ) throws TooManyBitsException
	{
		reset( numSignificantBits );
	}

	@Override
	public final void reset( final int numSignificantBits ) throws TooManyBitsException
	{
		if( numSignificantBits > MAX_BITS )
		{
			throw new TooManyBitsException( "Max bits exceeded - only support up to " + MAX_BITS );
		}
		if( log.isTraceEnabled() )
		{
			log.trace("Resetting num significant bits to " + numSignificantBits );
		}
		this.numSignificantBits = numSignificantBits;
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow( this.numSignificantBits );
		maxIntForBits = bMaxIntForBits.longValue() - 1;

		allKeys.clear();
		noBoundaryKeys.clear();
		deltas.clear();

		numTotalKeys = 0;
		numKeysAdded = 0;
	}

	@SuppressWarnings("boxing")
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

	@SuppressWarnings("boxing")
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

	@SuppressWarnings("boxing")
	private void derefDelta( final long deltaToDeref )
	{
		final int count = deltas.get( deltaToDeref );
		if( count == 1 )
		{
			deltas.remove( deltaToDeref );
		}
		else
		{
			deltas.put( deltaToDeref, count - 1 );
		}
	}

	@Override
	public void addValue( final int numSignificantBits, final long sv ) throws TooManyBitsException
	{
		numTotalKeys++;

		if( this.numSignificantBits != numSignificantBits )
		{
			reset( numSignificantBits );
		}

		final boolean existingKey = allKeys.containsKey( sv );
		if( existingKey )
		{
			allKeys.put( sv, allKeys.get( sv ) );
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

	@SuppressWarnings("boxing")
	@Override
	public NumBitsAndConfidence getNumBitsAndConfidence()
	{
		final int numKeys = allKeys.size();

		if( numKeys < 10 )
		{
			// Not enough values yet
			return new NumBitsAndConfidence( numSignificantBits, -1, 0.0f );
		}

		final Set<Long> deltaKeySet = deltas.keySet();
		final int deltaKeySetSize = deltaKeySet.size();
		if( deltaKeySetSize < 1 )
		{
			throw new RuntimeException("Failed sanity check");
		}
		final Iterator<Long> dIter = deltaKeySet.iterator();

		// Use a tenth of the min delta as the error bound for other deltas being a multiple
		final double maxError = 0.1;

		boolean deltasConform = true;

//		log.trace( "Testing delta conformity" );

		final long minDelta = dIter.next();
		double curMinDelta = minDelta;
		float confidence = 1.0f;
		int divisor = 2;

		while( dIter.hasNext() )
		{
			final long deltaToTest = dIter.next();

			double numMinDeltas = (deltaToTest) / curMinDelta;
			double remainder = numMinDeltas % 1;

			while( remainder > maxError )
			{
				if( divisor >= 8 )
				{
					deltasConform = false;
					break;
				}
				curMinDelta = minDelta / divisor;
				divisor++;
				confidence = confidence / 2;

				numMinDeltas = (deltaToTest) / curMinDelta;
				remainder = numMinDeltas % 1;
			}
		}
//		log.trace( "Calculating num bits" );
		final double numUniqValsForMinDeltaDouble = (maxIntForBits) / curMinDelta;
		final long numUniqValsForMinDelta = (long)numUniqValsForMinDeltaDouble;

		// Now convert num unique values into number of bits
		final int numBits = FastMath.log2( numUniqValsForMinDelta+1 );

		if( !deltasConform )
		{
			return new NumBitsAndConfidence( numSignificantBits, -1, 0.0f );
		}
		else
		{
			return new NumBitsAndConfidence( numSignificantBits, numBits, confidence );
		}
	}

	@Override
	public void dumpNFirstUniqueValues( final int n )
	{
		final long[] sourceVals = allKeys.keys().elements();
		final long[] sortedVals = new long[sourceVals.length];
		System.arraycopy( sourceVals, 0, sortedVals, 0, sourceVals.length );
		Arrays.sort( sortedVals );
		for( int i = 0 ; i < n && i < sourceVals.length ; ++i )
		{
			final long k = sortedVals[i];
			final long numForVal = allKeys.get( k );
			log.debug("Have " + numForVal + " occurences of value " + k );

			log.debug("This is bitset " + longToBitsetString( k ) );
		}
	}

	private String longToBitsetString( final long v )
	{
		final StringBuilder sb = new StringBuilder();
		for( int b = 0 ; b < 64 ; ++b )
		{
			final long testVal = (0x01L << (63-b));
			sb.append( (v & testVal) == 0 ? '0' : '1' );
		}
		return sb.toString();
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
