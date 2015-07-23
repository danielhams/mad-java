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
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestSampleCachingServiceOverReadOneFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestSampleCachingServiceOverReadOneFile.class.getName() );

	// First one should be a wav so the forward skipping works
	private final static String testFileName = "../../5TEST/audio-test-files/audiofiles/ExampleBeats_stereo.wav";
	private final static String outputFileName = "tmpoutput/overreadoutput_scs.wav";

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
		gac = sch.makeAppContext( "/samplecachingservicebeans.xml", "/samplecachingservicetest.properties" );

		hsc = gac.getBean( HibernateSessionController.class );
		scc = gac.getBean( SampleCachingController.class );
		scsi = (SampleCachingServiceImpl)gac.getBean( SampleCachingService.class );
		bbc = scsi.getBlockBufferingConfiguration();
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	boolean haveFloats = false;
	int i = 0 ;

	private void checkForData( final float[] floats, final int numFloats )
	{
		for( ; i < numFloats ; ++i )
		{
			if( floats[i] != 0.0f )
			{
				haveFloats = true;
				break;
			}
		}
	}

	public void testOverReadingAFile() throws Exception
	{
		log.debug( "Will attempt to over read a file." );

//		int blockLengthInFloats = bbc.blockLengthInFloats;

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

		final File inputFile = new File(testFileName);

		final Transaction t = tls.beginTransaction();
		final SampleCacheClient scc1 = scc.registerCacheClientForFile( inputFile.getAbsolutePath() );
		t.commit();
		final int numChannels = scc1.getNumChannels();
		final int sampleRate = scc1.getSampleRate();
		final long totalFrames = scc1.getTotalNumFrames();

		final int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;
		final int numFramesToRead = numFloatsToRead / numChannels;

		final float[] outputFrameFloats = new float[ numFloatsToRead ];

		log.debug("Audio file has " + scc1.getTotalNumFrames() + " frames");

		final File outputFile = new File(outputFileName);
		final File outputDir = outputFile.getParentFile();
		outputDir.mkdirs();

		final WaveFileWriter waveWriter = new WaveFileWriter( outputFile.getAbsolutePath(), numChannels, sampleRate, (short)16);

		// Read a block with some zeros half a second before the start
		final long halfSecondFrames = (long)(sampleRate * 0.5);
		long curPos = -halfSecondFrames;
		// Half a second after the end
		final long endPos = totalFrames + halfSecondFrames;

		scc1.setCurrentFramePosition( curPos );

		scsi.registerForBufferFillCompletion( scc1, cfl );
		cb.await();

		boolean startGettingData = false;
		boolean outputEnd = false;
		haveFloats = false;

		while( curPos < endPos )
		{
			final RealtimeMethodReturnCodeEnum rtm = scsi.readSamplesForCacheClient( scc1, outputFrameFloats, 0, numFramesToRead );
			if( rtm != RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				log.error("Failed reading frames for under/over test");
			}
			i = 0 ;
			checkForData( outputFrameFloats, numFloatsToRead );
			if( !startGettingData && haveFloats )
			{
				log.debug("Started getting data at frame " + (curPos + (i/numChannels)) );
				startGettingData = true;
			}
			else if( startGettingData && !haveFloats )
			{
				if( !outputEnd )
				{
					log.debug("Stopped getting data at frame " + (curPos + (i/numChannels)) );
					outputEnd = true;
				}
			}
			waveWriter.writeFloats( outputFrameFloats, numFloatsToRead );
			curPos += numFramesToRead;
			scc1.setCurrentFramePosition( curPos );
			scsi.registerForBufferFillCompletion( scc1, cfl );
			cb.await();
		}

		waveWriter.close();

		scc.unregisterCacheClientForFile( scc1 );

		hsc.releaseThreadSession();

		log.debug( "All done" );
	}
}
