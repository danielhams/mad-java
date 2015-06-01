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

import java.awt.Component;
import java.awt.Dialog;

import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialogCallback;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public interface GuiService
{
	UserPreferencesMVCView getUserPreferencesMVCView( UserPreferencesMVCController userPrefsMVCController ) throws DatastoreException;

	RackModelRenderingComponent createGuiForRackDataModel( RackDataModel rackDataModel ) throws DatastoreException;

	MadDefinitionListModel getMadDefinitionsModel() throws DatastoreException;

	/**
	 * <p>This method allows the enclosing application to register the tabbed pane
	 * container that the application may use to add tabs to.</p>
	 * <p>This is intended to allow compound container components - such as the sub rack
	 * that have an internal "rack" or page concept - to ask for and display additional tabs.</p>
	 * @param rackTabbedPane the enclosing application tab container
	 */
	void registerRackTabbedPane( GuiTabbedPane rackTabbedPane );

	void addContainerTab( ContainerTab subrackTab, boolean isClosesable );

	void removeContainerTab( ContainerTab subrackTab );

	/**
	 * <p>Helper method for displaying a yes/no type question dialog
	 * to the user.</p>
	 *
	 * @param parentComponent the enclosing component (used for centering)
	 * @param message the message to display
	 * @param title the title for the dialog
	 * @param messageType the message type (see {@link Dialog})
	 * @param options an array of strings that are the text displayed on each option
	 * @param defaultChoice which should be the default (on return press, for example)
	 * @param callback a callback procedure that will receive the results of the dialog
	 */
	void showYesNoQuestionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback );

	/**
	 * <p>Helper method for displaying a text input dialog
	 * to the user.</p>
	 *
	 * @param parentComponent the enclosing component (used for centering)
	 * @param message the message to display
	 * @param title the title for the dialog
	 * @param messageType the message type
	 * @param initialValue initial value to show (if required)
	 * @param callback a callback procedure that will receive the results of the dialog
	 */
	void showTextInputDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String initialValue,
			TextInputDialogCallback callback );

	/**
	 * <p>Helper method for displaying a message dialog
	 * to the user.</p>
	 *
	 * @param parentComponent the enclosing component (used for centering)
	 * @param message the message to display
	 * @param title the title for the dialog
	 * @param messageType the message type
	 * @param callback a callback procedure that will be called when the dialog closes
	 */
	void showMessageDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			MessageDialogCallback callback );

	/**
	 * <p>Helper method for displaying a directory selection dialog
	 * to the user.</p>
	 *
	 * @param parentComponent the enclosing component (user for centering)
	 * @param message the message for the dialog
	 * @param title the title for the dialog
	 * @param messageType the message type
	 * @param callback a callback called with the directory result or null when the dialog closes
	 */
	void showDirectorySelectionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			DirectorySelectionDialogCallback callback );
}
