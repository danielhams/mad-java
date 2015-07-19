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

package uk.co.modularaudio.mads.base.bessel4.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.Bessel4LP246Filter;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class Bessel4FilterMadInstance extends MadInstance<Bessel4FilterMadDefinition,Bessel4FilterMadInstance>
{
//	private static Log log = LogFactory.getLog( Bessel4FilterMadInstance.class.getName() );

	// Instance related vars
	int sampleRate = DataRate.CD_QUALITY.getValue();

	private FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;

	protected Bessel4LP246Filter leftFilter = new Bessel4LP246Filter();
	protected Bessel4LP246Filter rightFilter = new Bessel4LP246Filter();

	private final Limiter outputLimiter = new Limiter( 0.99f, 20 );

	public Bessel4FilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final Bessel4FilterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		leftFilter.clear( sampleRate );
		rightFilter.clear( sampleRate );
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
		final boolean inLConnected = channelConnectedFlags.get( Bessel4FilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ Bessel4FilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = inLcb.floatBuffer;
		final boolean inRConnected = channelConnectedFlags.get( Bessel4FilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ Bessel4FilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = inRcb.floatBuffer;

		final boolean outLConnected = channelConnectedFlags.get( Bessel4FilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ Bessel4FilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = outLcb.floatBuffer;
		final boolean outRConnected = channelConnectedFlags.get( Bessel4FilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ Bessel4FilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = outRcb.floatBuffer;

		if( outLConnected )
		{
			if( !inLConnected )
			{
				Arrays.fill( outLfloats, 0.0f );
			}
			else
			{
				System.arraycopy(inLfloats, frameOffset, outLfloats, frameOffset, numFrames);
				if( desiredFilterMode != FrequencyFilterMode.NONE )
				{
					leftFilter.filter( outLfloats, frameOffset, numFrames );
				}
			}
			outputLimiter.filter( outLfloats, frameOffset, numFrames );
		}

		if( outRConnected )
		{
			if( !inRConnected )
			{
				Arrays.fill( outRfloats, 0.0f );
			}
			else
			{
				System.arraycopy(inRfloats, frameOffset, outRfloats, frameOffset, numFrames);
				if( desiredFilterMode != FrequencyFilterMode.NONE )
				{
					rightFilter.filter( outRfloats, frameOffset, numFrames );
				}
			}
			outputLimiter.filter( outRfloats, frameOffset, numFrames );
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredFilterMode( final FrequencyFilterMode mode )
	{
		this.desiredFilterMode = mode;
	}
}
