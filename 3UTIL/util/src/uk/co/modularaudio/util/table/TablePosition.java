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

package uk.co.modularaudio.util.table;

public class TablePosition
{
	public int x;
	public int y;

	public TablePosition(final int x, final int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString()
	{
		final StringBuilder retVal = new StringBuilder(10);
		retVal.append("(");
		retVal.append(x);
		retVal.append(", ");
		retVal.append(y);
		retVal.append(")");
		return retVal.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		if( !(obj instanceof TablePosition) )
		{
			return false;
		}
		final TablePosition other = (TablePosition) obj;
		if( x != other.x )
		{
			return false;
		}
		if( y != other.y )
		{
			return false;
		}
		return true;
	}
}
