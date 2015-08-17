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

package test.uk.co.modularaudio.util.audio.fileio;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestHighResolutionWaveFileWritingReading extends TestCase
{
	private static Log log = LogFactory.getLog( TestHighResolutionWaveFileWritingReading.class.getName() );

	private static final String OUTPUT_DIR = "tmpoutput";
	private static final String KNOWN_GOOD_FILENAME = OUTPUT_DIR + File.separatorChar + "known_good.wav";
	private static final String ROUNDTRIP_ONE_FILENAME = OUTPUT_DIR + File.separatorChar + "roundtrip_one.wav";

	private static final int NUM_CHANNELS = 1;

	public static final int NUM_BITS_8 = 8;
	public static final int NUM_BITS_16 = 16;
	public static final int NUM_BITS_24 = 24;
	public static final int NUM_BITS_32 = 32;

//	private static int NUM_BITS_TO_USE = NUM_BITS_32;

	private static final int SAMPLE_RATE = 44100;

//	private static final double MAX_EXPECTED_DIFF_D =
//			( NUM_BITS_TO_USE == NUM_BITS_32 ?
//					AudioMath.MIN_FLOATING_POINT_32BIT_VAL_D :
//						( NUM_BITS_TO_USE == NUM_BITS_24 ?
//								AudioMath.MIN_FLOATING_POINT_24BIT_VAL_D :
//									( NUM_BITS_TO_USE == NUM_BITS_16 ?
//											AudioMath.MIN_FLOATING_POINT_16BIT_VAL_D :
//												AudioMath.MIN_FLOATING_POINT_8BIT_VAL_D ) ) );

	private static final double[] TEST_INPUT_DOUBLES = new double[] {
		0.0001,
		0.0002,
		0.0003,
		0.0004,
		0.0005,
		0.0006,
		0.0007,
		0.0008,
		0.0009,
		0.0010,
		0.0020,
		0.0030,
		0.0040,
		0.0050,
		0.0060,
		0.0070,
		0.0080,
		0.0090,
		0.0100,
		0.0,
		0.5,
		1.0,
		-0.5,
		-1.0
	};

	private static final int NUM_TEST_FRAMES = TEST_INPUT_DOUBLES.length;

	private static final float[] TEST_INPUT_FLOATS = new float[NUM_TEST_FRAMES];

	static
	{
		for( int s = 0 ; s < NUM_TEST_FRAMES ; ++s )
		{
			TEST_INPUT_FLOATS[s] = (float)TEST_INPUT_DOUBLES[s];
		}
	}

	public TestHighResolutionWaveFileWritingReading()
	{
	}

	public void testWriteReadAndWriteAgain() throws Exception
	{
		final int[] bitsToUse = {
				NUM_BITS_8,
				NUM_BITS_16,
				NUM_BITS_24,
				NUM_BITS_32
		};
		final double[] acceptableDiffs = {
				AudioMath.MIN_FLOATING_POINT_8BIT_VAL_D,
				AudioMath.MIN_FLOATING_POINT_16BIT_VAL_D,
				AudioMath.MIN_FLOATING_POINT_24BIT_VAL_D,
				AudioMath.MIN_FLOATING_POINT_32BIT_VAL_D
		};

		for( int index = 0 ; index < bitsToUse.length ; ++index )
		{
			final int bits = bitsToUse[index];
			final double expectedDiff = acceptableDiffs[index];
			log.debug( "Doing pass with " + bits + " bits and expected diff of " +
					MathFormatter.slowDoublePrint( expectedDiff, 12, true ) );
			internalCheckOfBitsAndDiff( bits, expectedDiff );
		}
	}

	private void internalCheckOfBitsAndDiff( final int bitsToUse, final double maxExpectedDiff ) throws Exception
	{
		final WaveFileWriter knownGoodWriter = new WaveFileWriter( KNOWN_GOOD_FILENAME, NUM_CHANNELS, SAMPLE_RATE, (short)bitsToUse );

		final double[] knownGoodOutputFloats = new double[NUM_TEST_FRAMES];
		System.arraycopy( TEST_INPUT_DOUBLES, 0, knownGoodOutputFloats, 0, NUM_TEST_FRAMES );

		knownGoodWriter.writeFramesDoubles( knownGoodOutputFloats, 0, NUM_TEST_FRAMES );

		knownGoodWriter.close();

		// Now read it back in
		final float[] myInputFloats = new float[NUM_TEST_FRAMES];
		final WaveFileReader myReader = new WaveFileReader( KNOWN_GOOD_FILENAME );
		myReader.readFrames( myInputFloats, 0, 0, NUM_TEST_FRAMES );
		myReader.close();

		// Write once more
		final WaveFileWriter myWriter = new WaveFileWriter( ROUNDTRIP_ONE_FILENAME, NUM_CHANNELS, SAMPLE_RATE, (short)bitsToUse);
		myWriter.writeFrames( myInputFloats, 0, NUM_TEST_FRAMES );
		myWriter.close();

		// Finally read and compare
		final float[] finalFloats = new float[NUM_TEST_FRAMES];
		final WaveFileReader finalReader = new WaveFileReader( ROUNDTRIP_ONE_FILENAME );
		finalReader.readFrames( finalFloats, 0, 0, NUM_TEST_FRAMES );
		finalReader.close();

		for( int f = 0 ; f < NUM_TEST_FRAMES ; ++f )
		{
			final float outputFloat = myInputFloats[f];
			final float inputFloat = finalFloats[f];
			final float absDiff = Math.abs( outputFloat - inputFloat );
			final String index = String.format("%02d", f );
			log.debug("MW Abs orig("  +
					MathFormatter.slowDoublePrint( knownGoodOutputFloats[f], 12, true ) +
					") diff " + index + "(" +
					MathFormatter.slowFloatPrint( outputFloat, 12, true ) + ")->(" +
					MathFormatter.slowFloatPrint( inputFloat, 12, true ) + ") diff(" +
					MathFormatter.slowFloatPrint( absDiff, 12, false ) + ")" +
					( absDiff > maxExpectedDiff ?
							" FAILED" : "" )
					);
			assertTrue( absDiff <= maxExpectedDiff );
		}
	}

}
