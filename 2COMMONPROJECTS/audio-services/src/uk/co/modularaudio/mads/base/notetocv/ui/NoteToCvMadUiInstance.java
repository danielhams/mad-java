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

package uk.co.modularaudio.mads.base.notetocv.ui;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvIOQueueBridge;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.mads.base.notetocv.ui.NoteOnTypeChoiceUiJComponent.NoteOnType;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNonConfigurableMadUiInstance;

public class NoteToCvMadUiInstance extends NoEventsNonConfigurableMadUiInstance<NoteToCvMadDefinition, NoteToCvMadInstance>
{
//	private static Log log = LogFactory.getLog( NoteToCvMadUiInstance.class.getName() );

	public NoteToCvMadUiInstance( final NoteToCvMadInstance instance,
			final NoteToCvMadUiDefinition componentUiDefinition )
	{
		super( componentUiDefinition.getCellSpan(), instance, componentUiDefinition );
	}

	public void sendNoteOnType( final NoteOnType noteOnType )
	{
		sendTemporalValueToInstance( NoteToCvIOQueueBridge.COMMAND_NOTE_ON_TYPE, noteOnType.ordinal() );
	}

	public void sendFrequencyGlideMillis( final float guiDesiredFrequencyGlideMillis )
	{
		sendTemporalValueToInstance( NoteToCvIOQueueBridge.COMMAND_FREQ_GLIDE_MILLIS, Float.floatToIntBits( guiDesiredFrequencyGlideMillis ) );
	}

	public void sendNoteChannel( final int channelNum )
	{
		sendCommandValueToInstance( NoteToCvIOQueueBridge.COMMAND_CHANNEL_NUM, channelNum );
	}
}
