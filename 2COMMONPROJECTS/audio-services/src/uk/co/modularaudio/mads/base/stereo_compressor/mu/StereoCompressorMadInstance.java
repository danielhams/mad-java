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

package uk.co.modularaudio.mads.base.stereo_compressor.mu;

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

public class StereoCompressorMadInstance extends MadInstance<StereoCompressorMadDefinition,StereoCompressorMadInstance>
{
//	private static Log log = LogFactory.getLog( StereoCompressorMadInstance.class.getName() );

	private int framesBetweenMeterReadings;

	private long lastMeterReadingTimestamp;
	private float previousInLeftMeterReading;
	private float previousInRightMeterReading;
	private float currentInLeftMeterReading;
	private float currentInRightMeterReading;

	private float previousOutLeftMeterReading;
	private float previousOutRightMeterReading;
	private float currentOutLeftMeterReading;
	private float currentOutRightMeterReading;

	private float previousEnvMeterReading;
	private float currentEnvMeterReading;

	private float previousAttenuationMeterReading = 1.0f;
	// Bit special - we attenuation, so the value goes down
	private float currentAttenuationMeterReading = 1.0f;

	private static final int VALUE_CHASE_MILLIS = 4;
	protected float curValueRatio;
	protected float newValueRatio = 1.0f;

	protected int sampleRate;
	protected int periodLength;

	protected float desiredThresholdDb;
	private float curThresholdDb = 1.0f;

	protected float desiredCompRatio = 0.5f;
	private float curCompRatio = 0.5f;

	private float leftSquaresSum;
	private float rightSquaresSum;

	public ThresholdTypeEnum desiredThresholdType = ThresholdTypeEnum.RMS;

	protected float desiredAttack;
	protected int attackSamples;
	protected float desiredRelease;
	protected int releaseSamples;
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

	private int numSamplesForLookahead;
	private float[] emptyPeriodFloats;
	private UnsafeFloatRingBuffer leftAudioRingBuffer;
	private UnsafeFloatRingBuffer rightAudioRingBuffer;

