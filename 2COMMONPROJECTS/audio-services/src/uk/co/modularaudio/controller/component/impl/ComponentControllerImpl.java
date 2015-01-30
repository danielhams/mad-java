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

import uk.co.modularaudio.controller.component.ComponentController;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class ComponentControllerImpl implements ComponentWithLifecycle, ComponentController
{
	private MadComponentService componentService;
	private MadComponentUiService componentUiService;

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	public void setComponentService(final MadComponentService componentService)
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	@Override
	public MadDefinitionListModel listDefinitionsAvailable() throws DatastoreException
	{
		return componentService.listDefinitionsAvailable();
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		return componentUiService.getUiSpanForDefinition( definition );
	}

}
