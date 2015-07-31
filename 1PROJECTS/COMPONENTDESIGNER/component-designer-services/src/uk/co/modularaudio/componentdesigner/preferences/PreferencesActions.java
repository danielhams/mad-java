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

package uk.co.modularaudio.componentdesigner.preferences;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.actions.ApplyPreferencesChangesAction;
import uk.co.modularaudio.componentdesigner.preferences.actions.CancelPreferencesChangesAction;
import uk.co.modularaudio.service.configuration.ConfigurationService;

public class PreferencesActions
{
	public static final String CANCEL_PREFERENCES_NAME = "Cancel Changes";
	public static final String APPLY_PREFERENCES_NAME = "Apply Changes";

	private final CancelPreferencesChangesAction cancelPreferencesChangesAction;
	private final ApplyPreferencesChangesAction applyPreferencesChangesAction;

	public PreferencesActions( final ComponentDesigner componentDesigner,
			final ComponentDesignerFrontController fc,
			final PreferencesDialog preferencesDialog,
			final ConfigurationService configurationService )
	{
		cancelPreferencesChangesAction = new CancelPreferencesChangesAction(fc, preferencesDialog);
		applyPreferencesChangesAction = new ApplyPreferencesChangesAction(fc, preferencesDialog);
	}

	public CancelPreferencesChangesAction getCancelAction()
	{
		return cancelPreferencesChangesAction;
	}

	public ApplyPreferencesChangesAction getApplyAction()
	{
		return applyPreferencesChangesAction;
	}

}
