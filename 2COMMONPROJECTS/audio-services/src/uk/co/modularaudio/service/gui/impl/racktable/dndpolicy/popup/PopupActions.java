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

package uk.co.modularaudio.service.gui.impl.racktable.dndpolicy.popup;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class PopupActions
{
	private static Log log = LogFactory.getLog( PopupActions.class.getName() );
	
	public Action rename;
	public Action delete;

	private RackDataModel rackDataModel = null;
	private RackComponent componentForAction = null;
	private RackService rackService = null;
	private GuiService guiService = null;
	private AbstractGuiAudioComponent guiComponent = null;
	
	public class RenameAction extends AbstractAction
	{
		private static final long serialVersionUID = -2712982145491802426L;

		public RenameAction()
		{
			this.putValue( NAME, "Rename" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			log.debug("Would attempt to rename " + componentForAction.getComponentName());
			try
			{
				String defaultNewName = componentForAction.getComponentName();
				String question = "Please enter the new name for " + defaultNewName;
				String title = "Enter new component name";
				String userNewName = (String) JOptionPane.showInputDialog( guiComponent,
						question,
						title,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						defaultNewName );
				
				if( userNewName != null )
				{
					rackService.renameContents( rackDataModel, componentForAction, userNewName );
				}
			}
			catch(Exception re)
			{
				String msg = "Exception caught renaming rack contents: " + re.toString();
				log.error( msg, re );
			}
		}
	}
	
	public class DeleteAction extends AbstractAction
	{
		private static final long serialVersionUID = -2817415117171786425L;
		
		public DeleteAction()
		{
			this.putValue( NAME, "Delete");
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			log.debug("Would attempt to delete " + componentForAction.getComponentName() );
			try
			{
				String defaultNewName = componentForAction.getComponentName();
				String question = "Are you sure you wish to delete " + defaultNewName;
				String title = "Delete Component";
				
				String[] options = {"Delete it",
	                    "Don't delete it" };
				
				DeleteComponentYesNoCallback deleteCallback = new DeleteComponentYesNoCallback( rackService,
						rackDataModel,
						componentForAction );

				guiService.showYesNoQuestionDialog( guiComponent,
						question,
						title,
						JOptionPane.YES_NO_CANCEL_OPTION,
						options,
						options[1],
						deleteCallback );
			}
			catch(Exception re)
			{
				String msg = "Exception caught renaming rack contents: " + re.toString();
				log.error( msg, re );
			}
		}
	}
	
	public PopupActions( RackService rackService, GuiService guiService )
	{
		this.rackService  = rackService;
		this.guiService = guiService;
		rename = new RenameAction();
		delete = new DeleteAction();
	}

	public void setPopupData( RackDataModel rackDataModel, RackComponent rackComponent, AbstractGuiAudioComponent guiComponent )
	{
		this.rackDataModel = rackDataModel;
		this.componentForAction = rackComponent;
		this.guiComponent = guiComponent;
	}
}
