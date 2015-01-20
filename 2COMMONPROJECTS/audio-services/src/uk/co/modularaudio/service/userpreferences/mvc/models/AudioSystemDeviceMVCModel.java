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

	public AudioSystemDeviceMVCModel(Collection<AudioSystemDeviceComboItem> startupItems)
	{
		super(startupItems);
	}

	public void setSelectedItemById(String deviceId)
	{
		AudioSystemDeviceComboItem item = this.idToElementMap.get( deviceId );

		if( item != null )
		{
			this.setSelectedItemByIndex( this.getItemIndex( item ) );
		}
	}

	public List<AudioSystemDeviceComboItem> getElements()
	{
		return this.theList;
	}

	public void removeElements(Set<AudioSystemDeviceComboItem> clocksToRemove)
	{
		int currentlySelectedItemIndex = getSelectedItemIndex();
		AudioSystemDeviceComboItem currentlySelectedItem = null;
		log.debug("In clock remove elements CSII is " + currentlySelectedItemIndex );
		if( currentlySelectedItemIndex != -1 )
		{
			currentlySelectedItem = getElementAt( currentlySelectedItemIndex );
			log.debug("This maps to device " + currentlySelectedItem.getDisplayString() );
			if( clocksToRemove.contains( currentlySelectedItem ) )
			{
				log.debug("Setting currently selected item to -1");
				currentlySelectedItem = null;
				this.setSelectedItemByIndex( -1 );
			}
		}
		for( AudioSystemDeviceComboItem ci : clocksToRemove )
		{
			log.debug("Removing clock " + ci.getDisplayString() );
			this.theList.remove( ci );
			this.idToElementMap.remove( ci.getId() );
		}
		
		// Now make sure we still have the previously selected element selected
		if( currentlySelectedItem != null )
		{
			int newSelectedIndex = this.theList.indexOf( currentlySelectedItem );
			this.currentlySelectedItemIndex = newSelectedIndex;
		}
		
	}

	public boolean containsDevice( AudioSystemDeviceComboItem deviceComboItem )
	{
		return this.theList.contains( deviceComboItem );
	}

	public void addNewElements(Set<AudioSystemDeviceComboItem> clocksToAdd)
	{
		for( AudioSystemDeviceComboItem cdci : clocksToAdd )
		{
			this.theList.add( cdci );
			this.idToElementMap.put( cdci.getId(), cdci );
		}
	}
	
	

}
