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

package uk.co.modularaudio.mads.base.controllerhistogram.ui;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramIOQueueBridge;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadDefinition;
import uk.co.modularaudio.mads.base.controllerhistogram.mu.ControllerHistogramMadInstance;
import uk.co.modularaudio.mads.base.controllerhistogram.util.HistogramDisplay;
import uk.co.modularaudio.mads.base.controllerhistogram.util.ControllerEventReceivedListener;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ControllerHistogramMadUiInstance
	extends AbstractNoNameChangeNonConfigurableMadUiInstance<ControllerHistogramMadDefinition, ControllerHistogramMadInstance>
{
	private static Log log = LogFactory.getLog( ControllerHistogramMadUiInstance.class.getName() );

	private HistogramDisplay noteHistogramDisplay;
	private final ArrayList<ControllerEventReceivedListener> noteReceivedListeners = new ArrayList<ControllerEventReceivedListener>();

	public ControllerHistogramMadUiInstance( final ControllerHistogramMadInstance instance,
			final ControllerHistogramMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
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
	public void consumeQueueEntry( final ControllerHistogramMadInstance instance,
			final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case ControllerHistogramIOQueueBridge.COMMAND_OUT_NOTE_NANOS:
			{
				final int value = (int)nextOutgoingEntry.value;
//				log.debug( "Received note nanos" );
				noteHistogramDisplay.addNoteDiffNano( value );
				for( final ControllerEventReceivedListener nrl : noteReceivedListeners )
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

	public void setNoteHistogramDisplay( final HistogramDisplay nhd )
	{
		this.noteHistogramDisplay = nhd;
	}

	public void addNoteReceivedListener( final ControllerEventReceivedListener nrl )
	{
		noteReceivedListeners.add( nrl );
	}
}
