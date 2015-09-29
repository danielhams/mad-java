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

package uk.co.modularaudio.mads.base.scopelarge.ui;

import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadDefinition;
import uk.co.modularaudio.mads.base.scopelarge.mu.ScopeLargeMadInstance;
import uk.co.modularaudio.mads.base.scopen.ui.ScopeNDisplayUiJComponent;

public class ScopeLargeDisplayUiJComponent
	extends ScopeNDisplayUiJComponent<ScopeLargeMadDefinition, ScopeLargeMadInstance, ScopeLargeMadUiInstance>
{
	private static final long serialVersionUID = -1857532354579036054L;

	public ScopeLargeDisplayUiJComponent( final ScopeLargeMadDefinition definition,
			final ScopeLargeMadInstance instance,
			final ScopeLargeMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition,
				instance,
				uiInstance,
				controlIndex,
				ScopeLargeMadUiDefinition.NUM_AMP_MARKS,
				ScopeLargeMadUiDefinition.NUM_TIME_MARKS,
				ScopeLargeMadUiDefinition.NUM_AMP_DECIMAL_PLACES );
	}
}
