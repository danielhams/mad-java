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

import java.util.HashSet;
import java.util.Set;


public class Recomposition
{
	public int x = -1;
	public int y = -1;
	public int width = -1;
	public int height = -1;
	public Set<FreeEntry> freeEntriesToRemove = new HashSet<FreeEntry>();
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("xy(");
		sb.append(x);
		sb.append(",");
		sb.append(y);
		sb.append(")wh(");
		sb.append(width);
		sb.append(",");
		sb.append(height);
		sb.append(")");
		
		return sb.toString();
	}
}
