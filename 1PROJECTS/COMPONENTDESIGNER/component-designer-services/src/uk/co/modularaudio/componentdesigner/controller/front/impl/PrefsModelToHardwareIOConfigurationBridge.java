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

package uk.co.modularaudio.componentdesigner.controller.front.impl;

import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemDeviceComboItem;
import uk.co.modularaudio.service.userpreferences.mvc.comboitems.AudioSystemMidiDeviceComboItem;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.BufferSizeSliderMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemBufferSizeMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemMidiDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.GuiFpsMVCModel;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PrefsModelToHardwareIOConfigurationBridge
{
	private PrefsModelToHardwareIOConfigurationBridge()
	{
	}
	
	public static HardwareIOConfiguration modelToConfiguration( UserPreferencesMVCModel audioPrefsModel ) throws DatastoreException
	{
		GuiFpsMVCModel fpsModel = audioPrefsModel.getFpsComboModel();

		AudioSystemDeviceMVCModel consumerDeviceComboModel = audioPrefsModel.getOutputDeviceComboModel();
		int outputChannelsIndex = consumerDeviceComboModel.getSelectedItemIndex();
		AudioSystemDeviceComboItem oc = ( outputChannelsIndex != -1 ? consumerDeviceComboModel.getElementAt(  outputChannelsIndex ) : null );
		boolean outputChanSelected = ( oc != null ? oc.getValue() != null : false );
		
		AudioSystemDeviceMVCModel producerDeviceComboModel = audioPrefsModel.getInputDeviceComboModel();
		int inputChannelsIndex = producerDeviceComboModel.getSelectedItemIndex();
		AudioSystemDeviceComboItem ic = (inputChannelsIndex != -1 ? producerDeviceComboModel.getElementAt( inputChannelsIndex ) : null );
		boolean inputChanSelected = ( ic != null ? ic.getValue() != null : false );
		
		AudioSystemBufferSizeMVCModel bufferSizeModel = audioPrefsModel.getBufferSizeModel();
	
		AudioSystemMidiDeviceMVCModel consumerMidiDeviceComboModel = audioPrefsModel.getOutputMidiDeviceComboModel();
		int cmdi = consumerMidiDeviceComboModel.getSelectedItemIndex();
		AudioSystemMidiDeviceComboItem cmdci = (cmdi != -1 ? consumerMidiDeviceComboModel.getElementAt( cmdi ) : null );
		boolean consumerMidiSelected = ( cmdci != null ? cmdci.getValue() != null : false );
		
		AudioSystemMidiDeviceMVCModel producerMidiDeviceComboModel = audioPrefsModel.getInputMidiDeviceComboModel();
		int pmdi = producerMidiDeviceComboModel.getSelectedItemIndex();
		AudioSystemMidiDeviceComboItem pmdci = (pmdi != -1 ? producerMidiDeviceComboModel.getElementAt( pmdi ) : null );
		boolean producerMidiSelected = ( pmdci != null ? pmdci.getValue() != null : false );
		
		if( outputChanSelected || inputChanSelected )
		{
			AudioSystemDeviceComboItem consumerDevice = null;
			if( outputChanSelected )
			{
				consumerDevice = consumerDeviceComboModel.getElementAt( outputChannelsIndex );
			}
			AudioSystemDeviceComboItem producerDevice = null;
			if( inputChanSelected )
			{
				producerDevice = producerDeviceComboModel.getElementAt( inputChannelsIndex );
			}
			
			AudioSystemMidiDeviceComboItem consumerMidiDevice = null;
			if( consumerMidiSelected )
			{
				consumerMidiDevice = cmdci;
			}
			AudioSystemMidiDeviceComboItem producerMidiDevice = null;
			if( producerMidiSelected )
			{
				producerMidiDevice = pmdci;
			}
			
			// The audio provider can potentionally takes an array of device channel configurations, so we'll build one elements arrays
			// here
			int fps = fpsModel.getFpsValue();
			AudioHardwareDevice consumerChannelConfig = null;
			AudioHardwareDevice producerChannelConfig = null;
			int bufferSizeFrames = BufferSizeSliderMVCController.modelIndexToBufferSizeMap.get( bufferSizeModel.getBufferSizeFramesValue() );
			MidiHardwareDevice consumerMidiConfig = null;
			MidiHardwareDevice producerMidiConfig = null;
			
			if( consumerDevice != null )
			{
				consumerChannelConfig = consumerDevice.getValue();
			}

			if( producerDevice != null )
			{
				producerChannelConfig = producerDevice.getValue();
			}
			
			if( consumerMidiDevice != null )
			{
				consumerMidiConfig = consumerMidiDevice.getValue();
			}

			if( producerMidiDevice != null )
			{
				producerMidiConfig = producerMidiDevice.getValue();
			}
			
			HardwareIOConfiguration retVal = new HardwareIOConfiguration( fps,
					consumerChannelConfig,
					producerChannelConfig,
					bufferSizeFrames,
					consumerMidiConfig,
					producerMidiConfig );
			return retVal;
		}
		else
		{
			throw new DatastoreException( "One input or output audio channel must be selected" );
		}
	}
}
