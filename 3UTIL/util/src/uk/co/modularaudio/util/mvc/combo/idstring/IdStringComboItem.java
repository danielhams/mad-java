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

	public IdStringComboItem( final String id, final String displayString )
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayString == null) ? 0 : displayString.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( !(obj instanceof IdStringComboItem) )
		{
			return false;
		}
		final IdStringComboItem other = (IdStringComboItem) obj;
		if( displayString == null )
		{
			if( other.displayString != null )
			{
				return false;
			}
		}
		else if( !displayString.equals( other.displayString ) )
		{
			return false;
		}
		if( id == null )
		{
			if( other.id != null )
			{
				return false;
			}
		}
		else if( !id.equals( other.id ) )
		{
			return false;
		}
		return true;
	}

}
