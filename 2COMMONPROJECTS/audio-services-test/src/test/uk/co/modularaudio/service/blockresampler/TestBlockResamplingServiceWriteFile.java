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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.samplecaching.SampleCachingTestDefines;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestBlockResamplingServiceWriteFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceWriteFile.class.getName() );

	private static int MAX_SPEED = 10;

	private final static String inputFileName = "../../5TEST/audio-test-files/audiofiles/440hz_sine_44100_30secs_stereo.wav";
	private final static String oldOutputFileName = "tmpoutput/BlockResamplingTest_Old.wav";
	private final static String newOutputFileName = "tmpoutput/BlockResamplingTest_New.wav";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private HibernateSessionController hsc;
	private SampleCachingController scc;
	private SampleCachingService scsi;
	private BlockResamplerService brsi;

	private final Limiter limiter = new Limiter( 0.98, 25 );

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

	public final static float VALUE_CHASE_MILLIS = 10.0f;

	public final static float PLAYBACK_DIRECTION = -1.0f;

	public final static float NUM_SECONDS = 3.0f;

	public final static int OUTPUT_SAMPLE_RATE = 48000;

	private final CyclicBarrier cb = new CyclicBarrier( 2 );
	private final CacheFillListener cfl = new CacheFillListener( cb );

	public TestBlockResamplingServiceWriteFile()
	{
	}

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() );
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( SampleCachingTestDefines.BEANS_FILENAME,
				SampleCachingTestDefines.CONFIGURATION_FILENAME );

		hsc = gac.getBean( HibernateSessionController.class );
		scc = gac.getBean( SampleCachingController.class );
		scsi = gac.getBean( SampleCachingService.class );
		brsi = gac.getBean( BlockResamplerService.class );
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

//	public void testReadAndWrite() throws Exception
//	{
//		if( true )
//		{
//			readWriteOneFile( inputFileName, oldOutputFileName );
//		}
//		readWriteOneFileVarispeed( inputFileName, newOutputFileName );
//	}

	public float updatePlaybackSpeed( final float currentSpeed )
	{
		final float absSpeed = currentSpeed * PLAYBACK_DIRECTION;
		if( absSpeed > 0.25f )
		{
			return (absSpeed - 0.01f) * PLAYBACK_DIRECTION;
		}
		else
		{
			return currentSpeed;
		}
	}

	public void testReadWriteOneFile()
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException, BrokenBarrierException, NoSuchHibernateSessionException
	{
		log.debug( "Beginning test read write one file" );

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

		final ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );
		// Fill temp storage with crap to show up any issues with boundary cases
//		Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );

		final File inputFile = new File(inputFileName);
		final Transaction t = tls.beginTransaction();
		final BlockResamplingClient brc = scc.createResamplingClient( inputFile.getAbsolutePath(), BlockResamplingMethod.CUBIC );
		t.commit();
		final SampleCacheClient cc = brc.getSampleCacheClient();

		final int numChannels = cc.getNumChannels();
		final long totalNumFrames = cc.getTotalNumFrames();
		final int sampleRate = cc.getSampleRate();

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

		// Wait to be notified a new pass has occured
		scsi.registerForBufferFillCompletion(cc, cfl );
		cb.await();

		final float[] outputLeftFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] outputRightFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] speedFloats = new float[outputFrameLength * MAX_SPEED];

		final File outputFile = new File(oldOutputFileName);
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFile.getAbsolutePath(),
			2,
			outputSampleRate,
			(short) 16 );

		long numToOutput = numFramesToOutput;
		long numSoFar = 0;
		float playbackSpeed = PLAYBACK_SPEED * PLAYBACK_DIRECTION;
		final SpringAndDamperDoubleInterpolator speedSad = new SpringAndDamperDoubleInterpolator( -MAX_SPEED, MAX_SPEED );
		speedSad.reset( outputSampleRate );
		speedSad.hardSetValue( playbackSpeed );

		// Use an offset to verify block boundary reads
		while (numToOutput > 0)
		{
//			Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );
			final int numFramesThisRound = (int) (outputFrameLength < numToOutput ? outputFrameLength : numToOutput);
//			log.debug("fetchAndResample of " + numFramesThisRound + " frames for output position " + numSoFar );

			speedSad.checkForDenormal();
			speedSad.generateControlValues( speedFloats, 0, numFramesThisRound );

			final int outputPos = 0;
			final float[] tmpBuffer = tempEventStorage.temporaryFloatArray;
			final int tmpBufferOffset = 4000;

			final RealtimeMethodReturnCodeEnum retCode =
					brsi.fetchAndResample( brc,
							outputSampleRate,
							speedFloats[0],
							outputLeftFloats, outputPos,
							outputRightFloats, outputPos,
							numFramesThisRound,
							tmpBuffer, tmpBufferOffset );

			limiter.filter( outputLeftFloats, 0, numFramesThisRound );
			limiter.filter( outputRightFloats, 0, numFramesThisRound );

			channelToInterleaved( outputLeftFloats, outputRightFloats, outputFrameFloats, numFramesThisRound );

			if (retCode == RealtimeMethodReturnCodeEnum.SUCCESS)
			{
				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );
//				waveFileWriter.writeFloats( outputLeftFloats, numFramesThisRound );

				readFramePosition = readFramePosition + numFramesThisRound;
				numToOutput -= numFramesThisRound;
				numSoFar += numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}

			// Wait to be notified a new pass has occured
			scsi.registerForBufferFillCompletion(cc, cfl);
			cb.await();

			playbackSpeed = updatePlaybackSpeed( playbackSpeed );
			speedSad.notifyOfNewValue( playbackSpeed );
		}

		brsi.destroyResamplingClient( brc );

		waveFileWriter.close();

		hsc.releaseThreadSession();

		log.debug( "All done - output " + numSoFar + " frames" );
	}

	public void testReadWriteOneFileVarispeed()
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException, BrokenBarrierException, NoSuchHibernateSessionException
	{
		log.debug( "Beginning test read write one file varispeed" );
		final ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );
		// Fill temp storage with crap to show up any issues with boundary cases
