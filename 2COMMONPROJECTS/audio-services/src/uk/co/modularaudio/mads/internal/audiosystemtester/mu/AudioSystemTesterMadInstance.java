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

package uk.co.modularaudio.mads.internal.audiosystemtester.mu;

import java.util.Map;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
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
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactory;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveTableType;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class AudioSystemTesterMadInstance extends MadInstance<AudioSystemTesterMadDefinition,AudioSystemTesterMadInstance>
{
//	private static Log log = LogFactory.getLog( AudioSystemTesterMadInstance.class.getName());

	private final static float TEST_WAVE_HERTZ = 120.0f;
	private final static float TEST_WAVE_AMP = 0.5f;

	private final AudioSystemTesterMadInstanceConfiguration instanceConfiguration;
	private final OscillatorFactory of;

	private int sampleRate;
	private Oscillator oscillator;

	public AudioSystemTesterMadInstance( final InternalComponentsCreationContext creationContext,
			final String instanceName,
			final AudioSystemTesterMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
		throws MadProcessingException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		this.of = creationContext.getOscillatorFactory();

		instanceConfiguration = new AudioSystemTesterMadInstanceConfiguration( creationParameterValues );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

			oscillator = of.createOscillator( OscillatorWaveTableType.BAND_LIMITED, OscillatorInterpolationType.LINEAR, OscillatorWaveShape.SQUARE );
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing to do.
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers ,
			int frameOffset , final int numFrames  )
	{
		final int numOutputChannels = instanceConfiguration.getNumOutputChannels();

		if( numOutputChannels >= 1 )
		{
			final int genIndex = instanceConfiguration.getIndexForOutputChannel( 0 );

			final MadChannelBuffer genCb = channelBuffers[ genIndex ];
			final float[] genFloats = genCb.floatBuffer;
			oscillator.oscillate( genFloats, TEST_WAVE_HERTZ, 0.0f, 1.0f, 0, numFrames, sampleRate );

			for( int s = 0 ; s < numFrames ; s++ )
			{
				genFloats[ s ] = genFloats[ s ] * TEST_WAVE_AMP;
			}

			for( int i = 1 ; i < numOutputChannels ; i++ )
			{
				final int outputIndex = instanceConfiguration.getIndexForOutputChannel( i );

				if( channelConnectedFlags.get( outputIndex ) )
				{
					final MadChannelBuffer outputChannelBuffer = channelBuffers[ outputIndex ];
					final float[] outputFloats = outputChannelBuffer.floatBuffer;
					System.arraycopy( genFloats, 0, outputFloats, 0, numFrames );
				}
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public AudioSystemTesterMadInstanceConfiguration getInstanceConfiguration()
	{
		return instanceConfiguration;
	}
}

