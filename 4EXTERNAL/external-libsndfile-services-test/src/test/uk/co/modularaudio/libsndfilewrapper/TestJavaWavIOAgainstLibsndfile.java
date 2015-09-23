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

package test.uk.co.modularaudio.libsndfilewrapper;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libsndfilewrapper.LibSndfileWrapperLoader;
import uk.co.modularaudio.libsndfilewrapper.swig.SF_INFO;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_SNDFILE_tag;
import uk.co.modularaudio.libsndfilewrapper.swig.libsndfile;
import uk.co.modularaudio.util.audio.fileio.WaveFileReader;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestJavaWavIOAgainstLibsndfile extends TestCase
{
	private static Log log = LogFactory.getLog( TestJavaWavIOAgainstLibsndfile.class.getName() );

	private final static String OUTPUT_DIR = "tmpoutput";
	private final static String OUTPUT_MU_WAVE = OUTPUT_DIR + File.separatorChar + "muwaveoutput.wav";
	private final static String OUTPUT_MU_WAVE2 = OUTPUT_DIR + File.separatorChar + "muwaveoutput2.wav";

	private final static float[] TEST_FLOATS = new float[] {
		-0.2f,
		-1.0f,
		1.0f,
		0.0f,
		-0.9f,
		-0.8f,
		-0.7f,
		-0.6f,
		-0.5f,
		-0.4f,
		-0.3f,
		-0.2f,
		-0.1f,
		0.0f,
		0.5f,
		0.9f,
		0.008f,
	};

	private final static int NUM_TEST_FRAMES = TEST_FLOATS.length;

	public final static int NUM_BITS_8 = 8;
	public final static int NUM_BITS_16 = 16;
	public final static int NUM_BITS_24 = 24;
	public final static int NUM_BITS_32 = 32;

	private final static int SAMPLE_RATE = 48000;

	public TestJavaWavIOAgainstLibsndfile()
	{
		LibSndfileWrapperLoader.loadIt();
	}

	public void writeMUWavFile( final int numBitsToUse ) throws Exception
	{
		final File outputFile = new File(OUTPUT_MU_WAVE);
		final File parentDir = outputFile.getParentFile();
		parentDir.mkdirs();

		final WaveFileWriter muWriter = new WaveFileWriter( OUTPUT_MU_WAVE, 1, SAMPLE_RATE, (short)numBitsToUse );
		muWriter.writeFrames( TEST_FLOATS, 0, NUM_TEST_FRAMES );
		muWriter.close();
	}

	public void readMUWavFile( final double maxExpectedDiff ) throws Exception
	{
		final WaveFileReader wfr = new WaveFileReader( OUTPUT_MU_WAVE );

		final float[] floatsRead = new float[NUM_TEST_FRAMES];
		wfr.readFrames( floatsRead, 0, 0, NUM_TEST_FRAMES );
		wfr.close();

		// Now lets see how off they are
		for( int s = 0 ; s < NUM_TEST_FRAMES ; ++s )
		{
			final float expectedFloat = TEST_FLOATS[s];
			final float receivedFloat = floatsRead[s];
			final float absDiff = Math.abs( expectedFloat - receivedFloat);
			log.trace( "MURead - Expected: " + MathFormatter.slowFloatPrint( expectedFloat, 16, true ) +
					" Received: " + MathFormatter.slowFloatPrint( receivedFloat, 16, true ) +
					" Diff: " + MathFormatter.slowFloatPrint( absDiff, 16, true) +
					( absDiff > maxExpectedDiff ?
							" FAILED" : "" )
					);
		}
	}

	public void readMUWithLSFWavFileWriteAgain( final int numBitsToUse, final double maxExpectedDiff ) throws Exception
	{
		final float[] lsfReadValues = readWavFile( "MU-LSF", OUTPUT_MU_WAVE, maxExpectedDiff );

		final WaveFileWriter writer2 = new WaveFileWriter( OUTPUT_MU_WAVE2, 1, SAMPLE_RATE, (short)numBitsToUse );
		writer2.writeFrames( lsfReadValues, 0, NUM_TEST_FRAMES );
		writer2.close();

		final float[] lsfReadValues2 = readWavFile( "MU-LSF2", OUTPUT_MU_WAVE2, maxExpectedDiff );

		for( int i = 0 ; i < NUM_TEST_FRAMES ; ++i )
		{
			final float diff = Math.abs( lsfReadValues[i] - lsfReadValues2[i] );
			if( diff != 0.0f )
			{
				final String index = String.format( "%02d",  i );
				log.error("MU Round trip error on value " + index + "(" +
						MathFormatter.slowFloatPrint( lsfReadValues[i], 12, true) + ")->(" +
						MathFormatter.slowFloatPrint( lsfReadValues2[i], 12, true) +
						") of " +
						diff + " where Nbit error is " +
						maxExpectedDiff );
			}
			assertTrue( diff == 0.0f );
		}
	}

	public float[] readWavFile( final String tracePrefix, final String fileToRead,
			final double maxExpectedDiff ) throws Exception
	{
		final SF_INFO sf = new SF_INFO();

		final File inputFile = new File(fileToRead);

		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = libsndfile.sf_open( inputFile.getAbsolutePath(), libsndfile.SFM_READ, sf );

		if( sndfilePtr == null )
		{
			throw new DatastoreException( "Unknown format (returned null)" );
		}

		final int sampleRate = sf.getSamplerate();
		final long numFrames = sf.getFrames();
		final int numChannels = sf.getChannels();
		log.trace( tracePrefix + "- Apparently have opened the file with (" + sampleRate + ") (" + numFrames + ") (" + numChannels
				+ ")" );

		final float[] floatsRead = new float[NUM_TEST_FRAMES];

		libsndfile.CustomSfReadFloatsOffset( sndfilePtr, floatsRead, 0, NUM_TEST_FRAMES );

		libsndfile.sf_close( sndfilePtr );

		sf.delete();

		log.trace( tracePrefix + "- Max expected diff of " + MathFormatter.slowDoublePrint( maxExpectedDiff, 16, true ) );

		// Now lets see how off they are
		for( int s = 0 ; s < NUM_TEST_FRAMES ; ++s )
		{
			final float expectedFloat = TEST_FLOATS[s];
			final float receivedFloat = floatsRead[s];
			final float absDiff = Math.abs( expectedFloat - receivedFloat);
			log.trace( tracePrefix + "- Expected: " + MathFormatter.slowFloatPrint( expectedFloat, 16, true ) +
					" Received: " + MathFormatter.slowFloatPrint( receivedFloat, 16, true ) +
					" Diff: " + MathFormatter.slowFloatPrint( absDiff, 16, true) +
					( absDiff > maxExpectedDiff ?
							" FAILED" : "" )
					);
		}
		return floatsRead;
	}

	public void testWavWriteReadCycles() throws Exception
	{
		final int[] bitsToUse = {
				NUM_BITS_8,
				NUM_BITS_16,
				NUM_BITS_24,
				NUM_BITS_32
		};
		final double[] expectedDiffs = {
				AudioMath.MIN_SIGNED_FLOATING_POINT_8BIT_VAL_D,
				AudioMath.MIN_SIGNED_FLOATING_POINT_16BIT_VAL_D,
				AudioMath.MIN_SIGNED_FLOATING_POINT_24BIT_VAL_D,
				AudioMath.MIN_SIGNED_FLOATING_POINT_32BIT_VAL_D
		};

		for( int index = 0 ; index < bitsToUse.length ; ++index )
		{
			final int bits = bitsToUse[index];
			final double expectedDiff = expectedDiffs[index];
			log.trace( "Doing pass with " + bits + " bits and expected diff of " +
					MathFormatter.slowDoublePrint( expectedDiff, 16, true ) );
			writeMUWavFile( bits );
			readMUWavFile( expectedDiff );
			readMUWithLSFWavFileWriteAgain( bits, expectedDiff );
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		log.trace( "Beginning MU write" );
		final TestJavaWavIOAgainstLibsndfile a = new TestJavaWavIOAgainstLibsndfile();
		a.testWavWriteReadCycles();
	}

}
