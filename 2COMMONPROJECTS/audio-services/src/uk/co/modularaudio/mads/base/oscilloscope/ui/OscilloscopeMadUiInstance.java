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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureRepetitionsEnum;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureTriggerEnum;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeIOQueueBridge;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeWriteableScopeData;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.pooling.dumb.ObjectPool;
import uk.co.modularaudio.util.pooling.dumb.ObjectPool.ObjectPoolLifecycleManager;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public class OscilloscopeMadUiInstance extends AbstractNonConfigurableMadUiInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance>
	implements IOQueueEventUiConsumer<OscilloscopeMadInstance>, ObjectPoolLifecycleManager<OscilloscopeWriteableScopeData>
{
	private static Log log = LogFactory.getLog( OscilloscopeMadUiInstance.class.getName() );

	private static final float MAX_CAPTURE_MILLIS = 5000.0f;
	private int maxCaptureBufferLength = -1;

	// Stuff for our processing
	protected ObjectPool<OscilloscopeWriteableScopeData> scopeDataPool = null;

	protected boolean uiActive = false;

	private long knownAudioIOLatencyNanoseconds = 0;

	private int numScopeDatasConsumed = 0;
//	private int displayTicks = 0;

	private ScopeDataListener scopeDataListener = null;

	private OscilloscopeCaptureRepetitionsEnum repetitionsChoice = OscilloscopeCaptureRepetitionsEnum.CONTINOUS;

	private boolean shouldEmit = true;

	private int currentCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), 1.0f );

	public FloatJSliderModel valueFloatSliderModel = new FloatJSliderModel( 20.0, 1.0, MAX_CAPTURE_MILLIS, 1.0 );

	public OscilloscopeMadUiInstance( OscilloscopeMadInstance instance,
			OscilloscopeMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );

		maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), MAX_CAPTURE_MILLIS );

		scopeDataPool = new ObjectPool<OscilloscopeWriteableScopeData>( this, 10, false );
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		long newAln = timingParameters.getNanosOutputLatency();

		if( newAln != knownAudioIOLatencyNanoseconds )
		{
			knownAudioIOLatencyNanoseconds = newAln;
			recalculateDataPool( knownAudioIOLatencyNanoseconds );
		}
//		log.debug("Tick received.");

		// Process messages before we pass the tick to the controls (and thus the display)
		numScopeDatasConsumed = 0;
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );
		if( numScopeDatasConsumed > 1 )
		{
//			log.debug("Consumed extra scope datas - total consumed: " + numScopeDatasConsumed );
		}

		if( uiActive )
		{
//			log.debug("Ui is active");
//			if( displayTicks == 0 )
			if( shouldEmit )
			{
				OscilloscopeWriteableScopeData scopeDataToSend = scopeDataPool.reserveObject();
//				OscilloscopeWriteableScopeData scopeDataToSend = null;
				if( scopeDataToSend != null )
				{
//					log.debug("Sending fresh scope data to aui - num used " + scopeDataPool.getNumUsed() + " num free now " + scopeDataPool.getNumFree() );
					sendScopeData( scopeDataToSend );
				}
				else
				{
//					log.warn("Missing scope data to send - numFree is " + scopeDataPool.getNumFree() + " and numUsed is " + scopeDataPool.getNumUsed());
				}
			}
//			displayTicks++;
//			if( displayTicks > 0 )
//			{
//				displayTicks = 0;
//			}
		}

		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void consumeQueueEntry( OscilloscopeMadInstance instance,
			IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case OscilloscopeIOQueueBridge.COMMAND_OUT_SCOPE_DATA:
			{
				Object obj = nextOutgoingEntry.object;
				OscilloscopeWriteableScopeData newDisplayData = (OscilloscopeWriteableScopeData)obj;
//				log.debug("Received scope data with timestamp " + nextOutgoingEntry.realEventTimestamp );

				if( scopeDataListener != null && newDisplayData.desiredDataLength == currentCaptureBufferLength && shouldEmit)
				{
					scopeDataListener.processScopeData( newDisplayData );
					if( repetitionsChoice == OscilloscopeCaptureRepetitionsEnum.ONCE )
					{
						shouldEmit = false;
					}
				}

				// Only release the data back to the pool if the buffer length matches
				if( newDisplayData.internalBufferLength == maxCaptureBufferLength )
				{
					scopeDataPool.releaseObject( newDisplayData );
				}
				else
				{
					scopeDataPool.removeObject( newDisplayData );
				}
				numScopeDatasConsumed++;

				break;
			}
			default:
			{
				log.error("Unknown output command from MI: " + nextOutgoingEntry.command );
				break;
			}
		}
	}

	@Override
	public OscilloscopeWriteableScopeData createNewInstance()
	{
//		log.debug("Creating extra scope instance.");
		OscilloscopeWriteableScopeData oscilloscopeWriteableScopeData = new OscilloscopeWriteableScopeData(
				maxCaptureBufferLength );
		oscilloscopeWriteableScopeData.desiredDataLength = currentCaptureBufferLength;
		resetInstanceForReuse( oscilloscopeWriteableScopeData );
		return oscilloscopeWriteableScopeData;
	}

	@Override
	public void resetInstanceForReuse( OscilloscopeWriteableScopeData objectToBeReused )
	{
		objectToBeReused.currentWriteIndex = 0;
		objectToBeReused.written = false;
		objectToBeReused.desiredDataLength = currentCaptureBufferLength;
	}

	private void recalculateDataPool( long audioIOLatencyNanos )
	{
		// Yes, there is a bug here in that we have to invalidate the pool when the latency changes as we need to resize the buffers
		// (maxCaptureBufferLength changes)

		long numMillisLatency = TimeUnit.NANOSECONDS.toMillis( audioIOLatencyNanos );
//		log.debug( "The latency in millis is " + numMillisLatency );
		maxCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), MAX_CAPTURE_MILLIS );
		double secondsLatency = (numMillisLatency / 1000.0);
		double numScopesNeededForLatencyDouble = (secondsLatency * 30 * 2) * 2;
