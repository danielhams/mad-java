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

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Color;

public class RotaryViewColors
{
	public final Color bgColor;
	public final Color fgColor;
	public final Color textboxBgColor;
	public final Color textboxFgColor;
	public final Color selectionColor;
	public final Color selectedTextColor;
	public final Color knobOutlineColor;
	public final Color knobFillColor;
	public final Color knobExtentColor;
	public final Color knobIndicatorColor;
	public final Color labelColor;
	public final Color unitsColor;

	public RotaryViewColors(
			final Color bgColor,
			final Color fgColor,
			final Color textboxBgColor,
			final Color textboxFgColor,
			final Color selectionColor,
			final Color selectedTextColor,
			final Color knobOutlineColor,
			final Color knobFillColor,
			final Color knobExtentColor,
			final Color knobIndicatorColor,
			final Color labelColor,
			final Color unitsColor )
	{
		this.bgColor = bgColor;
		this.fgColor = fgColor;
		this.textboxBgColor = textboxBgColor;
		this.textboxFgColor = textboxFgColor;
		this.selectionColor = selectionColor;
		this.selectedTextColor = selectedTextColor;
		this.knobOutlineColor = knobOutlineColor;
		this.knobFillColor = knobFillColor;
		this.knobExtentColor = knobExtentColor;
		this.knobIndicatorColor = knobIndicatorColor;
		this.labelColor = labelColor;
		this.unitsColor = unitsColor;
	}
}
