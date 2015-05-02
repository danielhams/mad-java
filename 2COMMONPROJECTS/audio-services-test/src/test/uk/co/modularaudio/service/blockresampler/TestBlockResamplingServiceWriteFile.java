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
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.NTBlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.dsp.Limiter;
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

//	private final static String inputFile = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20130215/3836570_The_Monkey_Dessert_Original_Mix.mp3";
	private final static String inputFile = "/home/dan/Music/CanLoseMusic/Albums/LFO - Frequencies (1991)/11 - Mentok 1.mp3";
//	private final static String inputFile = "/home/dan/Temp/stereo_17khzsine_2min.wav";
//	private final static String inputFile = "/home/dan/Temp/stereo_2_5khzsine_2min.wav";
//	private final static String inputFile = "/home/dan/Temp/stereo_50hzsine_2min.wav";
//	private final static String inputFile = "/home/dan/Temp/stereo_silence_10sec.wav";
//	private final static String inputFile = "/home/dan/Temp/stereo_cosina_beats.wav";
	private final static String outputFile = "/tmp/BlockResamplingTest_Old.wav";
	private final static String newOutputFile = "/tmp/BlockResamplingTest_New.wav";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private AdvancedComponentsFrontController acfc;
	private HibernateSessionController hsc;
	private SampleCachingService scsi;
	private BlockResamplerService brsi;

	private final Limiter limiter = new Limiter( 0.98, 25 );

	private final TestBufferFillCompletionListener completionListener;

//	float playbackSpeed = 2.0f;
//	float playbackSpeed = 1.19572734683248646f;
//	float playbackSpeed = 1.1f;
//	private final static float PLAYBACK_SPEED = 1.0002f;
//	private final static float PLAYBACK_SPEED = 1.0f;
//	private final static float PLAYBACK_SPEED = 0.999f;
	private final static float PLAYBACK_SPEED = 0.7f;
//	private final static float PLAYBACK_SPEED = 0.5f;
//	private final static float PLAYBACK_SPEED = 0.32435634637f;
//	private final static float PLAYBACK_SPEED = 0.1f;
//	float playbackSpeed = 0.01f;

	private final static float PLAYBACK_DIRECTION = -1.0f;

	private final static float NUM_SECONDS = 10.0f;

	private final static int OUTPUT_SAMPLE_RATE = 48000;

	public TestBlockResamplingServiceWriteFile()
	{
		completionListener = new TestBufferFillCompletionListener();
	}

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() );
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( "/samplecachingservicebeans.xml",
				"/samplecachingservicetest.properties" );

		acfc = gac.getBean( AdvancedComponentsFrontController.class );
		hsc = gac.getBean( HibernateSessionController.class );
		scsi = gac.getBean( SampleCachingService.class );
//		bbc = scsi.getBlockBufferingConfiguration();
		brsi = gac.getBean( BlockResamplerService.class );
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadAndWrite() throws Exception
	{
		if( true )
		{
			readWriteOneFile( inputFile, outputFile );
		}
		newReadWriteOneFile( inputFile, newOutputFile );
	}

	private void readWriteOneFile( final String inputFilename, final String outputFilename )
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException
	{
		log.debug( "Will attempt to read a file from start to end." );

		final ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );
		// Fill temp storage with crap to show up any issues with boundary cases
		Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );

		final SampleCacheClient scc1 = acfc.registerCacheClientForFile( inputFilename );
//		InternalResamplingClient trpd = new InternalResamplingClient( scc1, BlockResamplingMethod.LINEAR, readFramePosition, 0.0f );
//		BlockResamplingClient brc = brsi.createResamplingClient( inputFilename, BlockResamplingMethod.LINEAR );
		final BlockResamplingClient brc = brsi.promoteSampleCacheClientToResamplingClient(scc1,
				BlockResamplingMethod.CUBIC );

		final int numChannels = scc1.getNumChannels();
		final long totalNumFrames = scc1.getTotalNumFrames();
		final int sampleRate = scc1.getSampleRate();

//		final int outputSampleRate = OUTPUT_SAMPLE_RATE
		final int outputSampleRate = sampleRate;

		// Do four seconds worth of source
		final long numFramesToOutput = (long)(sampleRate * NUM_SECONDS);

		final int outputFrameFloatsLength = 4096;
		final int outputFrameLength = outputFrameFloatsLength / numChannels;
		final float[] outputFrameFloats = new float[outputFrameFloatsLength];

		long readFramePosition = totalNumFrames / 2;
		brc.setFramePosition( readFramePosition );
		brc.setFpOffset( 0.0f );
		scsi.registerForBufferFillCompletion( scc1, completionListener );
		scsi.addJobToCachePopulationThread();
		completionListener.waitForSignal();

		final float[] outputLeftFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] outputRightFloats = new float[outputFrameLength * MAX_SPEED];

		Thread.sleep( 200 );

