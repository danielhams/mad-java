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
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;

public class SaveFileAction extends AbstractAction
{
	private static final long serialVersionUID = -4249015082380141979L;

	private static Log log = LogFactory.getLog( SaveFileAction.class.getName() );

	private final ComponentDesignerFrontController fc;

	private final SaveAsFileAction saveAsFileAction;

	public SaveFileAction( final ComponentDesignerFrontController fcin,
			final SaveAsFileAction saveAsFileAction )
	{
		this.fc = fcin;
		this.saveAsFileAction = saveAsFileAction;
		this.putValue( NAME, "Save File" );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
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
			catch(final FileNotFoundException fnfe)
			{
			}

			if( !fileSaved )
			{
				saveAsFileAction.actionPerformed( e );
			}
		}
		catch (final Exception ex)
		{
			final String msg = "Exception caught performing save action: " + ex.toString();
			log.error( msg, ex );
		}
	}
}
