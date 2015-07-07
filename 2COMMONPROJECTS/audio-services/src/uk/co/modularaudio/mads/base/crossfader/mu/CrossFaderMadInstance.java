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
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class CrossFaderMadInstance extends MadInstance<CrossFaderMadDefinition, CrossFaderMadInstance>
{
//	private static Log log = LogFactory.getLog( CrossFaderMadInstance.class.getName() );

	private int sampleRate;

	private final SpringAndDamperDoubleInterpolator ampAInterpolator = new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f );
	private final SpringAndDamperDoubleInterpolator ampBInterpolator = new SpringAndDamperDoubleInterpolator( 0.0f, 1.0f );

	public CrossFaderMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CrossFaderMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		ampAInterpolator.hardSetValue( 0.5f );
		ampBInterpolator.hardSetValue( 0.5f );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		ampAInterpolator.reset( sampleRate );
		ampBInterpolator.reset( sampleRate );
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
		final boolean in1LConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT );
		final boolean in1RConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN1_RIGHT );

		final boolean in2LConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN2_LEFT );
		final boolean in2RConnected = channelConnectedFlags.get( CrossFaderMadDefinition.CONSUMER_CHAN2_RIGHT );

		final boolean outLConnected = channelConnectedFlags.get( CrossFaderMadDefinition.PRODUCER_OUT_LEFT );
		final boolean outRConnected = channelConnectedFlags.get( CrossFaderMadDefinition.PRODUCER_OUT_RIGHT );

		// Now mix them together with the precomputed amps
		// only if we have at least one input and output connected
		if( (outLConnected && (in1LConnected || in2LConnected))
			||
			(outRConnected && (in1RConnected || in2RConnected))
				)
		{
			final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

			final int ampABufferIndex = 0;
			final int ampBBufferIndex = numFrames;

			ampAInterpolator.generateControlValues( tmpBuffer, ampABufferIndex, numFrames );
			ampBInterpolator.generateControlValues( tmpBuffer, ampBBufferIndex, numFrames );

			final MadChannelBuffer in1Lcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN1_LEFT ];
			final float[] in1LBuffer = in1Lcb.floatBuffer;
			final MadChannelBuffer in1Rcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN1_RIGHT ];
			final float[] in1RBuffer = in1Rcb.floatBuffer;
			final MadChannelBuffer in2Lcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN2_LEFT ];
			final float[] in2LBuffer = in2Lcb.floatBuffer;
			final MadChannelBuffer in2Rcb = channelBuffers[ CrossFaderMadDefinition.CONSUMER_CHAN2_RIGHT ];
			final float[] in2RBuffer = in2Rcb.floatBuffer;
			final MadChannelBuffer outLcb = channelBuffers[ CrossFaderMadDefinition.PRODUCER_OUT_LEFT ];
			final float[] outLBuffer = outLcb.floatBuffer;
			final MadChannelBuffer outRcb = channelBuffers[ CrossFaderMadDefinition.PRODUCER_OUT_RIGHT ];
			final float[] outRBuffer = outRcb.floatBuffer;

			for( int i = 0 ; i < numFrames ; i++ )
			{
				final float ampA = tmpBuffer[ampABufferIndex+i];
				final float ampB = tmpBuffer[ampBBufferIndex+i];

				final float in1lval = in1LBuffer[ frameOffset + i ];
				final float in2lval = in2LBuffer[ frameOffset + i ];
				final float lVal = (in1lval * ampA) + (in2lval * ampB);
				outLBuffer[ frameOffset + i ] = lVal;

				final float in1rval = in1RBuffer[ frameOffset + i ];
				final float in2rval = in2RBuffer[ frameOffset + i ];

				final float rVal = (in1rval * ampA) + (in2rval * ampB);
				outRBuffer[ frameOffset + i ] = rVal;
			}

			ampAInterpolator.checkForDenormal();
			ampBInterpolator.checkForDenormal();
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredAmps( final float ampA, final float ampB )
	{
		ampAInterpolator.notifyOfNewValue( ampA );
		ampBInterpolator.notifyOfNewValue( ampB );
	}
}
