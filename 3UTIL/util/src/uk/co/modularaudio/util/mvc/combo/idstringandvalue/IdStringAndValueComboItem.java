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

package uk.co.modularaudio.util.mvc.combo.idstringandvalue;

import uk.co.modularaudio.util.mvc.combo.ComboItem;

public class IdStringAndValueComboItem<B> implements ComboItem
{
	private String id = null;
	private String displayString = null;
	private B value = null;
	
	// For shits and giggles, a list of toilet paper sizes
	public IdStringAndValueComboItem( String id, String displayString, B value )
	{
		this.id = id;
		this.displayString = displayString;
		this.value = value;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getDisplayString()
	{
		return displayString;
	}
	
	public B getValue()
	{
		return value;
	}

}
