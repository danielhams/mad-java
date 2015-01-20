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

package uk.co.modularaudio.mads.base.notemultiplexer.ui;

import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadDefinition;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadInstance;
import uk.co.modularaudio.mads.base.notemultiplexer.mu.NoteMultiplexerMadInstanceConfiguration;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public class NoteMultiplexerMadUiInstance extends MadUiInstance<NoteMultiplexerMadDefinition, NoteMultiplexerMadInstance>
{
	private Span span = null;

	public NoteMultiplexerMadUiInstance( NoteMultiplexerMadInstance instance,
			NoteMultiplexerMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
		
		NoteMultiplexerMadInstanceConfiguration instanceConfiguration = instance.getInstanceConfiguration();
		int numInputChannels = 1;
		int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		
		int startXOffset = NoteMultiplexerMadUiDefinition.INPUT_CHANNELS_START.x;
		int startYOffset = NoteMultiplexerMadUiDefinition.INPUT_CHANNELS_START.y;
		int numChannelsWidth = numInputChannels * NoteMultiplexerMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		int outputChannelsHeight = numOutputChannels * NoteMultiplexerMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		
		int totalWidth = startXOffset + numChannelsWidth + NoteMultiplexerMadUiDefinition.INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
				numChannelsWidth;
		int totalHeight = startYOffset + outputChannelsHeight;
		
		// 150 x 50  per cell
		int numCellsWide = (totalWidth / GuiRackPanel.frontGridSize.width) + (totalWidth % GuiRackPanel.frontGridSize.width > 0 ? 1 : 0 );
		int numCellsHigh = (totalHeight / GuiRackPanel.frontGridSize.height) + (totalHeight % GuiRackPanel.frontGridSize.height > 0 ? 1 : 0 );
		
		span = new Span( numCellsWide, numCellsHigh );
	}

	@Override
	public Span getCellSpan()
	{
		return span;
	}

	@Override
	public void consumeQueueEntry( NoteMultiplexerMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
