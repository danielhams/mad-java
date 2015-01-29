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

package uk.co.modularaudio.mads.base.sampleplayer.ui;

import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerIOQueueBridge;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadDefinition;
import uk.co.modularaudio.mads.base.sampleplayer.mu.SingleSamplePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.audio.midi.MidiNote;

public class SingleSamplePlayerMadUiInstance extends NoEventsNoNameChangeNonConfigurableMadUiInstance<SingleSamplePlayerMadDefinition, SingleSamplePlayerMadInstance>
{
//	private static Log log = LogFactory.getLog( SingleSamplePlayerMadUiInstance.class.getName() );

	public SingleSamplePlayerMadUiInstance( final SingleSamplePlayerMadInstance instance,
			final SingleSamplePlayerMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void sendDesiredStartPosition( final float startPosMillis )
	{
		sendTemporalValueToInstance( SingleSamplePlayerIOQueueBridge.COMMAND_START_POS, Float.floatToIntBits( startPosMillis ) );
	}

	public void sendRootNoteChoice( final MidiNote mn )
	{
		sendTemporalValueToInstance( SingleSamplePlayerIOQueueBridge.COMMAND_ROOT_NOTE, mn.getMidiNumber() );
	}
}
