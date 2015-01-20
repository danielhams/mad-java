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

package uk.co.modularaudio.mads.base.crossfader.mu;

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
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class CrossFaderMadInstance extends MadInstance<CrossFaderMadDefinition, CrossFaderMadInstance>
{
//	private static Log log = LogFactory.getLog( OscillatorMadInstance.class.getName() );

	private static final int VALUE_CHASE_MILLIS = 10;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate = -1;

	private float instanceRealAmpA = 0.0f;
	private float instanceRealAmpB = 0.0f;

	public float desiredAmpA = 1.0f;
	public float desiredAmpB = 1.0f;

	public CrossFaderMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			CrossFaderMadDefinition definition,
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
		boolean in1LConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT );
		boolean in1RConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN1_RIGHT );

		boolean in2LConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN2_LEFT );
		boolean in2RConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN2_RIGHT );

		boolean outLConnected = channelConnectedFlags.get( CrossFaderMadDefinition.PRODUCER_OUT_LEFT );
		boolean outRConnected = channelConnectedFlags.get( CrossFaderMadDefinition.PRODUCER_OUT_RIGHT );

		// Now mix them together with the precomputed amps
		// only if we have at least one input and output connected
		if( (outLConnected && (in1LConnected || in2LConnected))
			||
			(outRConnected && (in1RConnected || in2RConnected))
				)
		{
			MadChannelBuffer in1Lcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT ];
			float[] in1LBuffer = in1Lcb.floatBuffer;
			MadChannelBuffer in1Rcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN1_RIGHT ];
			float[] in1RBuffer = in1Rcb.floatBuffer;
			MadChannelBuffer in2Lcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN2_LEFT ];
			float[] in2LBuffer = in2Lcb.floatBuffer;
			MadChannelBuffer in2Rcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN2_RIGHT ];
			float[] in2RBuffer = in2Rcb.floatBuffer;
			MadChannelBuffer outLcb = channelBuffers[ CrossFaderMadDefinition.PRODUCER_OUT_LEFT ];
			float[] outLBuffer = outLcb.floatBuffer;
			MadChannelBuffer outRcb = channelBuffers[ CrossFaderMadDefinition.PRODUCER_OUT_RIGHT ];
			float[] outRBuffer = outRcb.floatBuffer;
			for( int i = 0 ; i < numFrames ; i++ )
			{
				float in1lval = in1LBuffer[i];
				float in2lval = in2LBuffer[i];
				float lVal = (in1lval * instanceRealAmpA) + (in2lval * instanceRealAmpB);
				outLBuffer[i] = lVal;

				float in1rval = in1RBuffer[i];
				float in2rval = in2RBuffer[i];

				float rVal = (in1rval * instanceRealAmpA) + (in2rval * instanceRealAmpB);
				outRBuffer[i] = rVal;

				// Fade between the values
				instanceRealAmpA = ((instanceRealAmpA * curValueRatio) + (desiredAmpA * newValueRatio));
				instanceRealAmpB = ((instanceRealAmpB * curValueRatio) + (desiredAmpB * newValueRatio));
				// And dampen any values that are just noise.
				if( instanceRealAmpA > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && instanceRealAmpA < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
				{
					instanceRealAmpA = 0.0f;
				}
				if( instanceRealAmpB > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && instanceRealAmpB < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
				{
					instanceRealAmpB = 0.0f;
				}
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
