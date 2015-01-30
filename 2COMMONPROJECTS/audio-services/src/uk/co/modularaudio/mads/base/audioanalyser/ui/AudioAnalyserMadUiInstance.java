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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserIOQueueBridge;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadDefinition;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class AudioAnalyserMadUiInstance extends AbstractNoNameChangeConfigurableMadUiInstance<AudioAnalyserMadDefinition, AudioAnalyserMadInstance>
	implements IOQueueEventUiConsumer<AudioAnalyserMadInstance>
{
	private static Log log = LogFactory.getLog( AudioAnalyserMadUiInstance.class.getName() );

	// Maximum to buffer in entirety is five seconds
	public static final float MAX_CAPTURE_MILLIS = 6000.0f;

	protected long startupTimestamp = 0;

	private final AudioAnalyserUiBufferState uiBufferState;

	public AudioAnalyserMadUiInstance( final AudioAnalyserMadInstance instance,
			final AudioAnalyserMadUiDefinition componentUiDefinition )
	{
		super( instance, componentUiDefinition, componentUiDefinition.getCellSpan() );

		uiBufferState = new AudioAnalyserUiBufferState( instance, MAX_CAPTURE_MILLIS );
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup(ratesAndLatency, timingParameters, frameTimeFactory);
		startupTimestamp = System.nanoTime();
		uiBufferState.receiveStartup(ratesAndLatency, timingParameters);
	}

	@Override
	public void receiveStop()
	{
		uiBufferState.receiveStop();
		super.receiveStop();
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
	public void consumeQueueEntry( final AudioAnalyserMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case AudioAnalyserIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX:
			{
				final long value = nextOutgoingEntry.value;
				final int bufferNum = (int)((value ) & 0xFFFFFFFF);
				final int ringBufferIndex = (int)((value >> 32 ) & 0xFFFFFFFF);

				if( bufferNum == 0 && uiBufferState != null)
				{
					uiBufferState.receiveBufferIndexUpdate(nextOutgoingEntry.frameTime, ringBufferIndex);
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

	public void sendUiActive( final boolean active )
	{
		sendTemporalValueToInstance( AudioAnalyserIOQueueBridge.COMMAND_IN_ACTIVE, ( active ? 1 : 0 ) );
	}

	@Override
	public void destroy()
	{
		uiBufferState.destroy();
		super.destroy();
	}

	public AudioAnalyserUiBufferState getUiBufferState()
	{
		return uiBufferState;
	}
}
