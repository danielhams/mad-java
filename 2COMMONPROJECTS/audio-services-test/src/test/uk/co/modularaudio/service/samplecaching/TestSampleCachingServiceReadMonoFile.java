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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestSampleCachingServiceReadMonoFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingServiceReadMonoFile.class.getName() );

	private final static String testFile1 = "/home/dan/Temp/PhaseVocoderAudioFiles/monosine44_1_long.wav";

	private SpringComponentHelper sch = null;
	private GenericApplicationContext gac = null;

	private AdvancedComponentsFrontController frontController = null;
	private SampleCachingServiceImpl scsi = null;
	private BlockBufferingConfiguration bbc = null;

	@Override
	protected void setUp() throws Exception
	{
		List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
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
		log.debug( "Will attempt to read mono file from start to end." );

		int blockLengthInFloats = bbc.blockLengthInFloats;

		int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;

		float[] outputFrameFloats = new float[ numFloatsToRead ];

		SampleCacheClient scc1 = frontController.registerCacheClientForFile( testFile1 );
		int numChannels = scc1.getNumChannels();
		long numFrames = scc1.getTotalNumFrames();
		assert numChannels == 1;

		long curPos = 0;
		long numLeft = numFrames;

		while( curPos < numFrames )
		{
			int numThisRound = (int)( numLeft > blockLengthInFloats ? blockLengthInFloats : numLeft );

			RealtimeMethodReturnCodeEnum rc = scsi.readSamplesForCacheClient( scc1, outputFrameFloats, 0, numThisRound );

			assert rc == RealtimeMethodReturnCodeEnum.SUCCESS;

			curPos += numThisRound;
			scc1.setCurrentFramePosition( curPos );

			scsi.addJobToCachePopulationThread();

			Thread.sleep( 10 );
		}

		frontController.unregisterCacheClientForFile( scc1 );

		log.debug( "All done" );
	}
}