//		double numScopesNeededForLatencyDouble = (secondsLatency * 3 * 2) * 1.2;
//		log.debug("This is " + numScopesNeededForLatencyDouble + " num scopes needed per second");
//		int numScopesNeeded = Math.max( ((int) numScopesNeededPerSecondDouble) + 1, 4 ) * 4;
		int numScopesNeeded = Math.max( ((int) numScopesNeededForLatencyDouble) + 1, 4 );
//		log.debug("This computes to " + numScopesNeeded + " scope data blocks needed");

		scopeDataPool.resetMaxAllocated( numScopesNeeded );
	}

	public void registerScopeDataListener( ScopeDataListener scopeDataListener )
	{
		this.scopeDataListener = scopeDataListener;
	}

	public void doRecapture()
	{
		shouldEmit = true;
	}

	public void sendUiActive( boolean active )
	{
		this.uiActive = active;
		sendCommandValueToInstance( OscilloscopeIOQueueBridge.COMMAND_IN_ACTIVE, ( active ? 1 : 0 ) );
	}

	public void sendTriggerChoice( OscilloscopeCaptureTriggerEnum ev )
	{
		sendTemporalValueToInstance( OscilloscopeIOQueueBridge.COMMAND_IN_CAPTURE_TRIGGER, ev.ordinal() );
	}

	public void sendRepetitionChoice( OscilloscopeCaptureRepetitionsEnum rv )
	{
		repetitionsChoice = rv;
		sendTemporalValueToInstance( OscilloscopeIOQueueBridge.COMMAND_IN_CAPTURE_REPETITIONS, rv.ordinal() );
	}

	public void sendCaptureMillis( float captureMillis )
	{
		currentCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), captureMillis );
		currentCaptureBufferLength = (currentCaptureBufferLength < 1 ? 1 : (currentCaptureBufferLength > maxCaptureBufferLength ? maxCaptureBufferLength : currentCaptureBufferLength ) );
		sendCommandValueToInstance( OscilloscopeIOQueueBridge.COMMAND_IN_CAPTURE_MILLIS,  Float.floatToIntBits( captureMillis ));
	}

	public void sendScopeData( OscilloscopeWriteableScopeData dataToPassToInstance )
	{
		sendTemporalObjectToInstance( OscilloscopeIOQueueBridge.COMMAND_IN_SCOPE_DATA, dataToPassToInstance );
	}
}