//		Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

		final File inputFile = new File(inputFileName);
		final Transaction t = tls.beginTransaction();
		final BlockResamplingClient brc = brsi.createResamplingClient( inputFile.getAbsolutePath(),
				BlockResamplingMethod.CUBIC );
		t.commit();
		final SampleCacheClient cc = brc.getSampleCacheClient();

		final int numChannels = cc.getNumChannels();
		final long totalNumFrames = cc.getTotalNumFrames();
		final int sampleRate = cc.getSampleRate();

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

		// Wait to be notified a new pass has occured
		scsi.registerForBufferFillCompletion(cc, cfl);
		cb.await();

		final float[] outputLeftFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] outputRightFloats = new float[outputFrameLength * MAX_SPEED];
		final float[] speedFloats = new float[outputFrameLength * MAX_SPEED];

		final File outputFile = new File(newOutputFileName);
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFile.getAbsolutePath(),
			2,
			outputSampleRate,
			(short) 16 );

		long numToOutput = numFramesToOutput;
		long numSoFar = 0;
		float playbackSpeed = PLAYBACK_SPEED * PLAYBACK_DIRECTION;
		final SpringAndDamperDoubleInterpolator speedSad = new SpringAndDamperDoubleInterpolator( -MAX_SPEED, MAX_SPEED );
		speedSad.reset( outputSampleRate );
		speedSad.hardSetValue( playbackSpeed );

		// Use an offset to verify block boundary reads
		while (numToOutput > 0)
		{
//			Arrays.fill( tempEventStorage.temporaryFloatArray, BlockResamplerService.MAGIC_FLOAT );
			final int numFramesThisRound = (int) (outputFrameLength < numToOutput ? outputFrameLength : numToOutput);
//			log.debug("fetchAndResample of " + numFramesThisRound + " frames for output position " + numSoFar );

			speedSad.checkForDenormal();
			speedSad.generateControlValues( speedFloats, 0, numFramesThisRound );

			final int outputPos = 0;
			final float[] tmpBuffer = tempEventStorage.temporaryFloatArray;
			final int tmpBufferOffset = 4000;

			final RealtimeMethodReturnCodeEnum retCode =
					brsi.fetchAndResampleVarispeed( brc,
							outputSampleRate,
							speedFloats, 0,
							outputLeftFloats, outputPos,
							outputRightFloats, outputPos,
							numFramesThisRound,
							tmpBuffer, tmpBufferOffset );

			limiter.filter( outputLeftFloats, 0, numFramesThisRound );
			limiter.filter( outputRightFloats, 0, numFramesThisRound );

			channelToInterleaved( outputLeftFloats, outputRightFloats, outputFrameFloats, numFramesThisRound );

			if (retCode == RealtimeMethodReturnCodeEnum.SUCCESS)
			{
				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );
//				waveFileWriter.writeFloats( outputLeftFloats, numFramesThisRound );

				readFramePosition = readFramePosition + numFramesThisRound;
				numToOutput -= numFramesThisRound;
				numSoFar += numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}

			// Wait to be notified a new pass has occured
			scsi.registerForBufferFillCompletion(cc, cfl);
			cb.await();

//			scsi.dumpSampleCacheToLog();

			playbackSpeed = updatePlaybackSpeed( playbackSpeed );
			speedSad.notifyOfNewValue( playbackSpeed );
		}

		brsi.destroyResamplingClient( brc );

		waveFileWriter.close();

		hsc.releaseThreadSession();

		log.debug( "All done - output " + numSoFar + " frames" );
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
