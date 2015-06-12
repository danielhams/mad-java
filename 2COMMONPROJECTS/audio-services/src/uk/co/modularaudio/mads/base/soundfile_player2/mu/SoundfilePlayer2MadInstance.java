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

package uk.co.modularaudio.mads.base.soundfile_player2.mu;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.mads.internal.fade.mu.FadeDefinitions;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInWaveTable;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutWaveTable;
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.dsp.DcTrapFilter;
import uk.co.modularaudio.util.audio.format.DataRate;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SoundfilePlayer2MadInstance extends MadInstance<SoundfilePlayer2MadDefinition,SoundfilePlayer2MadInstance>
{
	private static Log log = LogFactory.getLog( SoundfilePlayer2MadInstance.class.getName() );

	protected boolean active = false;

	public enum PlayingState
	{
		STOPPED,
		PLAYING,
		PLAYING_FADE_IN,
		PLAYING_FADE_OUT
	};

	public static final float PLAYBACK_SPEED_MAX = 1.5f;

	public static final float GAIN_MAX_DB = 20.0f;

	private int numSamplesPerFrontEndPeriod = -1;

	private int sampleRate;

	private final DcTrapFilter leftDcTrap;
	private final DcTrapFilter rightDcTrap;

	private final AdvancedComponentsFrontController advancedComponentsFrontController;
	private final BlockResamplerService blockResamplerService;
	private final SampleCachingService sampleCachingService;
	private final JobExecutorService jobExecutorService;

	private BlockResamplingClient resampledSample;

	private long lastEmittedPosition;

	private PlayingState currentState = PlayingState.STOPPED;
	private PlayingState desiredState = PlayingState.STOPPED;

	private int numSamplesTillNextEvent;

	private float desiredPlaySpeed = 1.0f;
	private final SpringAndDamperDoubleInterpolator speedSad = new SpringAndDamperDoubleInterpolator(
			-PLAYBACK_SPEED_MAX, PLAYBACK_SPEED_MAX );
	private float desiredGain = 1.0f;
	private final SpringAndDamperDoubleInterpolator gainSad = new SpringAndDamperDoubleInterpolator(
			0.0f, AudioMath.dbToLevelF( GAIN_MAX_DB ) );

	private FadeInWaveTable fadeInWaveTable;
	private FadeOutWaveTable fadeOutWaveTable;
	private int fadePosition;

	public SoundfilePlayer2MadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final SoundfilePlayer2MadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		advancedComponentsFrontController = creationContext.getAdvancedComponentsFrontController();
		blockResamplerService = advancedComponentsFrontController.getBlockResamplerService();
		sampleCachingService = advancedComponentsFrontController.getSampleCachingService();
		jobExecutorService = advancedComponentsFrontController.getJobExecutorService();

		leftDcTrap = new DcTrapFilter( DataRate.SR_44100.getValue() );
		rightDcTrap = new DcTrapFilter( DataRate.SR_44100.getValue() );

	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
		throws MadProcessingException
	{
		final DataRate dataRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate();
		sampleRate = dataRate.getValue();
		numSamplesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();

		speedSad.reset( sampleRate );
		speedSad.hardSetValue( desiredPlaySpeed );
		gainSad.reset( sampleRate );
		gainSad.hardSetValue( desiredGain );

		numSamplesTillNextEvent = numSamplesPerFrontEndPeriod;

		leftDcTrap.recomputeR( sampleRate );
		rightDcTrap.recomputeR( sampleRate );

		fadeInWaveTable = new FadeInWaveTable( dataRate, FadeDefinitions.FADE_MILLIS );
		fadeOutWaveTable = new FadeOutWaveTable( dataRate, FadeDefinitions.FADE_MILLIS );
		fadePosition = 0;
	}

	@Override
	public void stop() throws MadProcessingException
	{
		// Nothing to do
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
		final MadChannelBuffer lb = channelBuffers[ SoundfilePlayer2MadDefinition.PRODUCER_LEFT ];
		final boolean leftConnected = channelConnectedFlags.get( SoundfilePlayer2MadDefinition.PRODUCER_LEFT );
		final float[] lfb = lb.floatBuffer;
		final MadChannelBuffer rb = channelBuffers[ SoundfilePlayer2MadDefinition.PRODUCER_RIGHT ];
		final boolean rightConnected = channelConnectedFlags.get( SoundfilePlayer2MadDefinition.PRODUCER_RIGHT );
		final float[] rfb = rb.floatBuffer;

		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		if( desiredState != currentState )
		{
			switch( currentState )
			{
				case STOPPED:
				{
					if( resampledSample == null )
					{
						emitStateChangedToStop( tempQueueEntryStorage,
								periodStartFrameTime,
								SoundfilePlayer2MadInstance.PlayingState.STOPPED );
						log.warn("Unable to flip to playing as no sample available.");
						currentState = PlayingState.STOPPED;
						desiredState = currentState;
					}
					else
					{
						currentState = PlayingState.PLAYING_FADE_IN;
						fadePosition = 0;
					}
					break;
				}
				case PLAYING:
				{
					currentState = PlayingState.PLAYING_FADE_OUT;
					fadePosition = 0;
				}
				default:
				{
					break;
				}
			}
		}

		if( resampledSample == null || currentState == PlayingState.STOPPED )
		{
			if( leftConnected )
			{
				Arrays.fill( lfb, 0.0f );
			}
			if( rightConnected )
			{
				Arrays.fill( rfb, 0.0f );
			}
			return RealtimeMethodReturnCodeEnum.SUCCESS;
		}

		int numStillToOutput = numFrames;
		int curOutputPos = 0;
		while( numStillToOutput > 0 )
		{
			if( active && numSamplesTillNextEvent <= 0 )
			{
				// Emit position event
				final long eventFrameTime = periodStartFrameTime + curOutputPos;
				final long curSamplePos = resampledSample.getFramePosition();
				if( curSamplePos != lastEmittedPosition )
				{
					emitDeltaPositionEvent( tempQueueEntryStorage, eventFrameTime, curSamplePos, resampledSample );
				}

				lastEmittedPosition = curSamplePos;

				numSamplesTillNextEvent = numSamplesPerFrontEndPeriod;
			}

			int numThisRound = ( numStillToOutput < numSamplesTillNextEvent ? numStillToOutput : numSamplesTillNextEvent );
			if( !active )
			{
				numThisRound = numFrames;
			}
//			log.debug("Doing " + numThisRound + " frames");
			if( !speedSad.checkForDenormal() )
			{
				speedSad.generateControlValues( tmpBuffer, 0, numThisRound );
				blockResamplerService.fetchAndResampleVarispeed(
						resampledSample,
						sampleRate,
						tmpBuffer, 0,
						lfb, frameOffset + curOutputPos,
						rfb, frameOffset + curOutputPos,
						numThisRound,
						tmpBuffer,
						numThisRound );
			}
			else
			{
				blockResamplerService.fetchAndResample(
						resampledSample,
						sampleRate,
						desiredPlaySpeed,
						lfb, frameOffset + curOutputPos,
						rfb, frameOffset + curOutputPos,
						numThisRound,
						tmpBuffer,
						numThisRound );
			}


			numStillToOutput -= numThisRound;
			numSamplesTillNextEvent -= numThisRound;
			curOutputPos += numThisRound;
		}

		// Apply gain
		gainSad.checkForDenormal();
		gainSad.generateControlValues( tmpBuffer, 0, numFrames );
		for( int f = 0 ; f < numFrames ; ++f )
		{
			lfb[frameOffset + f] *= tmpBuffer[f];
			rfb[frameOffset + f] *= tmpBuffer[f];
		}

		leftDcTrap.filter( lfb, frameOffset, numFrames );
		rightDcTrap.filter( rfb, frameOffset, numFrames );

		switch( currentState )
		{
			case PLAYING_FADE_IN:
			{
				final int numLeftToFade = fadeInWaveTable.capacity - fadePosition;

				final int numThisRound = (numLeftToFade > numFrames ? numFrames : numLeftToFade );

				for( int p = 0 ; p < numThisRound ; ++p, ++fadePosition )
				{
					final float fadeValue = fadeInWaveTable.floatBuffer[ fadePosition ];
					lfb[frameOffset + p] *= fadeValue;
					rfb[frameOffset + p] *= fadeValue;
				}

				if( fadePosition == fadeInWaveTable.capacity )
				{
					currentState = PlayingState.PLAYING;
				}
				break;
			}
			case PLAYING_FADE_OUT:
			{
				final int numLeftToFade = fadeOutWaveTable.capacity - fadePosition;

				int numThisRound = (numLeftToFade > numFrames ? numFrames : numLeftToFade );

				for( int p = 0 ; p < numThisRound ; ++p, ++fadePosition )
				{
					final float fadeValue = fadeOutWaveTable.floatBuffer[ fadePosition ];
					lfb[frameOffset + p] *= fadeValue;
					rfb[frameOffset + p] *= fadeValue;
				}

				while( numThisRound < numFrames )
				{
					lfb[frameOffset + numThisRound] = 0.0f;
					rfb[frameOffset + numThisRound] = 0.0f;
					numThisRound++;
				}

				if( fadePosition == fadeOutWaveTable.capacity )
				{
					currentState = PlayingState.STOPPED;
				}
				break;
			}
			default:
			{
				break;
			}
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public AdvancedComponentsFrontController getAdvancedComponentsFrontController()
	{
		return advancedComponentsFrontController;
	}

	protected void emitStateChangedToStop( final ThreadSpecificTemporaryEventStorage tses,
			final long currentFrameTime,
			final SoundfilePlayer2MadInstance.PlayingState state )
	{
		localBridge.queueTemporalEventToUi( tses,
				currentFrameTime,
				SoundfilePlayer2IOQueueBridge.COMMAND_OUT_STATE_CHANGE,
				state.ordinal(),
				null );
	}

	protected void emitDeltaPositionEvent( final ThreadSpecificTemporaryEventStorage tses,
			final long eventFrameTime,
			final long curPos,
			final BlockResamplingClient whichSample )
	{
		localBridge.queueTemporalEventToUi( tses,
				eventFrameTime,
				SoundfilePlayer2IOQueueBridge.COMMAND_OUT_FRAME_POSITION_DELTA,
				curPos,
				whichSample );
	}

	@Override
	public void destroy()
	{
		if( resampledSample != null )
		{
			try
			{
				blockResamplerService.destroyResamplingClient( resampledSample );
			}
			catch( final Exception e )
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught cleaning up sample cache client: " + e.toString(), e );
				}
			}
			resampledSample = null;
		}
		super.destroy();
	}

	public BlockResamplingClient getResampledSample()
	{
		return resampledSample;
	}

	public void setResampledSample( final BlockResamplingClient newSample )
	{
		resampledSample = newSample;
	}

	public void setDesiredState( final PlayingState newState )
	{
		desiredState = newState;
	}

	public void setDesiredPlaySpeed( final float newSpeed )
	{
		desiredPlaySpeed = newSpeed;
		speedSad.notifyOfNewValue( newSpeed );
	}

	public void setDesiredGain( final float newGain )
	{
		desiredGain = newGain;
		gainSad.notifyOfNewValue( newGain );
	}

	public void resetFramePosition( final long newFramePosition )
	{
		resampledSample.setFramePosition( newFramePosition );
		resampledSample.setFpOffset( 0.0f );
	}

	protected void addJobForSampleCachingService()
	{
		sampleCachingService.addJobToCachePopulationThread();
	}

	public JobExecutorService getJobExecutorService()
	{
		return jobExecutorService;
	}
}
