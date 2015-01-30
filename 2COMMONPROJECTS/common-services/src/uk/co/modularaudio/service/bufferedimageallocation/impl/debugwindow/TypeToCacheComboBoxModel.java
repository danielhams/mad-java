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

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.apache.mahout.math.list.LongArrayList;
import org.apache.mahout.math.map.OpenLongObjectHashMap;
import org.apache.mahout.math.map.OpenObjectLongHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.exception.DatastoreException;

public class TypeToCacheComboBoxModel implements ComboBoxModel<String>
{
	private final ArrayList<Integer> imageTypeValues = new ArrayList<Integer>();
	private final ArrayList<String> displayValues = new ArrayList<String>();

	private final OpenObjectLongHashMap<String> displayNameToCacheKeyMap = new OpenObjectLongHashMap<String>();

	private String selectedItem;

	public TypeToCacheComboBoxModel( final OpenLongObjectHashMap<AllocationCacheForImageType> typeToCacheMap )
		throws DatastoreException
	{
		imageTypeValues.add( -1 );
		displayValues.add( "" );

		final LongArrayList typeList = typeToCacheMap.keys();
		final int numTypes = typeList.size();
		for( int t = 0 ; t < numTypes ; t++ )
		{
			final long compoundKey = typeList.get( t );
			final int lifetime = (int)((compoundKey >> 32) & 0xffff);
			final AllocationLifetime al = AllocationLifetime.values()[lifetime];
			final int allocationType = (int)(compoundKey & 0xffff );
			final AllocationBufferType abt = AllocationBufferType.values()[ allocationType ];

			imageTypeValues.add( abt.ordinal() );
			final String displayName = al.toString() + " " + abt.toString();
			displayValues.add( displayName );
			displayNameToCacheKeyMap.put( displayName, compoundKey );
		}
	}

	@Override
	public int getSize()
	{
		return displayValues.size();
	}

	@Override
	public String getElementAt( final int index )
	{
		return displayValues.get( index );
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
	public void setSelectedItem( final Object anItem )
	{
		selectedItem  = (String)anItem;
	}

	@Override
	public Object getSelectedItem()
	{
		return selectedItem;
	}

	public long getSelectedCacheCompoundKey()
	{
		long retVal = -1;
		if( selectedItem != null )
		{
			if( displayNameToCacheKeyMap.containsKey( selectedItem ) )
			{
				retVal = displayNameToCacheKeyMap.get( selectedItem );
			}
		}

		return retVal;
	}

}
