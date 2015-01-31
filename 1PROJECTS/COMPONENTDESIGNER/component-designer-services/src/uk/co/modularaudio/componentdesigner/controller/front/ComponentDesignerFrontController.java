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

package uk.co.modularaudio.componentdesigner.controller.front;

import java.awt.Component;
import java.awt.Dialog;
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

/**
 * <p>The contract of the front controller that exposes functionality to the GUI
 * elements in the Component Designer application.</p>
 *
 * <p>Any implementing class will be required to maintain a number of pieces of
 * state to maintain the application graph, rendering chain etc.</p>
 *
 * @author dan
 *
 */
public interface ComponentDesignerFrontController
{
	/**
	 * Receive a display tick from the gui thread (however that is done
	 * - probably swing timer to begin with).
s	 */
	void receiveDisplayTick();

	// Gui components to display and drag and drop components in the rack
	/**
	 * Obtain the main swing component used to present the component
	 * designer rack including scroll bars
	 *
	 * @return The swing component that displays the rack
	 */
	RackModelRenderingComponent getGuiRack();

	// Rendering of the graph
	/**
	 * <p>Whether there is currently audio rendering happening.</p>
	 * <p>"Rendering" is both audio output using the current rack
	 * graph and timed execution of the GUI thread callbacks to
	 * active components.</p>
	 * @return true if audio and GUI presentation is running
	 */
	boolean isRendering();

	/**
	 * Toggle the state of rendering.
	 */
	void toggleRendering();

	/**
	 * <p>Allows callers to register a listener for changes in the state of rendering.</p>
	 * <p>In particular, this allows any "stop/start" button to be notified should
	 * rendering halt for some reason (such as overruns).</p>
	 * @param renderingStateListener the listener to add
	 */
	void addRenderingStateListener( RenderingStateListener renderingStateListener );
	/**
	 * @param renderingStateListener the listener to remove
	 */
	void removeRenderingStateListener( RenderingStateListener renderingStateListener );

	// Debugging methods
	void dumpRack() throws DatastoreException;
	void dumpProfileResults() throws DatastoreException;
	void toggleLogging();

	boolean isRackDirty(); // See if the current rack has been modified and needs to be saved.
	void newRack() throws DatastoreException;
	String getRackDataModelName(); // Used by file save dialog to set the default name
	void loadRackFromFile( String filename ) throws DatastoreException, IOException;
	void revertRack() throws DatastoreException, IOException;
	void saveRack() throws DatastoreException, FileNotFoundException, IOException;
	void saveRackToFile( String filename, String rackName ) throws DatastoreException, IOException;
	void ensureRenderingStoppedBeforeExit() throws DatastoreException, MadProcessingException;

	// Debugging method to get at the current rack data model
	RackDataModel getUserRack();

	// Audio IO configuration
	boolean startAudioEngine();
	boolean isAudioEngineRunning();
	void stopAudioEngine();

	// User Preferences
	UserPreferencesMVCView getUserPreferencesMVCView() throws DatastoreException;
	void reloadUserPreferences();
	boolean testUserPreferencesChanges();
	void applyUserPreferencesChanges();
	void cancelUserPreferencesChanges();

	void registerRackTabbedPane( GuiTabbedPane rackTabbedPane );

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
	 * <p>Helper method for displaying a yes/no type question dialog
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
	 * <p>Helper method for displaying a yes/no type question dialog
	 * to the user.</p>
	 *
	 * @param parentComponent the enclosing component (used for centering)
	 * @param message the message to display
	 * @param title the title for the dialog
	 * @param messageType the message type
	 * @param callback a callback procedure that will do called when the dialog closes
	 */
	void showMessageDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			MessageDialogCallback callback );

}
