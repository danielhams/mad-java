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

package uk.co.modularaudio.mads.base.feedbackdelay.mu;

import java.util.Arrays;
import java.util.Map;

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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodErrorContext;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FeedbackDelayMadInstance extends MadInstance<FeedbackDelayMadDefinition, FeedbackDelayMadInstance>
{
//	private static Log log = LogFactory.getLog( FeedbackDelayMadInstance.class.getName() );
	
	private static final int VALUE_CHASE_MILLIS = 1;

	public static final int MAX_DELAY_MILLIS = 1000 * 2;
	
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	protected int sampleRate = -1;

	protected int desiredDelayFrames = 0;
	protected float desiredFeedback = 0.0f;
	
	private int delayBufferLength = -1;
//	private FloatRingBuffer leftDelayBuffer = null;
//	private FloatRingBuffer rightDelayBuffer = null;
	private DelayBuffer leftDelayBuffer = null;
	private DelayBuffer rightDelayBuffer = null;

	private RealtimeMethodErrorContext errctx = new RealtimeMethodErrorContext();
	
	public FeedbackDelayMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			FeedbackDelayMadDefinition definition,
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
			
			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, VALUE_CHASE_MILLIS );
			curValueRatio = 1.0f - newValueRatio;
			
			delayBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( (int)sampleRate, MAX_DELAY_MILLIS );

			leftDelayBuffer = new DelayBuffer( delayBufferLength );
			rightDelayBuffer = new DelayBuffer( delayBufferLength );
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
			long periodStartFrameTime,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers, int numFrames )
	{
		errctx.reset();

		//
		boolean inLConnected = channelConnectedFlags.get( FeedbackDelayMadDefinition.CONSUMER_IN_LEFT );
		MadChannelBuffer inLcb = channelBuffers[ FeedbackDelayMadDefinition.CONSUMER_IN_LEFT ];
		float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );

		boolean inRConnected = channelConnectedFlags.get( FeedbackDelayMadDefinition.CONSUMER_IN_RIGHT );
		MadChannelBuffer inRcb = channelBuffers[ FeedbackDelayMadDefinition.CONSUMER_IN_RIGHT ];
		float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );
		
		boolean outLConnected = channelConnectedFlags.get( FeedbackDelayMadDefinition.PRODUCER_OUT_LEFT );
		MadChannelBuffer outLcb = channelBuffers[ FeedbackDelayMadDefinition.PRODUCER_OUT_LEFT ];
		float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );

		boolean outRConnected = channelConnectedFlags.get( FeedbackDelayMadDefinition.PRODUCER_OUT_RIGHT );
		MadChannelBuffer outRcb = channelBuffers[ FeedbackDelayMadDefinition.PRODUCER_OUT_RIGHT ];
		float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );
		
		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			if( desiredDelayFrames > 0 )
			{
				if( !errctx.andWith( doDelay( numFrames, inLfloats, outLfloats, leftDelayBuffer ) ) )
				{
					return errctx.getCurRetCode();
				}
			}
			else
			{
				System.arraycopy( inLfloats, 0, outLfloats, 0, numFrames );
			}
		}
		
		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			if( desiredDelayFrames > 0 )
			{
				if( !errctx.andWith( doDelay( numFrames, inRfloats, outRfloats, rightDelayBuffer ) ) )
				{
					return errctx.getCurRetCode();
				}
			}
			else
			{
				System.arraycopy( inRfloats, 0, outRfloats, 0, numFrames );
			}
		}
		return errctx.getCurRetCode();
	}
	
	private RealtimeMethodReturnCodeEnum doDelay( int numFrames, float[] incomingFloats, float[] outgoingFloats, DelayBuffer delayBuffer )
	{
		if( desiredDelayFrames == 0 )
		{
			System.arraycopy( incomingFloats, 0, outgoingFloats, 0, numFrames );
		}
		else
		{
			// First remove any excess samples we have in the delay buffer
			int numInDelayBuffer = delayBuffer.getNumReadable();
			int numExcess = numInDelayBuffer - desiredDelayFrames;
			int numZerosToOutput = (numExcess < 0 ? -numExcess : 0 );
			int curOutputPos = 0;

			if( numExcess > 0 )
			{
				// Dispose of the excess
				delayBuffer.moveForward( numExcess );
				numExcess = 0;
			}
			else if( numZerosToOutput > 0 )
			{
				int numZerosThisRound = ( numZerosToOutput < numFrames ? numZerosToOutput : numFrames );
				Arrays.fill( outgoingFloats, 0, numZerosThisRound, 0.0f );
				delayBuffer.write( incomingFloats, 0,  numZerosThisRound );
				curOutputPos += numZerosThisRound;
			}
			
			// Now loop around sample by sample
			float curvalue = 1.0f - desiredFeedback;
			for( int s = curOutputPos ; s < numFrames ; s++ )
			{
				outgoingFloats[s] = delayBuffer.readOne();
				float incomingSample = incomingFloats[ s ];
				float valToBuffer = incomingSample * curvalue + outgoingFloats[s] * desiredFeedback;
				delayBuffer.writeOne( valToBuffer );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
