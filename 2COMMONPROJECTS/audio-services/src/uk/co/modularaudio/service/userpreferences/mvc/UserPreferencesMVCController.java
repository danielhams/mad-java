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

import uk.co.modularaudio.service.userpreferences.mvc.controllers.BufferSizeSliderMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.GuiFpsComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputMidiDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputMidiDeviceComboMVCController;

public class UserPreferencesMVCController
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCController.class.getName() );

	private UserPreferencesMVCModel model;

	private final GuiFpsComboMVCController fpsComboController;

	private final InputDeviceComboMVCController inputDeviceComboController;
	private final OutputDeviceComboMVCController outputDeviceComboController;
	private final BufferSizeSliderMVCController bufferSizeSliderController;
	private final InputMidiDeviceComboMVCController inputMidiDeviceComboController;
	private final OutputMidiDeviceComboMVCController outputMidiDeviceComboController;

	public UserPreferencesMVCController( final UserPreferencesMVCModel model )
	{
		this.model = model;

		fpsComboController = new GuiFpsComboMVCController( model.getFpsComboModel(), this );

		inputDeviceComboController = new InputDeviceComboMVCController( model.getInputDeviceComboModel(), this );
		outputDeviceComboController = new OutputDeviceComboMVCController( model.getOutputDeviceComboModel(), this );
		bufferSizeSliderController = new BufferSizeSliderMVCController( model.getBufferSizeModel(), this );
		inputMidiDeviceComboController = new InputMidiDeviceComboMVCController( model.getInputMidiDeviceComboModel(), this );
		outputMidiDeviceComboController = new OutputMidiDeviceComboMVCController( model.getOutputMidiDeviceComboModel(), this );
	}

	public UserPreferencesMVCModel getModel()
	{
		return model;
	}

	public GuiFpsComboMVCController getFpsComboController()
	{
		return fpsComboController;
	}

	public InputDeviceComboMVCController getInputDeviceComboController()
	{
		return inputDeviceComboController;
	}

	public OutputDeviceComboMVCController getOutputDeviceComboController()
	{
		return outputDeviceComboController;
	}

	public BufferSizeSliderMVCController getBufferSizeSliderController()
	{
		return bufferSizeSliderController;
	}

	public InputMidiDeviceComboMVCController getInputMidiDeviceComboController()
	{
		return inputMidiDeviceComboController;
	}

	public OutputMidiDeviceComboMVCController getOutputMidiDeviceComboController()
	{
		return outputMidiDeviceComboController;
	}

	public void setModel( final UserPreferencesMVCModel newModel )
	{
		this.model = newModel;

		fpsComboController.setModel( model.getFpsComboModel() );

		inputDeviceComboController.setModel( model.getInputDeviceComboModel() );
		outputDeviceComboController.setModel( model.getOutputDeviceComboModel() );
		bufferSizeSliderController.setModel( model.getBufferSizeModel() );
		inputMidiDeviceComboController.setModel( model.getInputMidiDeviceComboModel() );
		outputMidiDeviceComboController.setModel( model.getOutputMidiDeviceComboModel() );
	}

}
