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

package test.uk.co.modularaudio.util.table.testobjs;

import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TableModelListener;

public class TestTableListener implements TableModelListener<AudioComponent, AudioComponentProperties>
{
	// Only a test object, don't care if we expose things we shouldn't
	public Object sourceObject = null;
	public int eventType = -1;
	public int firstRow = -1;
	public int lastRow = -1;
	
	public int numTimesCalled = 0;

	@Override
	public void tableChanged(TableModelEvent<AudioComponent, AudioComponentProperties> e)
	{
		sourceObject = e.getSource();
		eventType = e.getType();
		firstRow = e.getFirstRow();
		lastRow = e.getLastRow();
		numTimesCalled++;
	}
}
