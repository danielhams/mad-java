package test.uk.co.modularaudio.service.libmpg123audiofileio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.audiofileioregistry.impl.AudioFileIORegistryServiceImpl;
import uk.co.modularaudio.service.libmpg123audiofileio.LibMpg123AudioFileIOService;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;

public class TestReadingMp3
{
	private static Log log = LogFactory.getLog( TestReadingMp3.class.getName() );

	private final static String TEST_FILE = "/home/dan/Music/CanLoseMusic/Albums/House/Ministry Of Sound/The Annual 2004/Bonus CD/01-Fatboy Slim - Right Here Right Now.mp3";

	private final static String NOUT_FILE = "/data_slow/dan/temp/nativeoutfile.wav";

	private final static int BUFFER_LENGTH_FLOATS = 4096;

	private final AudioFileIORegistryServiceImpl firs;
	private final LibMpg123AudioFileIOService nmr;

	private final float[] buffer = new float[BUFFER_LENGTH_FLOATS];

	public TestReadingMp3() throws Exception
	{
		firs = new AudioFileIORegistryServiceImpl();
		nmr = new LibMpg123AudioFileIOService();

		nmr.setAudioFileIORegistryService( firs );

		firs.init();

		nmr.init();
	}

	public long testNative() throws Exception
	{
		final AudioFileHandleAtom fha = nmr.openForRead( TEST_FILE );
		final AudioFileIOService is = fha.getAudioFileIOService();

		final StaticMetadata sm = fha.getStaticMetadata();

		final int numChannels = sm.numChannels;
		final int sampleRate = sm.dataRate.getValue();
		final long numFrames = sm.numFrames;
		final WaveFileWriter fw = new WaveFileWriter( NOUT_FILE, numChannels, sampleRate, (short)16);

		final int numFramesPerBuffer = BUFFER_LENGTH_FLOATS / numChannels;

		long numFramesLeft = numFrames;
		long currentPositionFrames = 0;

		while( numFramesLeft > 0 )
		{
			final int numFramesThisRound = (int)(numFramesLeft > numFramesPerBuffer ? numFramesPerBuffer : numFramesLeft );

			final int numFramesRead = is.readFrames( fha, buffer, 0, numFramesThisRound, currentPositionFrames );

			if( numFramesRead == 0 )
			{
				break;
			}

			final int numFloatsRead = numFramesRead * numChannels;

			fw.writeFloats( buffer, numFloatsRead );

			currentPositionFrames += numFramesRead;
			numFramesLeft -= numFramesRead;
		}

		fw.close();

		return currentPositionFrames;
	}

	private final static int NUM_ITERATIONS = 3;

	public static void main( final String[] args ) throws Exception
	{
		final TestReadingMp3 jan = new  TestReadingMp3();

		for( int i = 0 ; i < NUM_ITERATIONS ; ++i )
		{
			final long aj = System.nanoTime();
			final long numNativeFrames = jan.testNative();
			final long an = System.nanoTime();

			final long nd = an - aj;

			log.trace( "N did " + numNativeFrames + " frames and took " + nd + "ns or " + (nd/1000) + "us or " + (nd/1000000) + "ms");

		}
	}

}
