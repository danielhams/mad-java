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

package uk.co.modularaudio.mads.base.foldbackdistortion.mu;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.dsp.FoldbackDistortion;
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

public class FoldbackDistortionMadInstance extends MadInstance<FoldbackDistortionMadDefinition,FoldbackDistortionMadInstance>
{
	private static final int VALUE_CHASE_MILLIS = 1;
	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;

	private long sampleRate = -1;

	public int desiredMaxFoldovers = 0;
	public float desiredThreshold = 0.0f;

	private int currentMaxFoldovers = 0;
	private float currentThreshold = 0.0f;

	private final FoldbackDistortion leftDistortion = new FoldbackDistortion();
	private final FoldbackDistortion rightDistortion = new FoldbackDistortion();

	public FoldbackDistortionMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final FoldbackDistortionMadDefinition definition,
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
		final boolean inLConnected = channelConnectedFlags.get( FoldbackDistortionMadDefinition.CONSUMER_IN_LEFT );
		final MadChannelBuffer inLcb = channelBuffers[ FoldbackDistortionMadDefinition.CONSUMER_IN_LEFT ];
		final float[] inLfloats = (inLConnected ? inLcb.floatBuffer : null );
		final boolean inRConnected = channelConnectedFlags.get( FoldbackDistortionMadDefinition.CONSUMER_IN_RIGHT );
		final MadChannelBuffer inRcb = channelBuffers[ FoldbackDistortionMadDefinition.CONSUMER_IN_RIGHT ];
		final float[] inRfloats = (inRConnected ? inRcb.floatBuffer : null );

		final boolean outLConnected = channelConnectedFlags.get( FoldbackDistortionMadDefinition.PRODUCER_OUT_LEFT );
		final MadChannelBuffer outLcb = channelBuffers[ FoldbackDistortionMadDefinition.PRODUCER_OUT_LEFT ];
		final float[] outLfloats = (outLConnected ? outLcb.floatBuffer : null );
		final boolean outRConnected = channelConnectedFlags.get( FoldbackDistortionMadDefinition.PRODUCER_OUT_RIGHT );
		final MadChannelBuffer outRcb = channelBuffers[ FoldbackDistortionMadDefinition.PRODUCER_OUT_RIGHT ];
		final float[] outRfloats = (outRConnected ? outRcb.floatBuffer : null );


		currentThreshold = desiredThreshold;
		currentMaxFoldovers = desiredMaxFoldovers;

		if( !inLConnected && outLConnected )
		{
			Arrays.fill( outLfloats, 0.0f );
		}
		else if( inLConnected && outLConnected )
		{
			leftDistortion.threshold = currentThreshold;
			leftDistortion.maxFoldbacks = currentMaxFoldovers;
			System.arraycopy( inLfloats, 0, outLfloats, 0, numFrames );
			leftDistortion.filter( outLfloats );
		}

		if( !inRConnected && outRConnected )
		{
			Arrays.fill( outRfloats, 0.0f );
		}
		else if( inRConnected && outRConnected )
		{
			rightDistortion.threshold = currentThreshold;
			rightDistortion.maxFoldbacks = currentMaxFoldovers;
			System.arraycopy( inRfloats, 0, outRfloats, 0, numFrames );
			rightDistortion.filter( outRfloats );
		}
		currentMaxFoldovers = desiredMaxFoldovers;
		currentThreshold = (currentThreshold * curValueRatio ) + (desiredThreshold * newValueRatio );

		// Do the folder over here...
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
