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

package uk.co.modularaudio.util.mvc.combo.idstring;

import uk.co.modularaudio.util.mvc.combo.ComboItem;

public class IdStringComboItem implements ComboItem
{
	private String id = null;
	private String displayString = null;
	
	public IdStringComboItem( String id, String displayString )
	{
		this.id = id;
		this.displayString = displayString;
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

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null )
		{
			return false;
		}
		else if( obj.getClass() == this.getClass() )
		{
			IdStringComboItem o = (IdStringComboItem)obj;
			return o.getId().equals( id );
		}
		return false;
	}

}
