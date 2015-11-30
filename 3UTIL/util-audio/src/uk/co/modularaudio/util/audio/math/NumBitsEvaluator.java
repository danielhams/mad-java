package uk.co.modularaudio.util.audio.math;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.DoubleArrayList;
import org.apache.mahout.math.map.OpenDoubleLongHashMap;
import org.apache.mahout.math.set.OpenDoubleHashSet;

import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class NumBitsEvaluator
{
	private static Log log = LogFactory.getLog( NumBitsEvaluator.class.getName() );

	// Map from int bits to num
	private final OpenDoubleLongHashMap valueHashMap = new OpenDoubleLongHashMap();

	public void addValue( final float sv )
	{
		final double dv = sv;
		if( valueHashMap.containsKey( dv ) )
		{
			final long num = valueHashMap.get( dv );
			valueHashMap.put( dv, num+1 );
		}
		else
		{
			valueHashMap.put( dv, 1 );
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
		final DoubleArrayList keysArrayList = valueHashMap.keys();
		final double[] keys = keysArrayList.elements();
		final double[] sortedVals = new double[keys.length];
		System.arraycopy( keys, 0, sortedVals, 0, keys.length );

		Arrays.sort( sortedVals );
		final int numDeltas = sortedVals.length - 1;

		final double[] deltas = new double[numDeltas];

		for( int f = 0 ; f < numDeltas ; ++f )
		{
			deltas[f] = sortedVals[f+1] - sortedVals[f];
		}

		// Now uniq and sort the deltas to work out minimum delta
		final OpenDoubleHashSet deltaHashSet = new OpenDoubleHashSet();
		for( final double d : deltas )
		{
			deltaHashSet.add( d );
		}
		final double[] uniqDeltas = deltaHashSet.keys().elements();
		final int numUniqDeltas = uniqDeltas.length;
		final double[] sortedDeltas = new double[numUniqDeltas];
		System.arraycopy( uniqDeltas, 0, sortedDeltas, 0, numUniqDeltas );
		Arrays.sort( sortedDeltas );

		final double minDelta = sortedDeltas[0];
		final double numUniqValsForMinDeltaDouble = 1.0 / minDelta;
		final long numUniqValsForMinDelta = (long)numUniqValsForMinDeltaDouble;

		assert( minDelta != 0.0 );
		log.info( "Min delta is " + MathFormatter.slowDoublePrint( minDelta, 12, true ) );

		// Use a tenth of the min delta as the error bound for other deltas being a multiple
		final double maxError = 0.1;

		boolean deltasConform = true;

		for( int d = 1 ; d < numUniqDeltas ; ++d )
		{
			final double deltaToTest = sortedDeltas[d];
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
		final double[] sourceVals = valueHashMap.keys().elements();
		final double[] sortedVals = new double[sourceVals.length];
		System.arraycopy( sourceVals, 0, sortedVals, 0, sourceVals.length );
		Arrays.sort( sortedVals );
		for( int i = 0 ; i < n ; ++i )
		{
			final double k = sortedVals[i];
			final long numForVal = valueHashMap.get( k );
			log.debug("Have " + numForVal + " occurences of delta " + MathFormatter.slowDoublePrint( k, 8, true ) );
		}
	}

}
