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
import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
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
	 * <p>Receive a display tick from the gui thread (however that is done
	 * - probably a swing timer to begin with).</p>
	 */
	void receiveDisplayTick();

	// Gui components to display and drag and drop components in the rack
	/**
	 * <p>Obtain the main swing component used to present the component
	 * designer rack including scroll bars.</p>
	 *
	 * @return The swing component that displays the rack
	 */
	RackModelRenderingComponent getGuiRack();
	/**
	 * <p>This method allows the enclosing application to register the tabbed pane
	 * container that the application may use to add tabs to.</p>
	 * <p>This is intended to allow compound container components - such as the sub rack
	 * that have an internal "rack" or page concept - to ask for and display additional tabs.</p>
	 * @param rackTabbedPane the enclosing application tab container
	 */
	void registerRackTabbedPane( GuiTabbedPane rackTabbedPane );

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
	/**
	 * <p>Dumps the contents of the rack to the console.</p>
	 * <p>In addition, a dump of the underlying MadGraphInstance and the
	 * resulting RenderingPlan are done.</p>
	 *
	 * @throws DatastoreException on internal error
	 */
	void dumpRack() throws DatastoreException;

	/**
	 * <p>Dumps the list of clients registered with the sample caching
	 * service and their related file positions is shown.</p>
	 *
	 * @throws DatastoreException on internal error
	 */
	void dumpSampleCache() throws DatastoreException;

	/**
	 * <p>Asks for and then dumps the most recently available profiling
	 * information filled in by the rendering thread for its currently
	 * executing rendering plan.</p>
	 * <p>Note: This is only the last filled in profiling data. To get
	 * current data it may be necessary to ask twice, thus clearing
	 * out old profiling data.</p>
	 * @throws DatastoreException on internal error
	 */
	void dumpProfileResults() throws DatastoreException;

	RenderingPlanProfileResults getProfileResults() throws DatastoreException;

	/**
	 * <p>Turn on/off the console output and debugging.</p>
	 */
	void toggleLogging();

	/**
	 * <p>Debugging method to get at the current rack data model.</p>
	 * <p>Should not be used by the ComponentDesigner, but is useful
	 * in testing code.</p>
	 */
	RackDataModel getUserRack();

	// Rack related maintenance methods
	/**
	 * <p>See if the current rack has been modified and needs to be saved.</p>
	 * <p>This method does not perform the save - that is left to the caller.</p>
	 * @return true if the rack (or some sub-rack inside) is considered changed
	 */
	boolean isRackDirty();
	/**
	 * <p>Set the application rack to a new empty rack.</p>
	 * <p>Empty in the sense of no user components - the rack
	 * still contains the rackmasterio of course.</p>
	 * @throws DatastoreException on unrecoverable error
	 */
	void newRack() throws DatastoreException;
	/**
	 * <p>Get the "title" or name of the current rack</p>
	 * <p>Used by file save dialog to set the default name.</p>
	 * @return string name of the rack
	 */
	String getRackDataModelName();
	/**
	 * <p>Load a rack from a file on the filesystem.</p>
	 * <p>The main rendering component for the rack will be
	 * updated appropriately to show any components contained in
	 * this new rack on successful load.</p>
	 * @param filename filesystem path of the rack to load
	 * @throws DatastoreException on unrecoverable error during load
	 * @throws IOException on filesystem/IO error during load
	 */
	void loadRackFromFile( String filename ) throws DatastoreException, IOException;

	/**
	 * <p>Reverts back to the version of the current stored on the filesystem.</p>
	 * @throws DatastoreException on unrecoverable error during revert
	 * @throws IOException on filesystem/IO error during revert
	 */
	void revertRack() throws DatastoreException, IOException;
	/**
	 * <p>Persists the current rack onto the filesystem as an XML file with the
	 * filesystem filename already known.</p>
	 * @throws FileNotFoundException when no current filename is set for the current rack
	 * @throws DatastoreException on unrecoverable error during save
	 * @throws IOException on filesystem/IO error during save
	 */
	void saveRack() throws DatastoreException, FileNotFoundException, IOException;

	/**
	 * <p>Persists the current rack onto the filesystem as an XML file with the
	 * supplied filesystem filename and rack name.</p>
	 * <p>The name the main rack is given by this function is the name that
	 * is displayed when a rack is loaded as a "sub rack".</p>
	 * @param filename the filesystem path where the contents will be saved
	 * @param rackName the name to give the main rack
	 * @throws DatastoreException on unrecoverable error during save
	 * @throws IOException on filesystem/IO error during save
	 */
	void saveRackToFile( String filename, String rackName ) throws DatastoreException, IOException;

	// Audio IO configuration
	/**
	 * <p>Will start the necessary underlying AppRenderingIO object for the current user preferences
	 * and begin output/input of audio and any configured MIDI.</p>
	 * <p>Note: This will only begin input/output of an empty rendering graph - to render using
	 * the components currently in the rack - toggleRendering() should be called after
	 * the audio engine is running.</p>
	 * @return true when audio engine successfully started
	 */
	boolean startAudioEngine();
	/**
	 * <p>Check if the system is already up and running with audio/MIDI IO.</p>
	 * @return current state of the audio engine IO
	 */
	boolean isAudioEngineRunning();
	/**
	 * <p>Stop any audio/MIDI IO.</p>
	 * <p>Note: like startAudioEngine(), this method concerns the underlying IO mechanism
	 * and will only halt that IO. It is good practice to stop any rendering using toggleRendering()
	 * before this method is called.</p>
	 */
	void stopAudioEngine();
	/**
	 * <p>Simple helper method that the component designer can call before shutdown to ensure
	 * appropriate checks are done before the application exits.</p>
	 * @throws DatastoreException on unrecoverable error
	 * @throws MadProcessingException on errors within the MAD processing such as Jack failures
	 */
	void ensureRenderingStoppedBeforeExit() throws DatastoreException, MadProcessingException;

	// User Preferences
	/**
	 * <p>Obtain the Swing component responsible for rendering the available user
	 * preferences.</p>
	 * <p>It is intended that the calling application present the view where required
	 * and call applyUserPreferencesChanges or cancelUserPreferencesChanges on completion.</p>
	 * @return a JComponent that can be displayed to alter the user preferences
	 * @throws DatastoreException on unrecoverable runtime error
	 */
	UserPreferencesMVCView getUserPreferencesMVCView() throws DatastoreException;
	/**
	 * <p>Reload (and overwrite current) user preferences from the filesystem.</p>
	 */
	void reloadUserPreferences();
	/**
	 * <p>After updates to the {@link UserPreferencesMVCView} are made, calling this
	 * method will halt any current audio IO and attempt to sanity check the
	 * chosen user preferences with a test.</p>
	 * @return success if no overruns/underruns detected during the test run
	 */
	boolean testUserPreferencesChanges();
	/**
	 * <p>Persist the updated user preferences to disk and switch the current audio
	 * system over to the chosen preferences.</p>
	 */
	void applyUserPreferencesChanges();

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
	 * @param callback a callback procedure that will do called when the dialog closes
	 */
	void showMessageDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			MessageDialogCallback callback );

}
