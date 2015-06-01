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

package uk.co.modularaudio.service.gui.mvc.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialogCallback;

public class ChooseDirButtonAction extends AbstractAction
{
	private static final long serialVersionUID = 3349600018661297035L;

	private static Log log = LogFactory.getLog( ChooseDirButtonAction.class.getName() );

	public interface DirectoryChoiceReceiver
	{
		void receiveDirectoryChoice( String fullDirectoryPath );
	};

	private final GuiService guiService;

	private final DirectoryChoiceReceiver choiceReceiver;

	public ChooseDirButtonAction( final GuiService guiService, final DirectoryChoiceReceiver choiceReceiver )
	{
		super( "Choose Directory" );

		this.guiService = guiService;

		this.choiceReceiver = choiceReceiver;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		// Show a model choose directory overlay
		// and setup the result to fire back to the intended recipient
		log.debug("Would show directory selection dialog");

		guiService.showDirectorySelectionDialog( (JComponent)e.getSource(),
				"Choose Directory", "Choose Directory", JOptionPane.INFORMATION_MESSAGE,
				new DirectorySelectionDialogCallback()
				{

					@Override
					public void receiveDirectorySelectionDialogClosed( final String dirPath )
					{
						choiceReceiver.receiveDirectoryChoice( dirPath );
					}
				});
	}
}
