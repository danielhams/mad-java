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

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;

public class NewFileAction extends AbstractAction
{
	private static Log log = LogFactory.getLog( NewFileAction.class.getName() );

	/**
	 *
	 */
	private final MainFrameActions mainFrameActions;

	private static final long serialVersionUID = 4608404122938289459L;

	private final ComponentDesignerFrontController fc;
	private final SaveFileAction saveFileAction;

	public NewFileAction( final MainFrameActions mainFrameActions , final ComponentDesignerFrontController fcin,
			final SaveFileAction saveFileAction )
	{
		this.mainFrameActions = mainFrameActions;
		this.fc = fcin;
		this.saveFileAction = saveFileAction;
		this.putValue(NAME, "New File");
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		log.debug("NewFileAction called.");
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
				fc.toggleRendering();
			}
			try
			{
				fc.newRack();
			}
			catch (final Exception ex)
			{
				final String msg = "Exception caught performing new file action: " + ex.toString();
				log.error( msg, ex );
			}
		}
	}
}
