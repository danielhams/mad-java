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
import uk.co.modularaudio.util.audio.dsp.FrequencyFilterMode;
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
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class FrequencyFilterMadInstance extends MadInstance<FrequencyFilterMadDefinition,FrequencyFilterMadInstance>
{
//	private static Log log = LogFactory.getLog( FrequencyFilterMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate;

	public FrequencyFilterMode desiredFilterMode = FrequencyFilterMode.LP;
	public float desiredFrequency = 80.0f;
	public float desiredBandwidth = 20.0f;
	public boolean desired24dB = false;

	private float currentFrequency = 0.0f;
	private float currentBandwidth = 0.0f;
	private boolean actual24dB;
	private final ButterworthFilter leftChannelButterworth = new ButterworthFilter();
	private final ButterworthFilter rightChannelButterworth = new ButterworthFilter();
	private final ButterworthFilter leftChannel24db = new ButterworthFilter();
	private final ButterworthFilter rightChannel24db = new ButterworthFilter();

	private float lastLeftValue;
	private float lastRightValue;

	public FrequencyFilterMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final FrequencyFilterMadDefinition definition,
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
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			final MadTimingParameters timingParameters,
			final long periodStartFrameTime,
			final MadChannelConnectedFlags channelConnectedFlags,
			final MadChannelBuffer[] channelBuffers,
			final int frameOffset,
			final int numFrames )
	{
		final boolean inLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		final boolean inRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );
		final boolean inCvFreqConnected = channelConnectedFlags.get(  FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY  );
		final MadChannelBuffer inFreq = channelBuffers[ FrequencyFilterMadDefinition.CONSUMER_IN_CV_FREQUENCY ];
		final float[] inCvFreqFloats = (inCvFreqConnected ? inFreq.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		final boolean outRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );


		final boolean complementLConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_LEFT );
		final MadChannelBuffer complementLcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_LEFT ];
		final float[] complementLfloats = (complementLConnected ? complementLcb.floatBuffer : null );
		final boolean complementRConnected = channelConnectedFlags.get( FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_RIGHT );
		final MadChannelBuffer complementRcb = channelBuffers[ FrequencyFilterMadDefinition.PRODUCER_COMPLEMENT_RIGHT ];
		final float[] complementRfloats = (complementRConnected ? complementRcb.floatBuffer : null );

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
			leftChannelButterworth.clear();
			rightChannelButterworth.clear();
			leftChannel24db.clear();
			rightChannel24db.clear();
			actual24dB = desired24dB;
			doingSwitch = true;
		}

		if( !inLConnected )
		{
			if( outLConnected )
			{
				Arrays.fill( outLfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
			if( complementLConnected )
			{
				Arrays.fill( complementLfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}
		else if( inLConnected && outLConnected )
		{
			System.arraycopy( inLfloats, frameOffset, outLfloats, frameOffset, numFrames );

			if( !inCvFreqConnected )
			{
				leftChannelButterworth.filter( outLfloats, frameOffset, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				if( desired24dB )
				{
					leftChannel24db.filter( outLfloats, frameOffset, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			else
			{
				leftChannelButterworth.filterWithFreq( outLfloats, frameOffset, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				if( desired24dB )
				{
					leftChannel24db.filterWithFreq( outLfloats, frameOffset, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}


			if( doingSwitch )
			{
				// We've switched from 12 to 24 db, stop huge pops by fading
				for( int i = 0 ; i < numFrames ; i++ )
				{
					final float preRatio = ((float)(numFrames - i ) / numFrames );
					final float curRatio = 1.0f - preRatio;
					final float curLValue = outLfloats[ frameOffset + i ];
					outLfloats[ frameOffset + i ] = (curLValue * curRatio) + (lastLeftValue * preRatio );
				}
			}

			if( complementLConnected )
			{
				for( int i = 0 ; i < numFrames ; ++i )
				{
					complementLfloats[frameOffset + i] = inLfloats[frameOffset + i] - outLfloats[frameOffset + i];
				}
			}

			lastLeftValue = outLfloats[ frameOffset + (numFrames - 1) ];
		}

		if( !inRConnected )
		{
			if( outRConnected )
			{
				Arrays.fill( outRfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
			if( complementRConnected )
			{
				Arrays.fill( complementRfloats, frameOffset, frameOffset + numFrames, 0.0f );
			}
		}
		else if( inRConnected && outRConnected )
		{
			System.arraycopy( inRfloats, frameOffset, outRfloats, frameOffset, numFrames );

			if(!inCvFreqConnected )
			{
				rightChannelButterworth.filter( outRfloats, frameOffset, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );

				if( desired24dB )
				{
					rightChannel24db.filter( outRfloats, frameOffset, numFrames, currentFrequency, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}
			else
			{
				rightChannelButterworth.filterWithFreq( outRfloats, frameOffset, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );

				if( desired24dB )
				{
					rightChannel24db.filterWithFreq( outRfloats, frameOffset, numFrames, inCvFreqFloats, currentBandwidth, desiredFilterMode, sampleRate );
				}
			}

			if( doingSwitch )
			{
				// We've switched from 12 to 24 db, stop huge pops by fading
				for( int i = 0 ; i < numFrames ; i++ )
				{
					final float preRatio = ((float)(numFrames - i ) / numFrames );
					final float curRatio = 1.0f - preRatio;
					final float curRValue = outRfloats[ frameOffset + i ];
					outRfloats[ frameOffset + i ] = (curRValue * curRatio) + (lastRightValue * preRatio );
				}
			}

			if( complementRConnected )
			{
				for( int i = 0 ; i < numFrames ; ++i )
				{
					complementRfloats[frameOffset + i] = inRfloats[frameOffset + i] - outRfloats[frameOffset + i];
				}
			}

			lastRightValue = outRfloats[ frameOffset + (numFrames - 1) ];
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
