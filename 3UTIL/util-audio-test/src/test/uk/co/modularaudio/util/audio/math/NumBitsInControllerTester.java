package test.uk.co.modularaudio.util.audio.math;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.audio.math.NumBitsEvaluator;
import uk.co.modularaudio.util.audio.math.NumBitsEvaluator.NumBitsAndConfidence;
import uk.co.modularaudio.util.audio.math.NumBitsEvaluatorAbstract;
import uk.co.modularaudio.util.audio.math.OldNumBitsEvaluatorRecalculating;
import uk.co.modularaudio.util.audio.math.OldNumBitsEvaluatorTreeMap;
import uk.co.modularaudio.util.audio.math.RecalculatingAccumulator;
import uk.co.modularaudio.util.audio.math.TooManyBitsException;
import uk.co.modularaudio.util.audio.math.TreeMapAccumulator;
import uk.co.modularaudio.util.timing.NanosTimestampFormatter;
import uk.co.modularaudio.util.timing.TestTimer;

public class NumBitsInControllerTester
{
	private static Log log = LogFactory.getLog( NumBitsInControllerTester.class.getName() );

//	private static final int NUM_TEST_VALUES = 60;
//	private static final int NUM_TEST_VALUES = 60 * 2;
//	private static final int NUM_TEST_VALUES = 300;
	private static final int NUM_TEST_VALUES = 1024;
//	private static final int NUM_TEST_VALUES = 2048;
//	private static final int NUM_TEST_VALUES = 4192;
//	private static final int NUM_TEST_VALUES = 8192;
//	private static final int NUM_TEST_VALUES = 8192 * 10;
//	private static final int NUM_TEST_VALUES = 8192 * 50;

//	private static final int NUM_SIGNIFICANT_BITS = 4;
//	private static final int NUM_SIGNIFICANT_BITS = 6;
//	private static final int NUM_SIGNIFICANT_BITS = 7;
//	private static final int NUM_SIGNIFICANT_BITS = 8;
//	private static final int NUM_SIGNIFICANT_BITS = 10;
//	private static final int NUM_SIGNIFICANT_BITS = 14;
	private static final int NUM_SIGNIFICANT_BITS = 16;
//	private static final int NUM_SIGNIFICANT_BITS = 20;
//	private static final int NUM_SIGNIFICANT_BITS = 32;
//	private static final int NUM_SIGNIFICANT_BITS = 48;
//	private static final int NUM_SIGNIFICANT_BITS = 60;

//	private static final int NUM_USED_BITS = 4;
//	private static final int NUM_USED_BITS = 5;
//	private static final int NUM_USED_BITS = 7;
//	private static final int NUM_USED_BITS = 10;
	private static final int NUM_USED_BITS = 14;
//	private static final int NUM_USED_BITS = 16;
//	private static final int NUM_USED_BITS = 20;
//	private static final int NUM_USED_BITS = 24;
//	private static final int NUM_USED_BITS = 32;

//	private final static int VALS_PER_CONF_CHECK = 1000;
	private final static int VALS_PER_CONF_CHECK = 1000 / 60;

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

		final Random r = new Random( System.nanoTime() );
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

	private NumBitsAndConfidence doNumBitsEvaluatorLoop(
			final String nbName,
			final NumBitsEvaluator numBitsEvaluator,
			final long[] sourceValuesToEvaluate,
			final boolean shouldLog ) throws TooManyBitsException
	{
		int numValuesSoFar = 0;

		final TestTimer timer = new TestTimer();

		timer.markBoundary( "Begin" );

		for( final long sv : sourceValuesToEvaluate )
		{
			numBitsEvaluator.addValue( NUM_SIGNIFICANT_BITS, sv );

			numValuesSoFar++;

			if( numValuesSoFar % VALS_PER_CONF_CHECK == 0 )
			{
				numBitsEvaluator.getNumBitsAndConfidence();
			}
		}

		timer.markBoundary( "Add Values" );

		if( shouldLog )
		{
			numBitsEvaluator.dumpNFirstUniqueValues( 100 );
		}

		final NumBitsAndConfidence numBitsAndConfidence = numBitsEvaluator.getNumBitsAndConfidence();
		final int numSignificantBits = numBitsAndConfidence.numSignificantBits;
		final int numDataBits = numBitsAndConfidence.numDataBits;
		final float confidence = numBitsAndConfidence.confidence;

		timer.markBoundary( "Made Estimate" );

		if( log.isInfoEnabled() )
		{
			if( confidence > 0.0f )
			{
				log.info( "Have enough values to make num bits estimate with data with " + numSignificantBits + " bits" );
				log.info( "After " + numValuesSoFar + " values num bits estimate is " + numDataBits +
						" with a confidence of " + confidence );
				final BigInteger bi = BigInteger.valueOf( 2 );
				final long numValuesForBits = bi.pow( numDataBits ).longValue();
				log.info( "This is " + numValuesForBits + " unique values");
			}
			else
			{
				log.info( "Failed to estimate number of bits." );
			}
		}

		timer.logTimes( nbName, log );

		log.info( "Loop inserted " + numValuesSoFar + " keys" );
		log.info( "Processed " + numBitsEvaluator.getNumTotalKeys() + " total keys" );
		log.info( "Processed " + numBitsEvaluator.getNumKeysAdded() + " non boundary keys" );
		log.info( "Processed " + numBitsEvaluator.getNumDeltas() + " deltas" );

		return numBitsAndConfidence;
	}

