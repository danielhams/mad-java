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

package uk.co.modularaudio.mads.base.scaleandoffset.mu;

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

public class ScaleAndOffsetMadInstance extends MadInstance<ScaleAndOffsetMadDefinition,ScaleAndOffsetMadInstance>
{
//	private static Log log = LogFactory.getLog( ScaleAndOffsetMadInstance.class.getName() );

	protected float desiredScaleValue = 1.0f;
	protected float desiredOffsetValue = 0.0f;

	public ScaleAndOffsetMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final ScaleAndOffsetMadDefinition definition,
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean inScaleConnected = channelConnectedFlags.get( ScaleAndOffsetMadDefinition.CONSUMER_CV_SCALE_IDX );
		final float[] inScaleFloats = channelBuffers[ ScaleAndOffsetMadDefinition.CONSUMER_CV_SCALE_IDX ].floatBuffer;

		final boolean inOffsetConnected = channelConnectedFlags.get( ScaleAndOffsetMadDefinition.CONSUMER_CV_OFFSET_IDX );
		final float[] inOffsetFloats = channelBuffers[ ScaleAndOffsetMadDefinition.CONSUMER_CV_OFFSET_IDX ].floatBuffer;

		final boolean inValueConnected = channelConnectedFlags.get( ScaleAndOffsetMadDefinition.CONSUMER_CV_IN_IDX );
		final float[] inValueFloats = channelBuffers[ ScaleAndOffsetMadDefinition.CONSUMER_CV_IN_IDX ].floatBuffer;

		final boolean outValueConnected = channelConnectedFlags.get( ScaleAndOffsetMadDefinition.PRODUCER_CV_OUT_IDX );
		final float[] outValueFloats = channelBuffers[ ScaleAndOffsetMadDefinition.PRODUCER_CV_OUT_IDX ].floatBuffer;

		if( outValueConnected )
		{
			if( inValueConnected )
			{
				if( !inScaleConnected && !inOffsetConnected )
				{
					// Use static scale and offset
					for( int i = 0 ; i < numFrames ; i++ )
					{
						outValueFloats[ frameOffset + i ] = (inValueFloats[frameOffset + i] * desiredScaleValue) + desiredOffsetValue;
					}
				}
				else if( !inScaleConnected && inOffsetConnected )
				{
					for( int i = 0 ; i < numFrames ; i++ )
					{
						outValueFloats[ frameOffset + i ] = (inValueFloats[frameOffset + i] * desiredScaleValue) + inOffsetFloats[frameOffset + i];
					}
				}
				else if( inScaleConnected && !inOffsetConnected )
				{
					for( int i = 0 ; i < numFrames ; i++ )
					{
						outValueFloats[ frameOffset + i ] = (inValueFloats[frameOffset + i] * inScaleFloats[frameOffset + i]) + desiredOffsetValue;
					}
				}
				else
				{
					// All connected
					for( int i = 0 ; i < numFrames ; i++ )
					{
						outValueFloats[ frameOffset + i ] = (inValueFloats[frameOffset + i] * inScaleFloats[frameOffset + i]) + inOffsetFloats[frameOffset + i];
					}
				}
			}
			else
			{
				// In value not connected
				for( int i = 0 ; i < numFrames ; i++ )
				{
					outValueFloats[ frameOffset + i ] = inOffsetFloats[ frameOffset + i ];
				}
			}
		}
//		log.debug("It is " + Arrays.toString( outValueFloats ) );
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

}
