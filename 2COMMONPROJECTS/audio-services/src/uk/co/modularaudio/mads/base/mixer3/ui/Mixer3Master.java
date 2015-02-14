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

package uk.co.modularaudio.mads.base.mixer3.ui;

import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadDefinition;
import uk.co.modularaudio.mads.base.mixer3.mu.Mixer3MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.master.MasterMixerPanelUiInstance;

public class Mixer3Master extends MasterMixerPanelUiInstance<Mixer3MadDefinition, Mixer3MadInstance, Mixer3MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public Mixer3Master( final Mixer3MadDefinition definition,
			final Mixer3MadInstance instance,
			final Mixer3MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