	public StereoCompressorMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final StereoCompressorMadDefinition definition,
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
			rightAudioRingBuffer = new UnsafeFloatRingBuffer( numSamplesForLookahead + (periodLength * 2));
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long currentTimestamp,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final boolean inWaveLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT );
		final MadChannelBuffer inWaveLeftCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT ];
		final float[] inWaveLeftFloats = inWaveLeftCb.floatBuffer;

		final boolean inWaveRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_WAVE_RIGHT );
		final MadChannelBuffer inWaveRightCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_WAVE_RIGHT ];
		final float[] inWaveRightFloats = inWaveRightCb.floatBuffer;

		final boolean inCompLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT );
		final MadChannelBuffer inCompLeftCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT ];
		final float[] inCompLeftFloats = inCompLeftCb.floatBuffer;

		final boolean inCompRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_COMP_RIGHT );
		final MadChannelBuffer inCompRightCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_COMP_RIGHT ];
		final float[] inCompRightFloats = inCompRightCb.floatBuffer;

		final boolean outWaveLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT);
		final MadChannelBuffer outWaveLeftCb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT ];
		final float[] outWaveLeftFloats = outWaveLeftCb.floatBuffer;

		final boolean outWaveRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_RIGHT );
		final MadChannelBuffer outWaveRightCb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_RIGHT ];
		final float[] outWaveRightFloats  = outWaveRightCb.floatBuffer;

		final boolean outDryLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_DRY_LEFT );
		final MadChannelBuffer outDryLeftDb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_DRY_LEFT ];
		final float[] outDryLeftFloats = outDryLeftDb.floatBuffer;

		final boolean outDryRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_DRY_RIGHT );
		final MadChannelBuffer outDryRightDb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_DRY_RIGHT ];
		final float[] outDryRightFloats = outDryRightDb.floatBuffer;

		final float[] leftCompFloats = (inCompLeftConnected ? inCompLeftFloats : inWaveLeftFloats );
		final float[] rightCompFloats = (inCompRightConnected ? inCompRightFloats : inWaveRightFloats );

		// Fill audio ring buffers (even if not used, user might switch over so lets populate them anyway
		populateOneChannelAudioRingBuffer( frameOffset, numFrames, inWaveLeftConnected, inWaveLeftFloats, leftAudioRingBuffer );
		populateOneChannelAudioRingBuffer( frameOffset, numFrames, inWaveRightConnected, inWaveRightFloats, rightAudioRingBuffer );

		// Populate the internal buffer with the incoming value(s)
		populateInternalAbsCompFloats( frameOffset, numFrames, leftCompFloats, rightCompFloats );

		// Walk the internal buffer looking for over / under threshold and
		// filling in the envelope buffer
		populateEnvelopeAndDbBuffer( frameOffset, numFrames );

		// Now using the envelope and db values to calculate the necessary amp amounts
		populateAmpAmounts( numFrames );

		// Copy the audio we will be processing into the output arrays
		populateOneChannelOutputWithAudio( numFrames, leftAudioRingBuffer,
				inWaveLeftConnected, inWaveLeftFloats,
				outWaveLeftConnected, outWaveLeftFloats,
				outDryLeftConnected, outDryLeftFloats );
		populateOneChannelOutputWithAudio( numFrames, rightAudioRingBuffer,
				inWaveRightConnected, inWaveRightFloats,
				outWaveRightConnected, outWaveRightFloats,
				outDryRightConnected, outDryRightFloats );

		// Now create the output values
		if( outWaveLeftConnected )
		{
			currentOutLeftMeterReading = processOneChannelOutput( numFrames, outWaveLeftFloats, currentOutLeftMeterReading );
		}
		if( outWaveRightConnected )
		{
			currentOutRightMeterReading = processOneChannelOutput( numFrames, outWaveRightFloats, currentOutRightMeterReading );
		}

		if( active && (lastMeterReadingTimestamp + framesBetweenMeterReadings) < currentTimestamp )
		{
			lastMeterReadingTimestamp = currentTimestamp;
//			log.debug("Emitting one at " + lastMeterReadingTimestamp);

			if( currentInLeftMeterReading != previousInLeftMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentInLeftMeterReading );
				final long valueOut = floatIntBits << 32;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER, valueOut, null );
				previousInLeftMeterReading = currentInLeftMeterReading;
			}

			currentInLeftMeterReading = 0.0f;

			if( currentInRightMeterReading != previousInRightMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentInRightMeterReading );
				final long valueOut = (floatIntBits << 32) | 1;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER, valueOut, null );
				previousInRightMeterReading = currentInRightMeterReading;
			}

			currentInRightMeterReading = 0.0f;

			if( currentOutLeftMeterReading != previousOutLeftMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentOutLeftMeterReading );
				final long valueOut = floatIntBits << 32;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER, valueOut, null );
				previousOutLeftMeterReading = currentOutLeftMeterReading;
			}

			currentOutLeftMeterReading = 0.0f;

			if( currentOutRightMeterReading != previousOutRightMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentOutRightMeterReading );
				final long valueOut = (floatIntBits << 32) | 1;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER, valueOut, null );
				previousOutRightMeterReading = currentOutRightMeterReading;
			}

			currentOutRightMeterReading = 0.0f;

			if( currentEnvMeterReading != previousEnvMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentEnvMeterReading );
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_ENV_VALUE, floatIntBits, null );
				previousEnvMeterReading = currentEnvMeterReading;
			}

			currentEnvMeterReading = 0.0f;

			if( currentAttenuationMeterReading != previousAttenuationMeterReading )
			{
				final long floatIntBits = Float.floatToIntBits( currentAttenuationMeterReading );
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_ATTENUATION, floatIntBits, null );
				previousAttenuationMeterReading = currentAttenuationMeterReading;
			}

			currentAttenuationMeterReading = 1.0f;
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private void populateOneChannelAudioRingBuffer(
			final int frameOffset,
			final int numFrames,
			final boolean waveConnected,
			final float[] waveFloats,
			final UnsafeFloatRingBuffer ringBuffer )
	{
		if( waveConnected )
		{
			ringBuffer.write( waveFloats, frameOffset, numFrames );
		}
		else
		{
			ringBuffer.write( emptyPeriodFloats, 0, numFrames );
		}
	}

	private void populateInternalAbsCompFloats(
			final int frameOffset,
			final int numFrames,
			final float[] leftCompFloats,
			final float[] rightCompFloats )
	{
		for( int s = 0 ; s < numFrames ; s++ )
		{
			final int index = frameOffset + s;
			float leftVal = (leftCompFloats == null ? 0.0f : leftCompFloats[ index ] );
			leftVal = (leftVal < 0.0f ? -leftVal : leftVal );
			if( leftVal > currentInLeftMeterReading )
			{
				currentInLeftMeterReading = leftVal;
			}
			float rightVal = (rightCompFloats == null ? 0.0f : rightCompFloats[ index ] );
			rightVal = (rightVal < 0.0f ? -rightVal : rightVal );
			if( rightVal > currentInRightMeterReading )
			{
				currentInRightMeterReading = rightVal;
			}

			float absVal;
			if( desiredThresholdType == ThresholdTypeEnum.PEAK )
			{
				absVal = (leftVal > rightVal ? leftVal : rightVal );
			}
			else
			{
				leftSquaresSum = ( (leftSquaresSum * curValueRatio) + ((leftVal * leftVal) * newValueRatio ) );
				rightSquaresSum = ( (rightSquaresSum * curValueRatio) + ((rightVal * rightVal) * newValueRatio ) );
				absVal = (leftSquaresSum > rightSquaresSum ? leftSquaresSum : rightSquaresSum );
				absVal = (float)Math.sqrt( absVal );
			}

			internalAbsCompFloats[ index ] = absVal;
		}
	}

	private void populateEnvelopeAndDbBuffer(
			final int frameOffset,
			final int numFrames )
	{
		float loopThresholdDb = curThresholdDb;

		int startIndex = 0;
		for( int s = 0 ; s < numFrames ; s++ )
		{
			final int index = frameOffset + s;
			loopThresholdDb = (loopThresholdDb * curValueRatio ) + (desiredThresholdDb * newValueRatio );

			final float absCompVal = internalAbsCompFloats[ index ];
			final float dbVal = AudioMath.levelToDbF( absCompVal );

			internalThresholdDbFloats[ index ] = loopThresholdDb;

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

	private void populateOneChannelOutputWithAudio( final int numFrames,
			final UnsafeFloatRingBuffer ringBuffer,
			final boolean inWaveConnected,
			final float[] inWaveFloats,
			final boolean outWaveConnected,
			final float[] outWaveFloats,
			final boolean outDryConnected,
			final float[] outDryFloats )
	{
		if( !desiredLookahead )
		{
			// Copy direct from source audio if available
			if( inWaveConnected )
			{
				if( outWaveConnected )
				{
					System.arraycopy( inWaveFloats, 0, outWaveFloats, 0, numFrames );
				}
				if( outDryConnected )
				{
					System.arraycopy( inWaveFloats, 0, outDryFloats, 0, numFrames );
				}
			}
			else
			{
				if( outWaveConnected )
				{
					Arrays.fill( outWaveFloats, 0.0f );
				}
				if( outDryConnected )
				{
					Arrays.fill( outDryFloats, 0.0f );
				}
			}
		}
		else // desired look ahead, use ring buffer
		{
			if( inWaveConnected )
			{
				// Use audio from the left and right ring buffers
				final int numInRing = ringBuffer.getNumReadable();

				if( numInRing >= numSamplesForLookahead + numFrames )
				{
					// Only take enough to leave numSamplesForLookahead in there
					final int numToTake = numInRing - numSamplesForLookahead;
					if( numToTake >= numFrames )
					{
						final int numToThrowAway = numToTake - numFrames;
						if( outWaveConnected )
						{
							if( numToThrowAway > 0 )
							{
								ringBuffer.read( outWaveFloats, 0, numToThrowAway );
							}
							// Now read the period
							ringBuffer.read( outWaveFloats, 0, numFrames );
							if( outDryConnected )
							{
								System.arraycopy( outWaveFloats, 0, outDryFloats, 0, numFrames );
							}
						}
						else
						{
							ringBuffer.moveForward( numFrames + numToThrowAway );
						}
					}
					else // Don't have enough in the ring buffer
					{
						if( outWaveConnected )
						{
							// Fill with zeros, then add on what we should read
							final int numZeros = numFrames - numToTake;
							Arrays.fill( outWaveFloats, 0, numZeros, 0.0f );
							ringBuffer.read( outWaveFloats, numZeros, numToTake );
							if( outDryConnected )
							{
								System.arraycopy( outWaveFloats, 0, outDryFloats, 0, numFrames );
							}
						}
						else if( outDryConnected )
						{
							// Fill with zeros, then add on what we should read
							final int numZeros = numFrames - numToTake;
							Arrays.fill( outDryFloats, 0, numZeros, 0.0f );
							ringBuffer.read( outDryFloats, numZeros, numToTake );
						}
						else
						{
							ringBuffer.moveForward( numToTake );
						}
					}
				}
			}
			else // !inWaveConnected
			{
				if( outWaveConnected )
				{
					Arrays.fill( outWaveFloats, 0.0f );
				}
				if( outDryConnected )
				{
					Arrays.fill( outDryFloats, 0.0f );
				}
			}
		}
	}

	private float processOneChannelOutput( final int numFrames,
			final float[] waveFloats,
			final float iCurrentOutMeterReading )
	{
		float currentOutMeterReading = iCurrentOutMeterReading;
		for( int s = 0 ; s < numFrames ; s++ )
		{
			final float sourceVal = waveFloats[ s ];
			final float outFloat = sourceVal * internalAmpFloats[ s ];
			if( outFloat > currentOutMeterReading )
			{
				currentOutMeterReading = outFloat;
			}
			waveFloats[ s ] = outFloat;
		}
		// Limit it
		limiterRt.filter( waveFloats, 0, waveFloats.length );
		return currentOutMeterReading;
	}

	protected void setActive( final boolean active )
	{
		this.active = active;
//		numSamplesProcessed = 0;
	}
}
