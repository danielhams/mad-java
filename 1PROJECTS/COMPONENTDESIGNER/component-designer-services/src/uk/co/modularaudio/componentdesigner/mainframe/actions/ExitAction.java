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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.ExitSignalReceiver;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;

public class ExitAction extends AbstractAction
{
	private final Log log = LogFactory.getLog( ExitAction.class.getName() );
	/**
	 *
	 */
	private final MainFrameActions mainFrameActions;

	private static final long serialVersionUID = 1303196363358495273L;

	private final ComponentDesignerFrontController fc;

	private final List<ExitSignalReceiver> exitSignalReceivers = new ArrayList<ExitSignalReceiver>();

	private final SaveFileAction saveFileAction;

	public ExitAction(final MainFrameActions mainFrameActions,
			final ComponentDesignerFrontController fc,
			final SaveFileAction saveFileAction )
	{
		this.mainFrameActions = mainFrameActions;
		this.fc = fc;
		this.saveFileAction = saveFileAction;
		this.putValue(NAME, "Exit");
	}

	public void addExitSignalReceiver( final ExitSignalReceiver er )
	{
		exitSignalReceivers.add( er );
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		if( fc.isRendering() )
		{
			fc.toggleRendering();
		}

		log.debug("ExitAction perform called.");
		int optionPaneResult = mainFrameActions.rackNotDirtyOrUserConfirmed();

		if( optionPaneResult == JOptionPane.YES_OPTION )
		{
			// Need to save it - call the save
			saveFileAction.actionPerformed( e );

			// Simulate the cancel in the save action if the rack is still dirty.
			optionPaneResult = ( fc.isRackDirty() ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION);
		}

		if( optionPaneResult == JOptionPane.NO_OPTION )
		{
			for( final ExitSignalReceiver esr : exitSignalReceivers )
			{
				esr.signalPreExit();
			}
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
			catch (final Exception e1)
			{
				final String msg = "Exception caught during destruction before exit: " + e1.toString();
				log.error( msg, e1 );
			}
			log.debug("Will signal exit");
			for( final ExitSignalReceiver esr : exitSignalReceivers )
			{
				esr.signalPostExit();
			}
		}
	}
}
