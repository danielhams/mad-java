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

package uk.co.modularaudio.mads.base.ms20filter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
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
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class Ms20FilterMadInstance extends MadInstance<Ms20FilterMadDefinition,Ms20FilterMadInstance>
{
//	private static Log log = LogFactory.getLog( Ms20FilterMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 2;

	public static final float MAXIMUM_RESONANCE_VALUE = 4.0f;
	public static final float MAXIMUM_THRESHOLD_VALUE = 4.0f;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private int sampleRate = -1;
	private int periodLength = -1;

	protected float currentFrequency = 20.0f;
	protected float currentFilterResonance = 1.0f;
	protected float currentSaturationThreshold = 0.9f;

	protected float desiredFrequency = 20.0f;
	protected float desiredFilterResonance = 1.0f;
	protected float desiredSaturationThreshold = 0.9f;

	protected FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.NONE;

	private final int oversamplingRatio = 4;
	private int oversamplingSampleRate = -1;
	private int oversamplingPeriodLength = -1;

	private Oversampler freqCvOversampler;
	private float[] freqCvOversampleBuffer;

	private Oversampler resCvOversampler;
	private float[] resCvOversampleBuffer;

	private Oversampler thresCvOversampler;
	private float[] thresCvOversampleBuffer;

	private float[] oversampleTemporaryBuffer;

	private Oversampler leftOversampler;
	private Oversampler rightOversampler;

	private Ms20FilterRuntime leftMs20FilterRuntime;
	private Ms20FilterRuntime rightMs20FilterRuntime;

	public Ms20FilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final Ms20FilterMadDefinition definition,
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

			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;

			oversamplingSampleRate = oversamplingRatio * sampleRate;
			oversamplingPeriodLength = oversamplingRatio * periodLength;

			freqCvOversampleBuffer = new float[ oversamplingPeriodLength ];
			freqCvOversampler = new Oversampler( sampleRate, periodLength, oversamplingRatio );

			resCvOversampleBuffer = new float[ oversamplingPeriodLength ];
			resCvOversampler = new Oversampler( sampleRate, periodLength, oversamplingRatio );

			thresCvOversampleBuffer = new float[ oversamplingPeriodLength ];
			thresCvOversampler = new Oversampler( sampleRate, periodLength, oversamplingRatio );

			oversampleTemporaryBuffer = new float[ oversamplingPeriodLength ];

			leftOversampler = new Oversampler( sampleRate, periodLength, oversamplingRatio );
			rightOversampler = new Oversampler( sampleRate, periodLength, oversamplingRatio );

			leftMs20FilterRuntime = new Ms20FilterRuntime( oversamplingSampleRate );
			rightMs20FilterRuntime = new Ms20FilterRuntime( oversamplingSampleRate );
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
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		final int numOversampledFrames = numFrames * oversamplingRatio;
		final boolean inLConnected = channelConnectedFlags.get( Ms20FilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ Ms20FilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );

		final boolean inRConnected = channelConnectedFlags.get( Ms20FilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ Ms20FilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );

		final boolean inCvFreqConnected = channelConnectedFlags.get(  Ms20FilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
		final MadChannelBuffer inFreq = channelBuffers[ Ms20FilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
		final float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );

		final boolean inCvResConnected = channelConnectedFlags.get(  Ms20FilterMadDefinition.CONSUMER_IN_CV_RESONANCE  );
		final MadChannelBuffer inRes = channelBuffers[ Ms20FilterMadDefinition.CONSUMER_IN_CV_RESONANCE ];
		final float[] inResFloats = (inCvResConnected ? inRes.floatBuffer : null );

		final boolean inCvThresConnected = channelConnectedFlags.get(  Ms20FilterMadDefinition.CONSUMER_IN_CV_THRESHOLD  );
		final MadChannelBuffer inThres = channelBuffers[ Ms20FilterMadDefinition.CONSUMER_IN_CV_THRESHOLD ];
		final float[] inThresFloats = (inCvThresConnected ? inThres.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( Ms20FilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ Ms20FilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );

		final boolean outRConnected = channelConnectedFlags.get( Ms20FilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ Ms20FilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );

		currentFrequency = (currentFrequency * curValueRatio) + (desiredFrequency * newValueRatio );
		currentFrequency = AudioMath.limitIt( currentFrequency, 1.0f, 22040.0f );

		currentFilterResonance = (currentFilterResonance * curValueRatio) + (desiredFilterResonance * newValueRatio );
		currentFilterResonance = AudioMath.limitIt( currentFilterResonance, 0.0f, MAXIMUM_RESONANCE_VALUE );

		currentSaturationThreshold = (currentSaturationThreshold * curValueRatio ) + (desiredSaturationThreshold * newValueRatio );
		currentSaturationThreshold = AudioMath.limitIt( currentSaturationThreshold, 0.0f, MAXIMUM_THRESHOLD_VALUE );

		if( inCvFreqConnected )
		{
			freqCvOversampler.oversample( inCvFreqFloats, frameOffset, numFrames, freqCvOversampleBuffer );
		}

		if( inCvResConnected )
		{
			for( int i = 0 ; i < numFrames ; i++ )
			{
				inResFloats[ i ] *= MAXIMUM_RESONANCE_VALUE;
			}
			resCvOversampler.oversample( inResFloats, frameOffset, numFrames, resCvOversampleBuffer );
		}

		if( inCvThresConnected )
		{
			for( int i = 0 ; i < numFrames ; i++ )
			{
				inThresFloats[ i ] *= MAXIMUM_THRESHOLD_VALUE;
			}
			thresCvOversampler.oversample( inThresFloats, frameOffset, numFrames, thresCvOversampleBuffer );
		}

		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, frameOffset, numFrames, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				leftOversampler.oversample( inLfloats, frameOffset, numFrames, oversampleTemporaryBuffer );
				if( inCvFreqConnected )
				{
					if( inCvResConnected )
					{
						if( inCvThresConnected )
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									freqCvOversampleBuffer,
									resCvOversampleBuffer,
									thresCvOversampleBuffer );
						}
						else
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									freqCvOversampleBuffer,
									resCvOversampleBuffer,
									currentSaturationThreshold );
						}
					}
					else
					{
						// No CV resonance connected
						if( inCvThresConnected )
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									freqCvOversampleBuffer,
									currentFilterResonance,
									thresCvOversampleBuffer );
						}
						else
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									freqCvOversampleBuffer,
									currentFilterResonance,
									currentSaturationThreshold );
						}
					}
				}
				else
				{
					// Freq not connected
					if( inCvResConnected )
					{
						if( inCvThresConnected )
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									currentFrequency,
									resCvOversampleBuffer,
									thresCvOversampleBuffer );
						}
						else
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									currentFrequency,
									resCvOversampleBuffer,
									currentSaturationThreshold );
						}
					}
					else
					{
						if( inCvThresConnected )
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									currentFrequency,
									currentFilterResonance,
									thresCvOversampleBuffer );
						}
						else
						{
							leftMs20FilterRuntime.filterFloats( desiredFilterMode,
									oversampleTemporaryBuffer,
									0,
									numOversampledFrames,
									currentFrequency,
									currentFilterResonance,
									currentSaturationThreshold );
						}
					}
				}
				leftOversampler.undersample( oversampleTemporaryBuffer, numOversampledFrames, outLfloats, frameOffset );
			}
			else
			{
				System.arraycopy( inLfloats, frameOffset, outLfloats, frameOffset, numFrames );
			}
		}

		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, frameOffset, numFrames, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			if( desiredFilterMode != FrequencyFilterMode.NONE )
			{
				rightOversampler.oversample( inRfloats, frameOffset, numFrames, oversampleTemporaryBuffer );
				if( inCvFreqConnected )
				{
					if( inCvResConnected )
					{
						if( inCvThresConnected )
						{
							rightMs20FilterRuntime.filterFloats( desiredFilterMode, oversampleTemporaryBuffer, 0, numOversampledFrames,
									freqCvOversampleBuffer, resCvOversampleBuffer, thresCvOversampleBuffer );
						}
						else
						{
							rightMs20FilterRuntime.filterFloats( desiredFilterMode, oversampleTemporaryBuffer, 0, numOversampledFrames,
									freqCvOversampleBuffer, resCvOversampleBuffer, currentSaturationThreshold );
						}
					}
					else
					{
						if( inCvThresConnected )
						{
							rightMs20FilterRuntime.filterFloats( desiredFilterMode, oversampleTemporaryBuffer, 0, numOversampledFrames,
									freqCvOversampleBuffer, currentFilterResonance, thresCvOversampleBuffer );
						}
						else
						{
							rightMs20FilterRuntime.filterFloats( desiredFilterMode, oversampleTemporaryBuffer, 0, numOversampledFrames,
									freqCvOversampleBuffer, currentFilterResonance, currentSaturationThreshold );
						}
					}
				}
				else
				{
					rightMs20FilterRuntime.filterFloats( desiredFilterMode, oversampleTemporaryBuffer, 0, numOversampledFrames, currentFrequency, currentFilterResonance, currentSaturationThreshold );
				}
				rightOversampler.undersample( oversampleTemporaryBuffer, numOversampledFrames, outRfloats, frameOffset );
			}
			else
			{
				System.arraycopy( inRfloats, 0, outRfloats, 0, numFrames );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
