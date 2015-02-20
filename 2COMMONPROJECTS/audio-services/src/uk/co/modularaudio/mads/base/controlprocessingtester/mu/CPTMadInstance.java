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
import uk.co.modularaudio.mads.base.controlprocessingtester.ui.CPTValueChaseMillisSliderUiJComponent;
import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.HalfHannWindowInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.NoneInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperInterpolator;
import uk.co.modularaudio.util.audio.controlinterpolation.SumOfRatiosInterpolator;
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

public class CPTMadInstance extends MadInstance<CPTMadDefinition, CPTMadInstance>
{
	private static Log log = LogFactory.getLog( CPTMadInstance.class.getName() );

	private ControlValueInterpolator ampInterpolator;

	private final NoneInterpolator noneInterpolator = new NoneInterpolator();
	private final SumOfRatiosInterpolator sorInterpolator = new SumOfRatiosInterpolator();
	private final LinearInterpolator lInterpolator = new LinearInterpolator();
	private final HalfHannWindowInterpolator hhInterpolator = new HalfHannWindowInterpolator();
	private final SpringAndDamperInterpolator sdInterpolator = new SpringAndDamperInterpolator();

	private final ControlValueInterpolator[] interpolators = new ControlValueInterpolator[5];

	private int sampleRate;
	private float desValueChaseMillis = CPTValueChaseMillisSliderUiJComponent.DEFAULT_CHASE_MILLIS;

	public CPTMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CPTMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		interpolators[0] = noneInterpolator;
		interpolators[1] = sorInterpolator;
		interpolators[2] = lInterpolator;
		interpolators[3] = hhInterpolator;
		interpolators[4] = sdInterpolator;
		ampInterpolator = interpolators[0];
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		sorInterpolator.reset( sampleRate, desValueChaseMillis );
		lInterpolator.reset( sampleRate, desValueChaseMillis );
		hhInterpolator.reset( sampleRate, desValueChaseMillis );
		sdInterpolator.reset( sampleRate, desValueChaseMillis );
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
			final MadChannelBuffer[] channelBuffers ,
			final int frameOffset,
			final int numFrames  )
	{
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

			// Use the temporary area as a place to put generate control values
			final float[] tmpArea = tempQueueEntryStorage.temporaryFloatArray;

			ampInterpolator.generateControlValues( tmpArea, 0, numFrames );

			for( int i = 0 ; i < numFrames ; i++ )
			{
				final int curIndex = frameOffset + i;
				outLBuffer[ curIndex ] = in1LBuffer[ curIndex ] * tmpArea[i];
				outRBuffer[ curIndex ] = in1RBuffer[ curIndex ] * tmpArea[i];

			}
			// And dampen any values that are just noise.
			ampInterpolator.checkForDenormal();
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredAmp( final float amp )
	{
//		log.trace( "Received amp change: " + amp );
		ampInterpolator.notifyOfNewValue( amp );
	}

	public void setInterpolatorByIndex( final int interpolatorIndex )
	{
		ampInterpolator = interpolators[interpolatorIndex];
		log.debug("Changed to " + ampInterpolator.getClass().getSimpleName() );
	}

	public void setChaseMillis( final float chaseMillis )
	{
		desValueChaseMillis = chaseMillis;
		sorInterpolator.reset( sampleRate, chaseMillis );
		lInterpolator.reset( sampleRate, chaseMillis );
		hhInterpolator.reset( sampleRate, chaseMillis );
		sdInterpolator.reset( sampleRate, chaseMillis );
	}
}
