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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialogPageEnum;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.util.audio.gui.mad.service.util.filesaveextension.CDFileSaveAccessory;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MainFrameActions
{
	private static final String MESSAGE_RACK_DIRT_CONFIRM_SAVE = "This rack has not been saved. Do you wish to save it?";
	private static final String CONFIG_KEY_DEFAULT_DIR = MainFrameActions.class.getSimpleName() + ".DefaultDirectory";

	private static final String TEXT_AUDIO_RECONFIG_WARNING = "The audio devices previously configured are not available. " +
		"Please choose which devices should be used instead.";
	private static final String TEXT_AUDIO_RECONFIG_TITLE = "Audio Reconfiguration";

	public class DumpGraphAction extends AbstractAction
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -8447845406158954693L;
		private ComponentDesignerFrontController fc = null;

		public DumpGraphAction( ComponentDesignerFrontController fc )
		{
			this.fc = fc;
			this.putValue(NAME, "Dump Rack/Graph To Console");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				fc.dumpRack();
			}
			catch (DatastoreException e1)
			{
				log.error( "Error executing dump graph: " + e1.toString(), e1 );
			}
		}
	}

	public class DumpProfileAction extends AbstractAction
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -3756758345674844578L;
		private ComponentDesignerFrontController fc = null;

		public DumpProfileAction( ComponentDesignerFrontController fc )
		{
			this.fc = fc;
			this.putValue(NAME, "Dump Profile To Console");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				fc.dumpProfileResults();
			}
			catch (DatastoreException e1)
			{
				log.error( "Error executing dump profile results: " + e1.toString(), e1 );
			}
		}
	}


	public class ToggleLoggingAction extends AbstractAction
	{

		/**
		 *
		 */
		private static final long serialVersionUID = 6567077333146253552L;

		private ComponentDesignerFrontController fc = null;

		public ToggleLoggingAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue(NAME, "Enable Logging");
			this.putValue(SELECTED_KEY, "EnableLogging.selected");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				fc.toggleLogging();
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing enable logging action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}

	public class NewFileAction extends AbstractAction
	{
		private static final long serialVersionUID = 4608404122938289459L;

		private ComponentDesignerFrontController fc = null;

		public NewFileAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue(NAME, "New File");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			log.debug("NewFileAction called.");
			int dirtyCheckVal = rackNotDirtyOrUserConfirmed();
			if( dirtyCheckVal == JOptionPane.YES_OPTION )
			{
				// Need to save it - call the save
				saveFileAction.actionPerformed( e );

				// Simulate the cancel in the save action if the rack is still dirty.
				dirtyCheckVal = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
			}

			// We don't check for cancel, as it will just fall through

			if( dirtyCheckVal == JOptionPane.NO_OPTION )
			{
				if( fc.isRendering() )
				{
					fc.toggleRendering();
				}
				try
				{
					fc.newRack();
				}
				catch (Exception ex)
				{
					String msg = "Exception caught performing new file action: " + ex.toString();
					log.error( msg, ex );
				}
			}
		}
	}

	private int rackNotDirtyOrUserConfirmed()
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

	public class OpenFileAction extends AbstractAction
	{
		private static final long serialVersionUID = -8580442441463163408L;

		private ComponentDesignerFrontController fc = null;

		public OpenFileAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue(NAME, "Open File");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			log.debug("OpenFileAction called.");
			try
			{
				int dirtyCheckVal = rackNotDirtyOrUserConfirmed();
				if( dirtyCheckVal == JOptionPane.YES_OPTION )
				{
					// Need to save it - call the save
					saveFileAction.actionPerformed( e );

					// Simulate the cancel in the save action if the rack is still dirty.
					dirtyCheckVal = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
				}

				// We don't check for cancel, as it will just fall through
				if( dirtyCheckVal == JOptionPane.NO_OPTION )
				{
					if( fc.isRendering() )
					{
						playStopAction.actionPerformed(e);
					}
					JFileChooser openFileChooser = new JFileChooser();
					openFileChooser.setCurrentDirectory( new File( defaultDirectory ) );

					int retVal = openFileChooser.showOpenDialog( mainFrame );
					if( retVal == JFileChooser.APPROVE_OPTION )
					{
						File f = openFileChooser.getSelectedFile();
						if( f != null )
						{
							log.debug("Attempting to load from file " + f.getAbsolutePath() );
							fc.loadRackFromFile( f.getAbsolutePath() );
						}
					}
				}
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing open file action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}

	public class RevertFileAction extends AbstractAction
	{
		private static final long serialVersionUID = -4249015082380141979L;

		private ComponentDesignerFrontController fc = null;

		public RevertFileAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue( NAME, "Revert File" );
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			log.debug("RevertFileAction called");

			try
			{
				// Check to see if we already have a filename associated with this rack - if not
				if( fc.isRendering() )
				{
					fc.toggleRendering();
				}
				fc.revertRack();
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing revert action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}

	public class SaveFileAction extends AbstractAction
	{
		private static final long serialVersionUID = -4249015082380141979L;

		private ComponentDesignerFrontController fc = null;

		public SaveFileAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue( NAME, "Save File" );
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			log.debug("SaveFileAction called");

			try
			{
				// Check to see if we already have a filename associated with this rack - if not
				// we pop up a file chooser dialog to set the filename
				boolean fileSaved = false;
				try
				{
					fc.saveRack();
					fileSaved = true;
				}
				catch(FileNotFoundException fnfe)
				{
				}

				if( !fileSaved )
				{
					saveAsFileAction.actionPerformed( e );
				}
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing save action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}



	public class SaveAsFileAction extends AbstractAction
	{
		private static final long serialVersionUID = -4249015082380141979L;

		private ComponentDesignerFrontController fc = null;

		public SaveAsFileAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue( NAME, "Save File As" );
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			log.debug("SaveFileAsAction called");

			try
			{
				JFileChooser saveFileChooser = new JFileChooser();
				String rackDataModelName = fc.getRackDataModelName();
				CDFileSaveAccessory fileSaveAccessory = new CDFileSaveAccessory( rackDataModelName );
				saveFileChooser.setAccessory( fileSaveAccessory );
				saveFileChooser.setCurrentDirectory( new File( defaultDirectory ) );
				int retVal = saveFileChooser.showSaveDialog( mainFrame );
				if( retVal == JFileChooser.APPROVE_OPTION )
				{
					File f = saveFileChooser.getSelectedFile();
					if( f != null )
					{
						String rackName = fileSaveAccessory.getFileName();
						log.debug("Attempting to save to file as " + f.getAbsolutePath() + " with name " + rackName );

						fc.saveRackToFile( f.getAbsolutePath(), rackName );
					}
				}
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing save file as action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}

	public class PlayStopAction extends AbstractAction
	{
		private static final long serialVersionUID = 255449800376156959L;

		private ComponentDesignerFrontController fc = null;

		public PlayStopAction( ComponentDesignerFrontController fcin )
		{
			this.fc = fcin;
			this.putValue(NAME, "Play/Stop");
			this.putValue(SELECTED_KEY, "PlayStop.selected");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				boolean canToggle = false;
				if( !fc.isRendering() )
				{
					if( !fc.isAudioEngineRunning() )
					{
						mainFrame.getToolbar().getPlayStopToggleButton().setSelected( false );
						// Launch the check audio config action
						// and exit.
						checkAudioConfigurationAction.actionPerformed( e );

						if( fc.isAudioEngineRunning() )
						{
							canToggle = true;
						}
					}
					else
					{
						canToggle = true;
					}
				}
				else
				{
					canToggle = true;
				}

				if( canToggle )
				{
					SwingUtilities.invokeLater( new Runnable()
					{

						@Override
						public void run()
						{
							fc.toggleRendering();
						}
					} );
				}
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing play/stop action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}

	public class ExitAction extends AbstractAction
	{
		private static final long serialVersionUID = 1303196363358495273L;

		private ComponentDesignerFrontController fc = null;

		public ExitAction(ComponentDesignerFrontController fc)
		{
			this.fc = fc;
			this.putValue(NAME, "Exit");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( fc.isRendering() )
			{
				fc.toggleRendering();
			}

			log.debug("ExitAction performed called.");
			int optionPaneResult = rackNotDirtyOrUserConfirmed();

			if( optionPaneResult == JOptionPane.YES_OPTION )
			{
				// Need to save it - call the save
				saveFileAction.actionPerformed( e );

				// Simulate the cancel in the save action if the rack is still dirty.
				optionPaneResult = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
			}

			if( optionPaneResult == JOptionPane.NO_OPTION )
			{
				// Stop the engine
				if( fc.isAudioEngineRunning() )
				{
					fc.stopAudioEngine();
				}
				// Give any components in the graph a chance to cleanup first
				try
				{
					fc.ensureRenderingStoppedBeforeExit();
				}
				catch (Exception e1)
				{
					String msg = "Exception caught during destruction before exit: " + e1.toString();
					log.error( msg, e1 );
				}
				log.debug("Will signal exit");
				exitSignalReceiver.signalExit();
			}
		}
	}

	public class ShowPreferencesAction extends AbstractAction
	{
		private static final long serialVersionUID = -5903263092723112562L;

//		private GuiFrontController fc = null;

		public ShowPreferencesAction( ComponentDesignerFrontController fc )
		{
//			this.fc = fc;
			this.putValue( NAME, "Preferences" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			fc.reloadUserPreferences();
			preferencesDialog.setLocationRelativeTo( mainFrame );
			preferencesDialog.setVisible( true );
		}
	}

	public class CheckAudioConfigurationAction extends AbstractAction
	{
		private static final long serialVersionUID = 3850927484100526941L;

		private ComponentDesignerFrontController fc = null;

		public CheckAudioConfigurationAction( ComponentDesignerFrontController fc )
		{
			this.fc = fc;
			this.putValue(NAME,  "Check Audio Configuration" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( !fc.startAudioEngine() )
			{
				preferencesDialog.loadPreferences();
				preferencesDialog.choosePage( PreferencesDialogPageEnum.AUDIO_SYSTEM );
				Runnable r = new Runnable()
				{
					@Override
					public void run()
					{
						preferencesDialog.setLocationRelativeTo( mainFrame );
						preferencesDialog.setVisible( true );
						fc.showMessageDialog( preferencesDialog, TEXT_AUDIO_RECONFIG_WARNING, TEXT_AUDIO_RECONFIG_TITLE, JOptionPane.WARNING_MESSAGE, null );
					}
				};

				SwingUtilities.invokeLater( r );

			}
		}
	}

	private static Log log = LogFactory.getLog( MainFrameActions.class.getName() );

	private ComponentDesignerFrontController fc = null;
	private MainFrame mainFrame = null;
	private PreferencesDialog preferencesDialog = null;
	private Action dumpGraphAction = null;
	private Action dumpProfileAction = null;
	private Action toggleLoggingAction = null;
	private Action newFileAction =  null;
	private Action openFileAction = null;
	private Action revertFileAction = null;
	private Action saveFileAction = null;
	private Action saveAsFileAction = null;
	private Action playStopAction = null;
	private Action exitAction = null;
	private Action showPreferencesAction = null;
	private Action checkAudioConfigurationAction = null;

	// The receiver of the exit signal
	private ExitSignalReceiver exitSignalReceiver = null;

	// A default directory for file options
	private String defaultDirectory = null;

	public MainFrameActions( ExitSignalReceiver exitSignalReceiver,
			ComponentDesignerFrontController fcin,
			MainFrame mainFrame,
			PreferencesDialog preferencesDialog,
			ConfigurationService configurationService )
	{
		this.exitSignalReceiver = exitSignalReceiver;
		this.fc = fcin;
		this.mainFrame = mainFrame;
		this.preferencesDialog = preferencesDialog;
		log.info("Constructing main actions.");
		dumpGraphAction = new DumpGraphAction( fc );
		dumpProfileAction = new DumpProfileAction( fc );
		toggleLoggingAction = new ToggleLoggingAction( fc );
		newFileAction = new NewFileAction( fc );
		openFileAction = new OpenFileAction( fc );
		revertFileAction = new RevertFileAction( fc );
		saveFileAction = new SaveFileAction( fc );
		saveAsFileAction = new SaveAsFileAction( fc );
		playStopAction = new PlayStopAction( fc );
		exitAction = new ExitAction( fc );
		showPreferencesAction = new ShowPreferencesAction( fc );
		checkAudioConfigurationAction = new CheckAudioConfigurationAction( fc );

		try
		{
			defaultDirectory = configurationService.getSingleStringValue( CONFIG_KEY_DEFAULT_DIR );
		}
		catch (RecordNotFoundException e)
		{
			log.info("Unable to fetch default directory from configuration - if needed set " + CONFIG_KEY_DEFAULT_DIR);
			defaultDirectory = "";
		}
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
}
