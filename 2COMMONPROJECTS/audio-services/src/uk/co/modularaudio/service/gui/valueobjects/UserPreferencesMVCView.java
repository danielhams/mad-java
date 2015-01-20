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

package uk.co.modularaudio.service.gui.valueobjects;

import uk.co.modularaudio.service.gui.valueobjects.mvc.AudioDeviceViewListCellRenderer;
import uk.co.modularaudio.service.gui.valueobjects.mvc.GuiFpsViewListCellRenderer;
import uk.co.modularaudio.service.gui.valueobjects.mvc.MidiDeviceViewListCellRenderer;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesBufferSizeMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesGuiFpsMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesInputDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesInputMidiDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesOutputDeviceMVCView;
import uk.co.modularaudio.service.gui.valueobjects.mvc.UserPreferencesOutputMidiDeviceMVCView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;


public class UserPreferencesMVCView
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCView.class.getName() );
	private UserPreferencesGuiFpsMVCView guiFpsMVCView = null;
	private UserPreferencesInputDeviceMVCView inputDeviceMVCView = null;
	private UserPreferencesOutputDeviceMVCView outputDeviceMVCView = null;
	private UserPreferencesBufferSizeMVCView bufferSizeMVCView = null;
	private UserPreferencesInputMidiDeviceMVCView inputMidiDeviceMVCView = null;
	private UserPreferencesOutputMidiDeviceMVCView outputMidiDeviceMVCView = null;
	
	public UserPreferencesMVCView( UserPreferencesMVCController userPrefsMVCController )
	{
		UserPreferencesMVCModel userPrefsModel = userPrefsMVCController.getModel();
		
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

	public void setModel( UserPreferencesMVCModel model )
	{
		guiFpsMVCView.setModel( model.getFpsComboModel() );
		inputDeviceMVCView.setModel( model.getInputDeviceComboModel() );
		outputDeviceMVCView.setModel( model.getOutputDeviceComboModel() );
		bufferSizeMVCView.setModel( model.getBufferSizeModel() );
		inputMidiDeviceMVCView.setModel( model.getInputMidiDeviceComboModel() );
		outputMidiDeviceMVCView.setModel( model.getOutputMidiDeviceComboModel() );
		
	}
}
