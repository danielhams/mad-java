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

package uk.co.modularaudio.mads.internal.fade.ui;

import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

public class FadeOutMadUiInstance extends MadUiInstance<FadeOutMadDefinition, FadeOutMadInstance>
{
	public FadeOutMadUiInstance( final FadeOutMadInstance instance,
			final FadeOutMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
	}

	@Override
	public Span getCellSpan()
	{
		return uiDefinition.getCellSpan();
	}

	@Override
	public void consumeQueueEntry( final FadeOutMadInstance instance, final IOQueueEvent nextOutgoingEntry)
	{
	}
}
