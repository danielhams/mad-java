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

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserMusicDirMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserPatchesMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserSubRacksMVCView;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class PreferencesGeneralPage extends JPanel
{
	private static final long serialVersionUID = -2922911338893619656L;
	private final UserPreferencesMVCView userPreferencesView;
	private final UserPreferencesUserPatchesMVCView userPatchesView;
	private final UserPreferencesUserSubRacksMVCView userSubRacksView;
	private final UserPreferencesUserMusicDirMVCView userMusicDirView;

	public PreferencesGeneralPage( final ComponentDesignerFrontController fc, final PreferencesDialog preferencesDialog )
			throws DatastoreException
	{
		this.userPreferencesView = preferencesDialog.getUserPreferencesView();

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		final JPanel generatePrefsPanel = new JPanel();

		final String dcLayoutString = "center";
		final String rowLayoutString = "";
		final String colLayoutString = "[][fill,grow,shrink]";
		generatePrefsPanel.setLayout( new MigLayout( dcLayoutString, colLayoutString, rowLayoutString ));

		final JLabel userPatchesLabel = new JLabel("Patches Directory:" );
		generatePrefsPanel.add( userPatchesLabel, "align right");
		userPatchesView = userPreferencesView.getUserPatchesMVCView();
		generatePrefsPanel.add( userPatchesView, "growx, shrink, wrap" );

		final JLabel userSubRacksLabel = new JLabel("Sub Racks Directory:" );
		generatePrefsPanel.add( userSubRacksLabel, "align right");
		userSubRacksView = userPreferencesView.getUserSubRacksMVCView();
		generatePrefsPanel.add( userSubRacksView, "growx, shrink, wrap" );

		final JLabel userMusicDirLabel = new JLabel("Music Directory:" );
		generatePrefsPanel.add( userMusicDirLabel, "align right" );
		userMusicDirView = userPreferencesView.getUserMusicDirMVCView();
		generatePrefsPanel.add( userMusicDirView, "growx, shrink, wrap" );

		this.add( generatePrefsPanel, "grow, shrink" );

		this.validate();
	}

}
