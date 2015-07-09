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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerIOQueueBridge;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.mads.base.waveroller.ui.WaveRollerScaleLimitComboUiJComponent.AmpScale;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillisMinOneSliderModel;

public class WaveRollerMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<WaveRollerMadDefinition, WaveRollerMadInstance>
	implements IOQueueEventUiConsumer<WaveRollerMadInstance>
{
	private static Log log = LogFactory.getLog( WaveRollerMadUiInstance.class.getName() );

	// Maximum to buffer in entirety is five seconds
	public static final float MAX_CAPTURE_MILLIS = 5000.0f;

	private WaveRollerDataListener scopeDataListener;

	private final List<ScaleLimitChangeListener> scaleChangeListeners = new ArrayList<ScaleLimitChangeListener>();

	private float desiredAmpScaleLimitDb = 0.0f;

	private float captureMillis = LogarithmicTimeMillisMinOneSliderModel.DEFAULT_MILLIS;

	public WaveRollerMadUiInstance( final WaveRollerMadInstance instance,
			final WaveRollerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		scopeDataListener.receiveStartup( ratesAndLatency, timingParameters );
	}

	@Override
	public void receiveStop()
	{
		super.receiveStop();
		scopeDataListener.receiveStop();
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		// Process messages before we pass the tick to the controls (and thus the display)
		localQueueBridge.receiveQueuedEventsToUi( tempEventStorage, instance, this );

		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
	}

	@Override
	public void consumeQueueEntry( final WaveRollerMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case WaveRollerIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
				final long value = nextOutgoingEntry.value;
				final int bufferNum = (int)((value ) & 0xFFFFFFFF);
				final int ringBufferIndex = (int)((value >> 32 ) & 0xFFFFFFFF);

				if( bufferNum == 0 )
				{
					scopeDataListener.receiveBufferIndexUpdate( nextOutgoingEntry.frameTime, ringBufferIndex );
				}
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown output command from MI: " + nextOutgoingEntry.command );
				}
				break;
			}
		}
	}

	public void setScopeDataListener( final WaveRollerDataListener scopeDataListener )
	{
		this.scopeDataListener = scopeDataListener;
		scopeDataListener.setCaptureTimeMillis( captureMillis );
	}

	public void setCaptureTime( final float captureMillis )
	{
		this.captureMillis = captureMillis;
		if( scopeDataListener != null )
		{
			scopeDataListener.setCaptureTimeMillis( captureMillis );
		}
	}

	public void sendUiActive( final boolean active )
	{
		sendTemporalValueToInstance( WaveRollerIOQueueBridge.COMMAND_IN_ACTIVE, ( active ? 1 : 0 ) );
	}

	public void addScaleChangeListener( final ScaleLimitChangeListener scl )
	{
		this.scaleChangeListeners.add( scl );
		scl.receiveScaleLimitChange( desiredAmpScaleLimitDb );
	}

	public void setDesiredAmpScaleLimit( final AmpScale ws )
	{
		desiredAmpScaleLimitDb = ws.getDb();
		for( final ScaleLimitChangeListener scl : scaleChangeListeners )
		{
			scl.receiveScaleLimitChange( desiredAmpScaleLimitDb );
		}
	}
}
