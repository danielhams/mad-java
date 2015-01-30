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

package uk.co.modularaudio.mads.base.audiocvconverter.ui;

import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadDefinition;
import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadInstance;
import uk.co.modularaudio.mads.base.audiocvconverter.mu.AudioCvConverterMadInstanceConfiguration;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeConfigurableMadUiInstance;
import uk.co.modularaudio.util.table.Span;

public class AudioCvConverterMadUiInstance extends NoEventsNoNameChangeConfigurableMadUiInstance<AudioCvConverterMadDefinition, AudioCvConverterMadInstance>
{
	public AudioCvConverterMadUiInstance( final AudioCvConverterMadInstance instance,
			final AudioCvConverterMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition, calculateSpanForInstanceConfiguration( instance.getInstanceConfiguration() ) );
	}


	private static Span calculateSpanForInstanceConfiguration( final AudioCvConverterMadInstanceConfiguration instanceConfiguration )
	{
		final int numInputChannels = instanceConfiguration.getNumInputChannels();

		final int startXOffset = AudioCvConverterMadUiDefinition.INPUT_CHANNELS_START.x;
		final int startYOffset = AudioCvConverterMadUiDefinition.INPUT_CHANNELS_START.y;
		final int numChannelsWidth = numInputChannels * AudioCvConverterMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;
		final int outputChannelsHeight = AudioCvConverterMadUiDefinition.CHANNEL_TO_CHANNEL_INCREMENT;

		final int totalWidth = startXOffset + numChannelsWidth + AudioCvConverterMadUiDefinition.INPUT_TO_OUTPUT_CHANNEL_INCREMENT +
				numChannelsWidth;
		final int totalHeight = startYOffset + outputChannelsHeight;

		// 150 x 50  per cell
		final int numCellsWide = (totalWidth / GuiRackPanel.FRONT_GRID_SIZE.width) + (totalWidth % GuiRackPanel.FRONT_GRID_SIZE.width > 0 ? 1 : 0 );
		final int numCellsHigh = (totalHeight / GuiRackPanel.FRONT_GRID_SIZE.height) + (totalHeight % GuiRackPanel.FRONT_GRID_SIZE.height > 0 ? 1 : 0 );

		return new Span( numCellsWide, numCellsHigh );
	}
}
