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

package uk.co.modularaudio.mads.internal.audiosystemtester.ui;

import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstance;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstanceConfiguration;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public class AudioSystemTesterMadUiInstance extends MadUiInstance<AudioSystemTesterMadDefinition, AudioSystemTesterMadInstance>
{
	private Span span = null;

	public AudioSystemTesterMadUiInstance( AudioSystemTesterMadInstance instance,
			AudioSystemTesterMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
		
		AudioSystemTesterMadInstanceConfiguration instanceConfiguration = instance.getInstanceConfiguration();
		int numOutputChannels = instanceConfiguration.getNumOutputChannels();
		
		int startXOffset = AudioSystemTesterMadUiDefinition.OUTPUT_CHANNELS_START.x;
		int startYOffset = AudioSystemTesterMadUiDefinition.OUTPUT_CHANNELS_START.y;
		int numChannelsWidth = numOutputChannels * AudioSystemTesterMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		
		int totalWidth = startXOffset + numChannelsWidth;
		int totalHeight = startYOffset + AudioSystemTesterMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		
		// 150 x 50  per cell
		int numCellsWide = (totalWidth / 150) + (totalWidth % 150 > 0 ? 1 : 0 );
		int numCellsHigh = (totalHeight / 50) + (totalHeight % 50 > 0 ? 1 : 0 );
		
		span = new Span( numCellsWide, numCellsHigh );
	}

	@Override
	public Span getCellSpan()
	{
		return span;
	}

	@Override
	public void consumeQueueEntry( AudioSystemTesterMadInstance instance, IOQueueEvent nextOutgoingEntry)
	{
	}
}
