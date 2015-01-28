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

package uk.co.modularaudio.service.userpreferences.mvc.hardware;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.mvc.combo.idstringandvalue.IdStringAndValueComboModel;

public class HardwareProviderMVCModel extends IdStringAndValueComboModel<HardwareProviderComboItem, HardwareProvider>
{
	private static Log log = LogFactory.getLog( HardwareProviderMVCModel.class.getName() );

	public HardwareProviderMVCModel(final Collection<HardwareProviderComboItem> startupItems)
	{
		super(startupItems);
	}

	public void setSelectedItemById(final String deviceId)
	{
		final HardwareProviderComboItem item = this.idToElementMap.get( deviceId );

		if( item != null )
		{
			this.setSelectedItemByIndex( this.getItemIndex( item ) );
		}
	}

	public List<HardwareProviderComboItem> getElements()
	{
		return this.theList;
	}

	public void removeElements(final Set<HardwareProviderComboItem> providersToRemove)
	{
		final int currentlySelectedItemIndex = getSelectedItemIndex();
		HardwareProviderComboItem currentlySelectedItem = null;
		if( log.isDebugEnabled() )
		{
			log.debug("In provider remove elements CSII is " + currentlySelectedItemIndex );
		}
		if( currentlySelectedItemIndex != -1 )
		{
			currentlySelectedItem = getElementAt( currentlySelectedItemIndex );
			log.debug("This maps to provider " + currentlySelectedItem.getDisplayString() );
			if( providersToRemove.contains( currentlySelectedItem ) )
			{
				log.debug("Setting currently selected item to -1");
				currentlySelectedItem = null;
				this.setSelectedItemByIndex( -1 );
			}
		}
		for( final HardwareProviderComboItem ci : providersToRemove )
		{
			log.debug("Removing provider " + ci.getDisplayString() );
			this.theList.remove( ci );
			this.idToElementMap.remove( ci.getId() );
		}

		// Now make sure we still have the previously selected element selected
		if( currentlySelectedItem != null )
		{
			final int newSelectedIndex = this.theList.indexOf( currentlySelectedItem );
			this.currentlySelectedItemIndex = newSelectedIndex;
		}
	}

	public boolean containsProvider( final HardwareProviderComboItem deviceComboItem )
	{
		return this.theList.contains( deviceComboItem );
	}

	public void addNewElements(final Set<HardwareProviderComboItem> providersToAdd)
	{
		for( final HardwareProviderComboItem cdci : providersToAdd )
		{
			this.theList.add( cdci );
			this.idToElementMap.put( cdci.getId(), cdci );
		}
	}
}
