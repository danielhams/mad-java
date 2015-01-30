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

package uk.co.modularaudio.service.userpreferences.mvc;

import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemBufferSizeMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemMidiDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.GuiFpsMVCModel;

public class UserPreferencesMVCModel
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCModel.class.getName() );

	private final GuiFpsMVCModel fpsComboModel;

	private final AudioSystemDeviceMVCModel inputDeviceComboModel;
	private final AudioSystemDeviceMVCModel outputDeviceComboModel;
	private final AudioSystemBufferSizeMVCModel bufferSizeModel;
	private final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel;
	private final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel;

	public UserPreferencesMVCModel(
			final GuiFpsMVCModel fpsComboModel,
			final AudioSystemDeviceMVCModel inputDeviceComboModel,
			final AudioSystemDeviceMVCModel outputDeviceComboModel,
			final AudioSystemBufferSizeMVCModel bufferSizeModel,
			final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel,
			final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel )
	{
		this.fpsComboModel = fpsComboModel;
		this.inputDeviceComboModel = inputDeviceComboModel;
		this.outputDeviceComboModel = outputDeviceComboModel;
		this.bufferSizeModel = bufferSizeModel;
		this.inputMidiDeviceComboModel = inputMidiDeviceComboModel;
		this.outputMidiDeviceComboModel = outputMidiDeviceComboModel;
	}

	public GuiFpsMVCModel getFpsComboModel() {
		return fpsComboModel;
	}

	public AudioSystemDeviceMVCModel getInputDeviceComboModel()
	{
		return inputDeviceComboModel;
	}

	public AudioSystemDeviceMVCModel getOutputDeviceComboModel()
	{
		return outputDeviceComboModel;
	}

	public AudioSystemBufferSizeMVCModel getBufferSizeModel()
	{
		return bufferSizeModel;
	}

	public AudioSystemMidiDeviceMVCModel getInputMidiDeviceComboModel()
	{
		return inputMidiDeviceComboModel;
	}

	public AudioSystemMidiDeviceMVCModel getOutputMidiDeviceComboModel()
	{
		return outputMidiDeviceComboModel;
	}
}
