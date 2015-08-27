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

package uk.co.modularaudio.mads.rackmasterio;

import java.util.Collection;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.ui.RackMasterIOMadUiDefinition;
import uk.co.modularaudio.service.madcomponentui.AbstractMadComponentUiFactory;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;

public class RackMasterIOComponentsUiFactory extends AbstractMadComponentUiFactory
{
	private RackMasterIOComponentsFactory rackMasterIOComponentsFactory = null;

	@Override
	public void setupTypeToDefinitionClasses() throws DatastoreException
	{
		final Collection<MadDefinition<?,?>> auds = rackMasterIOComponentsFactory.listDefinitions();
		for( final MadDefinition<?,?> aud : auds )
		{
			if( aud instanceof RackMasterIOMadDefinition )
			{
				final RackMasterIOMadDefinition fi = (RackMasterIOMadDefinition)aud;
				final RackMasterIOMadUiDefinition fiud = new RackMasterIOMadUiDefinition( bufferedImageAllocationService, fi, componentImageFactory );
				componentDefinitionToUiDefinitionMap.put( aud, fiud );
			}
		}
	}

	public void setRackMasterIOComponentsFactory( final RackMasterIOComponentsFactory internalComponentsFactory )
	{
		this.rackMasterIOComponentsFactory = internalComponentsFactory;
	}

}
