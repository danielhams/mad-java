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

package test.uk.co.modularaudio.libmpg123wrapper;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libmpg123wrapper.LibMpg123WrapperLoader;
import uk.co.modularaudio.libmpg123wrapper.swig.CArrayInt;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_int;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_mpg123_handle_struct;
import uk.co.modularaudio.libmpg123wrapper.swig.libmpg123;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_errors;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_param_flags;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_parms;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class AttemptToReadAFile extends TestCase
{
	private static Log log = LogFactory.getLog( AttemptToReadAFile.class.getName() );

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final float[] buffer = new float[ BUFFER_LENGTH_FLOATS ];

	private final static int SEEK_SET = 0;

	private final String inputFileName = "../../5TEST/audio-test-files/audiofiles/ExampleBeats.mp3";
	private final String outputFileName = "tmpoutput/javalibmpg123reader.wav";

	private final Limiter limiter = new Limiter( 0.98f, 25 );

	public AttemptToReadAFile() throws IOException
	{
		LibMpg123WrapperLoader.loadIt();
		final int initSuccess = libmpg123.mpg123_init();
		if( initSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			final String msg = "Failed mpg123 init: " + initSuccess;
			log.error( msg );
			throw new IOException( msg );
		}
	}

	public void testUseSwigWrapper() throws Exception
	{
		libmpg123.HandRolled( 25, "SomeString" );

		final CArrayInt errorInt = new CArrayInt( 1 );
		final SWIGTYPE_p_int error = errorInt.cast();
		final SWIGTYPE_p_mpg123_handle_struct handle = libmpg123.mpg123_new( null, error );
		final int errorValue = errorInt.getitem( 0 );
		if( errorValue != 0 )
		{
			throw new IOException("Error thrown in mpg123_new: " + errorValue );
		}

		libmpg123.mpg123_param( handle, mpg123_parms.MPG123_ADD_FLAGS, mpg123_param_flags.MPG123_FORCE_FLOAT.swigValue(), 0.0f );

		final File inputFile = new File(inputFileName);

		final int openSuccess = libmpg123.mpg123_open( handle, inputFile.getAbsolutePath() );

		if( openSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during mpg123_open");
		}

		final int formatCheck = libmpg123.CheckFormat( handle );

		if( formatCheck != mpg123_errors.MPG123_OK.swigValue() )
		{
			final String msg = "Failed format check";
			log.error( msg );
			throw new IOException( msg );
		}

		final long cSampleRate = libmpg123.GetFormatSampleRate( handle );
		final int cChannels = libmpg123.GetFormatChannels( handle );

		log.debug( "Custom JNI Methods say sample rate is (" + cSampleRate +") and channels(" +
				cChannels + ")");

		log.debug( "Opened " + inputFileName + " for reading" );

		final int scanSuccess = libmpg123.mpg123_scan( handle );

		if( scanSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during mpg123_scan");
		}

		final long seekResult = libmpg123.mpg123_seek( handle, 0, SEEK_SET );

		if( seekResult != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed seek to start");
		}

		final long totalFrames = libmpg123.mpg123_length( handle );
		log.debug("Have " + totalFrames + " frames");

		final File debugOutputFile = new File(outputFileName);
		final File parentDir = debugOutputFile.getParentFile();
		parentDir.mkdirs();

		final WaveFileWriter waveWriter = new WaveFileWriter( outputFileName,
				2,
				(int)cSampleRate,
				(short)16 );



		final int numFramesPerRound = BUFFER_LENGTH_FLOATS / cChannels;

		long numFramesLeft = totalFrames;

		final CArrayInt doneArray = new CArrayInt( 1 );
		final SWIGTYPE_p_int donePtr = doneArray.cast();

		while( numFramesLeft > 0 )
		{
			final long numFramesThisRound = (numFramesLeft > numFramesPerRound ? numFramesPerRound : numFramesLeft );
			final int numFloatsThisRound = (int)(numFramesThisRound * cChannels);

//			log.trace( "Asking for " + numFloatsThisRound + " floats this round" );

			final int rv = libmpg123.DecodeData( handle, buffer, 0, numFloatsThisRound, donePtr );

			final int numFloatsRead = doneArray.getitem( 0 );

			if( rv == mpg123_errors.MPG123_DONE.swigValue() )
			{
				log.trace( "Hit DONE with " + numFloatsRead );
			}
			else if( rv != mpg123_errors.MPG123_OK.swigValue() )
			{
				throw new IOException("Failed during DecodeData: " + rv);
			}
			else
			{
				if( numFloatsRead != numFloatsThisRound &&
						rv != mpg123_errors.MPG123_DONE.swigValue() )
				{
					throw new IOException("Failed to get as many floats as we asked for: asked(" +
							numFloatsThisRound + ") got(" + numFloatsRead + ")");
				}
			}

			final int numFramesRead = numFloatsRead / cChannels;
			assert( numFramesRead == numFramesThisRound );

			limiter.filter( buffer, 0, numFloatsRead );

			waveWriter.writeFloats( buffer, numFloatsThisRound );

			numFramesLeft -= numFramesThisRound;

			if( rv == mpg123_errors.MPG123_DONE.swigValue() )
			{
				log.trace("Finishing early");
				break;
			}
		}

		final int closeRv = libmpg123.mpg123_close( handle );
		if( closeRv != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during close: " + closeRv );
		}

		waveWriter.close();

		log.trace( "Done" );
	}
}
