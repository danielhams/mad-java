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

package uk.co.modularaudio.mads.base.prng.mu;

import java.util.Map;
import java.util.Random;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class PrngMadInstance extends MadInstance<PrngMadDefinition,PrngMadInstance>
{
	private Random randomGenerator;

	private static final int Q = 15;
	private static final float C1 = (1 << Q) - 1;
	private static final float C2 = ((int)(C1 / 3.0)) + 1;
	private static final float C3 = 1.0f / C1;

	public PrngMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final PrngMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			randomGenerator = new Random();
		}
		catch (final Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		final boolean outCvConnected = channelConnectedFlags.get( PrngMadDefinition.PRODUCER_CV_OUT );
		final float[] outCvFloats = channelBuffers[ PrngMadDefinition.PRODUCER_CV_OUT ].floatBuffer;

		if( outCvConnected )
		{
			for( int s = 0 ; s < numFrames ; s++ )
			{
//				outCvFloats[ s ] = nextPrng();
//				outCvFloats[ s ] = nextPrngAlt2();
				outCvFloats[ s ] = nextPrng();
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private float nextPrng()
	{
		final float random = randomGenerator.nextFloat();
		// (2.f * ((random * c2) + (random * c2) + (random * c2)) - 3.f * (c2 - 1.f)) * c3;
		final float noise = (2.f * ((random * C2) + (random * C2) + (random * C2)) - 3.f * (C2 - 1.f)) * C3;

		return noise;
	}
}
