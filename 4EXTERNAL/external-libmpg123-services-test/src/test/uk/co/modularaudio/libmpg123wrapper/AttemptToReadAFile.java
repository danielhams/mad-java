package test.uk.co.modularaudio.libmpg123wrapper;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.libmpg123wrapper.LibMpg123WrapperLoader;
import uk.co.modularaudio.libmpg123wrapper.swig.CArrayInt;
import uk.co.modularaudio.libmpg123wrapper.swig.CArrayLong;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_int;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_long;
import uk.co.modularaudio.libmpg123wrapper.swig.SWIGTYPE_p_mpg123_handle_struct;
import uk.co.modularaudio.libmpg123wrapper.swig.libmpg123;
import uk.co.modularaudio.libmpg123wrapper.swig.mpg123_errors;

public class AttemptToReadAFile
{
	private static Log log = LogFactory.getLog( AttemptToReadAFile.class.getName() );

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final float[] buffer = new float[ BUFFER_LENGTH_FLOATS ];

	private final static int SEEK_SET = 0;

	public AttemptToReadAFile()
	{
	}

	public void go() throws Exception
	{
//	final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/depeche_mode/a_broken_frame/a_photograph_of_you.ogg";
//	final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/ORB - The Orb's Adventures Beyond The Ultraworld/CD 1/02 - Earth Orbit Two- Earth (Gaia).flac";
	final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200911/974684_She_Came_Along_feat__Kid_Cudi_Sharam_s_Ecstasy_Of_Ibiza_Edit.mp3";

		LibMpg123WrapperLoader.loadIt();

		final int initSuccess = libmpg123.mpg123_init();

		if( initSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			final String msg = "Failed mpg123 init: " + initSuccess;
			log.error( msg );
			throw new IOException( msg );
		}

		final CArrayInt errorInt = new CArrayInt( 1 );
		final SWIGTYPE_p_int error = errorInt.cast();
		final SWIGTYPE_p_mpg123_handle_struct handle = libmpg123.mpg123_new( null, error );

		final int openSuccess = libmpg123.mpg123_open( handle, filePath );

		if( openSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during mpg123_open");
		}

		final CArrayLong rateLong = new CArrayLong( 1 );
		final SWIGTYPE_p_long rate = rateLong.cast();
		final CArrayInt channelsInt = new CArrayInt( 1 );
		final SWIGTYPE_p_int channels = channelsInt.cast();
		final CArrayInt encodingInt = new CArrayInt( 1 );
		final SWIGTYPE_p_int encoding = encodingInt.cast();

		final int getFormatSuccess = libmpg123.mpg123_getformat( handle, rate, channels, encoding );

		if( getFormatSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during mpg123_getformat");
		}

		log.debug( "Got back rate(" + rateLong.getitem( 0 ) + ") channels(" +
				channelsInt.getitem( 0 ) + ") encoding(" + encodingInt.getitem( 0 ) + ")");

		log.debug( "Opened " + filePath + " for reading" );

		final int scanSuccess = libmpg123.mpg123_scan( handle );

		if( scanSuccess != mpg123_errors.MPG123_OK.swigValue() )
		{
			throw new IOException("Failed during mpg123_scan");
		}

		final long seekResult = libmpg123.mpg123_seek( handle, 0, SEEK_SET );

		if( seekResult != 0 )
		{
			throw new IOException("Failed seek to start");
		}

		final long totalFrames = libmpg123.mpg123_length( handle );
		log.debug("Have " + totalFrames + " frames");


//		final SWIGTYPE_p_unsigned_char outMemory;
//
//		final int rv = libmpg123.mpg123_decode( handle, null, 0, outmemory, outmemsize, done );
//
//		if( rv == mpg123_errors.MPG123_DONE.swigValue() )
//		{
//			log.debug("We are done");
//		}

//		final SF_INFO sf = new SF_INFO();
//
//
//		log.trace( "Beginning" );
//
//		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = libsndfile.sf_open(
//				filePath,
//				libsndfile.SFM_READ,
//				sf );
//
//		if( sndfilePtr == null )
//		{
//			throw new DatastoreException( "Unknown format (returned null)" );
//		}
//
//		final int sampleRate = sf.getSamplerate();
//		final long numFrames = sf.getFrames();
//		final int numChannels = sf.getChannels();
//		log.trace( "Apparently have opened the file with (" +
//				sampleRate + ") (" +
//				numFrames + ") (" +
//				numChannels + ")" );
//
//		final WaveFileWriter waveWriter = new WaveFileWriter( "/tmp/javalibsndfilereader.wave",
//				2,
//				sampleRate,
//				(short)16 );
//
//		final int numFramesPerRound = BUFFER_LENGTH_FLOATS / numChannels;
//
//		long numFramesLeft = numFrames;
//
//		final CArrayFloat cArrayFloat = new CArrayFloat( BUFFER_LENGTH_FLOATS );
//		final SWIGTYPE_p_float floatPtr = cArrayFloat.cast();
//
//		while( numFramesLeft > 0 )
//		{
//			final long numFramesThisRound = (numFramesLeft > numFramesPerRound ? numFramesPerRound : numFramesLeft );
//
//			final long numFramesRead = libsndfile.sf_readf_float( sndfilePtr, floatPtr, numFramesThisRound );
//
//			assert( numFramesRead == numFramesThisRound );
//
//			final int numFloatsThisRound = (int)(numFramesThisRound * numChannels);
//			for( int i = 0 ; i < numFloatsThisRound ; ++i )
//			{
//				buffer[i] = cArrayFloat.getitem( i );
//			}
//
//			waveWriter.writeFloats( buffer, numFloatsThisRound );
//
//			numFramesLeft -= numFramesThisRound;
//
////			break;
//		}
//
//
//		libsndfile.sf_close( sndfilePtr );
//
//		waveWriter.close();

		log.trace( "Done" );
	}

	public static void main( final String[] args ) throws Exception
	{
		log.trace("Beginning libmpg123 read attempt");
		final AttemptToReadAFile a = new AttemptToReadAFile();

		a.go();
	}

}
