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

package uk.co.modularaudio.componentdesigner.mainframe.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;
import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;

public class OpenFileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( OpenFileAction.class.getName() );

	/**
	 *
	 */
	private final MainFrameActions mainFrameActions;

	private static final long serialVersionUID = -8580442441463163408L;

	private final ComponentDesignerFrontController fc;
	private final UserPreferencesController upc;

	private final MainFrame mainFrame;
	private final SaveFileAction saveFileAction;
	private final PlayStopAction playStopAction;

	public OpenFileAction( final MainFrameActions mainFrameActions,
			final ComponentDesignerFrontController fcin,
			final UserPreferencesController upc,
			final MainFrame mainFrame,
			final SaveFileAction saveFileAction,
			final PlayStopAction playStopAction )
	{
		this.mainFrameActions = mainFrameActions;
		this.fc = fcin;
		this.upc = upc;
		this.mainFrame = mainFrame;
		this.saveFileAction = saveFileAction;
		this.playStopAction = playStopAction;
		this.putValue(NAME, "Open File");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("OpenFileAction called.");
		try
		{
			int dirtyCheckVal = mainFrameActions.rackNotDirtyOrUserConfirmed();
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
				final JFileChooser openFileChooser = new JFileChooser();
				final String patchesDir = upc.getUserPreferencesMVCController().getModel().getUserPatchesModel().getValue();
				openFileChooser.setCurrentDirectory( new File( patchesDir ) );

				final int retVal = openFileChooser.showOpenDialog( mainFrame );
				if( retVal == JFileChooser.APPROVE_OPTION )
				{
					final File f = openFileChooser.getSelectedFile();
					if( f != null )
					{
						if( log.isDebugEnabled() )
						{
							log.debug("Attempting to load from file " + f.getAbsolutePath() );
						}
						fc.loadRackFromFile( f.getAbsolutePath() );
					}
				}
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing open file action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}
