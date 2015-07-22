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

package uk.co.modularaudio.controller.userpreferences;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.exception.DatastoreException;

/**
 * <p>Entry point for the macro operations that may be performed on user preferences.</p>
 * <p>The user preferences controller is a vertical responsibility controller that delegates
 * and/or coordinates work as appropriate to services implementing the required
 * functionality.</p>
 *
 * @author dan
 */
public interface UserPreferencesController
{
	/**
	 * <p>Obtain the MVC view for user preferences.</p>
	 * @see GuiService#getUserPreferencesMVCView(UserPreferencesMVCController)
	 */
	UserPreferencesMVCView getUserPreferencesMVCView() throws DatastoreException;
	/**
	 * <p>Obtain the MVC controller for user preferences.</p>
	 * @see UserPreferencesService#getUserPreferencesMVCController()
	 */
	UserPreferencesMVCController getUserPreferencesMVCController() throws DatastoreException;
	/**
	 * <p>Persist any changes made to the MVC model.</p>
	 * @see UserPreferencesService#applyUserPreferencesChanges(uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel)
	 */
	void applyUserPreferencesChanges() throws DatastoreException;

	/**
	 * <p>Revert any changes made to the MVC model and reload what
	 * was previously stored as preferences.</p>
	 * @see UserPreferencesService#createUserPreferencesModel()
	 */
	void reloadUserPreferences() throws DatastoreException;


	/**
	 * <p>Check if any changes made to the preferences have
	 * update the audio engine configuration and will require
	 * a restart.</p>
	 * @return if the preferences contained audio engine changes
	 * @throws DatastoreException
	 */
	boolean checkForAudioEnginePrefsChanges() throws DatastoreException;
}
