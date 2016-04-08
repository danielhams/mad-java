package uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.LongArrayList;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluatorAbstract.Accumulator.NumDeltasAndIterator;
import uk.co.modularaudio.util.math.FastMath;

public class NumBitsEvaluatorAbstract implements NumBitsEvaluator
{
	private static Log log = LogFactory.getLog( NumBitsEvaluatorAbstract.class.getName() );

	public final static int MAX_BITS = 62;

	public interface Accumulator
	{
		public class NumDeltasAndIterator
		{
			public NumDeltasAndIterator( final int numDeltas, final Iterator<Long> iterator )
			{
				this.numDeltas = numDeltas;
				this.iterator = iterator;
			}
			int numDeltas;
			Iterator<Long> iterator;
		}

		void reset( int numSignificantBits );

		void addValue( long sv );

		int getNumSourceValues();
		LongArrayList getAllSourceValues();

		NumDeltasAndIterator getSortedDeltaIterator();

		long getNumKeysAdded();

		long getNumTotalKeys();

		long getNumDeltas();
	};

	private int numSignificantBits;
	private long maxIntForBits;

	private final Accumulator accumulator;

	public NumBitsEvaluatorAbstract( final int numSignificantBits, final Accumulator deltaAccumulator)
		throws TooManyBitsException
	{
		this.accumulator = deltaAccumulator;
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

		accumulator.reset( numSignificantBits );
	}

	@Override
	public void addValue( final int numSignificantBits, final long sv ) throws TooManyBitsException
	{
		if( this.numSignificantBits != numSignificantBits )
		{
			reset( numSignificantBits );
		}

		accumulator.addValue( sv );
	}

	@Override
	public NumBitsAndConfidence getNumBitsAndConfidence()
	{
		final int numValues = accumulator.getNumSourceValues();
		final NumDeltasAndIterator ndai = accumulator.getSortedDeltaIterator();
		final int numDeltas = ndai.numDeltas;

		if( numValues < 10 || numDeltas < 1)
		{
			// Not enough values yet
			return new NumBitsAndConfidence( numSignificantBits, -1, 0.0f );
		}

		final Iterator<Long> dIter = ndai.iterator;

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
		final LongArrayList sourceVals = accumulator.getAllSourceValues();
		final TreeMap<Long,Integer> valueToCount = new TreeMap<Long,Integer>();
		for( int v = 0 ; v < sourceVals.size() ; ++v )
		{
			final long key = sourceVals.get( v );
			if( valueToCount.containsKey( key ) )
			{
				final int count = valueToCount.get( key );
				valueToCount.put( key, count + 1 );
			}
			else
			{
				valueToCount.put( key, 1 );
			}
		}
		final Long[] sortedKeys = valueToCount.keySet().toArray( new Long[valueToCount.size()] );
		for( int i = 0 ; i < n && i < sortedKeys.length ; ++i )
		{
			final long k = sortedKeys[i];
			final long numForVal = valueToCount.get( k );
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
		return accumulator.getNumKeysAdded();
	}

	@Override
	public long getNumTotalKeys()
	{
		return accumulator.getNumTotalKeys();
	}

	@Override
	public long getNumDeltas()
	{
		return accumulator.getNumDeltas();
	}
}
