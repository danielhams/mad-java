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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerIOQueueBridge;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.mads.base.soundfile_player.ui.runnable.GetAnalysisRunnable;
import uk.co.modularaudio.mads.base.soundfile_player.ui.runnable.LoadNewSoundFileRunnable;
import uk.co.modularaudio.mads.base.soundfile_player.ui.runnable.SoundFileLoadCompletionListener;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance.InstanceLifecycleListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerMadUiInstance extends
		AbstractNoNameChangeNonConfigurableMadUiInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance> implements
		IOQueueEventUiConsumer<SoundfilePlayerMadInstance>,
		SoundFileLoadCompletionListener,
		AnalysisFillCompletionListener
{
	private static Log log = LogFactory.getLog( SoundfilePlayerMadUiInstance.class.getName() );

	private final AdvancedComponentsFrontController advancedComponentsFrontController;
	private final SampleCachingService sampleCachingService;
	private final JobExecutorService jobExecutorService;
	private final String musicRoot;

	private final ArrayList<SoundfileSampleEventListener> sampleEventListeners = new ArrayList<SoundfileSampleEventListener>();

	private SoundfilePlayerZoomProducer zoomProducer;
	private ZoomDataListener zoomDataListener;

	protected BlockResamplingClient currentResampledSample;

	protected DataRate knownDataRate;

	protected List<InstanceLifecycleListener> lifecycleListeners = new ArrayList<InstanceLifecycleListener>();

	private final PositionJumpCacheRefresher positionJumpCacheRefresher;

	protected List<AnalysisFillCompletionListener> analysisFillListeners = new ArrayList<AnalysisFillCompletionListener>();

	public SoundfilePlayerMadUiInstance( final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );

		advancedComponentsFrontController = instance.getAdvancedComponentsFrontController();
		sampleCachingService = advancedComponentsFrontController.getSampleCachingService();
		jobExecutorService = instance.getJobExecutorService();

		musicRoot = advancedComponentsFrontController.getSampleSelectionMusicRoot();

		positionJumpCacheRefresher = new PositionJumpCacheRefresher( sampleEventListeners );
	}

	@Override
	public void consumeQueueEntry( final SoundfilePlayerMadInstance instance,
			final IOQueueEvent queueEvent )
	{
//		log.debug("Received queue event: " + queueEvent.toString() );
		switch( queueEvent.command )
		{
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_RECYCLE_SAMPLE:
			{
				BlockResamplingClient resampledSample = (BlockResamplingClient)queueEvent.object;
				try
				{
					advancedComponentsFrontController.unregisterCacheClientForFile( resampledSample.getSampleCacheClient() );
				}
				catch( final Exception e )
				{
					if( log.isErrorEnabled() )
					{
						log.error("Failed to unregister cache client for file: " + e.toString(), e );
					}
				}
				resampledSample = null;
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_CURRENT_SAMPLE:
			{
				final BlockResamplingClient curSample = (BlockResamplingClient)queueEvent.object;
				currentResampledSample = curSample;
				for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
				{
					sampleEventListeners.get(i).receiveSampleChangeEvent(currentResampledSample);
				}
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_FRAME_POSITION_DELTA:
			{
				final BlockResamplingClient rs = (BlockResamplingClient)queueEvent.object;
				if( rs == currentResampledSample )
				{
					for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
					{
						sampleEventListeners.get(i).receiveDeltaPositionEvent( queueEvent.value );
					}
				}
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_FRAME_POSITION_ABS:
			{
				final BlockResamplingClient rs = (BlockResamplingClient)queueEvent.object;
				if( rs == currentResampledSample )
				{
					for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
					{
						sampleEventListeners.get(i).receiveAbsPositionEvent( queueEvent.value );
					}
				}
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_FRAME_POSITION_ABS_WAIT_FOR_CACHE:
			{
				final BlockResamplingClient rs = (BlockResamplingClient)queueEvent.object;
				if( rs == currentResampledSample )
				{
					for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
					{
						sampleEventListeners.get(i).receiveAbsPositionEvent( queueEvent.value );
					}
				}
				// And queue a lambda to be called after the sample caching service finishes it's
				// current pass
				sampleCachingService.registerForBufferFillCompletion( rs.getSampleCacheClient(), positionJumpCacheRefresher );
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_STATE_CHANGE:
			{
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown message received in UI: " + queueEvent.command );
				}
				break;
			}
		}
	}

	public String getMusicRoot()
	{
		return musicRoot;
	}

	public void addSampleEventListener( final SoundfileSampleEventListener seListener )
	{
		sampleEventListeners.add( seListener );
		if( currentResampledSample != null )
		{
			seListener.receiveSampleChangeEvent( currentResampledSample );
		}
	}

	public void removeFileInfoReceiver( final SoundfileSampleEventListener fiReceiver )
	{
		sampleEventListeners.remove( fiReceiver );
	}

	public void addAnalysisFillListener( final AnalysisFillCompletionListener al )
	{
		analysisFillListeners.add( al );
	}

	public void removeAnalysisFillListener( final AnalysisFillCompletionListener al )
	{
		analysisFillListeners.remove( al );
	}

	public void setFileInfo( final String filename )
	{
		final LoadNewSoundFileRunnable loadRunnable = new LoadNewSoundFileRunnable( advancedComponentsFrontController,
				filename,
				this );
		jobExecutorService.submitJob( loadRunnable );
	}

	public void sendActive( final boolean active )
	{
		sendCommandValueToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0 ) );
	}

	public void sendPlayingSpeed( final float playingSpeed )
	{
		sendTemporalValueToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_PLAY_SPEED, Float.floatToIntBits(playingSpeed) );
	}

	public void sendPlayingStateChange( final SoundfilePlayerMadInstance.PlayingState desiredPlayingState )
	{
		sendTemporalValueToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_PLAYING_STATE,  desiredPlayingState.ordinal() );
	}

	public void sendFullRewind()
	{
		sendTemporalValueToInstance( SoundfilePlayerIOQueueBridge.COMMAND_IN_SHUTTLE_REWIND_TO_START, 1);
	}

	public void sendFullFfwd()
	{
		sendTemporalValueToInstance( SoundfilePlayerIOQueueBridge.COMMAND_IN_SHUTTLE_FFWD_TO_END, 1);
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick)
	{
		localQueueBridge.receiveQueuedEventsToUi(guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing(guiTemporaryEventStorage, timingParameters, currentGuiTick);
	}

	public void setZoomProducer( final SoundfilePlayerZoomProducer zoomProducer )
	{
		this.zoomProducer = zoomProducer;
		if( zoomDataListener != null && zoomProducer != null )
		{
			zoomProducer.setZoomDataListener(zoomDataListener);
		}
	}

	public void setZoomDataListener( final ZoomDataListener zoomDataListener )
	{
		this.zoomDataListener = zoomDataListener;
		if( zoomDataListener != null && zoomProducer != null )
		{
			zoomProducer.setZoomDataListener(zoomDataListener);
		}
	}

	@Override
	public void receiveStartup(final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		knownDataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();
		for( final InstanceLifecycleListener ll : lifecycleListeners )
		{
			ll.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		}
	}

	@Override
	public void receiveStop()
	{
		super.receiveStop();
		for( final InstanceLifecycleListener ll : lifecycleListeners )
		{
			ll.receiveStop();
		}
	}

	@Override
	public void notifyBufferFilled( final SampleCacheClient sampleCacheClient )
	{
		log.debug("Received notification that the buffer is filled. Promoting to resampler client.");
		currentResampledSample = advancedComponentsFrontController.promoteSampleCacheClientToResamplingClient( sampleCacheClient,
			BlockResamplingMethod.CUBIC );

		sendCommandObjectToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_RESAMPLED_SAMPLE, currentResampledSample );

		// And now submit a job to retrieve analysisData for the
		// file (bpm, gain + thumbnail)

		final GetAnalysisRunnable getAnalysisRunnable = new GetAnalysisRunnable( advancedComponentsFrontController,
				currentResampledSample.getSampleCacheClient().getLibraryEntry(),
				this );
		jobExecutorService.submitJob( getAnalysisRunnable );
	}

	@Override
	public void notifyLoadFailure()
	{
		log.debug("Received notification that the file load failed");
	}

	public void addLifecycleListener( final InstanceLifecycleListener ll )
	{
		lifecycleListeners.add( ll );
	}

	public void receiveOverviewPositionRequest( final float normalisedPosition )
	{
		if( currentResampledSample != null )
		{
			log.debug( "Received overview position request of " + normalisedPosition );
			final long totalNumFrames = currentResampledSample.getTotalNumFrames();
			log.debug( "Current sample has " + totalNumFrames + " frames");
			final int sampleRate = currentResampledSample.getSampleCacheClient().getLibraryEntry().getSampleRate();
			final int oneSecFrames = sampleRate;
			final double normSongPosDouble = ((double)normalisedPosition) * totalNumFrames;
			long newSongPos = (long)normSongPosDouble;
			if( newSongPos < -oneSecFrames )
			{
				newSongPos = -oneSecFrames;
			}
			if( newSongPos > (totalNumFrames + oneSecFrames) )
			{
				newSongPos = totalNumFrames + oneSecFrames;
			}
			log.debug("New song pos is " + newSongPos);

			sendTemporalValueToInstance( SoundfilePlayerIOQueueBridge.COMMAND_IN_POSITION_JUMP, newSongPos );
		}
	}

	@Override
	public void receiveAnalysedData( final AnalysedData analysedData )
	{
		for( final AnalysisFillCompletionListener al : analysisFillListeners )
		{
			al.receiveAnalysedData( analysedData );
		}
	}

	@Override
	public void notifyAnalysisFailure()
	{
		for( final AnalysisFillCompletionListener al : analysisFillListeners )
		{
			al.notifyAnalysisFailure();
		}
	}

	@Override
	public void receivePercentageComplete( final int percentageComplete )
	{
		for( final AnalysisFillCompletionListener al : analysisFillListeners )
		{
			al.receivePercentageComplete( percentageComplete );
		}
	}

}
