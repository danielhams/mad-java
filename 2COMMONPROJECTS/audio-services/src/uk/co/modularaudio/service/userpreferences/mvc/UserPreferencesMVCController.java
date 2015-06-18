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

import uk.co.modularaudio.service.userpreferences.mvc.controllers.GuiFpsComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.InputMidiDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.OutputMidiDeviceComboMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.RenderingCoresMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.UserMusicDirMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.UserPatchesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.controllers.UserSubRacksMVCController;

public class UserPreferencesMVCController
{
//	private static Log log = LogFactory.getLog( UserPreferencesMVCController.class.getName() );

	private UserPreferencesMVCModel model;

	private final RenderingCoresMVCController renderingCoresController;

	private final GuiFpsComboMVCController fpsComboController;

	private final InputDeviceComboMVCController inputDeviceComboController;
	private final OutputDeviceComboMVCController outputDeviceComboController;
	private final InputMidiDeviceComboMVCController inputMidiDeviceComboController;
	private final OutputMidiDeviceComboMVCController outputMidiDeviceComboController;

	private final UserPatchesMVCController userPatchesController;
	private final UserSubRacksMVCController userSubRacksController;
	private final UserMusicDirMVCController userMusicDirController;

	public UserPreferencesMVCController( final UserPreferencesMVCModel model )
	{
		this.model = model;

		renderingCoresController = new RenderingCoresMVCController( model.getRenderingCoresModel(), this );

		fpsComboController = new GuiFpsComboMVCController( model.getFpsComboModel(), this );

		inputDeviceComboController = new InputDeviceComboMVCController( model.getInputDeviceComboModel(), this );
		outputDeviceComboController = new OutputDeviceComboMVCController( model.getOutputDeviceComboModel(), this );
		inputMidiDeviceComboController = new InputMidiDeviceComboMVCController( model.getInputMidiDeviceComboModel(), this );
		outputMidiDeviceComboController = new OutputMidiDeviceComboMVCController( model.getOutputMidiDeviceComboModel(), this );

		userPatchesController = new UserPatchesMVCController( model.getUserPatchesModel() );
		userSubRacksController = new UserSubRacksMVCController( model.getUserSubRacksModel() );
		userMusicDirController = new UserMusicDirMVCController( model.getUserMusicDirModel() );
	}

	public UserPreferencesMVCModel getModel()
	{
		return model;
	}

	public RenderingCoresMVCController getRenderingCoresController()
	{
		return renderingCoresController;
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

		renderingCoresController.setModel( model.getRenderingCoresModel() );

		fpsComboController.setModel( model.getFpsComboModel() );

		inputDeviceComboController.setModel( model.getInputDeviceComboModel() );
		outputDeviceComboController.setModel( model.getOutputDeviceComboModel() );
		inputMidiDeviceComboController.setModel( model.getInputMidiDeviceComboModel() );
		outputMidiDeviceComboController.setModel( model.getOutputMidiDeviceComboModel() );

		userPatchesController.setModel( model.getUserPatchesModel() );
		userSubRacksController.setModel( model.getUserSubRacksModel() );
		userMusicDirController.setModel( model.getUserMusicDirModel() );
	}

	public UserPatchesMVCController getUserPatchesController()
	{
		return userPatchesController;
	}

	public UserSubRacksMVCController getUserSubRacksController()
	{
		return userSubRacksController;
	}

	public UserMusicDirMVCController getUserMusicDirController()
	{
		return userMusicDirController;
	}

}
