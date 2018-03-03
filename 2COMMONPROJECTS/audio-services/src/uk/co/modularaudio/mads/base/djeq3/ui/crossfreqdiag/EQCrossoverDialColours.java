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

package uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag;

import java.awt.Color;

import uk.co.modularaudio.mads.base.djeqn.ui.ColorDefines;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants.StdRotaryViewColor;

class EQCrossoverDialColours extends StdRotaryViewColor
{
	public EQCrossoverDialColours()
	{
		this.labelColor = Color.white;
		this.bgColor = ColorDefines.BACKGROUND_COLOR;
		this.knobIndicatorColor = ColorDefines.INDICATOR_COLOR;
	}
}