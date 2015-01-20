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

package uk.co.modularaudio.util.swing.table;

import java.awt.Component;

import uk.co.modularaudio.util.table.RackModelTableSpanningContents;
import uk.co.modularaudio.util.table.SpanningContentsProperties;

public interface GuiTable<A extends RackModelTableSpanningContents, B extends SpanningContentsProperties, C extends Component>
{
	public void addGuiComponentAtGrid( A tableModelComponent,
			C guiComponent,
			int indexInModel,
			int gridStartX, int gridStartY, int gridEndX, int gridEndY );

	public void insertGuiComponentFromModelIndex(int iCounter);

	public void updateGuiComponentFromModelIndex(int uCounter);

	public void removeGuiComponentFromModelIndex(int dCounter);

	public void contentsChangeBegin();

	public void contentsChangeEnd();
	
	public void destroy();
}
