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

package test.uk.co.modularaudio.service.samplecaching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestSampleCachingServiceWriteFileJumpAround extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingServiceWriteFileJumpAround.class.getName() );

	private final static String inputFileName = "../../5TEST/audio-test-files/audiofiles/ExampleBeats_mono.wav";
	private final static String outputFileName = "tmpoutput/scs_jumparound_read.wav";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private AdvancedComponentsFrontController frontController;
	private SampleCachingServiceImpl scsi;
	private BlockBufferingConfiguration bbc;

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() ) ;
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( SampleCachingTestDefines.BEANS_FILENAME, SampleCachingTestDefines.CONFIGURATION_FILENAME );

		frontController = gac.getBean( AdvancedComponentsFrontController.class );
		scsi = gac.getBean( SampleCachingServiceImpl.class );
		bbc = scsi.getBlockBufferingConfiguration();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadAndWrite() throws Exception
	{
		readWriteOneFile( inputFileName, outputFileName );
	}

	private void readWriteOneFile( final String inputFilename, final String outputFilename )
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException
	{
		log.debug( "Will attempt to read a file from start to end." );

		final int blockLengthInFloats = bbc.blockLengthInFloats;
		final int numChannels = 2;

		final int numBlocksToCopy = 20;
//		int numBlocksToCopy = 200;

		final int numFloatsToRead = (blockLengthInFloats * numBlocksToCopy) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final int outputFrameLength = 2048;
		final float[] outputFrameFloats = new float[ (outputFrameLength + 3) * numChannels ];

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;

		final File inputFile = new File(inputFilename);

		final SampleCacheClient scc1 = frontController.registerCacheClientForFile( inputFile.getAbsolutePath() );
		Thread.sleep( 200 );

		final int sampleRate = 44100;

		final File outputFile = new File( outputFilename );
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFile.getAbsolutePath(), 2, sampleRate, (short)16 );

		final long totalNumFrames = scc1.getTotalNumFrames();
		long numToRead = ( totalNumFrames < numFramesToRead ? totalNumFrames : numFramesToRead );
		// Use an offset to verify block boundary reads
		long readFramePosition = 0;
		scc1.setCurrentFramePosition( readFramePosition );
		while( numToRead > 0 )
		{
			final boolean isFirstFrame = (readFramePosition == 0);
			final int numFramesThisRound = (int)(outputFrameLength < numToRead ? outputFrameLength : numToRead );
			int numFramesWithInterpolationFrames = numFramesThisRound;
			int readArrayFramePosition = 0;
			if( isFirstFrame )
			{
				numFramesWithInterpolationFrames += 2;
				readArrayFramePosition = 1;
			}
			else
			{
				numFramesWithInterpolationFrames += 3;
				scc1.setCurrentFramePosition( readFramePosition - 1 );
			}
			final RealtimeMethodReturnCodeEnum retCode = scsi.readSamplesForCacheClient( scc1,
					outputFrameFloats, readArrayFramePosition * numChannels, numFramesWithInterpolationFrames );
			if( retCode == RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				waveFileWriter.writeFloats( outputFrameFloats, 2, numFramesThisRound * numChannels );

				readFramePosition = readFramePosition + numFramesThisRound;
				scc1.setCurrentFramePosition( readFramePosition );
				numToRead -= numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}
			// Give the cache population thread time to wake up / do its thing
			Thread.sleep( 5 );
		}

		frontController.unregisterCacheClientForFile( scc1 );

		waveFileWriter.close();

		log.debug( "All done" );
	}
}