//		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename,
//			2,
//			outputSampleRate,
//			(short) 16 );
		// Only use 1 channel while I'm debugging
		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename,
				1,
				outputSampleRate,
				(short) 16 );

		long numToOutput = numFramesToOutput;
		float playbackSpeed = PLAYBACK_SPEED * PLAYBACK_DIRECTION;
		// Use an offset to verify block boundary reads
		while (numToOutput > 0)
		{
			final int numFramesThisRound = (int) (outputFrameLength < numToOutput ? outputFrameLength : numToOutput);
			final RealtimeMethodReturnCodeEnum retCode = brsi.sampleClientFetchFramesResample( tempEventStorage.temporaryFloatArray,
					4000,
					brc,
					outputSampleRate,
					playbackSpeed,
					outputLeftFloats,
					outputRightFloats,
					0,
					numFramesThisRound,
					false );

			limiter.filter( outputLeftFloats, 0, numFramesThisRound );
			limiter.filter( outputRightFloats, 0, numFramesThisRound );

			channelToInterleaved( outputLeftFloats, outputRightFloats, outputFrameFloats, numFramesThisRound );

			if (retCode == RealtimeMethodReturnCodeEnum.SUCCESS)
			{
//				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );
				waveFileWriter.writeFloats( outputLeftFloats, numFramesThisRound );

				readFramePosition = readFramePosition + numFramesThisRound;
				numToOutput -= numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}
			scsi.registerForBufferFillCompletion( scc1, completionListener );
			scsi.addJobToCachePopulationThread();
			completionListener.waitForSignal();
			scsi.dumpSampleCacheToLog();

			if( playbackSpeed > 0.25f )
			{
				playbackSpeed -= 0.01f;
			}
//			if( playbackSpeed < 1.2f )
//			{
//				playbackSpeed += 0.01f;
//			}
		}

//		frontController.unregisterCacheClientForFile( scc1 );
		brsi.destroyResamplingClient(brc);

		waveFileWriter.close();

		log.debug( "All done" );
	}

	private void newReadWriteOneFile( final String inputFilename, final String outputFilename )
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException
	{
		log.debug( "Will attempt to read a file from start to end." );

		final ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );
		// Fill temp storage with crap to show up any issues with boundary cases
		Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );

		hsc.getThreadSession();
		final NTBlockResamplingClient brc = brsi.createNTResamplingClient( inputFilename,
				BlockResamplingMethod.CUBIC );
		final SampleCacheClient scc1 = brc.getSampleCacheClient();

		final int numChannels = scc1.getNumChannels();
		final long totalNumFrames = scc1.getTotalNumFrames();
		final int sampleRate = scc1.getSampleRate();

//		int outputSampleRate = OUTPUT_SAMPLE_RATE;
		final int outputSampleRate = sampleRate;

		// Do four seconds worth of source
		final long numFramesToOutput = (long)(sampleRate * NUM_SECONDS);

		final int outputFrameFloatsLength = 4096;
		final int outputFrameLength = outputFrameFloatsLength / numChannels;
		final float[] outputFrameFloats = new float[outputFrameFloatsLength];

		long readFramePosition = totalNumFrames / 2;
		brc.setFramePosition( readFramePosition );
		brc.setFpOffset( 0.0f );
		scsi.registerForBufferFillCompletion( scc1, completionListener );
		scsi.addJobToCachePopulationThread();
		completionListener.waitForSignal();

		final float[] outputLeftFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] outputRightFloats = new float[outputFrameLength * MAX_SPEED];

		Thread.sleep( 200 );

//		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename,
//			2,
//			outputSampleRate,
//			(short) 16 );
		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename,
				1,
				outputSampleRate,
				(short) 16 );

		long numToOutput = numFramesToOutput;
		long numSoFar = 0;
		float playbackSpeed = PLAYBACK_SPEED * PLAYBACK_DIRECTION;
		// Use an offset to verify block boundary reads
		while (numToOutput > 0)
		{
			Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );
			final int numFramesThisRound = (int) (outputFrameLength < numToOutput ? outputFrameLength : numToOutput);
//			log.debug("fetchAndResample of " + numFramesThisRound + " frames for output position " + numSoFar );
			final int outputPos = 0;
			final float[] tmpBuffer = tempEventStorage.temporaryFloatArray;
			final int tmpBufferOffset = 4000;
			final RealtimeMethodReturnCodeEnum retCode =
					brsi.fetchAndResample( brc,
							outputSampleRate,
							playbackSpeed,
							outputLeftFloats, outputRightFloats, outputPos,
							numFramesThisRound,
							tmpBuffer, tmpBufferOffset );

			limiter.filter( outputLeftFloats, 0, numFramesThisRound );
			limiter.filter( outputRightFloats, 0, numFramesThisRound );

			channelToInterleaved( outputLeftFloats, outputRightFloats, outputFrameFloats, numFramesThisRound );

			if (retCode == RealtimeMethodReturnCodeEnum.SUCCESS)
			{
//				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );
				waveFileWriter.writeFloats( outputLeftFloats, numFramesThisRound );

				readFramePosition = readFramePosition + numFramesThisRound;
				numToOutput -= numFramesThisRound;
				numSoFar += numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}
			scsi.registerForBufferFillCompletion( scc1, completionListener );
			scsi.addJobToCachePopulationThread();
			completionListener.waitForSignal();
//			scsi.dumpSampleCacheToLog();

			if( playbackSpeed > 0.25f )
			{
				playbackSpeed -= 0.01f;
			}
//			if( playbackSpeed < 1.2f )
//			{
//				playbackSpeed += 0.01f;
//			}
		}

		brsi.destroyNTResamplingClient( brc );

		waveFileWriter.close();

		hsc.releaseThreadSessionNoException();

		log.debug( "All done" );
	}

	private void channelToInterleaved( final float[] leftFloats, final float[] rightFloats, final float[] outputFrameFloats, final int numFrames )
	{
		int outputIndex = 0;
		for( int i = 0 ; i < numFrames ; i++ )
		{
			outputFrameFloats[ outputIndex++ ] = leftFloats[ i ];
			outputFrameFloats[ outputIndex++ ] = rightFloats[ i ];
		}
	}
}
