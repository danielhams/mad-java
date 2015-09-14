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

package uk.co.modularaudio.mads.base.scope.ui;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.modularaudio.mads.base.scope.mu.ScopeIOQueueBridge;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadInstance;
import uk.co.modularaudio.mads.base.scope.ui.ScopeRepetitionsChoiceUiJComponent.RepetitionChoice;
import uk.co.modularaudio.mads.base.scope.ui.ScopeTriggerChoiceUiJComponent.TriggerChoice;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.buffer.MultiChannelBackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class ScopeMadUiInstance extends
		AbstractNoNameChangeNonConfigurableMadUiInstance<ScopeMadDefinition, ScopeMadInstance> implements
		IOQueueEventUiConsumer<ScopeMadInstance>
{
	private static Log log = LogFactory.getLog( ScopeMadUiInstance.class.getName() );

	private int sampleRate = DataRate.CD_QUALITY.getValue();

	private int maxCaptureLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(
			sampleRate, LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );

	private final float[][] frontEndBuffers = new float[ScopeMadDefinition.NUM_VIS_CHANNELS][];

	private int frontEndWritePosition = 0;

	private MultiChannelBackendToFrontendDataRingBuffer backendRingBuffer;

	private final ArrayList<CaptureLengthListener> captureLengthListeners = new ArrayList<CaptureLengthListener>();
	private final ArrayList<ScopeSampleRateListener> sampleRateListeners = new ArrayList<ScopeSampleRateListener>();

	private ScopeDataVisualiser scopeDataVisualiser;

	public ScopeMadUiInstance( final ScopeMadInstance instance,
			final ScopeMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		setupFrontEndBuffers();
	}

	@Override
	public void receiveStartup(final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory)
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		backendRingBuffer = instance.getBackendRingBuffer();

		sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		maxCaptureLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate(
				sampleRate, LogarithmicTimeMillis1To1000SliderModel.MAX_MILLIS );
		setupFrontEndBuffers();

		for( final ScopeSampleRateListener srl : sampleRateListeners )
		{
			srl.receiveSampleRateChange( sampleRate );
		}
	}

	private void setupFrontEndBuffers()
	{
		if( frontEndBuffers[0] == null ||
				frontEndBuffers[0].length != maxCaptureLength )
		{
			for( int c = 0 ; c < ScopeMadDefinition.NUM_VIS_CHANNELS ; ++c )
			{
				frontEndBuffers[c] = new float[ maxCaptureLength ];
			}
		}
		frontEndWritePosition = 0;
	}

	@Override
	public void receiveStop()
	{
		super.receiveStop();
	}

	@Override
	public void destroy()
	{
		super.destroy();
	}

	@Override
	public void doDisplayProcessing(
			final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTick )
	{
		localQueueBridge.receiveQueuedEventsToUi(guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing(guiTemporaryEventStorage, timingParameters, currentGuiTick);
	}

	@Override
	public void consumeQueueEntry( final ScopeMadInstance instance,
			final IOQueueEvent queueEvent )
	{
		switch( queueEvent.command )
		{
			case ScopeIOQueueBridge.COMMAND_OUT_DATA_START:
			{
//				log.trace("Resetting front end write position with new data start");
				frontEndWritePosition = 0;
				break;
			}
			case ScopeIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
//				log.trace( "Received write index update" );

				final long value = queueEvent.value;
				final int newWriteIndex = (int) ((value) & 0xFFFFFFFF);
				receiveBufferIndexUpdate( queueEvent.frameTime, newWriteIndex );
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

	private void receiveBufferIndexUpdate( final long indexUpdateTimestamp, final int writeIndex )
	{
		final int numReadable = backendRingBuffer.frontEndGetNumReadableWithWriteIndex( writeIndex );
//		log.trace("Received index update with timestamp " + indexUpdateTimestamp + " with " + numReadable + " readable");

		final int spaceAvailable = maxCaptureLength - frontEndWritePosition;
		if( spaceAvailable <= 0 )
		{
//			log.error("Ran out of front end buffer to place incoming samples in - needed " + numReadable );
			backendRingBuffer.frontEndMoveUpToWriteIndex( writeIndex );
		}
		if( numReadable > 0 && spaceAvailable > 0 )
		{
//			log.trace( "Have space to put " + numReadable + " from back end - space is " + spaceAvailable +
//					" used is "  + (frontEndBufferLength - spaceAvailable) );
			final int numToRead = (spaceAvailable < numReadable ? spaceAvailable : numReadable);
			final int numRead = backendRingBuffer.read( frontEndBuffers, frontEndWritePosition, numToRead );

			if( numRead != numToRead )
			{
				if( log.isWarnEnabled() )
				{
					log.warn( "Expected " + numToRead + " from mad instance ring but read " + numRead );
				}
			}
			else
			{
//				log.trace( "Read " + numRead + " into front end buffers" );
			}
			frontEndWritePosition += numRead;

			scopeDataVisualiser.visualiseScopeBuffers( frontEndBuffers );
		}

//		log.debug("Have " + numRead + " new samples to process");
	}

	public void setCaptureTimeMillis( final float captureMillis )
	{
		final int intBits = Float.floatToIntBits( captureMillis );
		sendTemporalValueToInstance( ScopeIOQueueBridge.COMMAND_IN_CAPTURE_MILLIS, intBits );

		final int newCaptureSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, captureMillis );
//		log.trace( "New capture num samples is " + newCaptureSamples +
//				" previous was " + captureLengthSamples );

		for( final CaptureLengthListener cll : captureLengthListeners )
		{
			cll.receiveCaptureLengthMillis( captureMillis );
			cll.receiveCaptureLengthSamples( newCaptureSamples );
		}

	}

	public void sendUiActive( final boolean active )
	{
		sendTemporalValueToInstance( ScopeIOQueueBridge.COMMAND_IN_ACTIVE, (active == true ? 1 : 0 ) );
	}

	public void setDesiredTrigger( final TriggerChoice triggerChoice )
	{
		sendTemporalValueToInstance( ScopeIOQueueBridge.COMMAND_IN_TRIGGER, triggerChoice.ordinal() );
	}

	public void setDesiredRepetition( final RepetitionChoice repetitionChoice )
	{
		sendTemporalValueToInstance( ScopeIOQueueBridge.COMMAND_IN_REPETITION, repetitionChoice.ordinal() );
	}

	public void addCaptureLengthListener( final CaptureLengthListener cll )
	{
		captureLengthListeners.add( cll );
	}

	public void setScopeDataVisualiser( final ScopeDataVisualiser scopeDataVisualiser )
	{
		this.scopeDataVisualiser = scopeDataVisualiser;
	}

	public void sendRecapture()
	{
		sendTemporalValueToInstance( ScopeIOQueueBridge.COMMAND_IN_RECAPTURE, 1 );
	}

	public void addSampleRateListener( final ScopeSampleRateListener srl )
	{
		sampleRateListeners.add( srl );
	}
}
