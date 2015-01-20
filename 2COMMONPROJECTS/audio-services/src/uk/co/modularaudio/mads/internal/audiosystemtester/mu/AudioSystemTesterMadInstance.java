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
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.wavetablent.Oscillator;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorFactory;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorInterpolationType;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.wavetablent.OscillatorWaveTableType;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class AudioSystemTesterMadInstance extends MadInstance<AudioSystemTesterMadDefinition,AudioSystemTesterMadInstance>
{
//	private static Log log = LogFactory.getLog( AudioSystemTesterMadInstance.class.getName());
	
	private final static float TEST_WAVE_HERTZ = 120.0f;
	private final static float TEST_WAVE_AMP = 0.5f;
	
	private AudioSystemTesterMadInstanceConfiguration instanceConfiguration = null;

	private int sampleRate = -1;
//		
	private Oscillator oscillator = null;
	
	private final OscillatorFactory of;
	
	public AudioSystemTesterMadInstance( InternalComponentsCreationContext creationContext,
			String instanceName,
			AudioSystemTesterMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
		throws MadProcessingException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		this.of = creationContext.getOscillatorFactory();

		instanceConfiguration = new AudioSystemTesterMadInstanceConfiguration( creationParameterValues );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
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
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, final int numFrames )
	{
		int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		
		if( numOutputChannels >= 1 )
		{
			int genIndex = instanceConfiguration.getIndexForOutputChannel( 0 );
					
			MadChannelBuffer genCb = channelBuffers[ genIndex ];
			float[] genFloats = genCb.floatBuffer;
			oscillator.oscillate( genFloats, TEST_WAVE_HERTZ, 0.0f, 1.0f, 0, numFrames, sampleRate );

			for( int s = 0 ; s < numFrames ; s++ )
			{
				genFloats[ s ] = genFloats[ s ] * TEST_WAVE_AMP;
			}
			
			for( int i = 1 ; i < numOutputChannels ; i++ )
			{
				int outputIndex = instanceConfiguration.getIndexForOutputChannel( i );
				
				if( channelConnectedFlags.get( outputIndex ) )
				{
					MadChannelBuffer outputChannelBuffer = channelBuffers[ outputIndex ];
					float[] outputFloats = outputChannelBuffer.floatBuffer;
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

