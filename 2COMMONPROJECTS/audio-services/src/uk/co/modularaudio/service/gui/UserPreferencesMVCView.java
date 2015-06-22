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

package uk.co.modularaudio.service.gui;

import uk.co.modularaudio.service.gui.mvc.AudioDeviceViewListCellRenderer;
import uk.co.modularaudio.service.gui.mvc.GuiFpsViewListCellRenderer;
import uk.co.modularaudio.service.gui.mvc.MidiDeviceViewListCellRenderer;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesGuiFpsMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesRenderingCoresView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserMusicDirMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserPatchesMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesUserSubRacksMVCView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;


public class UserPreferencesMVCView
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCView.class.getName() );
	private final UserPreferencesRenderingCoresView renderingCoresView;
	private final UserPreferencesGuiFpsMVCView guiFpsMVCView;
	private final UserPreferencesInputDeviceMVCView inputDeviceMVCView;
	private final UserPreferencesOutputDeviceMVCView outputDeviceMVCView;
	private final UserPreferencesInputMidiDeviceMVCView inputMidiDeviceMVCView;
	private final UserPreferencesOutputMidiDeviceMVCView outputMidiDeviceMVCView;

	private final UserPreferencesUserPatchesMVCView userPatchesMVCView;
	private final UserPreferencesUserSubRacksMVCView userSubRacksMVCView;
	private final UserPreferencesUserMusicDirMVCView userMusicDirMVCView;

	public UserPreferencesMVCView( final GuiService guiService, final UserPreferencesMVCController userPrefsMVCController )
	{
		final UserPreferencesMVCModel userPrefsModel = userPrefsMVCController.getModel();

		renderingCoresView = new UserPreferencesRenderingCoresView( userPrefsModel.getRenderingCoresModel() );

		guiFpsMVCView = new UserPreferencesGuiFpsMVCView( userPrefsModel.getFpsComboModel(),
				userPrefsMVCController.getFpsComboController(),
				new GuiFpsViewListCellRenderer() );

		inputDeviceMVCView = new UserPreferencesInputDeviceMVCView( userPrefsModel.getInputDeviceComboModel(),
				userPrefsMVCController.getInputDeviceComboController(),
				new AudioDeviceViewListCellRenderer() );
		outputDeviceMVCView = new UserPreferencesOutputDeviceMVCView( userPrefsModel.getOutputDeviceComboModel(),
				userPrefsMVCController.getOutputDeviceComboController(),
				new AudioDeviceViewListCellRenderer() );
		inputMidiDeviceMVCView = new UserPreferencesInputMidiDeviceMVCView( userPrefsModel.getInputMidiDeviceComboModel(),
				userPrefsMVCController.getInputMidiDeviceComboController(),
				new MidiDeviceViewListCellRenderer() );
		outputMidiDeviceMVCView = new UserPreferencesOutputMidiDeviceMVCView( userPrefsModel.getOutputMidiDeviceComboModel(),
				userPrefsMVCController.getOutputMidiDeviceComboController(),
				new MidiDeviceViewListCellRenderer() );

		userPatchesMVCView = new UserPreferencesUserPatchesMVCView( guiService,
				userPrefsModel.getUserPatchesModel(),
				userPrefsMVCController.getUserPatchesController() );
		userSubRacksMVCView = new UserPreferencesUserSubRacksMVCView( guiService,
				userPrefsModel.getUserSubRacksModel(),
				userPrefsMVCController.getUserSubRacksController() );
		userMusicDirMVCView = new UserPreferencesUserMusicDirMVCView( guiService,
				userPrefsModel.getUserMusicDirModel(),
				userPrefsMVCController.getUserMusicDirController() );
	}

	public UserPreferencesRenderingCoresView getRenderingCoresView()
	{
		return renderingCoresView;
	}

	public UserPreferencesGuiFpsMVCView getGuiFpsMVCView()
	{
		return guiFpsMVCView;
	}

	public UserPreferencesInputDeviceMVCView getInputDeviceMVCView()
	{
		return inputDeviceMVCView;
	}

	public UserPreferencesOutputDeviceMVCView getOutputDeviceMVCView()
	{
		return outputDeviceMVCView;
	}

	public UserPreferencesInputMidiDeviceMVCView getInputMidiDeviceMVCView()
	{
		return inputMidiDeviceMVCView;
	}

	public UserPreferencesOutputMidiDeviceMVCView getOutputMidiDeviceMVCView()
	{
		return outputMidiDeviceMVCView;
	}

	public UserPreferencesUserPatchesMVCView getUserPatchesMVCView()
	{
		return userPatchesMVCView;
	}

	public UserPreferencesUserSubRacksMVCView getUserSubRacksMVCView()
	{
		return userSubRacksMVCView;
	}

	public UserPreferencesUserMusicDirMVCView getUserMusicDirMVCView()
	{
		return userMusicDirMVCView;
	}

	public void setModel( final UserPreferencesMVCModel model )
	{
		renderingCoresView.setModel( model.getRenderingCoresModel() );
		guiFpsMVCView.setModel( model.getFpsComboModel() );
		inputDeviceMVCView.setModel( model.getInputDeviceComboModel() );
		outputDeviceMVCView.setModel( model.getOutputDeviceComboModel() );
		inputMidiDeviceMVCView.setModel( model.getInputMidiDeviceComboModel() );
		outputMidiDeviceMVCView.setModel( model.getOutputMidiDeviceComboModel() );

		userPatchesMVCView.setModel( model.getUserPatchesModel() );
		userSubRacksMVCView.setModel( model.getUserSubRacksModel() );
		userMusicDirMVCView.setModel( model.getUserMusicDirModel() );
	}
}
