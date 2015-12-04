package uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenLongIntHashMap;
import org.apache.mahout.math.set.OpenLongHashSet;

import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class NumBitsEvaluator
{
	private static Log log = LogFactory.getLog( NumBitsEvaluator.class.getName() );

	// Map from int bits to num
	private final OpenLongIntHashMap valueHashMap = new OpenLongIntHashMap();

	private int numSignificantBits;

	public NumBitsEvaluator( final int numSignificantBits )
	{
		reset( numSignificantBits );
	}

	public final void reset( final int numSignificantBits )
	{
		this.numSignificantBits = numSignificantBits;
	}

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
	}

	public TwoTuple<Integer,Float> getNumBitsAndConfidence()
	{
		if( valueHashMap.size() < 10 )
		{
			// Not enough values yet
			return new TwoTuple<>( -1, 0.0f );
		}
		// Work out what the min delta between values is and check that
		// all deltas are a multiple of that min value
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow(  numSignificantBits );
		final long maxIntForBits = bMaxIntForBits.longValue() - 1;

		// First eliminate the zeroth and fully set values from having deltas
		// computed to get around fudging done by equipment without
		// the full bit range
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
			else
			{
				log.trace( "Eliminated boundary value " + oneKey );
				log.trace( "This is bitset " + longToBitsetString( oneKey ) );
			}
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
		long minDelta = Long.MAX_VALUE;

//		log.trace( "Computing deltas" );

		for( int f = 0 ; f < numDeltas ; ++f )
		{
			final long testDelta = sortedVals[f+1] - sortedVals[f];
			if( testDelta > 0 && testDelta < minDelta )
			{
				minDelta = testDelta;
			}
			uniqDeltas.add( testDelta );
		}

		assert( minDelta != 0 );
//		log.trace( "Min delta is " + minDelta );

		final int numUniqDeltas = uniqDeltas.size();

		final LongArrayList uniqDeltaArrayList = uniqDeltas.keys();
		final long[] uniqDeltaArray = uniqDeltaArrayList.elements();

//		log.trace( "Found " + numUniqDeltas + " unique deltas minDelta=" + minDelta );
//		for( int i = 0 ; i < numUniqDeltas ; ++i )
//		{
//			log.trace( "UniqueDelta[" + i + "]=" + uniqDeltaArray[i] );
//		}

		// Use a tenth of the min delta as the error bound for other deltas being a multiple
		final double maxError = 0.1;

		boolean deltasConform = true;

//		log.trace( "Testing delta conformity" );

		double curMinDelta = minDelta;
		float confidence = 1.0f;
		int divisor = 2;

		for( int d = 1 ; d < numUniqDeltas ; ++d )
		{
			final long deltaToTest = uniqDeltaArray[d];

			double numMinDeltas = (deltaToTest) / curMinDelta;
			double remainder = numMinDeltas % 1;
			do
			{
				if( remainder > maxError )
				{
					if( divisor < 8 )
					{
						curMinDelta = minDelta / divisor;
						divisor++;
						confidence = confidence / 2;
					}
					else
					{
						deltasConform = false;
						break;
					}
				}
				else
				{
					break;
				}
				numMinDeltas = (deltaToTest) / curMinDelta;
				remainder = numMinDeltas % 1;
			}
			while( remainder > maxError );
		}
//		log.trace( "Calculating num bits" );
		double numUniqValsForMinDeltaDouble = ((double)maxIntForBits) / curMinDelta;
		long numUniqValsForMinDelta = (long)numUniqValsForMinDeltaDouble;

		// Now convert num unique values into number of bits
		final int numBits = FastMath.log2( numUniqValsForMinDelta+1 );
//		if( log.isTraceEnabled() )
//		{
//			log.trace( "Estimated number of bits at " + numBits );
//		}

		if( !deltasConform )
		{
			return new TwoTuple<>( -1, 0.0f );
		}
		else
		{
			return new TwoTuple<>( numBits, confidence );
		}
	}

	public void dumpNFirstUniqueValues( final int n )
	{
		final long[] sourceVals = valueHashMap.keys().elements();
		final long[] sortedVals = new long[sourceVals.length];
		System.arraycopy( sourceVals, 0, sortedVals, 0, sourceVals.length );
		Arrays.sort( sortedVals );
		for( int i = 0 ; i < n && i < sourceVals.length ; ++i )
		{
			final long k = sortedVals[i];
			final long numForVal = valueHashMap.get( k );
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
}
