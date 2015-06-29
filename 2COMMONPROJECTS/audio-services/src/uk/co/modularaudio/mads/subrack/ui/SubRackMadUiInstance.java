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

package uk.co.modularaudio.mads.subrack.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.jpanel.PatchTabCloseListener;
import uk.co.modularaudio.mads.subrack.jpanel.SubRackPatchPanel;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.mads.subrack.ui.runnable.LoadNewSubrackRunnable;
import uk.co.modularaudio.mads.subrack.ui.runnable.SubrackLoadCompletionListener;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.service.util.filesaveextension.CDFileSaveAccessory;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class SubRackMadUiInstance extends AbstractMadUiInstance<SubRackMadDefinition, SubRackMadInstance>
	implements SubrackLoadCompletionListener
{
	private static Log log = LogFactory.getLog( SubRackMadUiInstance.class.getName() );

	private SubRackMadUiDefinition srUiDefinition;

	private boolean havePassedNoshowTick;

	private final RackService rackService;
	private final GuiService guiService;
	private final RackMarshallingService rackMarshallingService;
	private final JobExecutorService jobExecutorService;
	private final UserPreferencesService userPreferencesService;
	private RackDataModel subRackDataModel;

	private RackModelRenderingComponent guiRackPanel;
	private SubRackPatchPanel patchPanel;

	private final HashSet<PatchTabCloseListener> patchTabCloseListeners = new HashSet<PatchTabCloseListener>();

	public SubRackMadUiInstance( final SubRackMadInstance instance, final SubRackMadUiDefinition uiDefinition )
	{
		super( instance, uiDefinition );
		this.srUiDefinition = uiDefinition;

		this.rackService = instance.rackService;
		this.guiService = instance.guiService;
		this.rackMarshallingService = instance.rackMarshallingService;
		this.jobExecutorService = instance.jobExecutorService;
		this.userPreferencesService = instance.userPreferencesService;

		try
		{
			subRackDataModel = instance.getSubRackDataModel();

			guiRackPanel = guiService.createGuiForRackDataModel( subRackDataModel );
			patchPanel = new SubRackPatchPanel( this, guiRackPanel, rackService );
			patchPanel.setRackDataModel( subRackDataModel );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught creating sub rack ui instance: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		super.doDisplayProcessing( tempEventStorage, timingParameters, currentGuiTime );
//		super.receiveDisplayTick( tempEventStorage, currentGuiTime );
		final boolean showing = isSubrackShown();
		if( !showing && !havePassedNoshowTick )
		{
			// Force a receive display tick to all components to allow them to emit "inactive"
			passDisplayTickToSubRack( tempEventStorage, timingParameters, currentGuiTime, true );
			havePassedNoshowTick = true;
		}
		else
		{
			passDisplayTickToSubRack( tempEventStorage, timingParameters, currentGuiTime, false );
			if( showing )
			{
				havePassedNoshowTick = false;
			}
		}
	}

	private void passDisplayTickToSubRack( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime,
			final boolean forceTick )
	{
		// We need to call receive display tick on child if they are a subrack
		// or if we are "showing" then we call it on everyone

		boolean doAll = false;

		if( forceTick )
		{
			doAll = true;
		}
		if( subRackDataModel != null && patchPanel != null && patchPanel.isShowing() )
		{
			doAll = true;
		}

		final List<RackComponent> subRackComponents = subRackDataModel.getEntriesAsList();
		for( int i  =0 ; i < subRackComponents.size() ; i++ )
		{
			final RackComponent rackComponent = subRackComponents.get( i );
			final IMadUiInstance<?, ?> uiInstance = rackComponent.getUiInstance();

			if( doAll || uiInstance instanceof SubRackMadUiInstance )
			{
				uiInstance.receiveDisplayTick( tempEventStorage, timingParameters, currentGuiTime );
			}
		}
	}

	@Override
	public Span getCellSpan()
	{
		return srUiDefinition.getCellSpan();
	}

	@Override
	public void receiveComponentNameChange( final String newName )
	{
		// Update the tab with the new name
		instance.setCurrentPatchName( newName );
		patchPanel.setTitle( newName );
	}

	public void makeSubRackFrameVisible( final boolean setVisible )
	{
		// Either show or hide a rack gui for our data model
//		patchPanel.setVisible( setVisible );
		if( setVisible )
		{
//			log.debug("Would attach listeners...");
			guiService.addContainerTab( patchPanel, true );
		}
		else
		{
			guiService.removeContainerTab( patchPanel );
//			log.debug("Would detach listeners..");
			for( final PatchTabCloseListener l : patchTabCloseListeners )
			{
				l.receivePatchTabClose();
			}
		}
	}

	public boolean isSubrackShown()
	{
		return ( patchPanel == null ? false : patchPanel.isShowing() );
	}

	public void saveSubRack( final Component parent ) throws DatastoreException, IOException, RecordNotFoundException, MAConstraintViolationException
	{
		final JFileChooser saveFileChooser = new JFileChooser();
		final CDFileSaveAccessory cdSaveFileNameAccessory = new CDFileSaveAccessory( rackService.getRackName( subRackDataModel ) );
		saveFileChooser.setAccessory( cdSaveFileNameAccessory );

		final String subRackPatchDir = userPreferencesService.getUserSubRackPatchesDir();
		saveFileChooser.setCurrentDirectory( new File( subRackPatchDir ) );
		final int retVal = saveFileChooser.showSaveDialog( parent );
		if( retVal == JFileChooser.APPROVE_OPTION )
		{
			final File f = saveFileChooser.getSelectedFile();
			if( f != null )
			{
				if( log.isDebugEnabled() )
				{
					log.debug("Attempting to save patch to file " + f.getAbsolutePath() + " with name " + cdSaveFileNameAccessory.getFileName() );
				}
				rackService.setRackName( subRackDataModel, cdSaveFileNameAccessory.getFileName() );
				rackMarshallingService.saveSubRackToFile( subRackDataModel, f.getAbsolutePath() );
				// Only set dirty to false after successful save
				rackService.setRackDirty( subRackDataModel, false );
			}
		}
	}

	public void choosePatch( final Component parent ) throws DatastoreException, IOException, RecordNotFoundException, MAConstraintViolationException
	{
		// Open a choose file dialog and then attempt to load
		// the rack from there.
		// if successfull, pass it to the MI
		final JFileChooser openFileChooser = new JFileChooser();

		final String subRackPatchDir = userPreferencesService.getUserSubRackPatchesDir();
		openFileChooser.setCurrentDirectory( new File( subRackPatchDir ) );
		final int retVal = openFileChooser.showOpenDialog( parent );
		if( retVal == JFileChooser.APPROVE_OPTION )
		{
			final File f = openFileChooser.getSelectedFile();
			if( f != null )
			{
				final Runnable r = new LoadNewSubrackRunnable( rackMarshallingService, f.getAbsolutePath(), this );

				jobExecutorService.submitJob( r );
			}
		}
	}

	public void cleanup()
	{
		try
		{
			if( patchPanel != null )
			{
				if( guiService != null )
				{
					guiService.removeContainerTab( patchPanel );
				}
				if( guiRackPanel != null )
				{
					guiRackPanel.destroy();
				}
			}

			instance.destroySubRackDataModel();
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught cleaning up sub rack instance: " + e.toString();
			log.error( msg, e );
		}
		subRackDataModel = null;
		srUiDefinition = null;
		patchTabCloseListeners.clear();
	}

	public void addPatchTabCloseListener( final PatchTabCloseListener l )
	{
		patchTabCloseListeners.add( l );
	}

	public void removePatchTabCloseListener( final PatchTabCloseListener l )
	{
		patchTabCloseListeners.remove( l );
	}

	public void setSubRackDataModel( final RackDataModel newModel, final boolean destroyPrevious ) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// The mad subrack instance does the actual cleanup of the old data model when it gets reset.
		this.subRackDataModel = newModel;
		patchPanel.setRackDataModel( subRackDataModel );
		instance.setSubRackDataModel( subRackDataModel, destroyPrevious );
	}

	@Override
	public void consumeQueueEntry( final SubRackMadInstance instance, final IOQueueEvent nextOutgoingEntry)
	{
	}

	public RackService getRackService()
	{
		return rackService;
	}

	@Override
	public void notifyLoadCompleted( final RackDataModel newRackDataModel )
	{
		final RackDataModel oldPatch = subRackDataModel;
		try
		{
//			if( log.isDebugEnabled() )
//			{
//				log.debug("Rack load completed - replacing existing model with new one" );
//			}
			subRackDataModel = newRackDataModel;
			patchPanel.setRackDataModel( subRackDataModel );
			instance.setSubRackDataModel( subRackDataModel, false );

			// And destroy the old one
			rackService.destroyRackDataModel( oldPatch );
		}
		catch( final DatastoreException | MAConstraintViolationException | RecordNotFoundException e )
		{
			final String msg = "An exception was thrown updating to the new rack data model: " + e.toString();
			log.error( msg, e );

			subRackDataModel = oldPatch;
		}
	}

	@Override
	public void notifyLoadFailure()
	{
		log.error( "Received notification that the rack load failed" );
	}
}
