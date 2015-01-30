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

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadInstance;
import uk.co.modularaudio.service.madcomponent.AbstractMadComponentFactory;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class RackMasterIOComponentsFactory extends AbstractMadComponentFactory
{
	private Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > defClassToInsClassMap =
			new HashMap<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>>>();

	private RackMasterIOCreationContext creationContext = new RackMasterIOCreationContext();
	
	public RackMasterIOComponentsFactory()
	{
		defClassToInsClassMap.put( RackMasterIOMadDefinition.class, RackMasterIOMadInstance.class );
	}

	@Override
	public Map<Class<? extends MadDefinition<?, ?>>, Class<? extends MadInstance<?, ?>>> provideDefClassToInsClassMap()
			throws ComponentConfigurationException
	{
		return defClassToInsClassMap;
	}

	@Override
	public MadCreationContext getCreationContext()
	{
		return creationContext;
	}
}
