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

package uk.co.modularaudio.util.audio.mad;

import java.util.Comparator;
import java.util.Vector;

import uk.co.modularaudio.util.sorteditemmodel.SortedItemListModel;

public class MadDefinitionListModel extends SortedItemListModel<MadDefinition<?,?>>
{
	private static final long serialVersionUID = 2966287446255846584L;

	public MadDefinitionListModel(final Vector<MadDefinition<?, ?>> items, final Comparator<MadDefinition<?, ?>> comp)
	{
		super(items, comp);
	}

	@Override
	public int getSize()
	{
		return super.getSize();
	}

	@Override
	public MadDefinition<?,?> getElementAt( final int index )
	{
		return super.getElementAt(index);
	}
}
