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

package uk.co.modularaudio.mads.base.dctrap.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.DcTrapFilter;
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

public class DCTrapMadInstance extends MadInstance<DCTrapMadDefinition,DCTrapMadInstance>
{
//	private static Log log = LogFactory.getLog( DCTrapMadInstance.class.getName() );

	private DcTrapFilter leftDcFilter;
	private DcTrapFilter rightDcFilter;

	public DCTrapMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final DCTrapMadDefinition definition,
			final Map<MadParameterDefinition, String> params,
			final MadChannelConfiguration channels )
	{
		super( instanceName, definition, params, channels );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		final int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		leftDcFilter = new DcTrapFilter( sampleRate );
		rightDcFilter = new DcTrapFilter( sampleRate );
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// We don't do anything special on stop
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long startFrameTime,
			final MadChannelConnectedFlags connectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean inLeftWaveConnected = connectedFlags.get( DCTrapMadDefinition.CONSUMER_IN_WAVE_LEFT );
		final MadChannelBuffer inLeftWaveCb = channelBuffers[ DCTrapMadDefinition.CONSUMER_IN_WAVE_LEFT ];
		final float[] inLeftWaveFloats = inLeftWaveCb.floatBuffer;

		final boolean inRightWaveConnected = connectedFlags.get( DCTrapMadDefinition.CONSUMER_IN_WAVE_RIGHT );
		final MadChannelBuffer inRightWaveCb = channelBuffers[ DCTrapMadDefinition.CONSUMER_IN_WAVE_RIGHT ];
		final float[] inRightWaveFloats = inRightWaveCb.floatBuffer;

		final boolean outLeftWaveConnected = connectedFlags.get( DCTrapMadDefinition.PRODUCER_OUT_WAVE_LEFT );
		final MadChannelBuffer outLeftWaveCb = channelBuffers[ DCTrapMadDefinition.PRODUCER_OUT_WAVE_LEFT ];
		final float[] outLeftWaveFloats = outLeftWaveCb.floatBuffer;

		final boolean outRightWaveConnected = connectedFlags.get( DCTrapMadDefinition.PRODUCER_OUT_WAVE_RIGHT );
		final MadChannelBuffer outRightWaveCb = channelBuffers[ DCTrapMadDefinition.PRODUCER_OUT_WAVE_RIGHT ];
		final float[] outRightWaveFloats = outRightWaveCb.floatBuffer;

		// Now mix them together with the precomputed amps
		if( outLeftWaveConnected )
		{
			if( !inLeftWaveConnected )
			{
				Arrays.fill( inLeftWaveFloats, 0.0f );
			}
			System.arraycopy( inLeftWaveFloats, frameOffset, outLeftWaveFloats, frameOffset, numFrames );
			leftDcFilter.filter( outLeftWaveFloats, frameOffset, numFrames );
		}
		if( outRightWaveConnected )
		{
			if( !inRightWaveConnected )
			{
				Arrays.fill( inRightWaveFloats, 0.0f );
			}
			System.arraycopy( inRightWaveFloats, frameOffset, outRightWaveFloats, frameOffset, numFrames );
			rightDcFilter.filter( outRightWaveFloats, frameOffset, numFrames );
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
