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

public class TestSampleCachingServiceWriteFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingServiceWriteFile.class.getName() );

//	private final static String inputFile = "/music/Mp3Repository/TestForPac/hc.wav";
//	private final static String inputFile = "/media/663099F83099D003/Music/Mp3Repository/MyStuff200910/ExampleBeats.mp3";
//	private final static String inputFile1 = "/media/663099F83099D003/Music/PhaseVocoderAudioFiles/examplebeats_full.wav";
//	private final static String outputFile1 = "/tmp/scsout1.wav";

//	private final static String inputFile2 = "/media/663099F83099D003/Music/Mp3Repository/20130215/3836570_The_Monkey_Dessert_Original_Mix.mp3";
	private final static String inputFile2 = "/media/663099F83099D003/Music/Samples/House/VocalStabs/picturethisarecordingstudio.flac";
	private final static String outputFile2 = "/tmp/scsout2.wav";

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
//		readWriteOneFile( inputFile1, outputFile1 );
		readWriteOneFile( inputFile2, outputFile2 );
	}

	private void readWriteOneFile( final String inputFilename, final String outputFilename )
			throws DatastoreException, UnsupportedAudioFileException, InterruptedException, IOException,
			RecordNotFoundException
	{
		log.debug( "Will attempt to read a file from start to end." );

		final int blockLengthInFloats = bbc.blockLengthInFloats;
		final int numChannels = 2;

		final int numFloatsToRead = (blockLengthInFloats * 20) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final int outputFrameFloatsLength = 4096;
		final int outputFrameLength = outputFrameFloatsLength / numChannels;
		final float[] outputFrameFloats = new float[ outputFrameFloatsLength ];

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;

		final SampleCacheClient scc1 = frontController.registerCacheClientForFile( inputFilename );
		Thread.sleep( 200 );

		final int sampleRate = 44100;
		final WaveFileWriter waveFileWriter = new WaveFileWriter( outputFilename, 2, sampleRate, (short)16 );

		final long totalNumFrames = scc1.getTotalNumFrames();
		long numToRead = ( totalNumFrames < numFramesToRead ? totalNumFrames : numFramesToRead );
		// Use an offset to verify block boundary reads
		long readFramePosition = 0;
		scc1.setCurrentFramePosition( readFramePosition );
		while( numToRead > 0 )
		{
			final int numFramesThisRound = (int)(outputFrameLength < numToRead ? outputFrameLength : numToRead );
			final RealtimeMethodReturnCodeEnum retCode = scsi.readSamplesForCacheClient( scc1, outputFrameFloats, 0, numFramesThisRound );
			if( retCode == RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				waveFileWriter.writeFloats( outputFrameFloats, numFramesThisRound * numChannels );

				readFramePosition = readFramePosition + numFramesThisRound;
				scc1.setCurrentFramePosition( readFramePosition );
				numToRead -= numFramesThisRound;
			}
			else
			{
				log.debug( "File ended" );
				break;
			}
			Thread.sleep( 20 );
		}

		frontController.unregisterCacheClientForFile( scc1 );

		waveFileWriter.close();

		log.debug( "All done" );
	}
}
