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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.impl.SampleCachingServiceImpl;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;

public class TestBlockResamplingServiceReadOneFile extends TestCase
{
	public static Log log = LogFactory.getLog( TestBlockResamplingServiceReadOneFile.class.getName() );
	
	// First one should be a wav so the forward skipping works
	private final static String testFile = "/home/dan/tmp/ExampleBeats96000.wav";
	
	private SpringComponentHelper sch = null;
	private GenericApplicationContext gac = null;
	
	private AdvancedComponentsFrontController frontController = null;
	private SampleCachingServiceImpl scsi = null;
	private BlockBufferingConfiguration bbc = null;

	protected void setUp() throws Exception
	{
		List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		clientHelpers.add( new SpringHibernateContextHelper() ) ;
		clientHelpers.add( new PostInitPreShutdownContextHelper() );
		sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext();
		
		frontController = gac.getBean( AdvancedComponentsFrontController.class );
		scsi = gac.getBean( SampleCachingServiceImpl.class );
		bbc = scsi.getBlockBufferingConfiguration();
	}

	protected void tearDown() throws Exception
	{
		gac.close();
	}

	public void testReadingAFile() throws Exception
	{
		log.debug( "Will attempt to read a file from start to end." );
		
//		int blockLengthInFloats = bbc.blockLengthInFloats;
		int numChannels = 2;
		
		int numFloatsToRead = (bbc.blockLengthInFloats * 2) + 20;
		int numFramesToRead = numFloatsToRead / numChannels;
		
		float[] outputFrameFloats = new float[ numFloatsToRead ];
		
//		int outputFrameIndex = 0;
//		int numOutputFrames = 32;
//		float playbackSpeed = 1.0f;
		
		SampleCacheClient scc1 = frontController.registerCacheClientForFile( testFile );
		
		// Read a block with loads of zeros
		scc1.setCurrentFramePosition(-(bbc.blockLengthInFloats * 2) - 20);
		scsi.readSamplesForCacheClient(scc1, outputFrameFloats, 0, numFramesToRead );
		
		scc1.setCurrentFramePosition( scc1.getTotalNumFrames() - 20 );
		
		scsi.registerForBufferFillCompletion(scc1, new BufferFillCompletionListener()
		{
			
			@Override
			public void notifyBufferFilled(SampleCacheClient sampleCacheClient)
			{
				// Don't do anything we're using this to trigger a scan of the cache clients
			}
		});
		
		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );
		
		// Read the first block with some leading zeros
		scc1.setCurrentFramePosition( -10 );
		scsi.readSamplesForCacheClient( scc1,  outputFrameFloats,  0,  numFramesToRead );
		
		frontController.unregisterCacheClientForFile( scc1 );
		
		log.debug( "All done" );
	}
}
