package test.uk.co.modularaudio.util.audio.math;

import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluator;

public class NumBitsInControllerTester
{
	private static Log log = LogFactory.getLog( NumBitsInControllerTester.class.getName() );

	private static final int NUM_TEST_VALUES = 60 * 2;

	private static final int NUM_BITS = 14;

	private long[] generateValuesWithLimitedBits( final int numBits )
	{
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow(  numBits );
		final long maxIntForBits = bMaxIntForBits.longValue() - 1;
		final int numToShift = 63 - numBits;
		final Random r = new Random();
		final long[] retVal = new long[ NUM_TEST_VALUES ];

		for( int i = 0 ; i < NUM_TEST_VALUES ; ++i )
		{
			// Generate random normalised val between 0 and 1
			// then quantise to number of required bits
			// and scale back to normalised float
			final double randVal = r.nextDouble();

			final double scaledValue = randVal * maxIntForBits;

			//final long asLong = Math.round(scaledValue);
			final long asLong = (long)scaledValue;

			// And shift to fill the signed space of long

			final long shiftedLong = asLong << numToShift;

			retVal[i] = shiftedLong;
		}

		return retVal;
	}

	@Test
	public void test()
	{
		log.debug("Doing it!");

		final long[] sourceValuesToEvaluate = generateValuesWithLimitedBits( NUM_BITS );

		final NumBitsEvaluator numBitsEvaluator = new NumBitsEvaluator();
		int numValuesSoFar = 0;

		for( final long sv : sourceValuesToEvaluate )
		{
			numBitsEvaluator.addValue( sv );

			numValuesSoFar++;
		}

		numBitsEvaluator.dumpNFirstUniqueValues( 10 );

		final int numBitsEstimate = numBitsEvaluator.getNumBitsEstimate();

		if( numBitsEstimate != -1 )
		{
			log.info( "Have enough values to make num bits estimate" );
			log.info( "After " + numValuesSoFar + " values num bits estimate is " + numBitsEstimate );
			final BigInteger bi = BigInteger.valueOf( 2 );
			final long numValuesForBits = bi.pow( numBitsEstimate ).longValue();
			log.info( "This is " + numValuesForBits + " unique values");
		}
		else
		{
			log.info( "Failed to estimate number of bits" );
		}

//		log.info( "About to do 1000 calls" );
//		for( int i = 0 ; i < 1000 ; ++i )
//		{
//			numBitsEstimate = numBitsEvaluator.getNumBitsEstimate();
//		}
//		log.info( "Done 1000 calls" );
	}

}
