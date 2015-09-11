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

package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Color;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.util.swing.colouredtoggle.ColouredLabelToggle;
import uk.co.modularaudio.util.swing.colouredtoggle.ToggleReceiver;

public class ScopeTopTriggerToggle extends ColouredLabelToggle
{
	private static final long serialVersionUID = 7011689081245984767L;

//	private static Log log = LogFactory.getLog( ScopeTopTriggerToggle.class.getName() );

	public ScopeTopTriggerToggle( final ToggleReceiver toggleReceiver )
	{
		super( "Trigger",
				"Click to toggle display of the trigger signal",
				ScopeColours.BACKGROUND_COLOR,
				Color.WHITE,
				ScopeWaveDisplay.VIS_COLOURS[0],
				true,
				toggleReceiver,
				0 );
	}

}
