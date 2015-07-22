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
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestWaveFileWriting extends TestCase
{
	private static Log log = LogFactory.getLog( TestWaveFileWriting.class.getName() );

	private final String outputFilename = "tmpoutput/javawavfilewriter.wav";

	public void testWriteAFile()
		throws Exception
	{
		final int sampleRate = 44100;
		final short bitsPerSample = 16;

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter outputWriter = new WaveFileWriter( outputFile.getAbsolutePath(), 1, sampleRate, bitsPerSample );

		final int testDataLength = sampleRate;
		final float[] testData = new float[ testDataLength ];
		for( int i = 0 ; i < testDataLength ; i++ )
		{
			testData[i] = 1.0f / (1.0f + (i / 1000.0f) );
//			testData[i] = ( i % 10 == 0 ? 0.5f : -0.1f );
		}

		outputWriter.writeFloats( testData, testDataLength );

		outputWriter.close();
	}

	public void testDoSameAsCpp()
		throws Exception
	{
		final int sampleRate = 44100;
		final short bitsPerSample = 16;

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter wof = new WaveFileWriter( outputFile.getAbsolutePath(), 1, sampleRate, bitsPerSample );

		final float[] testData = new float[] { 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, -1.0f, -0.75f, -0.5f, -0.25f, 0.0f };
		wof.writeFloats( testData, testData.length );

		wof.close();

	}

	public void testWriteThenReadAndCompare()
		throws Exception
	{
		final int sampleRate = 44100;
		final short bitsPerSample = 16;
		final int numChannels = 1;

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final float[] testData = new float[] { 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, -1.0f, -0.75f, -0.5f, -0.25f, 0.0f };
		final double[] testDataAsDoubles = new double[ testData.length ];
		for( int i = 0 ; i < testData.length ; i++ )
		{
			testDataAsDoubles[ i ] = testData[i];
		}

		final WaveFileWriter wof = new WaveFileWriter( outputFile.getAbsolutePath(), numChannels, sampleRate, bitsPerSample );

		wof.writeFloats( testData, testData.length );
		wof.close();

		final WaveFileReader wif = new WaveFileReader( outputFile.getAbsolutePath() );

		final long numFloats = wif.getNumTotalFloats();

		final int numFloatsAsInt = (int)numFloats;
		if( numFloatsAsInt != testData.length )
		{
			log.debug("Expected " + testData.length + " values, but got " + numFloatsAsInt );
			throw new DatastoreException( "Reading after writing doesn't produce the same number of values!" );
		}
		final float[] resultData = new float[ numFloatsAsInt ];
		wif.read( resultData, 0, 0, numFloatsAsInt );

		for( int i = 0 ; i < testData.length ; ++i )
		{
			final float absDiff = Math.abs(testData[i] - resultData[i]);
			if( absDiff > AudioMath.MIN_FLOATING_POINT_16BIT_VAL_F )
			{
				log.debug("On index " + i + " expected " + testData[ i ] + " but got " + resultData[ i ] );
				log.debug("AbsDiff(" +
						MathFormatter.slowFloatPrint( absDiff, 16, true ) +
						") while min floating point 16 bit value is " +
						MathFormatter.slowFloatPrint( AudioMath.MIN_FLOATING_POINT_16BIT_VAL_F, 16, true ) );
			}
		}
	}
}
