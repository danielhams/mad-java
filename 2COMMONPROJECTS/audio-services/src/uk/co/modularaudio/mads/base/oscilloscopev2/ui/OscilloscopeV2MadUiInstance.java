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

package uk.co.modularaudio.mads.base.oscilloscopev2.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2MadDefinition;
import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2MadInstance;
import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2IOQueueBridge;
import uk.co.modularaudio.mads.base.oscilloscopev2.mu.OscilloscopeV2RingBuffer;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.swing.general.FloatJSliderModel;

public class OscilloscopeV2MadUiInstance extends AbstractNonConfigurableMadUiInstance<OscilloscopeV2MadDefinition, OscilloscopeV2MadInstance>
	implements IOQueueEventUiConsumer<OscilloscopeV2MadInstance>
{
	private static Log log = LogFactory.getLog( OscilloscopeV2MadUiInstance.class.getName() );

	private OscilloscopeV2MadInstance instance = null;
	private OscilloscopeV2RingBuffer leftCaptureRingBuffer = null;
	private OscilloscopeV2RingBuffer rightCaptureRingBuffer = null;

	protected boolean uiActive = false;

	private OscilloscopeV2ScopeDataListener scopeDataListener = null;

	private int currentCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), 1.0f );
	private int maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), OscilloscopeV2MadInstance.MAX_CAPTURE_MILLIS );

	private float[] internalLeftCaptureBuffer = new float[ AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), OscilloscopeV2MadInstance.MAX_CAPTURE_MILLIS ) ];
	private float[] internalRightCaptureBuffer = new float[ AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), OscilloscopeV2MadInstance.MAX_CAPTURE_MILLIS ) ];

	public FloatJSliderModel valueFloatSliderModel = new FloatJSliderModel( 20.0, 1.0, OscilloscopeV2MadInstance.MAX_CAPTURE_MILLIS, 1.0 );

	private long knownAudioLatencyNanos = 0;

	public OscilloscopeV2MadUiInstance( OscilloscopeV2MadInstance instance,
			OscilloscopeV2MadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
		this.instance = instance;
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		long newAln = timingParameters.getNanosOutputLatency();
		if( newAln != knownAudioLatencyNanos )
		{
			knownAudioLatencyNanos = newAln;
		}
//		log.debug("Tick received.");

		// Process messages before we pass the tick to the controls (and thus the display)
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );

		if( uiActive )
		{
//			log.debug("Is active");
			leftCaptureRingBuffer = instance.scope0CaptureRingBuffer;
			if( leftCaptureRingBuffer != null )
			{
				leftCaptureRingBuffer.oscilloscopeReadNumSamplesWindow( internalLeftCaptureBuffer, 0, currentCaptureBufferLength );
			}
			rightCaptureRingBuffer = instance.scope1CaptureRingBuffer;
			if( rightCaptureRingBuffer != null )
			{
				rightCaptureRingBuffer.oscilloscopeReadNumSamplesWindow( internalRightCaptureBuffer, 0, currentCaptureBufferLength );
			}
			scopeDataListener.processScopeData( currentCaptureBufferLength,
					leftCaptureRingBuffer.isConnected(),
					internalLeftCaptureBuffer,
					rightCaptureRingBuffer.isConnected(),
					internalRightCaptureBuffer );
		}

		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void consumeQueueEntry( OscilloscopeV2MadInstance instance,
			IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			default:
			{
				log.error("Unknown output command from MI: " + nextOutgoingEntry.command );
				break;
			}
		}
	}

	public void sendUiActive( boolean active )
	{
		uiActive = active;
		sendCommandValueToInstance( OscilloscopeV2IOQueueBridge.COMMAND_IN_ACTIVE, ( active ? 1 : 0 ) );
	}

	public void registerScopeDataListener( OscilloscopeV2ScopeDataListener scopeDataListener )
	{
		this.scopeDataListener = scopeDataListener;
	}

	public void setCaptureTimeMillis( float captureMillis )
	{
		currentCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), captureMillis );
		currentCaptureBufferLength = (currentCaptureBufferLength < 1 ? 1 :
			(currentCaptureBufferLength > maxRingBufferingInSamples ? maxRingBufferingInSamples :
				currentCaptureBufferLength ) );
		sendTemporalValueToInstance( OscilloscopeV2IOQueueBridge.COMMAND_IN_CAPTURE_MILLIS, Float.floatToIntBits( captureMillis ) );
	}
}
