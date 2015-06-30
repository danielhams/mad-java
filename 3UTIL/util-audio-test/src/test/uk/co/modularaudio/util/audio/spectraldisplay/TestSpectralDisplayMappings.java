package test.uk.co.modularaudio.util.audio.spectraldisplay;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LinearAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicDbAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicNaturalAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LinearFreqScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LogarithmicFreqScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestSpectralDisplayMappings extends TestCase
{
	private static Log log = LogFactory.getLog( TestSpectralDisplayMappings.class.getName() );

	private final static int NUM_TEST_BUCKETS = 100;

	private final static float MAX_VALUE_DB = -30.0f;
	private final static float MAX_VALUE = AudioMath.dbToLevelF( MAX_VALUE_DB );

	private final static float MAX_FREQ = 22050.0f;

	public void dtestLinearAmpScaleComputations() throws Exception
	{
		final LinearAmpScaleComputer lasc = new LinearAmpScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_VALUE, i );

			final int andBack = lasc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_VALUE, bucketRawValue );

			log.trace("AMP LINEAR For bucket " + i + " raw value=" +
					MathFormatter.slowFloatPrint( bucketRawValue, 7, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}
	}

	public void testLogAmpScaleComputations() throws Exception
	{
		log.debug("Max value DB is " + MAX_VALUE_DB + " which is " + MathFormatter.slowFloatPrint( MAX_VALUE, 7, false ) );
		final LogarithmicDbAmpScaleComputer lasc = new LogarithmicDbAmpScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_VALUE, i );

			final int andBack = lasc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_VALUE, bucketRawValue );

			final float bucketDbValue = AudioMath.levelToDbF( bucketRawValue );

			log.trace("AMP LOG-DB For bucket " + i + " raw value=(" +
					MathFormatter.slowFloatPrint( bucketRawValue, 7, false ) +
					") dbv(" +
					MathFormatter.slowFloatPrint( bucketDbValue, 7, false ) +
					") and back is " + andBack );

//			assertTrue( andBack == i );
		}
	}

	public void testLogAmpNaturalScaleComputations() throws Exception
	{
		final LogarithmicNaturalAmpScaleComputer lnasc = new LogarithmicNaturalAmpScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lnasc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_VALUE, i );

			final int andBack = lnasc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_VALUE, bucketRawValue );

			final float bucketDbValue = AudioMath.levelToDbF( bucketRawValue );

			log.trace("AMP LOG-NATURAL For bucket " + i + " raw value=(" +
					MathFormatter.slowFloatPrint( bucketRawValue, 7, false ) +
					") dbv(" +
					MathFormatter.slowFloatPrint( bucketDbValue, 7, false ) +
					") and back is " + andBack );

//			assertTrue( andBack == i );
		}
	}

	public void dtestLinearFreqScaleComputations() throws Exception
	{
		final LinearFreqScaleComputer lfsc = new LinearFreqScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lfsc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_FREQ, i );

			final int andBack = lfsc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_FREQ, bucketRawValue );

			log.trace("FREQ LINEAR For bucket " + i + " raw value=" +
					MathFormatter.slowFloatPrint( bucketRawValue, 6, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}
	}

	public void dtestLogFreqScaleComputations() throws Exception
	{
		final LogarithmicFreqScaleComputer lfsc = new LogarithmicFreqScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lfsc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_FREQ, i );

			final int andBack = lfsc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_FREQ, bucketRawValue );

			log.trace("FREQ LOGARITHMIC For bucket " + i + " raw value=" +
					MathFormatter.slowFloatPrint( bucketRawValue, 6, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}
	}

}
