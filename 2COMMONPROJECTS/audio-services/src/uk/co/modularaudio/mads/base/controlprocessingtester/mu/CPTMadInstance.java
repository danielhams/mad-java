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

package uk.co.modularaudio.mads.base.controlprocessingtester.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.base.crossfader.mu.CrossFaderMadInstance;
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

public class CPTMadInstance extends MadInstance<CPTMadDefinition, CPTMadInstance>
{
	private static Log log = LogFactory.getLog( CrossFaderMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 10;

	private float curValueRatio = 0.0f;
	private float newValueRatio = 1.0f;

	private long sampleRate = -1;

	private float instanceRealAmpA = 0.0f;

	private float desiredAmpA = 1.0f;

	private int framesPerFrontEndPeriod = -1;
	private int framesUntilIO = -1;

	public CPTMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CPTMadDefinition definition,
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

			framesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
			framesUntilIO = framesPerFrontEndPeriod;
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
			final int numFrames )
	{
		int numTemporalEvents = tempQueueEntryStorage.numTemporalEventsToInstance;
		if( numTemporalEvents > 0 )
		{
			log.debug("Have " + numTemporalEvents + " temporal events waiting");
		}
		final boolean in1LConnected = channelConnectedFlags.get( CPTMadDefinition.CONSUMER_CHAN1_LEFT );
		final boolean in1RConnected = channelConnectedFlags.get( CPTMadDefinition.CONSUMER_CHAN1_RIGHT );

		final boolean outLConnected = channelConnectedFlags.get( CPTMadDefinition.PRODUCER_OUT_LEFT );
		final boolean outRConnected = channelConnectedFlags.get( CPTMadDefinition.PRODUCER_OUT_RIGHT );

		// Now mix them together with the precomputed amps
		// only if we have at least one input and output connected
		if( (outLConnected && in1LConnected)
			||
			(outRConnected && in1RConnected)
			)
		{
			final MadChannelBuffer in1Lcb = channelBuffers[ CPTMadDefinition.CONSUMER_CHAN1_LEFT ];
			final float[] in1LBuffer = in1Lcb.floatBuffer;
			final MadChannelBuffer in1Rcb = channelBuffers[ CPTMadDefinition.CONSUMER_CHAN1_RIGHT ];
			final float[] in1RBuffer = in1Rcb.floatBuffer;
			final MadChannelBuffer outLcb = channelBuffers[ CPTMadDefinition.PRODUCER_OUT_LEFT ];
			final float[] outLBuffer = outLcb.floatBuffer;
			final MadChannelBuffer outRcb = channelBuffers[ CPTMadDefinition.PRODUCER_OUT_RIGHT ];
			final float[] outRBuffer = outRcb.floatBuffer;

			int framesLeft = numFrames;
			int curOutputIndex = 0;

			while( framesLeft > 0 )
			{
				if( framesUntilIO == 0 )
				{
					final long adjustedFrameTime = periodStartFrameTime + curOutputIndex;

					preProcess( tempQueueEntryStorage, timingParameters, adjustedFrameTime );

					framesUntilIO = framesPerFrontEndPeriod;
				}

				final int numThisRound = framesLeft < framesUntilIO ? framesLeft : framesUntilIO;

				for( int i = 0 ; i < numThisRound ; i++ )
				{
					final float in1lval = in1LBuffer[ curOutputIndex + i ];
					final float lVal = in1lval * instanceRealAmpA;
					outLBuffer[ curOutputIndex + i ] = lVal;

					final float in1rval = in1RBuffer[ curOutputIndex + i ];

					final float rVal = in1rval * instanceRealAmpA;
					outRBuffer[ curOutputIndex + i ] = rVal;

					// Fade between the values
					instanceRealAmpA = ((instanceRealAmpA * curValueRatio) + (desiredAmpA * newValueRatio));
					// And dampen any values that are just noise.
					if( instanceRealAmpA > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && instanceRealAmpA < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
					{
						instanceRealAmpA = 0.0f;
					}
				}

				curOutputIndex += numThisRound;
				framesUntilIO -= numThisRound;
				framesLeft -= numThisRound;
			}
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredAmps( final float ampA )
	{
		this.desiredAmpA = ampA;
	}
}
