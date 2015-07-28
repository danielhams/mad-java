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

package uk.co.modularaudio.controller.userpreferences.impl;

import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class UserPreferencesControllerImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, UserPreferencesController
{
	private UserPreferencesService userPreferencesService;
	private GuiService guiService;

	// Where we stored the user preferences model whilst it is being edited
	private UserPreferencesMVCController userPreferencesMVCController;
	private UserPreferencesMVCView userPreferencesMVCView;

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
	}

	public void setUserPreferencesService(final UserPreferencesService userPreferencesService)
	{
		this.userPreferencesService = userPreferencesService;
	}

	public void setGuiService( final GuiService guiService )
	{
		this.guiService = guiService;
	}

	@Override
	public UserPreferencesMVCController getUserPreferencesMVCController() throws DatastoreException
	{
		if( userPreferencesMVCController == null )
		{
			userPreferencesMVCController = userPreferencesService.getUserPreferencesMVCController();
		}

		return userPreferencesMVCController;
	}

	@Override
	public void applyUserPreferencesChanges()
			throws DatastoreException
	{
		userPreferencesService.applyUserPreferencesChanges( userPreferencesMVCController.getModel() );
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView()
			throws DatastoreException
	{
		if( userPreferencesMVCView == null )
		{
			userPreferencesMVCView = guiService.getUserPreferencesMVCView( userPreferencesMVCController );
		}
		return userPreferencesMVCView;
	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		try
		{
			getUserPreferencesMVCController();
			getUserPreferencesMVCView();
		}
		catch( final DatastoreException de )
		{
			throw new ComponentConfigurationException( de );
		}
	}

	@Override
	public void preShutdown()
	{
	}

	@Override
	public void reloadUserPreferences() throws DatastoreException
	{
		final UserPreferencesMVCModel model = userPreferencesService.createUserPreferencesModel();

		userPreferencesMVCController.setModel( model );
		userPreferencesMVCView.setModel( model );

		userPreferencesService.setupPreferencesSelections();
	}

	@Override
	public boolean checkForAudioEnginePrefsChanges() throws DatastoreException
	{
		return userPreferencesService.checkForAudioEnginePrefsChanges( userPreferencesMVCController.getModel() );
	}
}
