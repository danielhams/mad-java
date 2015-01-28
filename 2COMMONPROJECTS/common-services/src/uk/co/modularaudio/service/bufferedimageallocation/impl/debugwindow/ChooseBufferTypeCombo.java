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

package uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow;

import javax.swing.JComboBox;

import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.util.exception.DatastoreException;

public class ChooseBufferTypeCombo extends JComboBox<String>
{
	private static final long serialVersionUID = 4839026259124767686L;

	private final TypeToCacheComboBoxModel stringModel;

	public ChooseBufferTypeCombo( final OpenLongObjectHashMap<AllocationCacheForImageType> typeToCacheMap )
		throws DatastoreException
	{
		stringModel = new TypeToCacheComboBoxModel( typeToCacheMap );

		this.setModel( stringModel );
	}

	public long getSelectedCompoundKey()
	{
		return stringModel.getSelectedCacheCompoundKey();
	}

}
