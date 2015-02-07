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

package uk.co.modularaudio.componentdesigner.controller.gui.impl;

import java.awt.Component;

import uk.co.modularaudio.componentdesigner.controller.gui.GuiController;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

/**
 * @author dan
 *
 */
public class GuiControllerImpl implements ComponentWithLifecycle, GuiController
{
	private GuiService guiService;

	@Override
	public void destroy()
	{
		// Nothing to do
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		// Nothing to do
	}

	public void setGuiService(final GuiService guiService)
	{
		this.guiService = guiService;
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView( final UserPreferencesMVCController userPrefsMVCController )
			throws DatastoreException
	{
		return guiService.getUserPreferencesMVCView( userPrefsMVCController );
	}

	@Override
	public RackModelRenderingComponent createGuiForRackDataModel(
			final RackDataModel rackDataModel ) throws DatastoreException
	{
		return guiService.createGuiForRackDataModel( rackDataModel );
	}

	@Override
	public void registerRackTabbedPane( final GuiTabbedPane rackTabbedPane )
	{
		guiService.registerRackTabbedPane( rackTabbedPane );
	}

	@Override
	public void showYesNoQuestionDialog( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String[] options,
			final String defaultChoice,
			final YesNoQuestionDialogCallback callback )
	{
		guiService.showYesNoQuestionDialog( parentComponent, message, title, messageType,
				options, defaultChoice, callback );
	}

	@Override
	public void showTextInputDialog( final Component parentComponent, final String message,
			final String title, final int messageType, final String initialValue,
			final TextInputDialogCallback callback )
	{
		guiService.showTextInputDialog( parentComponent, message, title,
				messageType, initialValue, callback );
	}

	@Override
	public void showMessageDialog( final Component parentComponent, final String message,
			final String title,
			final int messageType,
			final MessageDialogCallback callback )
	{
		guiService.showMessageDialog( parentComponent, message, title, messageType, callback );
	}

}
