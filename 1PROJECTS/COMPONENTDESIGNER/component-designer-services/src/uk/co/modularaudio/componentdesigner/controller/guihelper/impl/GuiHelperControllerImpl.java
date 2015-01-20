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

package uk.co.modularaudio.componentdesigner.controller.guihelper.impl;

import java.awt.Component;

import uk.co.modularaudio.componentdesigner.controller.guihelper.GuiHelperController;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class GuiHelperControllerImpl implements ComponentWithLifecycle, GuiHelperController
{
	private GuiService guiService = null;
	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	public GuiService getGuiService()
	{
		return guiService;
	}

	public void setGuiService(GuiService guiService)
	{
		this.guiService = guiService;
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView( UserPreferencesMVCController userPrefsMVCController )
			throws DatastoreException
	{
		return guiService.getUserPreferencesMVCView( userPrefsMVCController );
	}

	@Override
	public MadDefinitionListModel getComponentTypesModel()
			throws DatastoreException
	{
		return guiService.getComponentTypesModel();
	}

	@Override
	public RackModelRenderingComponent createGuiForRackDataModel(
			RackDataModel rackDataModel ) throws DatastoreException
	{
		return guiService.createGuiForRackDataModel( rackDataModel );
	}

	@Override
	public void registerRackTabbedPane( GuiTabbedPane rackTabbedPane )
	{
		guiService.registerRackTabbedPane( rackTabbedPane );
	}

	@Override
	public void showYesNoQuestionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback )
	{
		guiService.showYesNoQuestionDialog( parentComponent, message, title, messageType,
				options, defaultChoice, callback );
	}

	@Override
	public void showTextInputDialog( Component parentComponent, String message,
			String title, int messageType, String initialValue,
			TextInputDialogCallback callback )
	{
		guiService.showTextInputDialog( parentComponent, message, title,
				messageType, initialValue, callback );
	}

	@Override
	public void showMessageDialog( Component parentComponent, String message,
			String title,
			int messageType,
			MessageDialogCallback callback )
	{
		guiService.showMessageDialog( parentComponent, message, title, messageType, callback );
	}

}
