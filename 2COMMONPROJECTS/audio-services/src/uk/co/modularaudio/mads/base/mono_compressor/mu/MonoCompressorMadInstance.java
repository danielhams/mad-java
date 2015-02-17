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

package uk.co.modularaudio.mads.base.mono_compressor.mu;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.stereo_gate.ui.ThresholdTypeEnum;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;
import uk.co.modularaudio.util.audio.dsp.Limiter;
import uk.co.modularaudio.util.audio.envelope.FixedTransitionAdsrEnvelope;
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
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MonoCompressorMadInstance extends MadInstance<MonoCompressorMadDefinition,MonoCompressorMadInstance>
{
	private int framesBetweenMeterReadings = 1000;

	private long lastMeterReadingTimestamp = 0;
	private float previousInLeftMeterReading = 0.0f;
	private float currentInLeftMeterReading = 0.0f;

	private float previousOutLeftMeterReading = 0.0f;
	private float currentOutLeftMeterReading = 0.0f;

	private float previousEnvMeterReading = 0.0f;
	private float currentEnvMeterReading = 0.0f;

	private float previousAttenuationMeterReading = 1.0f;
	// Bit special - we attenuation, so the value goes down
	private float currentAttenuationMeterReading = 1.0f;

	private static final int VALUE_CHASE_MILLIS = 4;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	protected int sampleRate = -1;
	protected int periodLength = -1;

	protected float desiredThresholdDb = 0.0f;
	private float curThresholdDb = 1.0f;

	protected float desiredCompRatio = 0.5f;
	private float curCompRatio = 0.5f;

	private float leftSquaresSum = 0.0f;

	public ThresholdTypeEnum desiredThresholdType = ThresholdTypeEnum.RMS;

	protected float desiredAttack = 0.0f;
	protected int attackSamples = 0;
	protected float desiredRelease = 0.0f;
	protected int releaseSamples = 0;
	protected FixedTransitionAdsrEnvelope adsrEnvelope = new FixedTransitionAdsrEnvelope();
	protected boolean gateOn;

	protected float desiredMakeupGain = 1.0f;
	private float curMakeupGain = 1.0f;

	protected boolean active;

	private Limiter limiterRt;

	private float[] internalAbsCompFloats;
	private float[] internalThresholdDbFloats;
	private float[] internalEnvelopeFloats;
	private float[] internalAmpFloats;

	public boolean desiredLookahead;

	private int numSamplesForLookahead = -1;
	private float[] emptyPeriodFloats;
	private UnsafeFloatRingBuffer leftAudioRingBuffer;

	public MonoCompressorMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final MonoCompressorMadDefinition definition,
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
			periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			framesBetweenMeterReadings = timingParameters.getSampleFramesPerFrontEndPeriod();

			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;

			// Work out how many samples 4ms is at this sample rate to initialise the audio ring buffers
			numSamplesForLookahead = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 4.0f );
			leftAudioRingBuffer = new UnsafeFloatRingBuffer( numSamplesForLookahead + (periodLength * 2) );

			emptyPeriodFloats = new float[ periodLength ];
			Arrays.fill( emptyPeriodFloats, 0.0f );

			internalAbsCompFloats = new float[ periodLength ];
			internalThresholdDbFloats = new float[ periodLength ];
			internalEnvelopeFloats = new float[ periodLength ];
			internalAmpFloats = new float[ periodLength ];

			limiterRt = new Limiter( 0.99f, 5f );
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
			final long currentTimestamp ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		final boolean inWaveLeftConnected = channelConnectedFlags.get( MonoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT );
		final MadChannelBuffer inWaveLeftCb = channelBuffers[ MonoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT ];
		final float[] inWaveLeftFloats = (inWaveLeftConnected ? inWaveLeftCb.floatBuffer : null );

		final boolean inCompLeftConnected = channelConnectedFlags.get( MonoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT );
		final MadChannelBuffer inCompLeftCb = channelBuffers[ MonoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT ];
		final float[] inCompLeftFloats = (inCompLeftConnected ? inCompLeftCb.floatBuffer : null );

		final boolean outWaveLeftConnected = channelConnectedFlags.get( MonoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT);
		final MadChannelBuffer outWaveLeftCb = channelBuffers[ MonoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT ];
		final float[] outWaveLeftFloats =( outWaveLeftConnected ? outWaveLeftCb.floatBuffer : null );

		final float[] leftCompFloats = (inCompLeftConnected ? inCompLeftFloats : inWaveLeftFloats );

		// Fill audio ring buffers (even if not used, user might switch over so lets populate them anyway
		populateAudioRingBuffers( numFrames, inWaveLeftConnected, inWaveLeftFloats );

		// Populate the internal buffer with the incoming value(s)
		populateInternalAbsCompFloats( numFrames, leftCompFloats );

		// Walk the internal buffer looking for over / under threshold and
		// filling in the envelope buffer
		populateEnvelopeAndDbBuffer( numFrames );

		// Now using the envelope and db values to calculate the necessary amp amounts
		populateAmpAmounts( numFrames );

		// Copy the audio we will be processing into the output arrays
		populateOutputWithAudio( numFrames, inWaveLeftFloats, outWaveLeftFloats );

		// Now create the output values
		processOutput( numFrames, outWaveLeftFloats );

		if( lastMeterReadingTimestamp + framesBetweenMeterReadings < currentTimestamp )
		{
			lastMeterReadingTimestamp = currentTimestamp;
//			log.debug("Emitting one at " + lastMeterReadingTimestamp);
			if( currentInLeftMeterReading != previousInLeftMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentInLeftMeterReading );
				emitTemporalToUi( tempQueueEntryStorage, currentTimestamp, MonoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER, floatIntBits );
				previousInLeftMeterReading = currentInLeftMeterReading;
			}

			currentInLeftMeterReading = 0.0f;

			if( currentOutLeftMeterReading != previousOutLeftMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentOutLeftMeterReading );
				emitTemporalToUi( tempQueueEntryStorage, currentTimestamp, MonoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER, floatIntBits );
				previousOutLeftMeterReading = currentOutLeftMeterReading;
			}

			currentOutLeftMeterReading = 0.0f;

			if( currentEnvMeterReading != previousEnvMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentEnvMeterReading );
				emitTemporalToUi( tempQueueEntryStorage, currentTimestamp, MonoCompressorIOQueueBridge.COMMAND_OUT_ENV_VALUE, floatIntBits );
				previousEnvMeterReading = currentEnvMeterReading;
			}

			currentEnvMeterReading = 0.0f;

			if( currentAttenuationMeterReading != previousAttenuationMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentAttenuationMeterReading );
				emitTemporalToUi( tempQueueEntryStorage, currentTimestamp, MonoCompressorIOQueueBridge.COMMAND_OUT_ATTENUATION, floatIntBits );
				previousAttenuationMeterReading = currentAttenuationMeterReading;
			}

			currentAttenuationMeterReading = 1.0f;
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	protected void emitTemporalToUi( final ThreadSpecificTemporaryEventStorage tses,
			final long frameTime,
			final int command,
			final long value )
		throws BufferOverflowException
	{
		if( active )
		{
			localBridge.queueTemporalEventToUi( tses, frameTime, command, value, null );
		}
	}

	private void populateAudioRingBuffers( final int numFrames, final boolean inWaveLeftConnected,
			final float[] inWaveLeftFloats )
	{
		if( inWaveLeftConnected )
		{
			leftAudioRingBuffer.write( inWaveLeftFloats, 0, numFrames );
		}
		else
		{
			leftAudioRingBuffer.write( emptyPeriodFloats, 0, numFrames );
		}
	}

	private void populateInternalAbsCompFloats( final int numFrames, final float[] leftCompFloats )
	{
		for( int s = 0 ; s < numFrames ; s++ )
		{
			float leftVal = (leftCompFloats == null ? 0.0f : leftCompFloats[ s ] );
			leftVal = (leftVal < 0.0f ? -leftVal : leftVal );
			if( leftVal > currentInLeftMeterReading )
			{
				currentInLeftMeterReading = leftVal;
			}

			float absVal;
			if( desiredThresholdType == ThresholdTypeEnum.PEAK )
			{
				absVal = leftVal;
			}
			else
			{
				leftSquaresSum = ( (leftSquaresSum * curValueRatio) + ((leftVal * leftVal) * newValueRatio ) );
				absVal = (float)Math.sqrt( leftSquaresSum );
			}

			internalAbsCompFloats[ s ] = absVal;
		}
	}

	private void populateEnvelopeAndDbBuffer( final int numFrames )
	{
		float loopThresholdDb = curThresholdDb;

		int startIndex = 0;
		for( int s = 0 ; s < numFrames ; s++ )
		{
			loopThresholdDb = (loopThresholdDb * curValueRatio ) + (desiredThresholdDb * newValueRatio );

			final float absCompVal = internalAbsCompFloats[ s ];
			final float dbVal = AudioMath.levelToDbF( absCompVal );

			internalThresholdDbFloats[ s ] = loopThresholdDb;

			if( !gateOn && dbVal > loopThresholdDb )
			{
				if( s > 0 )
				{
					adsrEnvelope.outputEnvelope( internalEnvelopeFloats, startIndex, s - startIndex );
				}
				adsrEnvelope.start( false, attackSamples, 0, releaseSamples, 1.0f, 1.0f );
				gateOn = true;
				startIndex = s;
			}
			else if( gateOn && dbVal < loopThresholdDb )
			{
				if( s > 0 )
				{
					adsrEnvelope.outputEnvelope( internalEnvelopeFloats, startIndex, s - startIndex );
				}
				adsrEnvelope.release();
				gateOn = false;
				startIndex = s;
			}
		}

		if( startIndex != ( numFrames - 1 ) )
		{
			// Output ongoing last section
			adsrEnvelope.outputEnvelope( internalEnvelopeFloats, startIndex, numFrames - startIndex );
		}

		curThresholdDb = loopThresholdDb;
	}

	private void populateAmpAmounts( final int numFrames )
	{
		float loopCompRatio = curCompRatio;
		float loopMakeupGain = curMakeupGain;

		for( int s = 0 ; s < numFrames ; s++ )
		{
			loopCompRatio = (loopCompRatio * curValueRatio ) + (desiredCompRatio * newValueRatio );
			loopMakeupGain = (loopMakeupGain * curValueRatio) + (desiredMakeupGain * newValueRatio );

			final float envFloat = internalEnvelopeFloats[ s ];
			if( envFloat > currentEnvMeterReading )
			{
				currentEnvMeterReading = envFloat;
			}

			// Resulting db is dbThreshold +
			// (1) env * compRatio * dbOver
			// (2) (1 - env) * dbOver;
			// Which means that when envelope == 0
			// We still output at full volume
			final float thresholdDb= internalThresholdDbFloats[ s ];

			float levelAmp;
			if( envFloat > 0.0f )
			{
				// Computing scaled amount
				final float scaledDbPart = (-thresholdDb) * loopCompRatio * envFloat;
				final float origDbPart = (-thresholdDb) * (1.0f - envFloat );

				final float newAdjustedDb = thresholdDb + scaledDbPart + origDbPart;
//				log.debug("Adjusted db(" + newAdjustedDb +")");
				levelAmp = AudioMath.dbToLevelF( newAdjustedDb );
			}
			else
			{
				levelAmp = 1.0f;
			}
			if( levelAmp < currentAttenuationMeterReading )
			{
				currentAttenuationMeterReading = levelAmp;
			}
			internalAmpFloats[ s ] =  levelAmp * loopMakeupGain;
		}

		curCompRatio = loopCompRatio;
		curMakeupGain = loopMakeupGain;
	}

	private void populateOutputWithAudio( final int numFrames, final float[] inWaveLeftFloats,
			final float[] outWaveLeftFloats )
	{
		if( !desiredLookahead )
		{
			// Copy direct from source audio if available
			if( inWaveLeftFloats != null && outWaveLeftFloats != null )
			{
				System.arraycopy( inWaveLeftFloats, 0, outWaveLeftFloats, 0, numFrames );
			}
			else if( outWaveLeftFloats != null )
			{
				Arrays.fill( outWaveLeftFloats, 0.0f );
			}

		}
		else
		{
			// Use audio from the left and right ring buffers
			final int numInLeft = leftAudioRingBuffer.getNumReadable();

			if( numInLeft >= numSamplesForLookahead + numFrames )
			{
				// Only take enough to leave numSamplesForLookahead in there
				final int numToTake = numInLeft - numSamplesForLookahead;
				if( numToTake >= numFrames )
				{
					final int numToThrowAway = numToTake - numFrames;
					if( outWaveLeftFloats != null )
					{
						if( numToThrowAway > 0 )
						{
							leftAudioRingBuffer.read( outWaveLeftFloats, 0, numToThrowAway );
						}
						// Now read the period
						leftAudioRingBuffer.read( outWaveLeftFloats, 0, numFrames );
					}
					else
					{
						leftAudioRingBuffer.moveForward( numFrames + numToThrowAway );
					}
				}
				else
				{
					if( outWaveLeftFloats != null )
					{
						// Fill with zeros, then add on what we should read
						final int numZeros = numFrames - numToTake;
						Arrays.fill( outWaveLeftFloats, 0, numZeros, 0.0f );
						leftAudioRingBuffer.read( outWaveLeftFloats, numZeros, numToTake );
					}
					else
					{
						leftAudioRingBuffer.moveForward( numToTake );
					}
				}
			}
			else
			{
				if( outWaveLeftFloats != null )
				{
					Arrays.fill( outWaveLeftFloats, 0.0f );
				}
			}
		}
	}

	private void processOutput( final int numFrames, final float[] outWaveLeftFloats )
	{
		if( outWaveLeftFloats != null )
		{
			for( int s = 0 ; s < numFrames ; s++ )
			{
				final float leftVal = outWaveLeftFloats[ s ];
				final float outLeftFloat = leftVal * internalAmpFloats[ s ];
				if( outLeftFloat > currentOutLeftMeterReading )
				{
					currentOutLeftMeterReading = outLeftFloat;
				}
				outWaveLeftFloats[ s ] = outLeftFloat;
			}
			// Limit it
			limiterRt.filter( outWaveLeftFloats, 0, outWaveLeftFloats.length );
		}
	}
}
