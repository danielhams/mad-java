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

import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.impl.BlockResamplerServiceImpl;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestBlockResamplingServiceFakeValues extends TestCase
{
	private static Log log = LogFactory.getLog( TestBlockResamplingServiceFakeValues.class.getName() );

	private SampleCachingService fakeSampleCacheingService;
	private BlockResamplerServiceImpl blockResamplingService;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		log.debug("Setting up");
		fakeSampleCacheingService = new FakeSampleCachingService();
		blockResamplingService = new BlockResamplerServiceImpl();
		blockResamplingService.setSampleCachingService(fakeSampleCacheingService);

		blockResamplingService.init();
	}

	@Override
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
		float playbackSpeed = 1.0f;
//		float playbackSpeed = 0.9f;
//		float playbackSpeed = 0.5f;
//		float playbackSpeed = 0.1f;

//		BlockResamplingMethod resamplingMethod = BlockResamplingMethod.NEAREST;
//		BlockResamplingMethod resamplingMethod = BlockResamplingMethod.LINEAR;
		final BlockResamplingMethod resamplingMethod = BlockResamplingMethod.CUBIC;

		final long position = 0;
		final float offset = 0.0f;
//		float offset = 0.2f;

		final ThreadSpecificTemporaryEventStorage tempEventStorage = new ThreadSpecificTemporaryEventStorage( 8192 );

//		SampleCacheClient scc = fakeSampleCacheingService.registerCacheClientForFile("blah");
//		InternalResamplingClient resampledSamplePlaybackDetails = new InternalResamplingClient( scc, resamplingMethod, 0, 0.0f );
		final BlockResamplingClient brc = blockResamplingService.createResamplingClient( "blah", resamplingMethod );

		brc.setFramePosition( position );
		brc.setFpOffset( offset );
		final int numPerIteration = 4;
		final long numFrames = numPerIteration * 10;
		log.debug("Fake sample has " + numFrames + " frames");

		final float[] outputLeftValues = new float[ numPerIteration ];
		final float[] outputRightValues = new float[ numPerIteration ];

		final int outputSampleRate = 44100;

		long curFrame = 0;

		String valsAsString;

		log.trace("Going forwards");
		for( ; curFrame < numFrames ; curFrame += numPerIteration )
		{
			log.debug("Asking to resample from position(" + brc.getFramePosition() +
					":" + MathFormatter.fastFloatPrint( brc.getFpOffset(), 4, false) +")" );

			blockResamplingService.fetchAndResample(
					brc,
					outputSampleRate,
					playbackSpeed,
					outputLeftValues, 0,
					outputRightValues, 0,
					numPerIteration,
					tempEventStorage.temporaryFloatArray,
					0 );

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
					":" + MathFormatter.fastFloatPrint( brc.getFpOffset(), 4, false) +")" );

			blockResamplingService.fetchAndResample(
					brc,
					outputSampleRate,
					playbackSpeed,
					outputLeftValues, 0,
					outputRightValues, 0,
					numPerIteration,
					tempEventStorage.temporaryFloatArray,
					0 );

			log.debug("Resampled values at speed " + playbackSpeed + " are: " );
			valsAsString = printValsAsString(numPerIteration,
					outputLeftValues);
			log.debug( valsAsString );
		}

	}

	private String printValsAsString(final int numPerIteration,
			final float[] outputLeftValues)
	{
		final StringBuilder sb = new StringBuilder();
		for( int s = 0 ; s < numPerIteration ; ++s )
		{
			sb.append( MathFormatter.fastFloatPrint(outputLeftValues[s],  5, false ) );
			sb.append( " " );
		}
		final String valsAsString = sb.toString();
		return valsAsString;
	}

}
