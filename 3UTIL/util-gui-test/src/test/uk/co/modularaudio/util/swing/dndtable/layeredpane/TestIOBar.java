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

package test.uk.co.modularaudio.util.swing.dndtable.layeredpane;

import uk.co.modularaudio.util.table.Span;

public class TestIOBar extends TestTC
{
	
	public Span span = new Span( 10, 2 );
	public String name = null;
	
	public TestIOBar()
	{
		super( 0 );
		this.name = "IO Bar";
	}
	
	public String getName()
	{
		return name;
	}

	@Override
	public Span getCellSpan()
	{
		return span;
	}

}
