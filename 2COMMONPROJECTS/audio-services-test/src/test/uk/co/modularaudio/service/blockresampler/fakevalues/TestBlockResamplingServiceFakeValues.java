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

package test.uk.co.modularaudio.service.blockresampler.fakevalues;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.impl.BlockResamplerServiceImpl;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestBlockResamplingServiceFakeValues extends TestCase
{
	private static Log log = LogFactory.getLog( TestBlockResamplingServiceFakeValues.class.getName() );
	
	private SampleCachingService fakeSampleCacheingService;
	private BlockResamplerServiceImpl blockResamplingService;

	protected void setUp() throws Exception
	{
		super.setUp();
		log.debug("Setting up");
		fakeSampleCacheingService = new FakeSampleCachingService();
		blockResamplingService = new BlockResamplerServiceImpl();
		blockResamplingService.setSampleCachingService(fakeSampleCacheingService);
		
		blockResamplingService.init();
	}

	protected void tearDown() throws Exception
	{
		blockResamplingService.destroy();
		super.tearDown();
		log.debug("Tearing down");
	}
	
	public void testUsingMonotonicIncreasingSamples()
		throws Exception
	{
		log.debug("Doing one test");
		
//		float playbackSpeed = 3.0f;
//		float playbackSpeed = 2.0f;
//		float playbackSpeed = 1.5f;
//		float playbackSpeed = 1.2f;
//		float playbackSpeed = 1.0f;
//		float playbackSpeed = 0.9f;
		float playbackSpeed = 0.5f;
//		float playbackSpeed = 0.1f;
		
//		BlockResamplingMethod resamplingMethod = BlockResamplingMethod.NEAREST;
//		BlockResamplingMethod resamplingMethod = BlockResamplingMethod.LINEAR;
		BlockResamplingMethod resamplingMethod = BlockResamplingMethod.CUBIC;
		
		long position = 0;
		float offset = 0.0f;
//		float offset = 0.2f;
		
		ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );
		
//		SampleCacheClient scc = fakeSampleCacheingService.registerCacheClientForFile("blah");
//		InternalResamplingClient resampledSamplePlaybackDetails = new InternalResamplingClient( scc, resamplingMethod, 0, 0.0f );
		BlockResamplingClient brc = blockResamplingService.createResamplingClient( "blah", resamplingMethod );
		
		brc.setFramePosition( position );
		brc.setFpOffset( offset );
		int numPerIteration = 4;
		long numFrames = numPerIteration * 10;
		log.debug("Fake sample has " + numFrames + " frames");
		
		float[] outputLeftValues = new float[ numPerIteration ];
		float[] outputRightValues = new float[ numPerIteration ];
		
		int outputSampleRate = 44100;
		
		long curFrame = 0;
		
		String valsAsString;
		
		log.trace("Going forwards");
		for( ; curFrame < numFrames ; curFrame += numPerIteration )
		{
			log.debug("Asking to resample from position(" + brc.getFramePosition() +
					":" + MathFormatter.slowFloatPrint( brc.getFpOffset(), 4, false) +")" );
			blockResamplingService.sampleClientFetchFramesResample( tempEventStorage.temporaryFloatArray,
					brc,
					outputSampleRate,
					playbackSpeed,
					outputLeftValues, outputRightValues,
					0,
					numPerIteration,
					false );
			log.debug("Resampled values at speed " + playbackSpeed + " are: " );
			valsAsString = printValsAsString(numPerIteration,
					outputLeftValues);
			log.debug( valsAsString );
		}
		
		// And attempt to go backwards, too
		log.trace("Going backwards");
		playbackSpeed = -playbackSpeed;
		for( ; curFrame > -16 ; curFrame -= numPerIteration )
		{
			log.debug("Asking to resample from position(" + brc.getFramePosition() +
					":" + MathFormatter.slowFloatPrint( brc.getFpOffset(), 4, false) +")" );
			blockResamplingService.sampleClientFetchFramesResample( tempEventStorage.temporaryFloatArray,
					brc,
					outputSampleRate,
					playbackSpeed,
					outputLeftValues, outputRightValues,
					0,
					numPerIteration,
					false );
			log.debug("Resampled values at speed " + playbackSpeed + " are: " );
			valsAsString = printValsAsString(numPerIteration,
					outputLeftValues);
			log.debug( valsAsString );
		}
		
	}

	private String printValsAsString(int numPerIteration,
			float[] outputLeftValues)
	{
		StringBuilder sb = new StringBuilder();
		for( int s = 0 ; s < numPerIteration ; ++s )
		{
			sb.append( MathFormatter.slowFloatPrint(outputLeftValues[s],  5, false ) );
			sb.append( " " );
		}
		String valsAsString = sb.toString();
		return valsAsString;
	}

}
