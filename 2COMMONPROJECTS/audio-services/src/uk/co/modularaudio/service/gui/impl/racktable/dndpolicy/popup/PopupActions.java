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
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;

public class PopupActions
{
	private static Log log = LogFactory.getLog( PopupActions.class.getName() );

	public final Action rename;
	public final Action delete;

	private final RackService rackService;
	private final GuiService guiService;

	private RackDataModel rackDataModel;
	private RackComponent componentForAction;
	private AbstractGuiAudioComponent guiComponent;

	public class RenameAction extends AbstractAction
	{
		private static final long serialVersionUID = -2712982145491802426L;

		public RenameAction()
		{
			this.putValue( NAME, "Rename" );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if( log.isTraceEnabled() )
			{
				log.trace("Would attempt to rename " + componentForAction.getComponentName());
			}
			try
			{
				final String defaultNewName = componentForAction.getComponentName();
				final String question = "Please enter the new name for " + defaultNewName;
				final String title = "Enter new component name";
				final String userNewName = (String) JOptionPane.showInputDialog( guiComponent,
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
			catch(final Exception re)
			{
				final String msg = "Exception caught renaming rack contents: " + re.toString();
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
		public void actionPerformed( final ActionEvent e )
		{
			if( log.isTraceEnabled() )
			{
				log.trace("Would attempt to delete " + componentForAction.getComponentName() );
			}
			try
			{
				final String defaultNewName = componentForAction.getComponentName();
				final String question = "Are you sure you wish to delete " + defaultNewName;
				final String title = "Delete Component";

				final String[] options = {"Delete it",
	                    "Don't delete it" };

				final DeleteComponentYesNoCallback deleteCallback = new DeleteComponentYesNoCallback( rackService,
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
			catch(final Exception re)
			{
				final String msg = "Exception caught renaming rack contents: " + re.toString();
				log.error( msg, re );
			}
		}
	}

	public PopupActions( final RackService rackService, final GuiService guiService )
	{
		this.rackService  = rackService;
		this.guiService = guiService;
		rename = new RenameAction();
		delete = new DeleteAction();
	}

	public void setPopupData( final RackDataModel rackDataModel, final RackComponent rackComponent, final AbstractGuiAudioComponent guiComponent )
	{
		this.rackDataModel = rackDataModel;
		this.componentForAction = rackComponent;
		this.guiComponent = guiComponent;
	}
}
