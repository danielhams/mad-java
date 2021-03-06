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

package uk.co.modularaudio.mads.base.specamplarge.ui;

import uk.co.modularaudio.mads.base.specampgen.ui.SpectralAmpGenMadUiInstance;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadDefinition;
import uk.co.modularaudio.mads.base.specamplarge.mu.SpecAmpLargeMadInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEventUiConsumer;

public class SpecAmpLargeMadUiInstance
	extends SpectralAmpGenMadUiInstance<SpecAmpLargeMadDefinition, SpecAmpLargeMadInstance>
		implements IOQueueEventUiConsumer<SpecAmpLargeMadInstance>
{
	public SpecAmpLargeMadUiInstance( final SpecAmpLargeMadInstance instance,
			final SpecAmpLargeMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
	}
}
