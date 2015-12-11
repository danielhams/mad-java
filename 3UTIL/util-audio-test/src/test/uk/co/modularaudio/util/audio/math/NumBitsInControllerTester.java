package test.uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluator;
import uk.co.modularaudio.util.audio.math.TooManyBitsException;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class NumBitsInControllerTester
{
	private static Log log = LogFactory.getLog( NumBitsInControllerTester.class.getName() );

//	private static final int NUM_TEST_VALUES = 60;
//	private static final int NUM_TEST_VALUES = 60 * 2;
//	private static final int NUM_TEST_VALUES = 300;
	private static final int NUM_TEST_VALUES = 1024;
//	private static final int NUM_TEST_VALUES = 4192;
//	private static final int NUM_TEST_VALUES = 8192;
//	private static final int NUM_TEST_VALUES = 8192 * 10;

//	private static final int NUM_SIGNIFICANT_BITS = 4;
//	private static final int NUM_SIGNIFICANT_BITS = 6;
//	private static final int NUM_SIGNIFICANT_BITS = 7;
//	private static final int NUM_SIGNIFICANT_BITS = 8;
//	private static final int NUM_SIGNIFICANT_BITS = 10;
//	private static final int NUM_SIGNIFICANT_BITS = 14;
//	private static final int NUM_SIGNIFICANT_BITS = 16;
//	private static final int NUM_SIGNIFICANT_BITS = 20;
//	private static final int NUM_SIGNIFICANT_BITS = 32;
	private static final int NUM_SIGNIFICANT_BITS = 48;
//	private static final int NUM_SIGNIFICANT_BITS = 60;

//	private static final int NUM_USED_BITS = 4;
//	private static final int NUM_USED_BITS = 5;
//	private static final int NUM_USED_BITS = 7;
//	private static final int NUM_USED_BITS = 10;
	private static final int NUM_USED_BITS = 14;
//	private static final int NUM_USED_BITS = 20;
//	private static final int NUM_USED_BITS = 24;
//	private static final int NUM_USED_BITS = 32;

	private long[] generateValuesWithLimitedBits( final int significantBits, final int usedBits )
		throws TooManyBitsException
	{
		final int numShiftBits = significantBits - usedBits;
		if( significantBits > 63 || usedBits > significantBits )
		{
			throw new TooManyBitsException( "Cannot do more than 63 bits and used bits must be < significant bits" );
		}
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxSigIntForBits = bi.pow( usedBits );
		final long maxSigIntForBits = bMaxSigIntForBits.longValue() - 1;

		final BigInteger bMaxTotIntForBits = bi.pow( significantBits );
		final long maxTotIntForBits = bMaxTotIntForBits.longValue() - 1;

		final Random r = new Random();
		final long[] retVal = new long[ NUM_TEST_VALUES ];

		int writePos = 0;

		while( writePos < NUM_TEST_VALUES )
		{
			// Generate random normalised val between 0 and 1
			// then quantise to number of required bits
			// and scale back to normalised float
			final double randVal = r.nextDouble();
//			log.trace("Using rand of " + randVal );

			final double scaledValue = randVal * maxSigIntForBits;

			//final long asLong = Math.round(scaledValue);
			final long asLong = (long)(scaledValue + 0.5);

			if( asLong == maxSigIntForBits )
			{
				// Simulate devices fudging how many bits they send by setting
				// the last entry (1.0) to fully set bits for the entire bit range
				retVal[writePos++] = maxTotIntForBits;
			}
			else
			{
				final long shiftedLong = asLong << numShiftBits;
				retVal[writePos++] = shiftedLong;
			}
		}

		return retVal;
	}

	@Test
	public void test() throws TooManyBitsException
	{
		log.debug("Doing it!");

		final long[] sourceValuesToEvaluate = generateValuesWithLimitedBits( NUM_SIGNIFICANT_BITS, NUM_USED_BITS );

		final NumBitsEvaluator numBitsEvaluator = new NumBitsEvaluator( NUM_SIGNIFICANT_BITS );
		int numValuesSoFar = 0;

		for( final long sv : sourceValuesToEvaluate )
		{
			numBitsEvaluator.addValue( NUM_SIGNIFICANT_BITS, sv );

			numValuesSoFar++;
		}

		numBitsEvaluator.dumpNFirstUniqueValues( 100 );

		final TwoTuple<Integer,Float> numBitsAndConfidence = numBitsEvaluator.getNumBitsAndConfidence();
		final int numBits = numBitsAndConfidence.getHead();
		final float confidence = numBitsAndConfidence.getTail();

		if( confidence > 0.0f )
		{
			log.info( "Have enough values to make num bits estimate" );
			log.info( "After " + numValuesSoFar + " values num bits estimate is " + numBits +
					" with a confidence of " + confidence );
			final BigInteger bi = BigInteger.valueOf( 2 );
			final long numValuesForBits = bi.pow( numBits ).longValue();
			log.info( "This is " + numValuesForBits + " unique values");
		}
		else
		{
			log.info( "Failed to estimate number of bits." );
		}

//		log.info( "About to do 1000 calls" );
//		for( int i = 0 ; i < 1000 ; ++i )
//		{
//			numBitsAndConfidence = numBitsEvaluator.getNumBitsAndConfidence();
//		}
//		log.info( "Done 1000 calls" );
	}

}
