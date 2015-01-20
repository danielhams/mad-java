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

import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemMidiDeviceComboItem;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.mvc.combo.idstringandvalue.IdStringAndValueComboModel;

public class AudioSystemMidiDeviceMVCModel extends IdStringAndValueComboModel<AudioSystemMidiDeviceComboItem, MidiHardwareDevice>
{
//	private static Log log = LogFactory.getLog( AudioSystemMidiDeviceMVCModel.class.getName() );

	public AudioSystemMidiDeviceMVCModel(Collection<AudioSystemMidiDeviceComboItem> startupItems)
	{
		super(startupItems);
	}

	public boolean containsDevice( AudioSystemMidiDeviceComboItem deviceComboItem )
	{
		return this.theList.contains( deviceComboItem );
	}

	public void addNewElements(Set<AudioSystemMidiDeviceComboItem> devicesToAdd)
	{
		for( AudioSystemMidiDeviceComboItem cdci : devicesToAdd )
		{
			this.theList.add( cdci );
			this.idToElementMap.remove( cdci.getId() );
		}
	}

	public List<AudioSystemMidiDeviceComboItem> getElements()
	{
		return this.theList;
	}

	public void setSelectedItemById(String deviceId)
	{
		AudioSystemMidiDeviceComboItem item = this.idToElementMap.get( deviceId );

		if( item != null )
		{
			this.setSelectedItemByIndex( this.getItemIndex( item ) );
		}
	}

}
