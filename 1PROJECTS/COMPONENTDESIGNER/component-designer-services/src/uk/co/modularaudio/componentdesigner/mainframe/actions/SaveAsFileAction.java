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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;
import uk.co.modularaudio.util.audio.gui.mad.service.util.filesaveextension.CDFileSaveAccessory;

public class SaveAsFileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( SaveAsFileAction.class.getName() );

	private static final long serialVersionUID = -4249015082380141979L;

	private final ComponentDesignerFrontController fc;
	private final UserPreferencesController upc;

	private final MainFrame mainFrame;

	public SaveAsFileAction( final ComponentDesignerFrontController fcin,
			final UserPreferencesController upc,
			final MainFrame mainFrame )
	{
		this.fc = fcin;
		this.upc = upc;
		this.mainFrame = mainFrame;

		this.putValue( NAME, "Save File As" );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("SaveFileAsAction called");

		try
		{
			final JFileChooser saveFileChooser = new JFileChooser();
			final String rackDataModelName = fc.getRackDataModelName();
			final CDFileSaveAccessory fileSaveAccessory = new CDFileSaveAccessory( rackDataModelName );
			saveFileChooser.setAccessory( fileSaveAccessory );
			final String patchesDir = upc.getUserPreferencesMVCController().getModel().getUserPatchesModel().getValue();
			saveFileChooser.setCurrentDirectory( new File( patchesDir ) );
			final int retVal = saveFileChooser.showSaveDialog( mainFrame );
			if( retVal == JFileChooser.APPROVE_OPTION )
			{
				final File f = saveFileChooser.getSelectedFile();
				if( f != null )
				{
					final String rackName = fileSaveAccessory.getFileName();
					if( log.isDebugEnabled() )
					{
						log.debug("Attempting to save to file as " + f.getAbsolutePath() + " with name " + rackName );
					}

					fc.saveRackToFile( f.getAbsolutePath(), rackName );
				}
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing save file as action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}
