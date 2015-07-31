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

package uk.co.modularaudio.componentdesigner.mainframe;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.actions.CheckAudioConfigurationAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.DumpGraphAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.DumpProfileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.DumpSampleCacheAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.ExitAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.NewFileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.OpenFileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.PlayStopAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.RevertFileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.SaveAsFileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.SaveFileAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.ShowPreferencesAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.ToggleLoggingAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.WindowAboutAction;
import uk.co.modularaudio.componentdesigner.mainframe.actions.WindowShowProfilingAction;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.componentdesigner.profiling.ProfilingWindow;
import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;
import uk.co.modularaudio.service.configuration.ConfigurationService;

public class MainFrameActions
{
	public static final String MESSAGE_RACK_DIRT_CONFIRM_SAVE = "This rack has not been saved. Do you wish to save it?";

	public static final String TEXT_AUDIO_RECONFIG_WARNING = "The audio devices previously configured are not available. " +
		"Please choose which devices should be used instead.";
	public static final String TEXT_AUDIO_RECONFIG_TITLE = "Audio Reconfiguration";

	static Log log = LogFactory.getLog( MainFrameActions.class.getName() );

	private final ComponentDesignerFrontController fc;
	private final MainFrame mainFrame;
	private final Action dumpGraphAction;
	private final Action dumpProfileAction;
	private final Action dumpSampleCacheAction;
	private final Action toggleLoggingAction;
	private final Action newFileAction;
	private final Action openFileAction;
	private final Action revertFileAction;
	private final SaveFileAction saveFileAction;
	private final SaveAsFileAction saveAsFileAction;
	private final PlayStopAction playStopAction;
	private final ExitAction exitAction;
	private final Action showPreferencesAction;
	private final CheckAudioConfigurationAction checkAudioConfigurationAction;

	private final WindowShowProfilingAction windowShowProfilingAction;
	private final WindowAboutAction windowAboutAction;

	public MainFrameActions( final ExitSignalReceiver exitSignalReceiver,
			final ComponentDesignerFrontController fcin,
			final UserPreferencesController upc,
			final MainFrame mainFrame,
			final PreferencesDialog preferencesDialog,
			final ProfilingWindow profilingWindow,
			final ConfigurationService configurationService )
	{
		this.fc = fcin;
		this.mainFrame = mainFrame;

		log.debug("Constructing main actions.");
		dumpGraphAction = new DumpGraphAction( fc );
		dumpProfileAction = new DumpProfileAction( fc );
		dumpSampleCacheAction = new DumpSampleCacheAction( fc );
		toggleLoggingAction = new ToggleLoggingAction( fc );
		saveAsFileAction = new SaveAsFileAction( fc, upc, mainFrame );
		saveFileAction = new SaveFileAction( fc, saveAsFileAction );
		checkAudioConfigurationAction = new CheckAudioConfigurationAction( fc, preferencesDialog, mainFrame );
		playStopAction = new PlayStopAction( fc, mainFrame, checkAudioConfigurationAction );
		openFileAction = new OpenFileAction( this, fc, upc, mainFrame, saveFileAction, playStopAction );
		revertFileAction = new RevertFileAction( fc );
		newFileAction = new NewFileAction( this, fc, saveFileAction );
		exitAction = new ExitAction( this, fc, saveFileAction );
		exitAction.addExitSignalReceiver( exitSignalReceiver );
		showPreferencesAction = new ShowPreferencesAction( fc, mainFrame, preferencesDialog );

		windowShowProfilingAction = new WindowShowProfilingAction( fc, profilingWindow );
		exitAction.addExitSignalReceiver( windowShowProfilingAction );
		windowAboutAction = new WindowAboutAction();
	}

	public int rackNotDirtyOrUserConfirmed()
	{
		int retVal = JOptionPane.CANCEL_OPTION;
		if( fc.isRackDirty() )
		{
			// Show a dialog asking if they want to save the file
			retVal = JOptionPane.showConfirmDialog( mainFrame, MESSAGE_RACK_DIRT_CONFIRM_SAVE );
		}
		else
		{
			retVal = JOptionPane.NO_OPTION;
		}
		return retVal;
	}

	public Action getToggleLoggingAction()
	{
		return toggleLoggingAction;
	}

	public Action getDumpGraphAction()
	{
		return dumpGraphAction;
	}

	public Action getDumpProfileAction()
	{
		return dumpProfileAction;
	}

	public Action getDumpSampleCacheAction()
	{
		return dumpSampleCacheAction;
	}

	public Action getNewFileAction()
	{
		return newFileAction;
	}

	public Action getOpenFileAction()
	{
		return openFileAction;
	}

	public Action getRevertFileAction()
	{
		return revertFileAction;
	}

	public Action getSaveFileAction()
	{
		return saveFileAction;
	}

	public Action getSaveAsFileAction()
	{
		return saveAsFileAction;
	}

	public Action getPlayStopAction()
	{
		return playStopAction;
	}

	public Action getExitAction()
	{
		return exitAction;
	}

	public Action getShowPreferencesAction()
	{
		return showPreferencesAction;
	}

	public Action getCheckAudioConfigurationAction()
	{
		return checkAudioConfigurationAction;
	}

	public Action getWindowShowProfilingAction()
	{
		return windowShowProfilingAction;
	}

	public Action getWindowAboutAction()
	{
		return windowAboutAction;
	}
}
