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

package uk.co.modularaudio.componentdesigner.controller.gui;

import java.awt.Component;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

/**
 * <p>Entry point for operations related to the GUI.</p>
 * <p>The gui helper controller is a vertical responsibility controller
 * that delegates and/or coordinates work as appropriate from services
 * that implement the required functionality.</p>
 *
 * @author dan
 */
public interface GuiController
{
	/**
	 * <p>Create a view for the supplied controller of a user
	 * preferences model.</p>
	 * @see GuiService#getUserPreferencesMVCView(UserPreferencesMVCController)
	 */
	UserPreferencesMVCView getUserPreferencesMVCView( UserPreferencesMVCController prefsModelController ) throws DatastoreException;

	/**
	 * <p>Create a Swing component that presents the supplied
	 * rack.</p>
	 * @see GuiService#createGuiForRackDataModel(RackDataModel)
	 */
	RackModelRenderingComponent createGuiForRackDataModel( RackDataModel rackDataModel ) throws DatastoreException;

	/**
	 * <p>Register a new tab in the application tab pane.</p>
	 * @see GuiService#registerRackTabbedPane(GuiTabbedPane)
	 */
	void registerRackTabbedPane( GuiTabbedPane rackTabbedPane );

	/**
	 * <p>Show a yes no dialog to the user.</p>
	 * @see GuiService#showYesNoQuestionDialog(Component, String, String, int, String[], String, YesNoQuestionDialogCallback)
	 */
	void showYesNoQuestionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback );

	/**
	 * <p>Show a text input dialog to the user.</p>
	 * @see GuiService#showTextInputDialog(Component, String, String, int, String, TextInputDialogCallback)
	 */
	void showTextInputDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String initialValue,
			TextInputDialogCallback callback );

	/**
	 * <p>Show a message dialog to the user.</p>
	 * @see GuiService#showMessageDialog(Component, String, String, int, MessageDialogCallback)
	 */
	void showMessageDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			MessageDialogCallback callback );
}
