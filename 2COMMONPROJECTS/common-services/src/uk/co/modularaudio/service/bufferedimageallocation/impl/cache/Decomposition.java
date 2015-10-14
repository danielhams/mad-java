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

package uk.co.modularaudio.service.bufferedimageallocation.impl.cache;

public class Decomposition
{
	public int smallerBlockX;
	public int smallerBlockY;
	public int smallerBlockWidth;
	public int smallerBlockHeight;
	public int largerBlockX;
	public int largerBlockY;
	public int largerBlockWidth;
	public int largerBlockHeight;
	public boolean hasLarger;

	public String smallBlockToString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "xy(" );
		sb.append( smallerBlockX );
		sb.append( "," );
		sb.append( smallerBlockY );
		sb.append( ")");
		sb.append( "wh(" );
		sb.append( smallerBlockWidth );
		sb.append( "," );
		sb.append( smallerBlockHeight );
		sb.append( ")" );

		return sb.toString();
	}

	public String largeBlockToString()
	{
		if( hasLarger )
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "xy(" );
			sb.append( largerBlockX );
			sb.append( "," );
			sb.append( largerBlockY );
			sb.append( ")wh(" );
			sb.append( largerBlockWidth );
			sb.append( "," );
			sb.append( largerBlockHeight );
			sb.append( ")" );
			return sb.toString();
		}
		else
		{
			return "No large block";
		}
	}

}
