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

package uk.co.modularaudio.mads.base.flipflop.mu;

import java.util.Map;

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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FlipFlopMadInstance extends MadInstance<FlipFlopMadDefinition,FlipFlopMadInstance>
{
	private static final int VALUE_CHASE_MILLIS = 20;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate = -1;

	private boolean inputHigh = false;
	private final int numDesiredCyclesPerFlipFlop = 4;
	private int numCyclesSeen = 0;

	private boolean outputHigh = false;

	public FlipFlopMadInstance(  final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final FlipFlopMadDefinition definition,
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
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;
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
		final boolean inWaveConnected = channelConnectedFlags.get( FlipFlopMadDefinition.CONSUMER_IN_WAVE );
		final MadChannelBuffer inWaveCb = channelBuffers[ FlipFlopMadDefinition.CONSUMER_IN_WAVE ];
		final float[] inWaveFloats = (inWaveConnected ? inWaveCb.floatBuffer : null );

		final boolean outCvConnected = channelConnectedFlags.get( FlipFlopMadDefinition.PRODUCER_OUT_CV );
		final MadChannelBuffer outCvCb = channelBuffers[ FlipFlopMadDefinition.PRODUCER_OUT_CV ];
		final float[] outCvFloats = (outCvConnected ? outCvCb.floatBuffer : null );

		// Now mix them together with the precomputed amps
		if( outCvConnected && inWaveConnected )
		{
			for( int i = 0 ; i < numFrames ; i++ )
			{
				final float inFloat = inWaveFloats[ i ];

				if( inputHigh )
				{
					if( inFloat < 0.0f )
					{
						inputHigh = false;
						numCyclesSeen++;
					}
				}
				else
				{
					if( inFloat >= 0.0f )
					{
						inputHigh = true;
						numCyclesSeen++;
					}
				}

				if( numCyclesSeen >= numDesiredCyclesPerFlipFlop )
				{
					outputHigh = !outputHigh;
					numCyclesSeen = 0;
				}

				// output value - running offset
				outCvFloats[ i ] = ( outputHigh ? 1.0f : 0.0f );

			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
