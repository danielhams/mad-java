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

package uk.co.modularaudio.mads.base.frequencyfilter.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.dsp.ButterworthFilterRT;
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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FrequencyFilterMadInstance extends MadInstance<FrequencyFilterMadDefinition,FrequencyFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadInstance.class.getName() );
	
	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	
	private long sampleRate = -1;

	public FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	public float desiredFrequency = 80.0f;
	public float desiredBandwidth = 20.0f;
	public boolean desired24dB = false;
	
	private float currentFrequency = 0.0f;
	private float currentBandwidth = 0.0f;
	private boolean actual24dB = false;
	private ButterworthFilterRT leftChannelButterworthRT = new ButterworthFilterRT();
	private ButterworthFilterRT rightChannelButterworthRT = new ButterworthFilterRT();
	private ButterworthFilterRT leftChannel24dbRT = new ButterworthFilterRT();
	private ButterworthFilterRT rightChannel24dbRT = new ButterworthFilterRT();

	private float lastLeftValue = 0.0f;
	private float lastRightValue = 0.0f;
	
	public FrequencyFilterMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			FrequencyFilterMadDefinition definition,
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
		boolean inLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_LEFT );
		MadChannelBuffer inLcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_LEFT ];
		float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		boolean inRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT );
		MadChannelBuffer inRcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT ];
		float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );
		boolean inCvFreqConnected = channelConnectedFlags.get(  FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
		MadChannelBuffer inFreq = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
		float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );
		
		boolean outLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT );
		MadChannelBuffer outLcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT ];
		float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		boolean outRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT );
		MadChannelBuffer outRcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );
		

		boolean complementLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_LEFT );
		MadChannelBuffer complementLcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_LEFT ];
		float[] complementLfloats = (complementLConnected ? complementLcb.floatBuffer : null );
		boolean complementRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_RIGHT );
		MadChannelBuffer complementRcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_RIGHT ];
		float[] complementRfloats = (complementRConnected ? complementRcb.floatBuffer : null );
		
		currentFrequency = (currentFrequency * curValueRatio) + (desiredFrequency * newValueRatio );
		if( currentFrequency <= 1.0f )
		{
			currentFrequency = 1.0f;
		}
		currentBandwidth = (currentBandwidth * curValueRatio ) + (desiredBandwidth * newValueRatio );

		boolean doingSwitch = false;
		if( actual24dB != desired24dB )
		{
			// Switch - lets clear out the 24 rt values so stop a spike
			leftChannelButterworthRT.clear();
			rightChannelButterworthRT.clear();
			leftChannel24dbRT.clear();
			rightChannel24dbRT.clear();
			actual24dB = desired24dB;
			doingSwitch = true;
		}
		
		if( !inLConnected )
		{
			if( outLConnected )
			{
				Arrays.fill( outLfloats, 0.0f );
			}
			if( complementLConnected )
			{
				Arrays.fill( complementLfloats, 0.0f );
			}
		}
		else if( inLConnected && outLConnected )
		{
			System.arraycopy( inLfloats, 0, outLfloats, 0, numFrames );
			
			if( !inCvFreqConnected )
			{
				ButterworthFilter.filter( leftChannelButterworthRT, outLfloats, 0, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				if( desired24dB )
				{
					ButterworthFilter.filter( leftChannel24dbRT, outLfloats, 0, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			else
			{
				ButterworthFilter.filterWithFreq( leftChannelButterworthRT, outLfloats, 0, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				if( desired24dB )
				{
					ButterworthFilter.filterWithFreq( leftChannel24dbRT, outLfloats, 0, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			
			
			if( doingSwitch )
			{
				// We've switched from 12 to 24 db, stop huge pops by fading
				for( int i = 0 ; i < numFrames ; i++ )
				{
					float preRatio = ((float)(numFrames - i ) / numFrames );
					float curRatio = 1.0f - preRatio;
					float curLValue = outLfloats[ i ];
					outLfloats[ i ] = (curLValue * curRatio) + (lastLeftValue * preRatio );
				}
			}
			
			if( complementLConnected )
			{
				for( int i = 0 ; i < numFrames ; ++i )
				{
					complementLfloats[i] = inLfloats[i] - outLfloats[i];
				}
			}
			
			lastLeftValue = outLfloats[ numFrames - 1 ];
		}
		
		if( !inRConnected )
		{
			if( outRConnected )
			{
				Arrays.fill( outRfloats, 0.0f );
			}
			if( complementRConnected )
			{
				Arrays.fill( complementRfloats, 0.0f );
			}
		}
		else if( inRConnected && outRConnected )
		{
			System.arraycopy( inRfloats, 0, outRfloats, 0, numFrames );
			
			if(!inCvFreqConnected )
			{
				ButterworthFilter.filter( rightChannelButterworthRT, outRfloats, 0, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				
				if( desired24dB )
				{
					ButterworthFilter.filter( rightChannel24dbRT, outRfloats, 0, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			else
			{
				ButterworthFilter.filterWithFreq( rightChannelButterworthRT, outRfloats, 0, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				
				if( desired24dB )
				{
					ButterworthFilter.filterWithFreq( rightChannel24dbRT, outRfloats, 0, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			
			if( doingSwitch )
			{
				// We've switched from 12 to 24 db, stop huge pops by fading
				for( int i = 0 ; i < numFrames ; i++ )
				{
					float preRatio = ((float)(numFrames - i ) / numFrames );
					float curRatio = 1.0f - preRatio;
					float curRValue = outRfloats[ i ];
					outRfloats[ i ] = (curRValue * curRatio) + (lastRightValue * preRatio );
				}
			}
			
			if( complementRConnected )
			{
				for( int i = 0 ; i < numFrames ; ++i )
				{
					complementRfloats[i] = inRfloats[i] - outRfloats[i];
				}
			}
			
			lastRightValue = outRfloats[ numFrames - 1 ];
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
