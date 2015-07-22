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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;

public class TestSampleCachingService extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingService.class.getName() );

	private final static String testFile1 = "/home/dan/Temp/PhaseVocoderAudioFiles/monosine44_1_long.wav";
	private final static String testFile2 = "/home/dan/Temp/PhaseVocoderAudioFiles/sine_stereo_441k_1khz_1min.wav";

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

	public void testReadingAFile() throws Exception
	{
		log.debug( "Will attempt to read a file from start to end." );

		final int blockLengthInFloats = bbc.blockLengthInFloats;

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

		final SampleCacheClient scc1 = frontController.registerCacheClientForFile( testFile1 );
//		int file1NumChannels = scc1.getNumChannels();
		SampleCacheClient scc2 = frontController.registerCacheClientForFile( testFile2 );
		final int file2NumChannels = scc2.getNumChannels();

		// And back (should free the block up)
		final int file2NumFramesToRead = numFloatsToRead / file2NumChannels;
		final long readFramePosition = (bbc.blockLengthInFloats * 2 + 40) / file2NumChannels;
		scc2.setCurrentFramePosition( readFramePosition );

		scsi.readSamplesForCacheClient( scc2,  outputFrameFloats, 0, file2NumFramesToRead );

		// Just over a boundary
		scc2.setCurrentFramePosition( (blockLengthInFloats * 2) / file2NumChannels );

		frontController.unregisterCacheClientForFile( scc1 );
		frontController.unregisterCacheClientForFile( scc2 );

		scc2 = frontController.registerCacheClientForFile( testFile2 );

		frontController.unregisterCacheClientForFile( scc2 );
		log.debug( "All done" );
	}
}
