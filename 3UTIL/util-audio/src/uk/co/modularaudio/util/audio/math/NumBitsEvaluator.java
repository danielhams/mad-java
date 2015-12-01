package uk.co.modularaudio.util.audio.math;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenLongLongHashMap;
import org.apache.mahout.math.set.OpenLongHashSet;

import uk.co.modularaudio.util.math.FastMath;

public class NumBitsEvaluator
{
	private static Log log = LogFactory.getLog( NumBitsEvaluator.class.getName() );

	// Map from int bits to num
	private final OpenLongLongHashMap valueHashMap = new OpenLongLongHashMap();

	public void addValue( final long sv )
	{
		if( valueHashMap.containsKey( sv ) )
		{
			final long num = valueHashMap.get( sv );
			valueHashMap.put( sv, num+1 );
		}
		else
		{
			valueHashMap.put( sv, 1L );
		}
	}

	public int getNumBitsEstimate()
	{
		if( valueHashMap.size() < 10 )
		{
			// Not enough values yet
			return -1;
		}
		// Work out what the min delta between values is and check that
		// all deltas are a multiple of that min value
		final LongArrayList keysArrayList = valueHashMap.keys();
		final long[] keys = keysArrayList.elements();

//		log.trace( "Sorting values" );

		final long[] sortedVals = new long[keys.length];
		System.arraycopy( keys, 0, sortedVals, 0, keys.length );
		Arrays.sort( sortedVals );

		final int numDeltas = keys.length - 1;

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

//		log.trace( "Found " + numUniqDeltas + " unique deltas" );

		final double numUniqValsForMinDeltaDouble = ((double)Long.MAX_VALUE) / minDelta;
		final long numUniqValsForMinDelta = (long)numUniqValsForMinDeltaDouble;

		// Use a tenth of the min delta as the error bound for other deltas being a multiple
		final double maxError = 0.1;

		boolean deltasConform = true;

		final LongArrayList uniqDeltaArrayList = uniqDeltas.keys();
		final long[] uniqDeltaArray = uniqDeltaArrayList.elements();

//		log.trace( "Testing delta conformity" );

		for( int d = 1 ; d < numUniqDeltas ; ++d )
		{
			final long deltaToTest = uniqDeltaArray[d];
			final double numMinDeltas = ((double)deltaToTest) / minDelta;

			final double remainder = numMinDeltas % 1;
			if( remainder > maxError )
			{
				deltasConform = false;
			}
		}
//		log.trace( "Calculating num bits" );

		// Now convert num unique values into number of bits
		final int numBits = FastMath.log2( numUniqValsForMinDelta+1 );
//		if( log.isTraceEnabled() )
//		{
//			log.trace( "Estimated number of bits at " + numBits );
//		}

		if( !deltasConform )
		{
			return -1;
		}
		else
		{
			return numBits;
		}
	}

	public void dumpNFirstUniqueValues( final int n )
	{
		final long[] sourceVals = valueHashMap.keys().elements();
		final long[] sortedVals = new long[sourceVals.length];
		System.arraycopy( sourceVals, 0, sortedVals, 0, sourceVals.length );
		Arrays.sort( sortedVals );
		for( int i = 0 ; i < n ; ++i )
		{
			final long k = sortedVals[i];
			final long numForVal = valueHashMap.get( k );
			log.debug("Have " + numForVal + " occurences of value " + k );
		}
	}
}
