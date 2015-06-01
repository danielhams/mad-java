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
import uk.co.modularaudio.service.userpreferences.mvc.controllers.UserPatchesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.models.UserPatchesMVCModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class UserPreferencesUserPatchesMVCView extends JPanel
{
	private static final long serialVersionUID = 6835400560615280405L;

	private UserPatchesMVCModel model;
	private final UserPatchesMVCController controller;

	private final JTextField patchesDirTextField;
	private final JButton chooseDirButton;

	public UserPreferencesUserPatchesMVCView( final GuiService guiService,
			final UserPatchesMVCModel userPatchesModel,
			final UserPatchesMVCController userPatchesController )
	{
		this.controller = userPatchesController;
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addColumnConstraint( "[grow,fill][]" );

		setLayout( msh.createMigLayout() );

		patchesDirTextField = new JTextField( userPatchesModel.getValue() );
		patchesDirTextField.setEditable( false );

		add( patchesDirTextField, "" );

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

		setModel( userPatchesModel );
	}

	public void setModel( final UserPatchesMVCModel userPatchesModel )
	{
		model = userPatchesModel;
		model.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				patchesDirTextField.setText( model.getValue() );
			}
		} );
	}

}
