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

package uk.co.modularaudio.mads.base.sampleandhold.mu;

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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SampleAndHoldMadInstance extends MadInstance<SampleAndHoldMadDefinition,SampleAndHoldMadInstance>
{
//	private static Log log = LogFactory.getLog( SampleAndHoldMadInstance.class.getName() );

	private float lastValuePulled = 0.0f;
	private float previousTriggerValue = 0.0f;

	public SampleAndHoldMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final SampleAndHoldMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
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

		final boolean inCvConnected = channelConnectedFlags.get( SampleAndHoldMadDefinition.CONSUMER_CV_IN );
		final float[] inCvFloats = channelBuffers[ SampleAndHoldMadDefinition.CONSUMER_CV_IN ].floatBuffer;
		final boolean inTriggerConnected = channelConnectedFlags.get( SampleAndHoldMadDefinition.CONSUMER_TRIGGER_IN );
		final float[] inTriggerFloats = channelBuffers[ SampleAndHoldMadDefinition.CONSUMER_TRIGGER_IN ].floatBuffer;
		final boolean outCvConnected = channelConnectedFlags.get( SampleAndHoldMadDefinition.PRODUCER_CV_OUT );
		final float[] outCvFloats = channelBuffers[ SampleAndHoldMadDefinition.PRODUCER_CV_OUT ].floatBuffer;

		if( outCvConnected )
		{
			if( inTriggerConnected && inCvConnected )
			{
				for( int s = 0 ; s < numFrames ; s++ )
				{
					final float curTriggerValue = inTriggerFloats[ s ];
					if( previousTriggerValue <= 0.0f && curTriggerValue > 0.0f )
					{
						lastValuePulled = inCvFloats[ s ];
					}
					else if( previousTriggerValue > 0.0f && curTriggerValue < 0.0f )
					{
						// Don't care.
					}
					outCvFloats[ s ] = lastValuePulled;
					previousTriggerValue = curTriggerValue;
				}
			}
			else
			{
				for( int s = 0 ; s < numFrames ; s++ )
				{
					outCvFloats[ s ] = lastValuePulled;
				}
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
