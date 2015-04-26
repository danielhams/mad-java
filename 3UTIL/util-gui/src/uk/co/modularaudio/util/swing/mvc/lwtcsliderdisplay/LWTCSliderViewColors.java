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

package uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay;

import java.awt.Color;

public class LWTCSliderViewColors
{
	public final Color bgColor;
	public final Color fgColor;
	public final Color textboxBgColor;
	public final Color textboxFgColor;
	public final Color selectionColor;
	public final Color selectedTextColor;
	public final Color labelColor;
	public final Color unitsColor;

	public LWTCSliderViewColors(
			final Color bgColor,
			final Color fgColor,
			final Color textboxBgColor,
			final Color textboxFgColor,
			final Color selectionColor,
			final Color selectedTextColor,
			final Color labelColor,
			final Color unitsColor )
	{
		this.bgColor = bgColor;
		this.fgColor = fgColor;
		this.textboxBgColor = textboxBgColor;
		this.textboxFgColor = textboxFgColor;
		this.selectionColor = selectionColor;
		this.selectedTextColor = selectedTextColor;
		this.labelColor = labelColor;
		this.unitsColor = unitsColor;
	}
}
