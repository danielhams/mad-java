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

package uk.co.modularaudio.componentdesigner.preferences.newhardware;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PreferencesHardwarePage extends JPanel
{
	private static final long serialVersionUID = 8643014949116729611L;
//	private UserPreferencesMVCView userPreferencesView = null;

	public PreferencesHardwarePage( ComponentDesignerFrontController fc, PreferencesDialog preferencesDialog ) throws DatastoreException
	{
//		this.fc = fc;
//		this.preferencesDialog = preferencesDialog;
//		this.userPreferencesView = preferencesDialog.getUserPreferencesView();

		String migLayoutString = "fill";
		this.setLayout( new MigLayout( migLayoutString ) );

//		JPanel deviceChoicePanel = getDeviceChoicePanel();
//		this.add( deviceChoicePanel, "grow, shrink" );

	}
}