	@Test
	public void test() throws TooManyBitsException
	{
		log.debug("Doing it!");

		final long[] sourceValuesToEvaluate = generateValuesWithLimitedBits( NUM_SIGNIFICANT_BITS, NUM_USED_BITS );
		final NumBitsEvaluator ornbe = new OldNumBitsEvaluatorRecalculating( NUM_SIGNIFICANT_BITS );
		final NumBitsEvaluator otmnbe = new OldNumBitsEvaluatorTreeMap( NUM_SIGNIFICANT_BITS );

		final RecalculatingAccumulator ra = new RecalculatingAccumulator();
		final NumBitsEvaluator rnbe = new NumBitsEvaluatorAbstract( NUM_SIGNIFICANT_BITS, ra );

		final TreeMapAccumulator tma = new TreeMapAccumulator();
		final NumBitsEvaluator tmbe = new NumBitsEvaluatorAbstract( NUM_SIGNIFICANT_BITS, tma );

		long ornbeDiff = 0;
		long otmbeDiff = 0;
		long rnbeDiff = 0;
		long tmbeDiff = 0;
		long before = 0;
		long after = 0;

		for( int i = 0 ; i < 10 ; ++i )
		{

			before = System.nanoTime();
			doNumBitsEvaluatorLoop( "ORNBE", ornbe, sourceValuesToEvaluate, false );
			after = System.nanoTime();
			ornbeDiff = after - before;

			before = System.nanoTime();
			doNumBitsEvaluatorLoop( "OTNBE", otmnbe, sourceValuesToEvaluate, false );
			after = System.nanoTime();
			otmbeDiff = after - before;

			before = System.nanoTime();
			doNumBitsEvaluatorLoop( "NRNBE", rnbe, sourceValuesToEvaluate, false );
			after = System.nanoTime();
			rnbeDiff = after - before;

			before = System.nanoTime();
			doNumBitsEvaluatorLoop( "NTMBE", tmbe, sourceValuesToEvaluate, false );
			after = System.nanoTime();
			tmbeDiff = after - before;

			log.info( "ORIGRECAL(" + NanosTimestampFormatter.formatTimestampForLogging( ornbeDiff, false ) +
					")");
			log.info( "ORIGTREEM(" + NanosTimestampFormatter.formatTimestampForLogging( otmbeDiff, false ) +
					")");
			log.info( "ACCURECAL(" + NanosTimestampFormatter.formatTimestampForLogging( rnbeDiff, false ) +
					")");
			log.info( "ACCUTREEM(" + NanosTimestampFormatter.formatTimestampForLogging( tmbeDiff, false ) +
					")");

			ornbe.reset( NUM_SIGNIFICANT_BITS );
			otmnbe.reset( NUM_SIGNIFICANT_BITS );
			rnbe.reset( NUM_SIGNIFICANT_BITS );
			tmbe.reset( NUM_SIGNIFICANT_BITS );

		}

		// Finally do and assertion test that the results are the same
		final NumBitsAndConfidence ornr = doNumBitsEvaluatorLoop( "ORNBE", ornbe, sourceValuesToEvaluate, false );
		final NumBitsAndConfidence otnr = doNumBitsEvaluatorLoop( "OTNBE", otmnbe, sourceValuesToEvaluate, false );
		final NumBitsAndConfidence rnr = doNumBitsEvaluatorLoop( "NRNBE", rnbe, sourceValuesToEvaluate, false );
		final NumBitsAndConfidence tnr = doNumBitsEvaluatorLoop( "NTMBE", tmbe, sourceValuesToEvaluate, false );

		log.trace( "Orecal result = " + ornr.toString() );
		log.trace( "Otreem result = " + otnr.toString() );
		log.trace( "Arecal result = " + rnr.toString() );
		log.trace( "Atreem result = " + tnr.toString() );

		assertEquals( "NumBits results do not match", rnr, tnr );
	}

}
