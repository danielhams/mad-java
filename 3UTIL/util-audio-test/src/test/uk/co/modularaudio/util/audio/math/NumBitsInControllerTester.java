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

	private static final int NUM_TEST_VALUES = 512;

	private static final int NUM_BITS = 20;

	private final static boolean isEven( final long v )
	{
		return (v % 2) == 0;
	}

	private final static long pow( final long a, final int b )
	{
	    if( b == 0 )        return 1;
	    if( b == 1 )        return a;
	    if( isEven( b ) )   return     pow( a * a, b/2 ); //even a=(a^2)^b/2
	    else                return a * pow( a * a, b/2 ); //odd  a=a*(a^2)^b/2

	}

	private float[] generateValuesWithLimitedBits( final int numBits )
	{
		final BigInteger bi = BigInteger.valueOf( 2 );
		final BigInteger bMaxIntForBits = bi.pow(  numBits );
		final long maxIntForBits = bMaxIntForBits.longValue() - 1;
		final Random r = new Random();
		final float[] retVal = new float[ NUM_TEST_VALUES ];

		for( int i = 0 ; i < NUM_TEST_VALUES ; ++i )
		{
			// Generate random normalised val between 0 and 1
			// then quantise to number of required bits
			// and scale back to normalised float
			final double randVal = r.nextDouble();

			final double scaledValue = randVal * maxIntForBits;

			final long asLong = Math.round(scaledValue);

			final double backAsNormVal = ((double)asLong) / maxIntForBits;

			retVal[i] = (float)backAsNormVal;
		}

		return retVal;
	}

	@Test
	public void test()
	{
		log.debug("Doing it!");

		final float[] sourceValuesToEvaluate = generateValuesWithLimitedBits( NUM_BITS );

		final NumBitsEvaluator numBitsEvaluator = new NumBitsEvaluator();
		int numValuesSoFar = 0;

		for( final float sv : sourceValuesToEvaluate )
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
		}
		else
		{
			log.info( "Failed to estimate number of bits" );
		}
	}

}
