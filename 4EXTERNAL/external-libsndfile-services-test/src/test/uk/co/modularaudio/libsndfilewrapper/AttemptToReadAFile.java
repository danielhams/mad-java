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
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libsndfilewrapper.LibSndfileWrapperLoader;
import uk.co.modularaudio.libsndfilewrapper.swig.CArrayFloat;
import uk.co.modularaudio.libsndfilewrapper.swig.SF_INFO;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_SNDFILE_tag;
import uk.co.modularaudio.libsndfilewrapper.swig.SWIGTYPE_p_float;
import uk.co.modularaudio.libsndfilewrapper.swig.libsndfile;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.exception.DatastoreException;

public class AttemptToReadAFile extends TestCase
{
	private static Log log = LogFactory.getLog( AttemptToReadAFile.class.getName() );

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final float[] buffer = new float[BUFFER_LENGTH_FLOATS];

	final String inputFilename = "../../5TEST/audio-test-files/audiofiles/ExampleBeats.flac";
	final String outputFilename = "tmpoutput/libsndfilereaderfromflac.wav";

	public AttemptToReadAFile()
	{
		LibSndfileWrapperLoader.loadIt();
	}

	/*
	 * A good example of how _not_ to use the JNI interface.
	 *
	 * The loop with calls to .get(i) per float is painful, so
	 * I added a custom method where you pass the float array
	 * and it fills the array directly.
	 */
	public void testUseSwigWrapperSlow() throws Exception
	{
		final SF_INFO sf = new SF_INFO();

		log.trace( "Beginning" );

		final File inputFile = new File(inputFilename);

		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = libsndfile.sf_open( inputFile.getAbsolutePath(), libsndfile.SFM_READ, sf );

		if( sndfilePtr == null )
		{
			throw new DatastoreException( "Unknown format (returned null)" );
		}

		final int sampleRate = sf.getSamplerate();
		final long numFrames = sf.getFrames();
		final int numChannels = sf.getChannels();
		log.trace( "Apparently have opened the file with (" + sampleRate + ") (" + numFrames + ") (" + numChannels
				+ ")" );

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveWriter = new WaveFileWriter( outputFile.getAbsolutePath(), 2, sampleRate,
				(short) 16 );

		final int numFramesPerRound = BUFFER_LENGTH_FLOATS / numChannels;

		long numFramesLeft = numFrames;

		final CArrayFloat cArrayFloat = new CArrayFloat( BUFFER_LENGTH_FLOATS );
		final SWIGTYPE_p_float floatPtr = cArrayFloat.cast();

		final long b = System.nanoTime();

		while( numFramesLeft > 0 )
		{
			final long numFramesThisRound = (numFramesLeft > numFramesPerRound ? numFramesPerRound : numFramesLeft);

			final long numFramesRead = libsndfile.sf_readf_float( sndfilePtr, floatPtr, numFramesThisRound );

			assert (numFramesRead == numFramesThisRound);

			final int numFloatsThisRound = (int) (numFramesThisRound * numChannels);
			for( int i = 0 ; i < numFloatsThisRound ; ++i )
			{
				buffer[i] = cArrayFloat.getitem( i );
			}

			waveWriter.writeFloats( buffer, numFloatsThisRound );

			numFramesLeft -= numFramesThisRound;
		}
		libsndfile.sf_close( sndfilePtr );

		waveWriter.close();

		sf.delete();

		final long a = System.nanoTime();
		final long diff = a - b;
		log.trace( "Slow Done in " + diff + " nanos or " + (diff / 1000.0f) + " micros or " + (diff / 1000000.0f)
				+ " millis" );
	}

	public void testUseSwigWrapperBulk() throws Exception
	{

		final SF_INFO sf = new SF_INFO();

		log.trace( "Beginning" );

		final File inputFile = new File(inputFilename);

		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = libsndfile.sf_open( inputFile.getAbsolutePath(), libsndfile.SFM_READ, sf );

		if( sndfilePtr == null )
		{
			throw new DatastoreException( "Unknown format (returned null)" );
		}

		final int sampleRate = sf.getSamplerate();
		final long numFrames = sf.getFrames();
		final int numChannels = sf.getChannels();
		log.trace( "Apparently have opened the file with (" + sampleRate + ") (" + numFrames + ") (" + numChannels
				+ ")" );

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveWriter = new WaveFileWriter( outputFile.getAbsolutePath(), 2, sampleRate,
				(short) 16 );

		final int numFramesPerRound = BUFFER_LENGTH_FLOATS / numChannels;

		long numFramesLeft = numFrames;

		final long b = System.nanoTime();

		while( numFramesLeft > 0 )
		{
			final long numFramesThisRound = (numFramesLeft > numFramesPerRound ? numFramesPerRound : numFramesLeft);
			final int numFloatsThisRound = (int)(numFramesThisRound * numChannels);

			final long numFloatsRead = libsndfile.CustomSfReadFloatsOffset( sndfilePtr, buffer, 0, numFloatsThisRound );

			if( numFloatsRead != numFloatsThisRound )
			{
				log.error("Failed reading frames: " + numFloatsRead );
				throw new IOException("Read error");
			}

			waveWriter.writeFloats( buffer, numFloatsThisRound );

			numFramesLeft -= numFramesThisRound;
		}

		libsndfile.sf_close( sndfilePtr );

		waveWriter.close();

		sf.delete();

		final long a = System.nanoTime();
		final long diff = a - b;
		log.trace( "Bulk done in " + diff + " nanos or " + (diff / 1000.0f) + " micros or " + (diff / 1000000.0f)
				+ " millis" );
	}

	public static void main( final String[] args ) throws Exception
	{
		log.trace( "Beginning libsndfile read attempt" );
		final AttemptToReadAFile a = new AttemptToReadAFile();

		a.testUseSwigWrapperSlow();
		a.testUseSwigWrapperBulk();
	}

}
