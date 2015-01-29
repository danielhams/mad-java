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

package uk.co.modularaudio.service.userpreferences.mvc.models;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemDeviceComboItem;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.mvc.combo.idstringandvalue.IdStringAndValueComboModel;

public class AudioSystemDeviceMVCModel extends IdStringAndValueComboModel<AudioSystemDeviceComboItem, AudioHardwareDevice>
{
	private static Log log = LogFactory.getLog( AudioSystemDeviceMVCModel.class.getName() );

	public AudioSystemDeviceMVCModel(final Collection<AudioSystemDeviceComboItem> startupItems)
	{
		super(startupItems);
	}

	public void setSelectedItemById(final String deviceId)
	{
		final AudioSystemDeviceComboItem item = idToElementMap.get( deviceId );

		if( item != null )
		{
			setSelectedItemByIndex( getItemIndex( item ) );
		}
	}

	public List<AudioSystemDeviceComboItem> getElements()
	{
		return this.theList;
	}

	public void removeElements(final Set<AudioSystemDeviceComboItem> clocksToRemove)
	{
		int currentlySelectedItemIndex = getSelectedItemIndex();
		AudioSystemDeviceComboItem currentlySelectedItem = null;
		if( log.isDebugEnabled() )
		{
			log.debug("In clock remove elements CSII is " + currentlySelectedItemIndex );
		}
		if( currentlySelectedItemIndex != -1 )
		{
			currentlySelectedItem = getElementAt( currentlySelectedItemIndex );
			if( log.isDebugEnabled() )
			{
				log.debug("This maps to device " + currentlySelectedItem.getDisplayString() );
			}
			if( clocksToRemove.contains( currentlySelectedItem ) )
			{
				if( log.isDebugEnabled() )
				{
					log.debug("Setting currently selected item to -1");
				}
				currentlySelectedItem = null;
				setSelectedItemByIndex( -1 );
			}
		}
		for( final AudioSystemDeviceComboItem ci : clocksToRemove )
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Removing clock " + ci.getDisplayString() );
			}
			theList.remove( ci );
			idToElementMap.remove( ci.getId() );
		}

		// Now make sure we still have the previously selected element selected
		if( currentlySelectedItem != null )
		{
			final int newSelectedIndex = theList.indexOf( currentlySelectedItem );
			currentlySelectedItemIndex = newSelectedIndex;
		}

	}

	public boolean containsDevice( final AudioSystemDeviceComboItem deviceComboItem )
	{
		return theList.contains( deviceComboItem );
	}

	public void addNewElements(final Set<AudioSystemDeviceComboItem> clocksToAdd)
	{
		for( final AudioSystemDeviceComboItem cdci : clocksToAdd )
		{
			theList.add( cdci );
			idToElementMap.put( cdci.getId(), cdci );
		}
	}



}
