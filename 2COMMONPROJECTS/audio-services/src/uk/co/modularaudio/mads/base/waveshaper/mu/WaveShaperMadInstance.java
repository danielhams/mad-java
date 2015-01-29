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

package uk.co.modularaudio.mads.base.waveshaper.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.waveshaper.mu.WaveShaperWaveTables.WaveType;
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
import uk.co.modularaudio.util.audio.wavetable.raw.RawWaveTable;
import uk.co.modularaudio.util.audio.wavetable.waveshaper.InterpolatingWaveShaper;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class WaveShaperMadInstance extends MadInstance<WaveShaperMadDefinition, WaveShaperMadInstance>
{
	private final WaveShaperWaveTables waveShaperWaveTables = new WaveShaperWaveTables();

	public WaveType curWaveType = WaveType.COMPRESSOR;

	public WaveShaperMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final WaveShaperMadDefinition definition,
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
			final MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		final boolean inConnected = channelConnectedFlags.get( WaveShaperMadDefinition.CONSUMER_AUDIO_IN );
		final float[] inFloats = channelBuffers[ WaveShaperMadDefinition.CONSUMER_AUDIO_IN ].floatBuffer;

		final boolean outConnected = channelConnectedFlags.get( WaveShaperMadDefinition.PRODUCER_AUDIO_OUT );
		final float[] outFloats = channelBuffers[ WaveShaperMadDefinition.PRODUCER_AUDIO_OUT ].floatBuffer;

		final RawWaveTable shaperTable = waveShaperWaveTables.getTable( curWaveType );

		if( outConnected )
		{
			if( inConnected )
			{
				System.arraycopy( inFloats, 0, outFloats, 0, numFrames );
//				TruncatingWaveShaper.shape( shaperTable, outFloats, numFrames );
				InterpolatingWaveShaper.shape( shaperTable, outFloats, numFrames );
			}
			else
			{
				Arrays.fill( outFloats, 0.0f );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
