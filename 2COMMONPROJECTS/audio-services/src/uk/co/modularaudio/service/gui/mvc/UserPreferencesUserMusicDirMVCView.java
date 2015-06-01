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

package uk.co.modularaudio.service.gui.mvc;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.mvc.actions.ChooseDirButtonAction;
import uk.co.modularaudio.service.gui.mvc.actions.ChooseDirButtonAction.DirectoryChoiceReceiver;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.UserMusicDirMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.models.UserMusicDirMVCModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class UserPreferencesUserMusicDirMVCView extends JPanel
{
	private static final long serialVersionUID = 6835400560615280405L;

	private UserMusicDirMVCModel model;
	private final UserMusicDirMVCController controller;

	private final JTextField musicDirTextField;
	private final JButton chooseDirButton;

	public UserPreferencesUserMusicDirMVCView( final GuiService guiService,
			final UserMusicDirMVCModel userMusicDirModel,
			final UserMusicDirMVCController userMusicDirController )
	{
		this.controller = userMusicDirController;
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addColumnConstraint( "[grow,fill][]" );

		setLayout( msh.createMigLayout() );

		musicDirTextField = new JTextField( userMusicDirModel.getValue() );
		musicDirTextField.setEditable( false );

		add( musicDirTextField, "" );

		chooseDirButton = new JButton("Choose Dir");
		chooseDirButton.setAction( new ChooseDirButtonAction( guiService,
				new DirectoryChoiceReceiver()
		{

			@Override
			public void receiveDirectoryChoice( final String fullDirectoryPath )
			{
				controller.setValue( this, fullDirectoryPath );
			}
		} ) );

		add( chooseDirButton, "" );

		setModel( userMusicDirModel );
	}

	public void setModel( final UserMusicDirMVCModel userMusicDirModel )
	{
		model = userMusicDirModel;
		model.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				musicDirTextField.setText( model.getValue() );
			}
		} );
	}

}
