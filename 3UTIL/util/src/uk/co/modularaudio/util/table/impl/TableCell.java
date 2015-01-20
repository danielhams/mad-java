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

package uk.co.modularaudio.util.table.impl;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;

public class TableCell<A extends RackModelTableSpanningContents>
{
	private A cellContents = null;
	private int cellX = -1;
	private int cellY = -1;
	private int originCellX = -1;
	private int originCellY = -1;
	
	public TableCell( int cellX, int cellY )
	{
		this.cellX = cellX;
		this.cellY = cellY;
		this.originCellX = cellX;
		this.originCellY = cellY;
	}

	public A getCellContents()
	{
		return cellContents;
	}

	public void setCellContents(A cellContents, int originCellX, int originCellY)
	{
		this.cellContents = cellContents;
		this.originCellX = originCellX;
		this.originCellY = originCellY;
	}

	public int getCellX()
	{
		return cellX;
	}

	public int getCellY()
	{
		return cellY;
	}

	public int getOriginCellX()
	{
		return originCellX;
	}

	public int getOriginCellY()
	{
		return originCellY;
	}
}
