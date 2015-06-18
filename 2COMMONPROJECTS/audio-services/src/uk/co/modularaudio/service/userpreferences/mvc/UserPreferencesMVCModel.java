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

import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.AudioSystemMidiDeviceMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.GuiFpsMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.RenderingCoresMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.UserMusicDirMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.UserPatchesMVCModel;
import uk.co.modularaudio.service.userpreferences.mvc.models.UserSubRacksMVCModel;

public class UserPreferencesMVCModel
{
	//private static Log log = LogFactory.getLog( UserPreferencesMVCModel.class.getName() );

	private final RenderingCoresMVCModel renderingCoresModel;

	private final GuiFpsMVCModel fpsComboModel;

	private final AudioSystemDeviceMVCModel inputDeviceComboModel;
	private final AudioSystemDeviceMVCModel outputDeviceComboModel;
	private final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel;
	private final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel;

	private final UserPatchesMVCModel userPatchesModel;
	private final UserSubRacksMVCModel userSubRacksModel;
	private final UserMusicDirMVCModel userMusicDirModel;

	public UserPreferencesMVCModel( final RenderingCoresMVCModel renderingCoresModel,
			final GuiFpsMVCModel fpsComboModel, final AudioSystemDeviceMVCModel inputDeviceComboModel,
			final AudioSystemDeviceMVCModel outputDeviceComboModel,
			final AudioSystemMidiDeviceMVCModel inputMidiDeviceComboModel,
			final AudioSystemMidiDeviceMVCModel outputMidiDeviceComboModel,
			final UserPatchesMVCModel userPatchesModel,
			final UserSubRacksMVCModel userSubRacksModel,
			final UserMusicDirMVCModel userMusicDirModel )
	{
		this.renderingCoresModel = renderingCoresModel;
		this.fpsComboModel = fpsComboModel;
		this.inputDeviceComboModel = inputDeviceComboModel;
		this.outputDeviceComboModel = outputDeviceComboModel;
		this.inputMidiDeviceComboModel = inputMidiDeviceComboModel;
		this.outputMidiDeviceComboModel = outputMidiDeviceComboModel;

		this.userPatchesModel = userPatchesModel;
		this.userSubRacksModel = userSubRacksModel;
		this.userMusicDirModel = userMusicDirModel;
	}

	public RenderingCoresMVCModel getRenderingCoresModel()
	{
		return renderingCoresModel;
	}

	public GuiFpsMVCModel getFpsComboModel()
	{
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

	public AudioSystemMidiDeviceMVCModel getInputMidiDeviceComboModel()
	{
		return inputMidiDeviceComboModel;
	}

	public AudioSystemMidiDeviceMVCModel getOutputMidiDeviceComboModel()
	{
		return outputMidiDeviceComboModel;
	}

	public UserPatchesMVCModel getUserPatchesModel()
	{
		return userPatchesModel;
	}

	public UserSubRacksMVCModel getUserSubRacksModel()
	{
		return userSubRacksModel;
	}

	public UserMusicDirMVCModel getUserMusicDirModel()
	{
		return userMusicDirModel;
	}
}
