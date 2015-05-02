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

package uk.co.modularaudio.mads.base.soundfile_player.mu;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
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
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SoundfilePlayerMadInstance extends MadInstance<SoundfilePlayerMadDefinition,SoundfilePlayerMadInstance>
{
	private static Log log = LogFactory.getLog( SoundfilePlayerMadInstance.class.getName() );

	protected boolean active = false;

	public enum PlayingState
	{
		STOPPED,
		PLAYING
	};

	public static final float PLAYBACK_SPEED_MAX = 1.5f;

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

	public SoundfilePlayerMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final SoundfilePlayerMadDefinition definition,
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
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		numSamplesPerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();

		speedSad.hardSetValue( desiredPlaySpeed );

		numSamplesTillNextEvent = numSamplesPerFrontEndPeriod;

		leftDcTrap.recomputeR( sampleRate );
		rightDcTrap.recomputeR( sampleRate );
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
		final MadChannelBuffer lb = channelBuffers[ SoundfilePlayerMadDefinition.PRODUCER_LEFT ];
		final boolean leftConnected = channelConnectedFlags.get( SoundfilePlayerMadDefinition.PRODUCER_LEFT );
		final float[] lfb = lb.floatBuffer;
		final MadChannelBuffer rb = channelBuffers[ SoundfilePlayerMadDefinition.PRODUCER_RIGHT ];
		final boolean rightConnected = channelConnectedFlags.get( SoundfilePlayerMadDefinition.PRODUCER_RIGHT );
		final float[] rfb = rb.floatBuffer;

		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;
		Arrays.fill( tmpBuffer, BlockResamplerService.MAGIC_FLOAT );

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
								SoundfilePlayerMadInstance.PlayingState.STOPPED );
						log.warn("Unable to flip to playing as no sample available.");
						break;
					}
					currentState = desiredState;
					break;
				}
				case PLAYING:
				default:
				{
					currentState = desiredState;
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

//		float usedPlaySpeed = (currentState == PlayingState.PLAYING ? desiredPlaySpeed : 0.0f );

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
			}
			else
			{
				tmpBuffer[0] = desiredPlaySpeed;
			}

			blockResamplerService.sampleClientFetchFramesResample(
					tempQueueEntryStorage.temporaryFloatArray,
//					numThisRound + 10,
					4000,
					resampledSample,
					sampleRate,
					tmpBuffer[0],
					lfb,
					rfb,
					frameOffset + curOutputPos,
					numThisRound,
					false );

			numStillToOutput -= numThisRound;
			numSamplesTillNextEvent -= numThisRound;
			curOutputPos += numThisRound;
		}

//		leftDcTrap.filter( lfb, frameOffset, numFrames );
//		rightDcTrap.filter( rfb, frameOffset, numFrames );
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public AdvancedComponentsFrontController getAdvancedComponentsFrontController()
	{
		return advancedComponentsFrontController;
	}

	protected void emitStateChangedToStop( final ThreadSpecificTemporaryEventStorage tses,
			final long currentFrameTime,
			final SoundfilePlayerMadInstance.PlayingState state )
	{
		localBridge.queueTemporalEventToUi( tses,
				currentFrameTime,
				SoundfilePlayerIOQueueBridge.COMMAND_OUT_STATE_CHANGE,
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
				SoundfilePlayerIOQueueBridge.COMMAND_OUT_FRAME_POSITION_DELTA,
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
				blockResamplerService.destroyResamplingClient(resampledSample);
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
