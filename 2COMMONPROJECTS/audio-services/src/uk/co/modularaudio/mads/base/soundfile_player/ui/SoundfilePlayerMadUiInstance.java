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
import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.samplecaching.BufferFillCompletionListener;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance.InstanceLifecycleListener;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerMadUiInstance extends
		AbstractNonConfigurableMadUiInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance> implements
		IOQueueEventUiConsumer<SoundfilePlayerMadInstance>,
		BufferFillCompletionListener
{
	private static Log log = LogFactory.getLog( SoundfilePlayerMadUiInstance.class.getName() );
	
	private final AdvancedComponentsFrontController advancedComponentsFrontController;
	private final SampleCachingService sampleCachingService;
	private final BlockResamplerService resamplerService;
	private final String musicRoot;
	
	private ArrayList<SoundfileSampleEventListener> sampleEventListeners = new ArrayList<SoundfileSampleEventListener>();
	
	private SoundfilePlayerZoomProducer zoomProducer = null;
	private ZoomDataListener zoomDataListener = null;
	
	protected BlockResamplingClient currentResampledSample = null;
	
	protected DataRate knownDataRate = null;
	
	protected List<InstanceLifecycleListener> lifecycleListeners = new ArrayList<InstanceLifecycleListener>();
	
	public SoundfilePlayerMadUiInstance( SoundfilePlayerMadInstance instance,
			SoundfilePlayerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		
		advancedComponentsFrontController = instance.getAdvancedComponentsFrontController();
		sampleCachingService = advancedComponentsFrontController.getSampleCachingService();
		resamplerService = advancedComponentsFrontController.getBlockResamplerService();

		musicRoot = advancedComponentsFrontController.getSampleSelectionMusicRoot();
	}
	
	@Override
	public void consumeQueueEntry( SoundfilePlayerMadInstance instance,
			IOQueueEvent queueEvent )
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
				catch( Exception e )
				{
					log.error("Failed to unregister cache client for file: " + e.toString(), e );
				}
				resampledSample = null;
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_CURRENT_SAMPLE:
			{
				BlockResamplingClient curSample = (BlockResamplingClient)queueEvent.object;
				currentResampledSample = curSample;
				for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
				{
					sampleEventListeners.get(i).receiveSampleChangeEvent(currentResampledSample);
				}
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_FRAME_POSITION_DELTA:
			{
				BlockResamplingClient rs = (BlockResamplingClient)queueEvent.object;
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
				BlockResamplingClient rs = (BlockResamplingClient)queueEvent.object;
				if( rs == currentResampledSample )
				{
					for( int i = 0 ; i < sampleEventListeners.size() ; ++i )
					{
						sampleEventListeners.get(i).receiveAbsPositionEvent( queueEvent.value );
					}
				}
				break;
			}
			case SoundfilePlayerIOQueueBridge.COMMAND_OUT_STATE_CHANGE:
			{
				break;
			}
			default:
			{
				log.error("Unknown message received in UI: " + queueEvent.command );
				break;
			}
		}
	}
	
	public String getMusicRoot()
	{
		return musicRoot;
	}

	public void addSampleEventListener( SoundfileSampleEventListener seListener )
	{
		sampleEventListeners.add( seListener );
		if( currentResampledSample != null )
		{
			seListener.receiveSampleChangeEvent( currentResampledSample );
		}
	}
	
	public void removeFileInfoReceiver( SoundfileSampleEventListener fiReceiver )
	{
		sampleEventListeners.remove( fiReceiver );
	}
	
	public void setFileInfo( String filename )
	{
		try
		{
			SampleCacheClient sampleCacheClient = advancedComponentsFrontController.registerCacheClientForFile( filename );
			log.debug("Registering for buffer fill completion");
			sampleCachingService.registerForBufferFillCompletion( sampleCacheClient, this );
		}
		catch ( Exception e )
		{
			String msg = "Exception caught attempting to create sample cache client for file " + filename +
					": " + e.toString();
			log.error( msg, e );
		}
	}
	
	public void sendActive( boolean active )
	{
		sendCommandValueToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0 ) );
	}
	
	public void sendPlayingSpeed( float playingSpeed )
	{
		sendTemporalValueToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_PLAY_SPEED, Float.floatToIntBits(playingSpeed) );
	}
	
	public void sendPlayingStateChange( SoundfilePlayerMadInstance.PlayingState desiredPlayingState )
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
			ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			MadTimingParameters timingParameters, long currentGuiTick)
	{
		localQueueBridge.receiveQueuedEventsToUi(guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing(guiTemporaryEventStorage, timingParameters, currentGuiTick);
	}
	
	public void setZoomProducer( SoundfilePlayerZoomProducer zoomProducer )
	{
		this.zoomProducer = zoomProducer;
		if( zoomDataListener != null && zoomProducer != null )
		{
			zoomProducer.setZoomDataListener(zoomDataListener);
		}
	}

	public void setZoomDataListener( ZoomDataListener zoomDataListener )
	{
		this.zoomDataListener = zoomDataListener;
		if( zoomDataListener != null && zoomProducer != null )
		{
			zoomProducer.setZoomDataListener(zoomDataListener);
		}
	}

	@Override
	public void receiveStartup(HardwareIOChannelSettings ratesAndLatency,
			MadTimingParameters timingParameters,
			MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		knownDataRate = ratesAndLatency.getAudioChannelSetting().getDataRate();
		for( InstanceLifecycleListener ll : lifecycleListeners )
		{
			ll.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		}
	}
	
	@Override
	public void receiveStop()
	{
		super.receiveStop();
		for( InstanceLifecycleListener ll : lifecycleListeners )
		{
			ll.receiveStop();
		}
	}

	@Override
	public void notifyBufferFilled( SampleCacheClient sampleCacheClient )
	{
		log.debug("Received notification that the buffer is filled!");
		currentResampledSample = resamplerService.promoteSampleCacheClientToResamplingClient( sampleCacheClient,
			BlockResamplingMethod.CUBIC );
			
		sendCommandObjectToInstance(SoundfilePlayerIOQueueBridge.COMMAND_IN_RESAMPLED_SAMPLE, currentResampledSample );
		
	}
	
	public void addLifecycleListener( InstanceLifecycleListener ll )
	{
		lifecycleListeners.add( ll );
	}

}
