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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.userpreferences.mvc.comboitems.GuiFpsComboItem;
import uk.co.modularaudio.util.mvc.combo.idstring.IdStringComboModel;

public class GuiFpsMVCModel extends IdStringComboModel<GuiFpsComboItem>
{
	private static Log log = LogFactory.getLog( GuiFpsMVCModel.class.getName() );

	public GuiFpsMVCModel(final Collection<GuiFpsComboItem> startupItems)
	{
		super(startupItems);
	}

	@Override
	public int getItemIndex(final GuiFpsComboItem item)
	{
		final int retVal = super.getItemIndex(item);

		if( retVal == -1 )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Attempting to get index of " + item.getDisplayString() + " failed!");
			}
			for( final GuiFpsComboItem i : idToElementMap.values() )
			{
				if( log.isErrorEnabled() )
				{
					log.error("OID(" + i.getId() + ") OV(" + i.getDisplayString() + ")");
				}
			}
		}

		return retVal;
	}

	public int getFpsValue()
	{
		return Integer.parseInt( getSelectedElement().getDisplayString() );
	}
}
