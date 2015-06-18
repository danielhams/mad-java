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

package uk.co.modularaudio.mads.base.sampleplayer.mu;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
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

public class SingleSamplePlayerMadInstance extends MadInstance<SingleSamplePlayerMadDefinition,SingleSamplePlayerMadInstance>
{
	enum PlayingState
	{
		NOT_PLAYING,
		PLAYING
	};
	private static Log log = LogFactory.getLog( SingleSamplePlayerMadInstance.class.getName() );

	private static final int SAMPLE_HARD_FADE_OUT_MILLIS = 10;
	protected int sampleRate = 0;
	private int periodLength = -1;

	protected float currentRootNoteFreq = 0.0f;

	protected float curValueRatio = 0.0f;
	protected float newValueRatio = 1.0f;
	private int numFramesFadeOut = -1;

	public final AdvancedComponentsFrontController advancedComponentsFrontController;

	public AtomicReference<SingleSampleRuntime> desiredSampleRuntime = new AtomicReference<SingleSampleRuntime>();
	public SingleSampleRuntime usedSampleRuntime;

	public final static int RESAMPLED_ARRAY_LENGTH = 4096;
	public final static int INTERLEAVED_ARRAY_LENGTH = RESAMPLED_ARRAY_LENGTH / 2;

	private SamplePlayerStateProcessor stateProcessor;

	protected int currentStartPosFrameNum;

	public SingleSamplePlayerMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final SingleSamplePlayerMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );

		this.advancedComponentsFrontController = creationContext.getAdvancedComponentsFrontController();
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();
			newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, SAMPLE_HARD_FADE_OUT_MILLIS );
			curValueRatio = 1.0f - newValueRatio;
			numFramesFadeOut = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, SAMPLE_HARD_FADE_OUT_MILLIS );

			stateProcessor = new SamplePlayerStateProcessor( this, advancedComponentsFrontController, sampleRate, periodLength );
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
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{

		final boolean inGateConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.CONSUMER_GATE_CV );
		final boolean inRetriggerConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.CONSUMER_RETRIGGER_CV );
		final boolean inFreqConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.CONSUMER_FREQ_CV);
		final boolean inAmpConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.CONSUMER_AMP_CV);
		final boolean audioOutLeftConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_L );
		final boolean audioOutRightConnected = channelConnectedFlags.get( SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_R );

		final SingleSampleRuntime inSampleRuntime = desiredSampleRuntime.get();
		if( inSampleRuntime != null )
		{
			if( inSampleRuntime != usedSampleRuntime )
			{
				usedSampleRuntime = inSampleRuntime;
			}
			inSampleRuntime.setUsed( true );
		}

		if( usedSampleRuntime == null)
		{
			// Output silence
			if( audioOutLeftConnected )
			{
				final MadChannelBuffer audioOutLeftBuf = channelBuffers[ SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_L ];
				Arrays.fill( audioOutLeftBuf.floatBuffer, 0.0f );
			}
			if( audioOutRightConnected )
			{
				final MadChannelBuffer audioOutRightBuf = channelBuffers[ SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_R ];
				Arrays.fill( audioOutRightBuf.floatBuffer, 0.0f );
			}
		}
		else
		{
			usedSampleRuntime.setRuntimeData( currentStartPosFrameNum, numFramesFadeOut, curValueRatio, newValueRatio );

			final MadChannelBuffer inGateBuf = channelBuffers[ SingleSamplePlayerMadDefinition.CONSUMER_GATE_CV ];
			final MadChannelBuffer inRetriggerBuf = channelBuffers[ SingleSamplePlayerMadDefinition.CONSUMER_RETRIGGER_CV ];
			final MadChannelBuffer inFreqBuf = channelBuffers[ SingleSamplePlayerMadDefinition.CONSUMER_FREQ_CV ];
			final MadChannelBuffer inAmpBuf = channelBuffers[ SingleSamplePlayerMadDefinition.CONSUMER_AMP_CV ];

			final MadChannelBuffer audioOutLeftBuf = channelBuffers[ SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_L ];
			final MadChannelBuffer audioOutRightBuf = channelBuffers[ SingleSamplePlayerMadDefinition.PRODUCER_AUDIO_OUT_R ];

			stateProcessor.processIncomingData(
					numFrames,
					inGateConnected, inGateBuf,
					inRetriggerConnected, inRetriggerBuf,
					inFreqConnected, inFreqBuf,
					inAmpConnected, inAmpBuf );

			stateProcessor.outputPeriodSamples( tempQueueEntryStorage.temporaryFloatArray,
					sampleRate,
					numFrames,
					inSampleRuntime,
					currentRootNoteFreq,
					inFreqConnected, inFreqBuf,
					inAmpConnected, inAmpBuf,
					audioOutLeftConnected, audioOutLeftBuf, audioOutRightConnected, audioOutRightBuf );

		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void releaseSampleCacheAtoms()
	{
		final SingleSampleRuntime curDesSamRun = desiredSampleRuntime.get();
		final boolean wasSame = ( usedSampleRuntime == curDesSamRun );
		if( usedSampleRuntime != null )
		{
			try
			{
				usedSampleRuntime.destroy();
			}
			catch (final Exception e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught attempting to release sample cache atom: " + e.toString(), e );
				}
			}
		}
		if( !wasSame )
		{
			try
			{
				curDesSamRun.destroy();
			}
			catch (final Exception e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught attempting to release sample cache atom: " + e.toString(), e );
				}
			}
		}
	}

	@Override
	public void destroy()
	{
		releaseSampleCacheAtoms();
		super.destroy();
	}
}
