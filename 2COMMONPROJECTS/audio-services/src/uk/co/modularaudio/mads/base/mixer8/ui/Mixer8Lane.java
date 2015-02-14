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

package uk.co.modularaudio.mads.base.mixer8.ui;

import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadDefinition;
import uk.co.modularaudio.mads.base.mixer8.mu.Mixer8MadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.lane.LaneMixerPanelUiInstance;

public class Mixer8Lane extends LaneMixerPanelUiInstance<Mixer8MadDefinition, Mixer8MadInstance, Mixer8MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public Mixer8Lane( final Mixer8MadDefinition definition,
			final Mixer8MadInstance instance,
			final Mixer8MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
