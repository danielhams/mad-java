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

package uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow.imagebrowser;

import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.RawImage;

public class RawImageComboModel implements ComboBoxModel<String>
{
	private final int numImages;
	private final long[] imageIndexToId;
	private final OpenLongObjectHashMap<RawImage> indexToRawImageMap = new OpenLongObjectHashMap<RawImage>();

	private String selectedItem;

	public RawImageComboModel( final AllocationCacheForImageType cache )
	{
//		this.cache = cache;
		final Set<RawImage> rawImages = cache.getRawImages();
		numImages = rawImages.size();
		imageIndexToId = new long[ numImages ];

		int curIndex = 0;
		for( final RawImage ri : rawImages )
		{
			final long rawImageId = ri.getRawImageId();
			indexToRawImageMap.put( rawImageId, ri );
			imageIndexToId[ curIndex++ ] = rawImageId;
		}
	}

	@Override
	public int getSize()
	{
		return numImages;
	}

	@Override
	public String getElementAt( final int index )
	{
		final long imageId = imageIndexToId[ index ];
		return imageId + "";
	}

	@Override
	public void addListDataListener( final ListDataListener l )
	{
	}

	@Override
	public void removeListDataListener( final ListDataListener l )
	{
	}

	@Override
	public String getSelectedItem()
	{
		return selectedItem;
	}

	@Override
	public void setSelectedItem( final Object anItem )
	{
		final String itemStr = (String)anItem;
		selectedItem = itemStr;
	}

}
