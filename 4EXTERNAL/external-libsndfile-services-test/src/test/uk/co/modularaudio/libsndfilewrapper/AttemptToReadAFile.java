package test.uk.co.modularaudio.libsndfilewrapper;

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

public class AttemptToReadAFile
{
	private static Log log = LogFactory.getLog( AttemptToReadAFile.class.getName() );

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final float[] buffer = new float[ BUFFER_LENGTH_FLOATS ];

	public AttemptToReadAFile()
	{
	}

	public void go() throws Exception
	{
		LibSndfileWrapperLoader.loadIt();

		final SF_INFO sf = new SF_INFO();

		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/depeche_mode/a_broken_frame/a_photograph_of_you.ogg";
//		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/ORB - The Orb's Adventures Beyond The Ultraworld/CD 1/02 - Earth Orbit Two- Earth (Gaia).flac";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200911/974684_She_Came_Along_feat__Kid_Cudi_Sharam_s_Ecstasy_Of_Ibiza_Edit.mp3";

		log.trace( "Beginning" );

		final SWIGTYPE_p_SNDFILE_tag sndfilePtr = libsndfile.sf_open(
				filePath,
				libsndfile.SFM_READ,
				sf );

		if( sndfilePtr == null )
		{
			throw new DatastoreException( "Unknown format (returned null)" );
		}

		final int sampleRate = sf.getSamplerate();
		final long numFrames = sf.getFrames();
		final int numChannels = sf.getChannels();
		log.trace( "Apparently have opened the file with (" +
				sampleRate + ") (" +
				numFrames + ") (" +
				numChannels + ")" );

		final WaveFileWriter waveWriter = new WaveFileWriter( "/tmp/javalibsndfilereader.wave",
				2,
				sampleRate,
				(short)16 );

		final int numFramesPerRound = BUFFER_LENGTH_FLOATS / numChannels;

		long numFramesLeft = numFrames;

		final CArrayFloat cArrayFloat = new CArrayFloat( BUFFER_LENGTH_FLOATS );
		final SWIGTYPE_p_float floatPtr = cArrayFloat.cast();

		final long b = System.nanoTime();

		while( numFramesLeft > 0 )
		{
			final long numFramesThisRound = (numFramesLeft > numFramesPerRound ? numFramesPerRound : numFramesLeft );

//			final long numFramesRead = libsndfile.sf_readf_float( sndfilePtr, floatPtr, numFramesThisRound );
			final long numFramesRead = libsndfile.CustomSfReadfFloat( sndfilePtr, buffer, numFramesThisRound );

			assert( numFramesRead == numFramesThisRound );

			final int numFloatsThisRound = (int)(numFramesThisRound * numChannels);
//			for( int i = 0 ; i < numFloatsThisRound ; ++i )
//			{
//				buffer[i] = cArrayFloat.getitem( i );
//			}

			waveWriter.writeFloats( buffer, numFloatsThisRound );

			numFramesLeft -= numFramesThisRound;

//			break;
		}


		libsndfile.sf_close( sndfilePtr );

		waveWriter.close();

		final long a = System.nanoTime();
		final long diff = a-b;
		log.trace( "Done in " + diff + " nanos or " + (diff/1000.0f) + " micros or " + (diff/1000000.0f) + " millis");

		log.trace( "Done" );
	}

	public static void main( final String[] args ) throws Exception
	{
		log.trace("Beginning libsndfile read attempt");
		final AttemptToReadAFile a = new AttemptToReadAFile();

		a.go();
	}

}
