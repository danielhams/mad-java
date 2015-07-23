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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.samplecaching.SampleCachingTestDefines;
import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;

public class TestBlockResamplingServiceMultiClient extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceMultiClient.class.getName() );

	private final static String[] inputFileNames = new String[] {
		"../../5TEST/audio-test-files/audiofiles/440hz_sine_44100_30secs_stereo.wav",
		"../../5TEST/audio-test-files/audiofiles/ExampleBeats_stereo.wav"
	};

	private final static String fileToMess =
			"../../5TEST/audio-test-files/audiofiles/ExampleBeats.flac";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private AdvancedComponentsFrontController frontController;
	private SampleCachingServiceImpl scsi;
	private BlockBufferingConfiguration bbc;
	private BlockResamplerService brs;

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() ) ;
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( SampleCachingTestDefines.BEANS_FILENAME,
				SampleCachingTestDefines.CONFIGURATION_FILENAME );

		frontController = gac.getBean( AdvancedComponentsFrontController.class );
		scsi = gac.getBean( SampleCachingServiceImpl.class );
		bbc = scsi.getBlockBufferingConfiguration();
		brs = gac.getBean( BlockResamplerService.class );
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadingAFile() throws Exception
	{
		log.debug( "Will attempt to read a file from start to end." );

//		int blockLengthInFloats = bbc.blockLengthInFloats;
		final int numChannels = 2;

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

		final BlockResamplingClient[] resampleDetails = new BlockResamplingClient[ inputFileNames.length ];

		for( int i = 0 ; i < inputFileNames.length ; ++i )
		{
			final File inputFile = new File(inputFileNames[i]);
			final SampleCacheClient scc = frontController.registerCacheClientForFile( inputFile.getAbsolutePath() );
//			resampleDetails[ i ] = new InternalResamplingClient(scc, BlockResamplingMethod.LINEAR, 0, 0.0f);
//			resampleDetails[ i ] = brs.createResamplingClient(testFiles[i], BlockResamplingMethod.LINEAR );
			resampleDetails[ i ] = brs.promoteSampleCacheClientToResamplingClient( scc, BlockResamplingMethod.LINEAR );
		}

		scsi.dumpSampleCacheToLog();

		// Move the first one and read a bit
		resampleDetails[0].setFramePosition(numFramesToRead);
		scsi.readSamplesForCacheClient( resampleDetails[0].getSampleCacheClient(), outputFrameFloats, 0, numFramesToRead);

		scsi.dumpSampleCacheToLog();

		// Now open up a new one
		final File extraFile = new File(fileToMess);
		final SampleCacheClient testCc = frontController.registerCacheClientForFile(extraFile.getAbsolutePath());

		scsi.dumpSampleCacheToLog();

		frontController.unregisterCacheClientForFile(testCc);

		for( final BlockResamplingClient trpb : resampleDetails )
		{
//			frontController.unregisterCacheClientForFile(trpb.getSampleCacheClient() );
			brs.destroyResamplingClient(trpb);
		}

		log.debug( "All done" );
	}
}
