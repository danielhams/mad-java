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

	private int framesBetweenMeterReadings = 1000;

	private long lastMeterReadingTimestamp = 0;
	private float previousInLeftMeterReading = 0.0f;
	private float previousInRightMeterReading = 0.0f;
	private float currentInLeftMeterReading = 0.0f;
	private float currentInRightMeterReading = 0.0f;

	private float previousOutLeftMeterReading = 0.0f;
	private float previousOutRightMeterReading = 0.0f;
	private float currentOutLeftMeterReading = 0.0f;
	private float currentOutRightMeterReading = 0.0f;

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
	private float rightSquaresSum = 0.0f;

	public ThresholdTypeEnum desiredThresholdType = ThresholdTypeEnum.RMS;

	protected float desiredAttack = 0.0f;
	protected int attackSamples = 0;
	protected float desiredRelease = 0.0f;
	protected int releaseSamples = 0;
	protected FixedTransitionAdsrEnvelope adsrEnvelope = new FixedTransitionAdsrEnvelope();
	protected boolean gateOn = false;

	protected float desiredMakeupGain = 1.0f;
	private float curMakeupGain = 1.0f;

	protected boolean active = false;

	private Limiter limiterRt = null;

	private float[] internalAbsCompFloats = null;
	private float[] internalThresholdDbFloats = null;
	private float[] internalEnvelopeFloats = null;
	private float[] internalAmpFloats = null;

	public boolean desiredLookahead = false;

	private int numSamplesForLookahead = -1;
	private float[] emptyPeriodFloats = null;
	private UnsafeFloatRingBuffer leftAudioRingBuffer = null;
	private UnsafeFloatRingBuffer rightAudioRingBuffer = null;

	public StereoCompressorMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			StereoCompressorMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
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
			long currentTimestamp,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		boolean inWaveLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT );
		MadChannelBuffer inWaveLeftCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_WAVE_LEFT ];
		float[] inWaveLeftFloats = (inWaveLeftConnected ? inWaveLeftCb.floatBuffer : null );

		boolean inWaveRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_WAVE_RIGHT );
		MadChannelBuffer inWaveRightCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_WAVE_RIGHT ];
		float[] inWaveRightFloats = (inWaveRightConnected ? inWaveRightCb.floatBuffer : null );

		boolean inCompLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT );
		MadChannelBuffer inCompLeftCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_COMP_LEFT ];
		float[] inCompLeftFloats = (inCompLeftConnected ? inCompLeftCb.floatBuffer : null );

		boolean inCompRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.CONSUMER_IN_COMP_RIGHT );
		MadChannelBuffer inCompRightCb = channelBuffers[ StereoCompressorMadDefinition.CONSUMER_IN_COMP_RIGHT ];
		float[] inCompRightFloats = (inCompRightConnected ? inCompRightCb.floatBuffer : null );

		boolean outWaveLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT);
		MadChannelBuffer outWaveLeftCb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_LEFT ];
		float[] outWaveLeftFloats =( outWaveLeftConnected ? outWaveLeftCb.floatBuffer : null );

		boolean outWaveRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_RIGHT );
		MadChannelBuffer outWaveRightCb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_WAVE_RIGHT ];
		float[] outWaveRightFloats  = ( outWaveRightConnected ? outWaveRightCb.floatBuffer : null );

		boolean outDryLeftConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_DRY_LEFT );
		MadChannelBuffer outDryLeftDb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_DRY_LEFT ];
		float[] outDryLeftFloats = (outDryLeftConnected ? outDryLeftDb.floatBuffer : null );

		boolean outDryRightConnected = channelConnectedFlags.get( StereoCompressorMadDefinition.PRODUCER_OUT_DRY_RIGHT );
		MadChannelBuffer outDryRightDb = channelBuffers[ StereoCompressorMadDefinition.PRODUCER_OUT_DRY_RIGHT ];
		float[] outDryRightFloats = (outDryRightConnected ? outDryRightDb.floatBuffer : null );

		float[] leftCompFloats = (inCompLeftConnected ? inCompLeftFloats : inWaveLeftFloats );
		float[] rightCompFloats = (inCompRightConnected ? inCompRightFloats : inWaveRightFloats );

		// Fill audio ring buffers (even if not used, user might switch over so lets populate them anyway
		populateOneChannelAudioRingBuffer( numFrames, inWaveLeftConnected, inWaveLeftFloats, leftAudioRingBuffer );
		populateOneChannelAudioRingBuffer( numFrames, inWaveRightConnected, inWaveRightFloats, rightAudioRingBuffer );

		// Populate the internal buffer with the incoming value(s)
		populateInternalAbsCompFloats( numFrames, leftCompFloats, rightCompFloats );

		// Walk the internal buffer looking for over / under threshold and
		// filling in the envelope buffer
		populateEnvelopeAndDbBuffer( numFrames );

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
//		processOutput( numFrames, outWaveLeftFloats, outWaveRightFloats );
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

				long floatIntBits = Float.floatToIntBits( currentInLeftMeterReading );
				long valueOut = floatIntBits << 32;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER, valueOut, null );
				previousInLeftMeterReading = currentInLeftMeterReading;
			}

			currentInLeftMeterReading = 0.0f;

			if( currentInRightMeterReading != previousInRightMeterReading )
			{
				long floatIntBits = Float.floatToIntBits( currentInRightMeterReading );
				long valueOut = (floatIntBits << 32) | 1;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_IN_METER, valueOut, null );
				previousInRightMeterReading = currentInRightMeterReading;
			}

			currentInRightMeterReading = 0.0f;

			if( currentOutLeftMeterReading != previousOutLeftMeterReading )
			{
				long floatIntBits = Float.floatToIntBits( currentOutLeftMeterReading );
				long valueOut = floatIntBits << 32;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER, valueOut, null );
				previousOutLeftMeterReading = currentOutLeftMeterReading;
			}

			currentOutLeftMeterReading = 0.0f;

			if( currentOutRightMeterReading != previousOutRightMeterReading )
			{
				long floatIntBits = Float.floatToIntBits( currentOutRightMeterReading );
				long valueOut = (floatIntBits << 32) | 1;
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_SIGNAL_OUT_METER, valueOut, null );
				previousOutRightMeterReading = currentOutRightMeterReading;
			}

			currentOutRightMeterReading = 0.0f;

			if( currentEnvMeterReading != previousEnvMeterReading )
			{
				long floatIntBits = Float.floatToIntBits( currentEnvMeterReading );
				long valueOut = (floatIntBits);
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_ENV_VALUE, valueOut, null );
				previousEnvMeterReading = currentEnvMeterReading;
			}

			currentEnvMeterReading = 0.0f;

			if( currentAttenuationMeterReading != previousAttenuationMeterReading )
			{
				long floatIntBits = Float.floatToIntBits( currentAttenuationMeterReading );
				long valueOut = (floatIntBits);
				localBridge.queueTemporalEventToUi( tempQueueEntryStorage, currentTimestamp, StereoCompressorIOQueueBridge.COMMAND_OUT_ATTENUATION, valueOut, null );
				previousAttenuationMeterReading = currentAttenuationMeterReading;
			}

			currentAttenuationMeterReading = 1.0f;
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private void populateOneChannelAudioRingBuffer( final int numFrames,
			final boolean waveConnected,
			final float[] waveFloats,
			final UnsafeFloatRingBuffer ringBuffer )
	{
		if( waveConnected )
		{
			ringBuffer.write( waveFloats, 0, numFrames );
		}
		else
		{
			ringBuffer.write( emptyPeriodFloats, 0, numFrames );
		}
	}

	private void populateInternalAbsCompFloats( final int numFrames, final float[] leftCompFloats, final float[] rightCompFloats )
	{
		for( int s = 0 ; s < numFrames ; s++ )
		{
			float leftVal = (leftCompFloats == null ? 0.0f : leftCompFloats[ s ] );
			leftVal = (leftVal < 0.0f ? -leftVal : leftVal );
			if( leftVal > currentInLeftMeterReading )
			{
				currentInLeftMeterReading = leftVal;
			}
			float rightVal = (rightCompFloats == null ? 0.0f : rightCompFloats[ s ] );
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

			internalAbsCompFloats[ s ] = absVal;
		}
	}

	private void populateEnvelopeAndDbBuffer( int numFrames )
	{
		float loopThresholdDb = curThresholdDb;

		int startIndex = 0;
		for( int s = 0 ; s < numFrames ; s++ )
		{
			loopThresholdDb = (loopThresholdDb * curValueRatio ) + (desiredThresholdDb * newValueRatio );

			float absCompVal = internalAbsCompFloats[ s ];
			float dbVal = AudioMath.levelToDbF( absCompVal );

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

	private void populateAmpAmounts( int numFrames )
	{
		float loopCompRatio = curCompRatio;
		float loopMakeupGain = curMakeupGain;

		for( int s = 0 ; s < numFrames ; s++ )
		{
			loopCompRatio = (loopCompRatio * curValueRatio ) + (desiredCompRatio * newValueRatio );
			loopMakeupGain = (loopMakeupGain * curValueRatio) + (desiredMakeupGain * newValueRatio );

			float envFloat = internalEnvelopeFloats[ s ];
			if( envFloat > currentEnvMeterReading )
			{
				currentEnvMeterReading = envFloat;
			}

			// Resulting db is dbThreshold +
			// (1) env * compRatio * dbOver
			// (2) (1 - env) * dbOver;
			// Which means that when envelope == 0
			// We still output at full volume
			float thresholdDb= internalThresholdDbFloats[ s ];

			float levelAmp;
			if( envFloat > 0.0f )
			{
				// Computing scaled amount
				float scaledDbPart = (-thresholdDb) * loopCompRatio * envFloat;
				float origDbPart = (-thresholdDb) * (1.0f - envFloat );

				float newAdjustedDb = thresholdDb + scaledDbPart + origDbPart;
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
				int numInRing = ringBuffer.getNumReadable();

				if( numInRing >= numSamplesForLookahead + numFrames )
				{
					// Only take enough to leave numSamplesForLookahead in there
					int numToTake = numInRing - numSamplesForLookahead;
					if( numToTake >= numFrames )
					{
						int numToThrowAway = numToTake - numFrames;
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
							int numZeros = numFrames - numToTake;
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
							int numZeros = numFrames - numToTake;
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

	private float processOneChannelOutput( int numFrames,
			float[] waveFloats,
			float currentOutMeterReading )
	{
		for( int s = 0 ; s < numFrames ; s++ )
		{
			float sourceVal = waveFloats[ s ];
			float outFloat = sourceVal * internalAmpFloats[ s ];
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

	protected void setActive( boolean active )
	{
		this.active = active;
//		numSamplesProcessed = 0;
	}
}
