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
import uk.co.modularaudio.service.gui.mvc.UserPreferencesBufferSizeMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesGuiFpsMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesInputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesOutputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.mvc.UserPreferencesRenderingCoresView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;


public class UserPreferencesMVCView
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCView.class.getName() );
	private final UserPreferencesRenderingCoresView renderingCoresView;
	private final UserPreferencesGuiFpsMVCView guiFpsMVCView;
	private final UserPreferencesInputDeviceMVCView inputDeviceMVCView;
	private final UserPreferencesOutputDeviceMVCView outputDeviceMVCView;
	private final UserPreferencesBufferSizeMVCView bufferSizeMVCView;
	private final UserPreferencesInputMidiDeviceMVCView inputMidiDeviceMVCView;
	private final UserPreferencesOutputMidiDeviceMVCView outputMidiDeviceMVCView;

	public UserPreferencesMVCView( final UserPreferencesMVCController userPrefsMVCController )
	{
		final UserPreferencesMVCModel userPrefsModel = userPrefsMVCController.getModel();

		renderingCoresView = new UserPreferencesRenderingCoresView(
				userPrefsModel.getRenderingCoresModel(),
				userPrefsMVCController.getRenderingCoresController() );

		guiFpsMVCView = new UserPreferencesGuiFpsMVCView( userPrefsModel.getFpsComboModel(),
				userPrefsMVCController.getFpsComboController(),
				new GuiFpsViewListCellRenderer() );

		inputDeviceMVCView = new UserPreferencesInputDeviceMVCView( userPrefsModel.getInputDeviceComboModel(),
				userPrefsMVCController.getInputDeviceComboController(),
				new AudioDeviceViewListCellRenderer() );
		outputDeviceMVCView = new UserPreferencesOutputDeviceMVCView( userPrefsModel.getOutputDeviceComboModel(),
				userPrefsMVCController.getOutputDeviceComboController(),
				new AudioDeviceViewListCellRenderer() );
		bufferSizeMVCView = new UserPreferencesBufferSizeMVCView( userPrefsModel.getBufferSizeModel(),
				userPrefsMVCController.getBufferSizeSliderController() );
		inputMidiDeviceMVCView = new UserPreferencesInputMidiDeviceMVCView( userPrefsModel.getInputMidiDeviceComboModel(),
				userPrefsMVCController.getInputMidiDeviceComboController(),
				new MidiDeviceViewListCellRenderer() );
		outputMidiDeviceMVCView = new UserPreferencesOutputMidiDeviceMVCView( userPrefsModel.getOutputMidiDeviceComboModel(),
				userPrefsMVCController.getOutputMidiDeviceComboController(),
				new MidiDeviceViewListCellRenderer() );
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

	public UserPreferencesBufferSizeMVCView getBufferSizeMVCView()
	{
		return bufferSizeMVCView;
	}

	public UserPreferencesInputMidiDeviceMVCView getInputMidiDeviceMVCView()
	{
		return inputMidiDeviceMVCView;
	}

	public UserPreferencesOutputMidiDeviceMVCView getOutputMidiDeviceMVCView()
	{
		return outputMidiDeviceMVCView;
	}

	public void setModel( final UserPreferencesMVCModel model )
	{
		renderingCoresView.setModel( model.getRenderingCoresModel() );
		guiFpsMVCView.setModel( model.getFpsComboModel() );
		inputDeviceMVCView.setModel( model.getInputDeviceComboModel() );
		outputDeviceMVCView.setModel( model.getOutputDeviceComboModel() );
		bufferSizeMVCView.setModel( model.getBufferSizeModel() );
		inputMidiDeviceMVCView.setModel( model.getInputMidiDeviceComboModel() );
		outputMidiDeviceMVCView.setModel( model.getOutputMidiDeviceComboModel() );

	}
}
