/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

	private static int NUM_TEST_BUCKETS = 101;

	private static float MAX_VALUE_DB = -30.0f;
	private static float MAX_VALUE = AudioMath.dbToLevelF( MAX_VALUE_DB );

//	private static float TEST_MIN_DB = -120.0f;
	private static float TEST_MIN_DB = -96.0f;
//	private static float TEST_MIN_DB = -80.0f;
//	private static float TEST_MIN_DB = -30.0f;

	private static float TEST_MAX_DB = 0.0f;
//	private static float TEST_MAX_DB = -10.0f;

//	private static float TEST_MIN_FREQ = 1000.0f;
//	private static float TEST_MAX_FREQ = 2000.0f;

	private static float TEST_MIN_FREQ = 0.0f;
	private static float TEST_MAX_FREQ = 22050.0f;

	private static int HALFWAY_BUCKET = (NUM_TEST_BUCKETS - 1) / 2;

	static
	{
		log.debug("Test min DB(" + TEST_MIN_DB + ")" );
		log.debug("Test min value(" + MathFormatter.fastFloatPrint( AudioMath.dbToLevelF( TEST_MIN_DB ), 7, false ) + ")" );
		log.debug("Test max DB(" + TEST_MAX_DB + ")" );
		log.debug("Test max value(" + MathFormatter.fastFloatPrint( AudioMath.dbToLevelF( TEST_MAX_DB ), 7, false ) + ")" );
	}

	public void testLinearAmpScaleComputations() throws Exception
	{
		final LinearAmpScaleComputer lasc = new LinearAmpScaleComputer();
		lasc.setParameters( NUM_TEST_BUCKETS, TEST_MIN_DB, TEST_MAX_DB );

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( i );
			final float bucketDbValue = AudioMath.levelToDbF( bucketRawValue );

			final int andBack = lasc.rawToMappedBucket( bucketRawValue );

			log.trace("AMP LINEAR For bucket " + i + " raw value=(" +
					MathFormatter.fastFloatPrint( bucketRawValue, 7, false ) +
					") dbv(" +
					MathFormatter.fastFloatPrint( bucketDbValue, 7, false ) +
					") and back to bucket " + andBack );

			assertTrue( andBack == i );
		}

		final float minBucketValue = lasc.mappedBucketToRaw( 0 );
		assertTrue( minBucketValue == AudioMath.dbToLevelF( TEST_MIN_DB ) );
		final float maxBucketValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS - 1 );
		assertTrue( maxBucketValue == AudioMath.dbToLevelF( TEST_MAX_DB ) );

		// Finally make sure that out of bounds values are sensibly mapped
		final float tooSmallValue = AudioMath.dbToLevelF( TEST_MIN_DB - 20.0f );
		final int tooSmallBucket = lasc.rawToMappedBucket( tooSmallValue );
		assertTrue( tooSmallBucket == 0 );

		final float tooLargeValue = AudioMath.dbToLevelF( TEST_MAX_DB + 20.0f );
		final int tooLargeBucket = lasc.rawToMappedBucket( tooLargeValue );
		assertTrue( tooLargeBucket == NUM_TEST_BUCKETS - 1 );

		if( 101 == NUM_TEST_BUCKETS && TEST_MIN_DB == -96.0F && TEST_MAX_DB == 0.0f )
		{
			final float linValueForHalfwayBucket = lasc.mappedBucketToRaw( HALFWAY_BUCKET );
			assertTrue( Math.abs(linValueForHalfwayBucket - 0.5000079) < 0.00001 );
		}
	}

	public void testLogAmpDbScaleComputations() throws Exception
	{
		log.debug("Max value DB is " + MAX_VALUE_DB + " which is " + MathFormatter.fastFloatPrint( MAX_VALUE, 7, false ) );
		final LogarithmicDbAmpScaleComputer lasc = new LogarithmicDbAmpScaleComputer();
		lasc.setParameters( NUM_TEST_BUCKETS, TEST_MIN_DB, TEST_MAX_DB );

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lasc.mappedBucketToRaw( i );

			final int andBack = lasc.rawToMappedBucket( bucketRawValue );

			final float bucketDbValue = AudioMath.levelToDbF( bucketRawValue );

			log.trace("AMP LOG-DB For bucket " + i + " raw value=(" +
					MathFormatter.fastFloatPrint( bucketRawValue, 7, false ) +
					") dbv(" +
					MathFormatter.fastFloatPrint( bucketDbValue, 7, false ) +
					") and back is " + andBack );

			assertTrue( andBack == i );
		}

		final float minBucketValue = lasc.mappedBucketToRaw( 0 );
		assertTrue( minBucketValue == AudioMath.dbToLevelF( TEST_MIN_DB ) );
		final float maxBucketValue = lasc.mappedBucketToRaw( NUM_TEST_BUCKETS - 1 );
		assertTrue( maxBucketValue == AudioMath.dbToLevelF( TEST_MAX_DB ) );

		// Finally make sure that out of bounds values are sensibly mapped
		final float tooSmallValue = AudioMath.dbToLevelF( TEST_MIN_DB - 20.0f );
		final int tooSmallBucket = lasc.rawToMappedBucket( tooSmallValue );
		assertTrue( tooSmallBucket == 0 );

		final float tooLargeValue = AudioMath.dbToLevelF( TEST_MAX_DB + 20.0f );
		final int tooLargeBucket = lasc.rawToMappedBucket( tooLargeValue );
		assertTrue( tooLargeBucket == NUM_TEST_BUCKETS - 1 );

		if( 101 == NUM_TEST_BUCKETS && TEST_MIN_DB == -96.0F && TEST_MAX_DB == 0.0f )
		{
			final float logDbValueForHalfwayBucket = lasc.mappedBucketToRaw( HALFWAY_BUCKET );
			assertTrue( Math.abs(logDbValueForHalfwayBucket - 0.0039811) < 0.00001 );
		}
	}

	public void testLogAmpNaturalScaleComputations() throws Exception
	{
		final LogarithmicNaturalAmpScaleComputer lnasc = new LogarithmicNaturalAmpScaleComputer();
		lnasc.setParameters( NUM_TEST_BUCKETS, TEST_MIN_DB, TEST_MAX_DB );

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lnasc.mappedBucketToRaw( i );

			final int andBack = lnasc.rawToMappedBucket( bucketRawValue );

			final float bucketDbValue = AudioMath.levelToDbF( bucketRawValue );

			log.trace("AMP LOG-NATURAL For bucket " + i + " raw value=(" +
					MathFormatter.fastFloatPrint( bucketRawValue, 7, false ) +
					") dbv(" +
					MathFormatter.fastFloatPrint( bucketDbValue, 7, false ) +
					") and back is " + andBack );

			assertTrue( andBack == i );
		}

		final float minBucketValue = lnasc.mappedBucketToRaw( 0 );
		assertTrue( minBucketValue == AudioMath.dbToLevelF( TEST_MIN_DB ) );
		final float maxBucketValue = lnasc.mappedBucketToRaw( NUM_TEST_BUCKETS - 1 );
		assertTrue( maxBucketValue == AudioMath.dbToLevelF( TEST_MAX_DB ) );

		// Finally make sure that out of bounds values are sensibly mapped
		final float tooSmallValue = AudioMath.dbToLevelF( TEST_MIN_DB - 20.0f );
		final int tooSmallBucket = lnasc.rawToMappedBucket( tooSmallValue );
		assertTrue( tooSmallBucket == 0 );

		final float tooLargeValue = AudioMath.dbToLevelF( TEST_MAX_DB + 20.0f );
		final int tooLargeBucket = lnasc.rawToMappedBucket( tooLargeValue );
		assertTrue( tooLargeBucket == NUM_TEST_BUCKETS - 1 );

		if( 101 == NUM_TEST_BUCKETS && TEST_MIN_DB == -96.0F && TEST_MAX_DB == 0.0f )
		{
			final float logNaturalValueForHalfwayBucket = lnasc.mappedBucketToRaw( HALFWAY_BUCKET );
			assertTrue( Math.abs(logNaturalValueForHalfwayBucket - 0.0143556) < 0.00001 );
		}
	}

	public void testLinearFreqScaleComputations() throws Exception
	{
		final LinearFreqScaleComputer lfsc = new LinearFreqScaleComputer();
		lfsc.setMinMaxFrequency( TEST_MIN_FREQ, TEST_MAX_FREQ );

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, i );

			final int andBack = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, bucketRawValue );

			log.trace("FREQ LINEAR For bucket " + i + " raw value=" +
					MathFormatter.fastFloatPrint( bucketRawValue, 6, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}

		final float minBucketvalue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, 0 );
		assertTrue( minBucketvalue == TEST_MIN_FREQ );
		final float maxBucketvalue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, NUM_TEST_BUCKETS - 1 );
		assertTrue( maxBucketvalue == TEST_MAX_FREQ );

		final float tooSmallValue = TEST_MIN_FREQ - 100.0f;
		final int tooSmallBucket = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, tooSmallValue );
		assertTrue( tooSmallBucket == 0 );

		final float tooLargeValue = TEST_MAX_FREQ + 100.0f;
		final int tooLargeBucket = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, tooLargeValue );
		assertTrue( tooLargeBucket == NUM_TEST_BUCKETS - 1 );
	}

	public void testLogFreqScaleComputations() throws Exception
	{
		final LogarithmicFreqScaleComputer lfsc = new LogarithmicFreqScaleComputer();
		lfsc.setMinMaxFrequency( TEST_MIN_FREQ, TEST_MAX_FREQ );

		for( int i = 0 ; i < NUM_TEST_BUCKETS ; ++i )
		{
			final float bucketRawValue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, i );

			final int andBack = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, bucketRawValue );

			log.trace("FREQ LOGARITHMIC For bucket " + i + " raw value=" +
					MathFormatter.fastFloatPrint( bucketRawValue, 6, false ) +
					" and back is " + andBack );

			assertTrue( andBack == i );
		}

		final float minBucketvalue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, 0 );
		assertTrue( minBucketvalue == TEST_MIN_FREQ );
		final float maxBucketvalue = lfsc.mappedBucketToRawMinMax( NUM_TEST_BUCKETS, NUM_TEST_BUCKETS - 1 );
		assertTrue( maxBucketvalue == TEST_MAX_FREQ );

		final float tooSmallValue = TEST_MIN_FREQ - 100.0f;
		final int tooSmallBucket = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, tooSmallValue );
		assertTrue( tooSmallBucket == 0 );

		final float tooLargeValue = TEST_MAX_FREQ + 100.0f;
		final int tooLargeBucket = lfsc.rawToMappedBucketMinMax( NUM_TEST_BUCKETS, tooLargeValue );
		assertTrue( tooLargeBucket == NUM_TEST_BUCKETS - 1 );
	}

}
