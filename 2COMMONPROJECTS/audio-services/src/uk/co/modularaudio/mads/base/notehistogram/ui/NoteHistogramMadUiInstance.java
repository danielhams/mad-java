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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramIOQueueBridge;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadDefinition;
import uk.co.modularaudio.mads.base.notehistogram.mu.NoteHistogramMadInstance;
import uk.co.modularaudio.mads.base.notehistogram.util.NoteHistogram;
import uk.co.modularaudio.util.audio.gui.mad.helper.AbstractNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class NoteHistogramMadUiInstance extends AbstractNoNameChangeNonConfigurableMadUiInstance<NoteHistogramMadDefinition, NoteHistogramMadInstance>
{
	private static Log log = LogFactory.getLog( NoteHistogramMadUiInstance.class.getName() );

	private final static int NUM_HISTOGRAM_BUCKETS = 40;
	private final static int NUM_FRAMES_PER_BUCKET = 25;

	private final NoteHistogram noteHistogram = new NoteHistogram( NUM_HISTOGRAM_BUCKETS, NUM_FRAMES_PER_BUCKET );

	public NoteHistogramMadUiInstance( final NoteHistogramMadInstance instance,
			final NoteHistogramMadUiDefinition uiDefinition )
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
	public void consumeQueueEntry( final NoteHistogramMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
		switch( nextOutgoingEntry.command )
		{
			case NoteHistogramIOQueueBridge.COMMAND_OUT_NOTE_DIFF:
			{
				final int value = (int)nextOutgoingEntry.value;

				noteHistogram.addNoteDiff( value );
				noteHistogram.dumpStats();

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
}
