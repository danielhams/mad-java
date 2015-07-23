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

import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.fileio.WaveFileWriter;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class TestBlockResamplingServiceOverReadOneFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceOverReadOneFile.class.getName() );

	private final static String testFile = "../../5TEST/audio-test-files/audiofiles/ExampleBeats_stereo.wav";

	private final static String outputFileName = "tmpoutput/overreadoutput_brs.wav";

	private SpringComponentHelper sch;
	private GenericApplicationContext gac;

	private HibernateSessionController hsc;
	private SampleCachingController scc;
	private SampleCachingServiceImpl scsi;
	private BlockResamplerService brs;

	private final Limiter LIM = new Limiter( 0.98f, 22 );

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
		scsi = gac.getBean( SampleCachingServiceImpl.class );
		brs = gac.getBean( BlockResamplerService.class );
	}

	@Override
	protected void tearDown() throws Exception
	{
		gac.close();
	}

	boolean haveFloats = false;
	int i = 0 ;

	private void checkForData( final float[] leftFloats, final float[] rightFloats, final int numFloats )
	{
		for( ; i < numFloats ; ++i )
		{
			if( leftFloats[i] != 0.0f || rightFloats[i] != 0.0f )
			{
				haveFloats = true;
				break;
			}
		}
	}

	private void interleave( final float[] leftFloats, final float[] rightFloats, final int srcOffset,
			final float[] output, final int destOffset,
			final int numFrames )
	{
		for( int f = 0 ; f < numFrames ; ++f )
		{
			final int srcIndex = srcOffset + f;
			final int destIndex = destOffset + f*2;
			output[destIndex] = leftFloats[srcIndex];
			output[destIndex + 1] = rightFloats[srcIndex];
		}
	}

	public void testOverReadingAFile() throws Exception
	{
		log.debug( "Will attempt to over read a file." );

		hsc.getThreadSession();
		final Session tls = ThreadLocalSessionResource.getSessionResource();

//		int blockLengthInFloats = bbc.blockLengthInFloats;

//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
		final float playbackSpeed = 2.0f;
//		final float playbackSpeed = 1.0f;
//		final float playbackSpeed = 0.5f;

		final File inputFile = new File(testFile);
		final Transaction t = tls.beginTransaction();
		SampleCacheClient cc1 = scc.registerCacheClientForFile( inputFile.getAbsolutePath() );
		t.commit();
		final int numChannels = cc1.getNumChannels();
		final int sampleRate = cc1.getSampleRate();
		final long totalFrames = cc1.getTotalNumFrames();

		final int numFramesPerRound = 8192;
		final int numFloatsPerRound = numFramesPerRound * numChannels;

		final float[] outputLeftFloats = new float[ numFramesPerRound ];
		final float[] outputRightFloats = new float[ numFramesPerRound ];

		final ThreadSpecificTemporaryEventStorage tes = new ThreadSpecificTemporaryEventStorage( 1024 * 8 );
		final float[] tmpBuffer = tes.temporaryFloatArray;

		log.debug("Audio file has " + cc1.getTotalNumFrames() + " frames");
		final File outputFile = new File( outputFileName );
		final File parentDir = outputFile.getParentFile();
		parentDir.mkdirs();

		final WaveFileWriter waveWriter = new WaveFileWriter( outputFileName, numChannels, sampleRate, (short)16);

		// Read a block with some zeros half a second before the start
		final long halfSecondFrames = (long)(sampleRate * 0.5);
		long curPos = -halfSecondFrames;
		// Half a second after the end
		final long endPos = totalFrames + halfSecondFrames;

		final BlockResamplingClient brc = brs.promoteSampleCacheClientToResamplingClient( cc1, BlockResamplingMethod.CUBIC );

		brc.setFramePosition( curPos );
		brc.setFpOffset( 0.0f );

		// Wait for sync on position;
		boolean startGettingData = false;

		cc1 = brc.getSampleCacheClient();

		scsi.registerForBufferFillCompletion(cc1, cfl);
		cb.await();

		boolean outputEnd = false;
		haveFloats = false;

		while( curPos < endPos )
		{
			final RealtimeMethodReturnCodeEnum rtm = brs.fetchAndResample( brc, sampleRate, playbackSpeed,
					outputLeftFloats, 0, outputRightFloats, 0,
					numFramesPerRound,
					tmpBuffer, 0 );
			if( rtm != RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				log.error("Failed reading frames for under/over test");
			}
			i = 0 ;
			checkForData( outputLeftFloats, outputRightFloats, numFramesPerRound );

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
			interleave( outputLeftFloats, outputRightFloats, 0,
					tmpBuffer, 0,
					numFramesPerRound );

			LIM.filter( tmpBuffer, 0, numFloatsPerRound );

			waveWriter.writeFloats( tmpBuffer, numFloatsPerRound );

			curPos = brc.getFramePosition();

			scsi.registerForBufferFillCompletion( cc1, cfl );
			cb.await();
		}

		waveWriter.close();

		scc.unregisterCacheClientForFile( cc1 );

		hsc.releaseThreadSession();

		log.debug( "All done" );
	}
}
