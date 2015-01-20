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

package uk.co.modularaudio.controller.component.impl;

import java.util.Map;

import uk.co.modularaudio.controller.component.ComponentController;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class ComponentControllerImpl implements ComponentWithLifecycle, ComponentController
{
	
	private MadComponentService componentService = null;
	private MadComponentUiService componentUiService = null;

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	public void setComponentService(MadComponentService componentService)
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	@Override
	public MadDefinitionListModel listDefinitionsAvailable() throws DatastoreException
	{
		return componentService.listDefinitionsAvailable();
	}

	@Override
	public MadDefinition<?,?> findDefinitionById( String definitionId )
			throws DatastoreException, RecordNotFoundException
	{
		return componentService.findDefinitionById( definitionId );
	}

	@Override
	public MadInstance<?,?> createInstanceFromDefinition(
			MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues,
			String name ) throws MadProcessingException, DatastoreException, RecordNotFoundException
	{
		return componentService.createInstanceFromDefinition( definition, parameterValues, name );
	}

	@Override
	public Span getUiSpanForDefinition( MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		return componentUiService.getUiSpanForDefinition( definition );
	}

}
