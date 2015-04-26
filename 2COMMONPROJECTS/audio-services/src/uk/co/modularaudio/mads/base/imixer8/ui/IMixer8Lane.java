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

package uk.co.modularaudio.mads.base.imixer8.ui;

import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadDefinition;
import uk.co.modularaudio.mads.base.imixer8.mu.IMixer8MadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.lane.LaneMixerPanelUiInstance;

public class IMixer8Lane extends LaneMixerPanelUiInstance<IMixer8MadDefinition, IMixer8MadInstance, IMixer8MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public IMixer8Lane( final IMixer8MadDefinition definition,
			final IMixer8MadInstance instance,
			final IMixer8MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
