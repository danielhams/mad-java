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
import java.io.FileNotFoundException;
import java.io.IOException;

import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public interface ComponentDesignerFrontController
{
	// Receive a display tick from the gui thread (Swing timer for now)
	void receiveDisplayTick();
	
	// Gui components to display and drag and drop components in the rack	
	RackModelRenderingComponent getGuiRack();

	// Rendering of the graph
	boolean isRendering();
	void toggleRendering();
	void addRenderingStateListener( RenderingStateListener renderingStateListener );
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

	void showYesNoQuestionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback );

	void showTextInputDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String initialValue,
			TextInputDialogCallback callback );

	void showMessageDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			MessageDialogCallback callback );

}
