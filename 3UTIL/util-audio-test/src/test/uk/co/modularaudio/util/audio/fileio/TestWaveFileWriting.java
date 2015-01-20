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

import javax.sound.sampled.AudioFormat;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fileio.BrokenAudioDataFetcher;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.exception.DatastoreException;

public class TestWaveFileWriting extends TestCase
{
	private static Log log = LogFactory.getLog( TestWaveFileWriting.class.getName() );

	public void testWriteAFile()
		throws Exception
	{
		int sampleRate = 44100;
		short bitsPerSample = 16;
		WaveFileWriter outputWriter = new WaveFileWriter( "/tmp/javawavefilewriter.out", 1, sampleRate, bitsPerSample );

		int testDataLength = sampleRate;
		float[] testData = new float[ testDataLength ];
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
		int sampleRate = 44100;
		short bitsPerSample = 16;
		WaveFileWriter wof = new WaveFileWriter( "/tmp/javawavout.wav", 1, sampleRate, bitsPerSample );

		float[] testData = new float[] { 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, -1.0f, -0.75f, -0.5f, -0.25f, 0.0f };
		wof.writeFloats( testData, testData.length );

		wof.close();

	}

	public void testWriteThenReadAndCompare()
		throws Exception
	{
		int sampleRate = 44100;
		short bitsPerSample = 16;
		int numChannels = 1;

		String filename = "/tmp/compareit.wav";

		float[] testData = new float[] { 0.0f, 0.25f, 0.5f, 0.75f, 1.0f, -1.0f, -0.75f, -0.5f, -0.25f, 0.0f };
		double[] testDataAsDoubles = new double[ testData.length ];
		for( int i = 0 ; i < testData.length ; i++ )
		{
			testDataAsDoubles[ i ] = testData[i];
		}
//		long sampleRate = 44100;
//
//		WavFile wof = WavFile.newWavFile( new File( filename ), 1, testData.length, 16, sampleRate );
//		wof.writeFrames( testDataAsDoubles, testData.length );
//		wof.close();


		WaveFileWriter wof = new WaveFileWriter( filename, numChannels, sampleRate, bitsPerSample );

		wof.writeFloats( testData, testData.length );
		wof.close();

		BrokenAudioDataFetcher dataFetcher = new BrokenAudioDataFetcher();

		AudioFormat desiredAudioFormat = new AudioFormat( sampleRate, bitsPerSample, numChannels, true, false );
		dataFetcher.open( desiredAudioFormat, new File( filename ) );
		long numFloats = dataFetcher.getNumTotalFloats();
		int numFloatsAsInt = (int)numFloats;
		if( numFloatsAsInt != testData.length )
		{
			log.debug("Expected " + testData.length + " values, but got " + numFloatsAsInt );
			throw new DatastoreException( "Reading after writing doesn't produce the same number of values!" );
		}
		float[] resultData = new float[ numFloatsAsInt ];
		dataFetcher.read( resultData, 0, 0, numFloatsAsInt );

		for( int i = 0 ; i < testData.length ; ++i )
		{
			if( testData[ i ] != resultData[ i ] )
			{
				log.debug("On index " + i + " expected " + testData[ i ] + " but got " + resultData[ i ] );
			}
		}
	}
}
