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

package uk.co.modularaudio.mads.base.imixer3.ui;

import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadDefinition;
import uk.co.modularaudio.mads.base.imixer3.mu.IMixer3MadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.lane.LaneMixerPanelUiInstance;

public class IMixer3Lane extends LaneMixerPanelUiInstance<IMixer3MadDefinition, IMixer3MadInstance, IMixer3MadUiInstance>
{
	private static final long serialVersionUID = 7431791158964357287L;

	public IMixer3Lane( final IMixer3MadDefinition definition,
			final IMixer3MadInstance instance,
			final IMixer3MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex );
	}

}
