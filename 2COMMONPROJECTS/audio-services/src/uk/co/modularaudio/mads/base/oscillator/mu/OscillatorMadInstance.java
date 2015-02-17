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

package uk.co.modularaudio.mads.base.oscillator.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class OscillatorMadInstance extends MadInstance<OscillatorMadDefinition,OscillatorMadInstance>
{
	private static Log log = LogFactory.getLog( OscillatorMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 1;
	private int sampleRate;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	protected float oscillationFrequency = 100.0f;
	// The actual value passed to the oscillator
	protected float runtimeOscillationFrequency = 100.0f;
	protected OscillatorWaveShape curWaveShape = OscillatorWaveShape.SINE;

	private OscillatorWaveShape usedWaveShape = OscillatorWaveShape.SINE;
	private OscillatorInstances oscillatorTables;
	private Oscillator oscillator;

	private final static boolean CHECK_NAN = false;

	public OscillatorMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final OscillatorMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		try
		{
			oscillatorTables = new OscillatorInstances( creationContext.getOscillatorFactory() );
			oscillator = oscillatorTables.getOscillator( OscillatorWaveShape.SINE );
		}
		catch( final Exception e )
		{
			final String msg ="Exception caught getting an oscillator: " + e.toString();
			log.error( msg, e );
		}
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
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{
		final boolean cvFreqConnected = channelConnectedFlags.get( OscillatorMadDefinition.CONSUMER_CV_FREQ );
		final MadChannelBuffer cvFreqBuf = channelBuffers[ OscillatorMadDefinition.CONSUMER_CV_FREQ ];
		final boolean audioOutConnected = channelConnectedFlags.get( OscillatorMadDefinition.PRODUCER_AUDIO_OUT );
		final MadChannelBuffer audioOutBuf = channelBuffers[ OscillatorMadDefinition.PRODUCER_AUDIO_OUT ];
		final boolean cvOutConnected = channelConnectedFlags.get( OscillatorMadDefinition.PRODUCER_CV_OUT );
		final MadChannelBuffer cvOutBuf = channelBuffers[ OscillatorMadDefinition.PRODUCER_CV_OUT ];
		if( !audioOutConnected && !cvOutConnected )
		{
			// Do nothing, we have no output anyway
		}
		else
		{
			float[] cvFreqFloats = null;
			if( cvFreqConnected )
			{
				cvFreqFloats = cvFreqBuf.floatBuffer;
			}

			// Need one of the buffers to render into
			final float[] genFloats = (audioOutConnected ? audioOutBuf.floatBuffer : cvOutBuf.floatBuffer );

			if( usedWaveShape != curWaveShape )
			{
				usedWaveShape = curWaveShape;
				oscillator = oscillatorTables.getOscillator( usedWaveShape );
			}

			if( cvFreqConnected )
			{
				oscillator.oscillate( genFloats, cvFreqFloats, 0.0f, 1.0f, frameOffset, numFrames, sampleRate );
			}
			else
			{
				runtimeOscillationFrequency = (runtimeOscillationFrequency * curValueRatio) + (oscillationFrequency * newValueRatio );
				oscillator.oscillate( genFloats, runtimeOscillationFrequency, 0.0f, 1.0f, frameOffset, numFrames, sampleRate );
			}

			if( audioOutConnected && cvOutConnected )
			{
				// We rendered into audio out, copy it over into the cv out
				final float[] cvOutFloats = cvOutBuf.floatBuffer;
				System.arraycopy( genFloats, frameOffset, cvOutFloats, frameOffset, numFrames );
			}
		}
		if( CHECK_NAN )
		{
			final float[] audioOutFloats = audioOutBuf.floatBuffer;
			final float[] cvOutFloats = cvOutBuf.floatBuffer;

			for( int i = 0 ; i < numFrames ; i++ )
			{
				if( audioOutConnected )
				{
					if( audioOutFloats[ frameOffset + i ] == Float.NaN )
					{
						log.error("Generated an audio NaN");
					}

					if( cvOutFloats[ frameOffset + i ] == Float.NaN )
					{
						log.error("Generated a cv NaN");
					}
				}
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
