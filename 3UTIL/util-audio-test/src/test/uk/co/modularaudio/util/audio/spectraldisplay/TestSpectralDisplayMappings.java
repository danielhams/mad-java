package test.uk.co.modularaudio.util.audio.spectraldisplay;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LinearAmpScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.ampscale.LogarithmicAmpScaleComputer;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestSpectralDisplayMappings extends TestCase
{
	private static Log log = LogFactory.getLog( TestSpectralDisplayMappings.class.getName() );

	private final static int NUM_TEST_BUCKETS = 100;

	private final static float MAX_VALUE_DB = -30.0f;
	private final static float MAX_VALUE = AudioMath.dbToLevelF( MAX_VALUE_DB );

	public void testLinearAmpScaleComputations() throws Exception
	{
		final LinearAmpScaleComputer lasc = new LinearAmpScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_VALUE, i );

			final int andBack = lasc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_VALUE, bucketRawValue );

			log.trace("LINEAR For bucket " + i + " raw value=" +
					MathFormatter.fastFloatPrint( bucketRawValue, 7, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}
	}

	public void testLogAmpScaleComputations() throws Exception
	{
		log.debug("Max value DB is " + MAX_VALUE_DB + " which is " + MathFormatter.slowFloatPrint( MAX_VALUE, 7, false ) );
		final LogarithmicAmpScaleComputer lasc = new LogarithmicAmpScaleComputer();

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS, MAX_VALUE, i );

			final int andBack = lasc.rawToMappedBucket( NUM_TEST_BUCKETS, MAX_VALUE, bucketRawValue );

			log.trace("LOGARITHMIC For bucket " + i + " raw value=" +
					MathFormatter.fastFloatPrint( bucketRawValue, 7, false ) +
					" and back is " + andBack );

//			assertTrue( andBack == i );
		}
	}
}
