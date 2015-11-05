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

package uk.co.modularaudio.mads.base.notehistogram.ui;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramIOQueueBridge;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadInstance;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteHistogramDisplay;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteReceivedListener;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class NoteHistogramMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<NoteHistogramMadDefinition, NoteHistogramMadInstance>
{
	private static Log log = LogFactory.getLog( NoteHistogramMadUiInstance.class.getName() );

	private NoteHistogramDisplay noteHistogramDisplay;
	private final ArrayList<NoteReceivedListener> noteReceivedListeners = new ArrayList<NoteReceivedListener>();

	public NoteHistogramMadUiInstance( final NoteHistogramMadInstance instance,
			final NoteHistogramMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	@Override
	public void receiveStartup( final HardwareIOChannelSettings ratesAndLatency, final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
	{
		super.receiveStartup( ratesAndLatency, timingParameters, frameTimeFactory );
		final int sampleRate = ratesAndLatency.getAudioChannelSetting().getDataRate().getValue();
		noteHistogramDisplay.setSampleRate( sampleRate );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTick )
	{
		// Process incoming queue messages before we let the controls have a chance to process;
		localQueueBridge.receiveQueuedEventsToUi( guiTemporaryEventStorage, instance, this );
		super.doDisplayProcessing( guiTemporaryEventStorage, timingParameters, currentGuiTick );
	}

	@Override
	public void consumeQueueEntry( final NoteHistogramMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case NoteHistogramIOQueueBridge.COMMAND_OUT_NOTE_DIFF:
			{
				final int value = (int)nextOutgoingEntry.value;
//				log.debug( "Received note diff" );
				noteHistogramDisplay.addNoteDiff( value );
				for( final NoteReceivedListener nrl : noteReceivedListeners )
				{
					nrl.receivedNote();
				}
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("Unknown command received: " + nextOutgoingEntry.command );
				}
			}
		}
	}

	public void resetHistogram()
	{
		noteHistogramDisplay.reset();
	}

	public void setNoteHistogramDisplay( final NoteHistogramDisplay nhd )
	{
		this.noteHistogramDisplay = nhd;
	}

	public void addNoteReceivedListener( final NoteReceivedListener nrl )
	{
		noteReceivedListeners.add( nrl );
	}
}
