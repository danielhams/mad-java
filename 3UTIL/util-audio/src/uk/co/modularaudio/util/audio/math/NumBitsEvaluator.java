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
			valueHashMap.put( sv, 1 );
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
		final long[] sortedVals = new long[keys.length];
		System.arraycopy( keys, 0, sortedVals, 0, keys.length );

		Arrays.sort( sortedVals );
		final int numDeltas = sortedVals.length - 1;

		final long[] deltas = new long[numDeltas];

		for( int f = 0 ; f < numDeltas ; ++f )
		{
			deltas[f] = sortedVals[f+1] - sortedVals[f];
		}

		// Now uniq and sort the deltas to work out minimum delta
		final OpenLongHashSet deltaHashSet = new OpenLongHashSet();
		for( final long d : deltas )
		{
			deltaHashSet.add( d );
		}
		final long[] uniqDeltas = deltaHashSet.keys().elements();
		final int numUniqDeltas = uniqDeltas.length;
		final long[] sortedDeltas = new long[numUniqDeltas];
		System.arraycopy( uniqDeltas, 0, sortedDeltas, 0, numUniqDeltas );
		Arrays.sort( sortedDeltas );

		final long minDelta = sortedDeltas[0];
		final double numUniqValsForMinDeltaDouble = Long.MAX_VALUE / minDelta;
		final long numUniqValsForMinDelta = (long)numUniqValsForMinDeltaDouble;

		assert( minDelta != 0 );
		log.info( "Min delta is " + minDelta );

		// Use a tenth of the min delta as the error bound for other deltas being a multiple
		final double maxError = 0.1;

		boolean deltasConform = true;

		for( int d = 1 ; d < numUniqDeltas ; ++d )
		{
			final long deltaToTest = sortedDeltas[d];
			final double numMinDeltas = deltaToTest / minDelta;

			final double remainder = numMinDeltas % 1;
			if( remainder > maxError )
			{
//				log.error( "For delta of " + MathFormatter.slowDoublePrint( deltaToTest, 12, true ) );
//				log.error( "Delta remainder of " + MathFormatter.slowDoublePrint( remainder, 12, true ) +
//						" when maxError is " + MathFormatter.slowDoublePrint( maxError, 12, true ) );
				deltasConform = false;
//				break;
			}
		}

		// Now convert num unique values into number of bits
		final int numBits = FastMath.log2( numUniqValsForMinDelta+1 );
		if( log.isDebugEnabled() )
		{
			log.debug( "Estimated number of bits at " + numBits );
		}

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
