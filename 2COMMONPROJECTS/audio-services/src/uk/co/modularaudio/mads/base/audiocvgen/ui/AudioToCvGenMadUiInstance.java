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

package uk.co.modularaudio.mads.base.audiocvgen.ui;

import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenMadDefinition;
import uk.co.modularaudio.mads.base.audiocvgen.mu.AudioToCvGenMadInstance;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNIOQueueBridge;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;
import uk.co.modularaudio.util.table.Span;

public class AudioToCvGenMadUiInstance<D extends AudioToCvGenMadDefinition<D,I>, I extends AudioToCvGenMadInstance<D,I>>
	extends NoEventsNoNameChangeNonConfigurableMadUiInstance<D, I>
{
//	private static Log log = LogFactory.getLog( AudioToCvGenMadUiInstance.class.getName() );

	public AudioToCvGenMadUiInstance( final Span span,
			final I instance,
			final MadUiDefinition<D, I> componentUiDefinition )
	{
		super( span, instance, componentUiDefinition );
	}

	public void sendUiActive( final boolean active )
	{
		sendCommandValueToInstance( MixerNIOQueueBridge.COMMAND_IN_ACTIVE, (active ? 1 : 0 ) );
	}
}
