package test.uk.co.modularaudio.libmpg123wrapper;

import java.io.IOException;

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

public class AttemptToReadAFile
{
	private static Log log = LogFactory.getLog( AttemptToReadAFile.class.getName() );

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final float[] buffer = new float[ BUFFER_LENGTH_FLOATS ];

	private final static int SEEK_SET = 0;

//	final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/depeche_mode/a_broken_frame/a_photograph_of_you.ogg";
//	final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/ORB - The Orb's Adventures Beyond The Ultraworld/CD 1/02 - Earth Orbit Two- Earth (Gaia).flac";
	final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200911/974684_She_Came_Along_feat__Kid_Cudi_Sharam_s_Ecstasy_Of_Ibiza_Edit.mp3";

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

	public void go() throws Exception
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

		final int openSuccess = libmpg123.mpg123_open( handle, filePath );

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

		log.debug( "Opened " + filePath + " for reading" );

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

		final WaveFileWriter waveWriter = new WaveFileWriter( "/tmp/javalibmpg123reader.wave",
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

	public static void main( final String[] args ) throws Exception
	{
		log.trace("Beginning libmpg123 read attempt");
		final AttemptToReadAFile a = new AttemptToReadAFile();

		a.go();
	}

}
