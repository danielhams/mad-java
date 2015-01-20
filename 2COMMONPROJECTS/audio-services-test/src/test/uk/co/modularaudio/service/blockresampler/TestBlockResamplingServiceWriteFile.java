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

package test.uk.co.modularaudio.service.blockresampler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.impl.BlockResamplerServiceImpl;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestBlockResamplingServiceWriteFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceWriteFile.class.getName() );

	private static int MAX_SPEED = 10;

	// private final static String inputFile =
	// "/music/Mp3Repository/TestForPac/hc.wav";
	// private final static String inputFile =
	// "/media/663099F83099D003/Music/Mp3Repository/MyStuff200910/ExampleBeats.mp3";
//	private final static String inputFile1 = "/media/663099F83099D003/Music/PhaseVocoderAudioFiles/examplebeats_full.wav";
//	private final static String outputFile1 = "/tmp/scsout1.wav";

	// private final static String inputFile2 =
	// "/media/663099F83099D003/Music/Mp3Repository/20130215/3836570_The_Monkey_Dessert_Original_Mix.mp3";
	private final static String inputFile2 = "/media/663099F83099D003/Music/Samples/House/VocalStabs/picturethisarecordingstudio.flac";
	private final static String outputFile2 = "/tmp/scsout2.wav";

	private SpringComponentHelper sch = null;
	private GenericApplicationContext gac = null;

	private AdvancedComponentsFrontController frontController = null;
	private SampleCachingServiceImpl scsi = null;
//	private BlockBufferingConfiguration bbc = null;
	private BlockResamplerServiceImpl brsi = null;

	protected void setUp() throws Exception
	{
		List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() );
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext();

		frontController = gac.getBean( AdvancedComponentsFrontController.class );
		scsi = gac.getBean( SampleCachingServiceImpl.class );
//		bbc = scsi.getBlockBufferingConfiguration();
		brsi = gac.getBean( BlockResamplerServiceImpl.class );
	}

	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadAndWrite() throws Exception
	{
//		readWriteOneFile( inputFile1, outputFile1 );
		readWriteOneFile( inputFile2, outputFile2 );
	}

	private void readWriteOneFile( String inputFilename, String outputFilename )
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException
	{
		log.debug( "Will attempt to read a file from start to end." );
		
		ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );

		SampleCacheClient scc1 = frontController.registerCacheClientForFile( inputFilename );
//		InternalResamplingClient trpd = new InternalResamplingClient( scc1, BlockResamplingMethod.LINEAR, readFramePosition, 0.0f );
//		BlockResamplingClient brc = brsi.createResamplingClient( inputFilename, BlockResamplingMethod.LINEAR );
		BlockResamplingClient brc = brsi.promoteSampleCacheClientToResamplingClient(scc1, BlockResamplingMethod.LINEAR );
		
		int numChannels = scc1.getNumChannels();
		long totalNumFrames = scc1.getTotalNumFrames();
		long numFramesToRead = totalNumFrames;

		int outputFrameFloatsLength = 4096;
		int outputFrameLength = outputFrameFloatsLength / numChannels;
		float[] outputFrameFloats = new float[outputFrameFloatsLength];

		long readFramePosition = 0;
		scc1.setCurrentFramePosition( readFramePosition );
		
		float playbackSpeed = 0.7f;
		
		float[] outputLeftFloats = new float[outputFrameLength * MAX_SPEED];
		float[] outputRightFloats = new float[outputFrameLength * MAX_SPEED];

		Thread.sleep( 200 );

		int sampleRate = 44100;
		WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename, 2, sampleRate, (short) 16 );

		long numToRead = (totalNumFrames < numFramesToRead ? totalNumFrames : numFramesToRead);
		// Use an offset to verify block boundary reads
		while (numToRead > 0)
		{
			int numFramesThisRound = (int) (outputFrameLength < numToRead ? outputFrameLength : numToRead);
			RealtimeMethodReturnCodeEnum retCode = brsi.sampleClientFetchFramesResample( tempEventStorage.temporaryFloatArray,
					brc,
					sampleRate,
					playbackSpeed,
					outputLeftFloats,
					outputRightFloats,
					0,
					numFramesThisRound,
					false );

			channelToInterleaved( outputLeftFloats, outputRightFloats, outputFrameFloats, numFramesThisRound );

			if (retCode == RealtimeMethodReturnCodeEnum.SUCCESS)
			{
				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );

				readFramePosition = readFramePosition + numFramesThisRound;
				numToRead -= numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}
			Thread.sleep( 50 );
			scsi.dumpSampleCacheToLog();
		}

//		frontController.unregisterCacheClientForFile( scc1 );
		brsi.destroyResamplingClient(brc);

		waveFileWriter.close();

		log.debug( "All done" );
	}

	private void channelToInterleaved( float[] leftFloats, float[] rightFloats, float[] outputFrameFloats, int numFrames )
	{
		int outputIndex = 0;
		for( int i = 0 ; i < numFrames ; i++ )
		{
			outputFrameFloats[ outputIndex++ ] = leftFloats[ i ];
			outputFrameFloats[ outputIndex++ ] = rightFloats[ i ];
		}
	}
}
