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

package test.uk.co.modularaudio.util.audio.datafetcher;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import uk.co.modularaudio.util.audio.fileio.BrokenAudioDataFetcher;
import uk.co.modularaudio.util.audio.fileio.IAudioDataFetcher;

public class AudioDataFetcherTest extends TestCase
{
	private static Log log = LogFactory.getLog( AudioDataFetcherTest.class.getName() );
	
	static
	{
		BasicConfigurator.configure();
	}
	
	private final static String TEST_FILE_WAV = "/music/Mp3Repository/TestForPac/hc.wav";
	private final static String TEST_FILE_MP3 = "/music/Mp3Repository/TestForPac/hc.mp3";
	private final static String TEST_FILE_OGG = "/music/Mp3Repository/TestForPac/hc.ogg";
	private final static String TEST_FILE_FLAC = "/music/Mp3Repository/TestForPac/hc.flac";
	
	private final static int NUM_CHANNELS = 2;
	private final static int SAMPLE_RATE = 44100;
	
	public void testGettingStreamInfo() throws Exception
	{
		try
		{
			log.debug("Begining stream tests on wav");
			doOneFile(TEST_FILE_WAV );
			doOneFile(TEST_FILE_WAV );
			doOneFile(TEST_FILE_WAV );
		}
		catch( Exception e )
		{
			String msg = "Exception caught during wav test: " + e.toString();
			log.error( msg, e );
		}
		try
		{
			log.debug("Begining stream tests on mp3");
			doOneFile(TEST_FILE_MP3 );
			doOneFile(TEST_FILE_MP3 );
			doOneFile(TEST_FILE_MP3 );
		}
		catch( Exception e )
		{
			String msg = "Exception caught during mp3 test: " + e.toString();
			log.error( msg, e );
		}

		try
		{
			log.debug("Begining stream tests on ogg");
			doOneFile(TEST_FILE_OGG );
			doOneFile(TEST_FILE_OGG );
			doOneFile(TEST_FILE_OGG );
		}
		catch( Exception e )
		{
			String msg = "Exception caught during ogg test: " + e.toString();
			log.error( msg, e );
		}

		try
		{
			log.debug("Begining stream tests on flac");
			doOneFile(TEST_FILE_FLAC );
			doOneFile(TEST_FILE_FLAC );
			doOneFile(TEST_FILE_FLAC );
		}
		catch( Exception e )
		{
			String msg = "Exception caught during flac test: " + e.toString();
			log.error( msg, e );
		}
	}
	private void doOneFile(String fileToTest) throws UnsupportedAudioFileException, IOException
	{
		long timeBefore = System.currentTimeMillis();
		File f = new File( fileToTest );
		AudioFormat raf = new AudioFormat(Encoding.PCM_SIGNED, SAMPLE_RATE, 16, NUM_CHANNELS, NUM_CHANNELS * 2, SAMPLE_RATE, false );
		IAudioDataFetcher aisf = new BrokenAudioDataFetcher();
		aisf.open( raf, f );
		log.debug("Got back " + aisf.getNumTotalFloats() + " frames in total");
		
		int NUM_FLOATS = 1024;
		float[] destBuf = new float[ NUM_FLOATS ];
		int destOffset = 0;
		long startPos = 0;
		int length = NUM_FLOATS;
		// Attempt to fetch some floats at various points in the stream
		int numRead = aisf.read( destBuf, destOffset, startPos, length );
		assertTrue( numRead == length );
		startPos = 167266 * 2;
		numRead = aisf.read( destBuf, destOffset, startPos, length );
		assertTrue( numRead == length );
		// Now back to zero
		startPos = 0;
		numRead = aisf.read( destBuf, destOffset, startPos, length );
		assertTrue( numRead == length );
		// And once more with feeling
		startPos = 201056;
		numRead = aisf.read( destBuf, destOffset, startPos, length);
		assertTrue( numRead == length );
		long timeAfter = System.currentTimeMillis();
		log.debug("Total time: " + (timeAfter - timeBefore) );
	}
}
