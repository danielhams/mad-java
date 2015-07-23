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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.blockresampler.CacheFillListener;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestSampleCachingServiceReadMonoFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingServiceReadMonoFile.class.getName() );

	private final static String inputFileName = "../../5TEST/audio-test-files/audiofiles/ExampleBeats_mono.wav";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private HibernateSessionController hsc;
	private SampleCachingController scc;
	private SampleCachingServiceImpl scsi;
	private BlockBufferingConfiguration bbc;

	private final CyclicBarrier cb = new CyclicBarrier( 2 );
	private final CacheFillListener cfl = new CacheFillListener( cb );

	@Override
	protected void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() ) ;
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( SampleCachingTestDefines.BEANS_FILENAME, SampleCachingTestDefines.CONFIGURATION_FILENAME );

		hsc = gac.getBean( HibernateSessionController.class );
		scc = gac.getBean( SampleCachingController.class );
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

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

		final int blockLengthInFloats = bbc.blockLengthInFloats;

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

		final File inputFile = new File(inputFileName);
		final Transaction t = tls.beginTransaction();
		final SampleCacheClient scc1 = scc.registerCacheClientForFile( inputFile.getAbsolutePath() );
		t.commit();
		final int numChannels = scc1.getNumChannels();
		final long numFrames = scc1.getTotalNumFrames();
		assert numChannels == 1;

		long curPos = 0;
		final long numLeft = numFrames;

		scsi.registerForBufferFillCompletion( scc1, cfl );
		cb.await();

		while( curPos < numFrames )
		{
			final int numThisRound = (int)( numLeft > blockLengthInFloats ? blockLengthInFloats : numLeft );

			final RealtimeMethodReturnCodeEnum rc = scsi.readSamplesForCacheClient( scc1, outputFrameFloats, 0, numThisRound );

			assert rc == RealtimeMethodReturnCodeEnum.SUCCESS;

			curPos += numThisRound;
			scc1.setCurrentFramePosition( curPos );

			scsi.registerForBufferFillCompletion( scc1, cfl );
			cb.await();
		}

		scc.unregisterCacheClientForFile( scc1 );

		hsc.releaseThreadSession();

		log.debug( "All done" );
	}
}
