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

package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;

public class ShowPreferencesAction extends AbstractAction
{
	private static final long serialVersionUID = -5903263092723112562L;

	private final ComponentDesignerFrontController fc;

	private final MainFrame mainFrame;

	private final PreferencesDialog preferencesDialog;

	public ShowPreferencesAction( final ComponentDesignerFrontController fc,
			final MainFrame mainFrame,
			final PreferencesDialog preferencesDialog )
	{
		this.fc = fc;
		this.mainFrame = mainFrame;
		this.preferencesDialog = preferencesDialog;
		this.putValue( NAME, "Preferences" );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		fc.reloadUserPreferences();
		preferencesDialog.setLocationRelativeTo( mainFrame );
		preferencesDialog.setVisible( true );
	}
}
