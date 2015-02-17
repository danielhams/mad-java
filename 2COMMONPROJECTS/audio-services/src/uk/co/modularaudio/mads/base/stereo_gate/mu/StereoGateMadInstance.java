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

package uk.co.modularaudio.mads.base.stereo_gate.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.stereo_gate.ui.ThresholdTypeEnum;
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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class StereoGateMadInstance extends MadInstance<StereoGateMadDefinition,StereoGateMadInstance>
{
	private static Log log = LogFactory.getLog( StereoGateMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 10;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate = -1;

	protected float desiredThreshold = 0.0f;
	private float curThreshold = 1.0f;

	private float leftSquaresSum = 0.0f;
	private float rightSquaresSum = 0.0f;

	public ThresholdTypeEnum desiredThresholdType = ThresholdTypeEnum.RMS;

	public StereoGateMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final StereoGateMadDefinition definition,
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
			final MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		final boolean inWaveLeftConnected = channelConnectedFlags.get( StereoGateMadDefinition.CONSUMER_IN_WAVE_LEFT );
		final MadChannelBuffer inWaveLeftCb = channelBuffers[ StereoGateMadDefinition.CONSUMER_IN_WAVE_LEFT ];
		final float[] inWaveLeftFloats = (inWaveLeftConnected ? inWaveLeftCb.floatBuffer : null );

		final boolean inWaveRightConnected = channelConnectedFlags.get( StereoGateMadDefinition.CONSUMER_IN_WAVE_RIGHT );
		final MadChannelBuffer inWaveRightCb = channelBuffers[ StereoGateMadDefinition.CONSUMER_IN_WAVE_RIGHT ];
		final float[] inWaveRightFloats = (inWaveRightConnected ? inWaveRightCb.floatBuffer : null );

		final boolean outGateConnected = channelConnectedFlags.get( StereoGateMadDefinition.PRODUCER_OUT_CV_GATE );
		final MadChannelBuffer outGateCb = channelBuffers[ StereoGateMadDefinition.PRODUCER_OUT_CV_GATE ];
		final float[] outGateFloats =( outGateConnected ? outGateCb.floatBuffer : null );

		final boolean outOverConnected = channelConnectedFlags.get( StereoGateMadDefinition.PRODUCER_OUT_CV_OVERDRIVE );
		final MadChannelBuffer outOverCb = channelBuffers[ StereoGateMadDefinition.PRODUCER_OUT_CV_OVERDRIVE ];
		final float[] outOverFloats  = ( outOverConnected ? outOverCb.floatBuffer : null );

		float loopThreshold = curThreshold;

		switch( desiredThresholdType )
		{
			case PEAK:
			{
				for( int s = 0 ; s < numFrames ; s++ )
				{
					float leftVal = 0.0f;
					if( inWaveLeftConnected )
					{
						leftVal = inWaveLeftFloats[ s ];
						leftVal = (leftVal < 0.0f ? -leftVal : leftVal );
					}
					float rightVal = 0.0f;
					if( inWaveRightConnected )
					{
						rightVal = inWaveRightFloats[ s ];
						rightVal = (rightVal < 0.0f ? -rightVal : rightVal );
					}
					loopThreshold = (loopThreshold * curValueRatio ) + (desiredThreshold * newValueRatio );

					float amountOver = 0.0f;
					if( leftVal > loopThreshold )
					{
						amountOver = leftVal - loopThreshold;
					}
					if( rightVal > loopThreshold )
					{
						amountOver = rightVal - loopThreshold;
					}

					if( outGateConnected )
					{
						outGateFloats[ s ] = (amountOver > 0.0f ? 1.0f : 0.0f );
					}
					if( outOverConnected )
					{
						outOverFloats[ s ] = amountOver;
					}
				}
				curThreshold =  loopThreshold;
				break;
			}
			case RMS:
			{
				for( int s = 0 ; s < numFrames ; s++ )
				{
					if( inWaveLeftConnected )
					{
						final float leftVal = inWaveLeftFloats[ s ];
						leftSquaresSum = ((leftSquaresSum + (leftVal * leftVal))) / 2.0f;
					}
					final float leftRms = (float)Math.sqrt( leftSquaresSum );
					if( inWaveRightConnected )
					{
						final float rightVal = inWaveRightFloats[ s ];
						rightSquaresSum = ((rightSquaresSum + (rightVal * rightVal) )) / 2.0f;
					}
					final float rightRms = (float)Math.sqrt( rightSquaresSum );
					loopThreshold = (loopThreshold * curValueRatio ) + (desiredThreshold * newValueRatio );

					float amountOver = 0.0f;
					if( leftRms > rightRms && leftRms > loopThreshold )
					{
						amountOver = leftRms - loopThreshold;
					}
					else if( rightRms > loopThreshold )
					{
						amountOver = rightRms - loopThreshold;
					}

					if( outGateConnected )
					{
						outGateFloats[ s ] = (amountOver > 0.0f ? 1.0f : 0.0f );
					}
					if( outOverConnected )
					{
						outOverFloats[ s ] = amountOver;
					}
				}
				curThreshold =  loopThreshold;
				break;
			}
			default:
			{
				final String msg = "Unknown threshold type: " + desiredThresholdType;
				log.error( msg );
				return RealtimeMethodReturnCodeEnum.FAIL_FATAL;
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
