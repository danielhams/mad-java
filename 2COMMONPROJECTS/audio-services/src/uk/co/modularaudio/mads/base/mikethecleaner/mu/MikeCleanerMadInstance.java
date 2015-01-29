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

package uk.co.modularaudio.mads.base.mikethecleaner.mu;

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

public class MikeCleanerMadInstance extends MadInstance<MikeCleanerMadDefinition,MikeCleanerMadInstance>
{
	private static final int VALUE_CHASE_MILLIS = 1;
	private long sampleRate = 0;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	public float threshold = 0.0f;

	public MikeCleanerMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final MikeCleanerMadDefinition definition,
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		final boolean inConnected = channelConnectedFlags.get( MikeCleanerMadDefinition.CONSUMER_AUDIO_IN );
		final MadChannelBuffer inCb = channelBuffers[ MikeCleanerMadDefinition.CONSUMER_AUDIO_IN ];
		final float[] inFloats = (inConnected ? inCb.floatBuffer : null );

		final boolean outConnected = channelConnectedFlags.get( MikeCleanerMadDefinition.PRODUCER_AUDIO_OUT );
		final MadChannelBuffer outCb = channelBuffers[ MikeCleanerMadDefinition.PRODUCER_AUDIO_OUT ];
		final float[] outFloats = (outConnected ? outCb.floatBuffer : null );

		if( inConnected && outConnected )
		{
			for( int i = 0 ; i < numFrames ; i++ )
			{
				float val = inFloats[i];
				if( Math.abs( val ) < threshold )
				{
					val = 0.0f;
				}
				outFloats[i] = val;
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
