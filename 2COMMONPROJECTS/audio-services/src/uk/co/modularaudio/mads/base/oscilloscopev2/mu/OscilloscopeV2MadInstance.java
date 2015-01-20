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

package uk.co.modularaudio.mads.base.oscilloscopev2.mu;

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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class OscilloscopeV2MadInstance extends MadInstance<OscilloscopeV2MadDefinition,OscilloscopeV2MadInstance>
{
	private static Log log = LogFactory.getLog( OscilloscopeV2MadInstance.class.getName() );
	
	public static final int MAX_CAPTURE_MILLIS = 5000;
	
	protected int sampleRate = 0;
	
	protected boolean active = false;
	
	protected OscilloscopeV2CaptureTriggerEnum captureTrigger = OscilloscopeV2CaptureTriggerEnum.NONE;
	protected OscilloscopeV2CaptureRepetitionsEnum captureRepetitions = OscilloscopeV2CaptureRepetitionsEnum.CONTINOUS;
	
	public int desiredCaptureSamples = 1;

	public int maxRingBufferingInSamples = -1;
	
	public OscilloscopeV2RingBuffer scope0CaptureRingBuffer = null;
	public OscilloscopeV2RingBuffer scope1CaptureRingBuffer = null;

	public OscilloscopeV2MadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			OscilloscopeV2MadDefinition definition,
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

//			long audioIOLatencyNanos = timingParameters.getAudioIOLatencyNanos();
//			log.debug("The startup timing info is " + timingParameters.getTimestampUsedForPullingFromQueues() + ", " + audioIOLatencyNanos );

			maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, MAX_CAPTURE_MILLIS );
			int latencyInSamples = AudioTimingUtils.getNumSamplesForNanosAtSampleRate( sampleRate, timingParameters.getNanosOutputLatency() );
			
			scope0CaptureRingBuffer = new OscilloscopeV2RingBuffer( maxRingBufferingInSamples, latencyInSamples );
			scope1CaptureRingBuffer = new OscilloscopeV2RingBuffer( maxRingBufferingInSamples, latencyInSamples );

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
		boolean in0Connected = channelConnectedFlags.get( OscilloscopeV2MadDefinition.CONSUMER_AUDIO_SIGNAL0 );
		MadChannelBuffer in0ChannelBuffer = channelBuffers[ OscilloscopeV2MadDefinition.CONSUMER_AUDIO_SIGNAL0 ];
		float[] in0Floats = (in0Connected ? in0ChannelBuffer.floatBuffer : null );
		boolean in0CvConnected = channelConnectedFlags.get( OscilloscopeV2MadDefinition.CONSUMER_CV_SIGNAL0 );
		MadChannelBuffer in0CvChannelBuffer = channelBuffers[ OscilloscopeV2MadDefinition.CONSUMER_CV_SIGNAL0 ];
		float[] in0CvFloats = (in0CvConnected ? in0CvChannelBuffer.floatBuffer : null );

		boolean in1Connected = channelConnectedFlags.get( OscilloscopeV2MadDefinition.CONSUMER_AUDIO_SIGNAL1 );
		MadChannelBuffer in1ChannelBuffer = channelBuffers[ OscilloscopeV2MadDefinition.CONSUMER_AUDIO_SIGNAL1 ];
		float[] in1Floats = (in1Connected ? in1ChannelBuffer.floatBuffer : null );
		boolean in1CvConnected = channelConnectedFlags.get( OscilloscopeV2MadDefinition.CONSUMER_CV_SIGNAL1 );
		MadChannelBuffer in1CvChannelBuffer = channelBuffers[ OscilloscopeV2MadDefinition.CONSUMER_CV_SIGNAL1 ];
		float[] in1CvFloats = (in1CvConnected ? in1CvChannelBuffer.floatBuffer : null );
		
		if( active )
		{			
			try
			{
				if( in0Connected )
				{
					scope0CaptureRingBuffer.setStatus( true, true );
					scope0CaptureRingBuffer.write( in0Floats, 0, numFrames );
				}
				else if( in0CvConnected )
				{
					scope0CaptureRingBuffer.setStatus( true, false );
					scope0CaptureRingBuffer.write( in0CvFloats, 0, numFrames );
				}
				else
				{
					scope0CaptureRingBuffer.setStatus( false, false );
					scope0CaptureRingBuffer.clear();
					scope0CaptureRingBuffer.writeZeros( numFrames );
				}
				
				if( in1Connected )
				{
					scope1CaptureRingBuffer.setStatus( true, true );
					scope1CaptureRingBuffer.write( in1Floats, 0, numFrames );
				}
				else if( in1CvConnected )
				{
					scope1CaptureRingBuffer.setStatus( true, false );
					scope1CaptureRingBuffer.write( in1CvFloats, 0, numFrames );
				}
				else
				{
					scope1CaptureRingBuffer.setStatus( false, false );
					scope1CaptureRingBuffer.clear();
					scope1CaptureRingBuffer.writeZeros( numFrames );
				}
			}
			catch(Exception boe )
			{
				// Do nothing.
				log.debug("Caught boe: " + boe.toString() );
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
