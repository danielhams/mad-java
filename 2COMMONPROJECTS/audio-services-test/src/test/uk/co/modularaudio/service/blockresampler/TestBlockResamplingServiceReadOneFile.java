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
import java.util.concurrent.CyclicBarrier;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.samplecaching.SampleCachingTestDefines;
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

public class TestBlockResamplingServiceReadOneFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceReadOneFile.class.getName() );

	// First one should be a wav so the forward skipping works
	private final static String inputFilename = "../../5TEST/audio-test-files/audiofiles/ExampleBeats_stereo.wav";

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
		gac = sch.makeAppContext( SampleCachingTestDefines.BEANS_FILENAME,
				SampleCachingTestDefines.CONFIGURATION_FILENAME );

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
		log.debug( "Will attempt to read a file from start to end." );

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

//		int blockLengthInFloats = bbc.blockLengthInFloats;
		final int numChannels = 2;

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;

		final File inputFile = new File(inputFilename);
		final Transaction t = tls.beginTransaction();
		final SampleCacheClient scc1 = scc.registerCacheClientForFile( inputFile.getAbsolutePath() );
		t.commit();

		// Read a block with loads of zeros
		scc1.setCurrentFramePosition(-(bbc.blockLengthInFloats * 2) - 20);
		scsi.readSamplesForCacheClient(scc1, outputFrameFloats, 0, numFramesToRead );

		scc1.setCurrentFramePosition( scc1.getTotalNumFrames() - 20 );

		scsi.registerForBufferFillCompletion(scc1, cfl);
		cb.await();

		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );

		// Read the first block with some leading zeros
		scc1.setCurrentFramePosition( -10 );
		scsi.registerForBufferFillCompletion(scc1, cfl);
		cb.await();
		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );

		scc.unregisterCacheClientForFile( scc1 );

		hsc.releaseThreadSession();

		log.debug( "All done" );
	}
}
