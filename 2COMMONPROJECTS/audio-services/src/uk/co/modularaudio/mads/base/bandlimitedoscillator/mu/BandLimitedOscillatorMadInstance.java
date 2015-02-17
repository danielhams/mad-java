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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.mu;

import java.util.Arrays;
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
import uk.co.modularaudio.util.audio.oscillatortable.NoWaveTableForShapeException;
import uk.co.modularaudio.util.audio.oscillatortable.Oscillator;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorFactoryException;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class BandLimitedOscillatorMadInstance extends MadInstance<BandLimitedOscillatorMadDefinition,BandLimitedOscillatorMadInstance>
{
	private static Log log = LogFactory.getLog( BandLimitedOscillatorMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 1;
	private int sampleRate;

	private final BandLimitedOscillatorInstances oscillatorInstances;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	protected float oscillationFrequency = 100.0f;
	// The actual value passed to the oscillator
	protected float runtimeOscillationFrequency = 100.0f;

	protected OscillatorWaveShape desiredWaveShape = OscillatorWaveShape.SAW;

	private OscillatorWaveShape usedWaveShape = OscillatorWaveShape.SAW;
	private Oscillator oscillator;

	private final static boolean CHECK_NAN = false;

	public float desiredPulsewidth = 1.0f;
	public float runtimePulsewidth = 1.0f;

	public BandLimitedOscillatorMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final BandLimitedOscillatorMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration ) throws NoWaveTableForShapeException, OscillatorFactoryException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		oscillatorInstances = new BandLimitedOscillatorInstances( creationContext.getOscillatorFactory() );
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

			oscillator = oscillatorInstances.getOscillator( usedWaveShape );
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
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames  )
	{

		final boolean cvFreqConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_FREQ );
		final MadChannelBuffer cvFreqBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_FREQ ];
		float[] cvFreqFloats = cvFreqBuf.floatBuffer;

		final boolean phaseConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_PHASE );
		final MadChannelBuffer phaseBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_PHASE ];
		final float[] phaseSamples = phaseBuf.floatBuffer;

		final boolean triggerConnected = channelConnectedFlags.get(  BandLimitedOscillatorMadDefinition.CONSUMER_CV_TRIGGER );
		final MadChannelBuffer triggerBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_TRIGGER ];
		final float[] triggerSamples = triggerBuf.floatBuffer;

		final boolean pwConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.CONSUMER_CV_PULSEWIDTH );
		final MadChannelBuffer pwBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.CONSUMER_CV_PULSEWIDTH ];
		float[] pwFloats = pwBuf.floatBuffer;

		final boolean audioOutConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.PRODUCER_AUDIO_OUT );
		final MadChannelBuffer audioOutBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.PRODUCER_AUDIO_OUT ];
		final float[] audioOutFloats = audioOutBuf.floatBuffer;

		final boolean cvOutConnected = channelConnectedFlags.get( BandLimitedOscillatorMadDefinition.PRODUCER_CV_OUT );
		final MadChannelBuffer cvOutBuf = channelBuffers[ BandLimitedOscillatorMadDefinition.PRODUCER_CV_OUT ];
		final float[] cvOutFloats = cvOutBuf.floatBuffer;

		if( !audioOutConnected && !cvOutConnected )
		{
			// Do nothing, we have no output anyway
		}
		else
		{
			if( cvFreqConnected )
			{
				cvFreqFloats = cvFreqBuf.floatBuffer;
			}

			if( pwConnected )
			{
				pwFloats = pwBuf.floatBuffer;
			}

			// Need one of the buffers to render into
			final float[] genFloats = (audioOutConnected ? audioOutBuf.floatBuffer : cvOutBuf.floatBuffer );

			if( usedWaveShape != desiredWaveShape )
			{
				oscillator = oscillatorInstances.getOscillator( desiredWaveShape );
				usedWaveShape = desiredWaveShape;
			}

			if( !triggerConnected )
			{
				if( cvFreqConnected )
				{
					if( pwConnected )
					{
						oscillator.oscillate( genFloats, cvFreqFloats, 0.0f, pwFloats, frameOffset, numFrames, sampleRate );
					}
					else
					{
						runtimePulsewidth = (runtimePulsewidth * curValueRatio ) + (desiredPulsewidth * newValueRatio );
						oscillator.oscillate( genFloats, cvFreqFloats, 0.0f, runtimePulsewidth, frameOffset, numFrames, sampleRate );
					}
				}
				else
				{
					runtimeOscillationFrequency = (runtimeOscillationFrequency * curValueRatio) + (oscillationFrequency * newValueRatio );
					if( pwConnected )
					{
						oscillator.oscillate( genFloats, runtimeOscillationFrequency, 0.0f, pwFloats, frameOffset, numFrames, sampleRate );
					}
					else
					{
						runtimePulsewidth = (runtimePulsewidth * curValueRatio ) + (desiredPulsewidth * newValueRatio );
						oscillator.oscillate( genFloats, runtimeOscillationFrequency, 0.0f, runtimePulsewidth, frameOffset, numFrames, sampleRate );
					}
				}

				if( audioOutConnected && cvOutConnected )
				{
					// We rendered into audio out, copy it over into the cv out
					System.arraycopy( genFloats, frameOffset, cvOutFloats, frameOffset, numFrames );
				}
			}
			else
			{
				// Have trigger samples - lets assume we have phase, too

				// Lets fill some false values to make it easier in the loops

				if( !cvFreqConnected )
				{
					runtimeOscillationFrequency = (runtimeOscillationFrequency * curValueRatio) + (oscillationFrequency * newValueRatio );
					Arrays.fill( cvFreqFloats, frameOffset, frameOffset + numFrames, runtimeOscillationFrequency );
				}

				if( !pwConnected )
				{
					runtimePulsewidth = (runtimePulsewidth * curValueRatio ) + (desiredPulsewidth * newValueRatio );
					Arrays.fill( pwFloats, frameOffset, frameOffset + numFrames, runtimePulsewidth );
				}

				int samplesLeft = numFrames;
				final int currentSampleIndex = 0;
				int checkStartIndex = 0;

				do
				{
					int s = currentSampleIndex;

					for( ; s < numFrames ; s++ )
					{
						if( triggerSamples[ frameOffset + s ] > 0.0f )
						{
							final int length = s - checkStartIndex;

							// Allow the oscillator to finish existing oscillation
							if( length > 0 )
							{
								// Finish the existing wave
								oscillator.oscillate( genFloats, cvFreqFloats, 0.0f, pwFloats, frameOffset + checkStartIndex, length, sampleRate );
							}
							if( phaseConnected )
							{
								oscillator.resetPhase( phaseSamples[ frameOffset + s ] );
							}
							else
							{
								oscillator.resetPhase( (float)(Math.random() ) );
							}
							checkStartIndex = s;
							samplesLeft -= length;
						}
					}

					if( s >= numFrames )
					{
						final int length = numFrames - checkStartIndex;
						// Output up to the final period sample
						oscillator.oscillate( genFloats, cvFreqFloats, 0.0f, pwFloats, frameOffset + checkStartIndex, length, sampleRate );
						samplesLeft -= length;
					}
				}
				while( samplesLeft > 0 );

				if( audioOutConnected && cvOutConnected )
				{
					// We rendered into audio out, copy it over into the cv out
					System.arraycopy( genFloats, frameOffset, cvOutFloats, frameOffset, numFrames );
				}

			}
		}

		if( CHECK_NAN )
		{

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
