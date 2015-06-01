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

package uk.co.modularaudio.service.gui.impl;

import java.awt.Component;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.ContainerTab;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.service.gui.impl.guirackpanel.RackServiceToBackActionAdaptor;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionComparator;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialog;
import uk.co.modularaudio.util.swing.dialog.directoryselection.DirectorySelectionDialogCallback;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialog;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialog;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class GuiServiceImpl implements ComponentWithLifecycle, GuiService
{
	private static Log log = LogFactory.getLog( GuiServiceImpl.class.getName() );

	private GuiComponentFactoryService guiComponentFactoryService;
	private MadComponentService componentService;
	private RackService rackService;
	private BufferedImageAllocationService bufferedImageAllocationService;

	private MadDefinitionListModel madDefinitionsModel;

	private GuiTabbedPane tabbedPane;

	private YesNoQuestionDialog yesNoQuestionDialog;
	private TextInputDialog textInputDialog;
	private MessageDialog messageDialog;

	private DirectorySelectionDialog directorySelectionDialog;

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
		if( messageDialog != null )
			messageDialog.dispose();
		if( textInputDialog != null )
			textInputDialog.dispose();
		if( yesNoQuestionDialog != null )
			yesNoQuestionDialog.dispose();
	}

	public void setGuiComponentFactoryService(final GuiComponentFactoryService guiComponentFactoryService)
	{
		this.guiComponentFactoryService = guiComponentFactoryService;
	}

	public void setRackService(final RackService rackService)
	{
		this.rackService = rackService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setBufferedImageAllocationService(
			final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView( final UserPreferencesMVCController userPrefsModelController )
			throws DatastoreException
	{
		return new UserPreferencesMVCView( this, userPrefsModelController );
	}

	@Override
	public RackModelRenderingComponent createGuiForRackDataModel( final RackDataModel rackDataModel )
			throws DatastoreException
	{
		final GuiRackPanel guiRackPanel = new GuiRackPanel( guiComponentFactoryService,
				this,
				rackService,
				componentService,
				bufferedImageAllocationService,
				new RackServiceToBackActionAdaptor( rackService, rackDataModel ),
				rackDataModel );
		return guiRackPanel;
	}

	@Override
	public MadDefinitionListModel getMadDefinitionsModel() throws DatastoreException
	{
		if( madDefinitionsModel == null )
		{
			// Obtain the list of available component types from the component service
			try
			{
				madDefinitionsModel = componentService.listDefinitionsAvailable();
			}
			catch (final DatastoreException e)
			{
				final String msg = "Exception caught creating the mad definition list model: " + e.toString();
				log.error( msg, e );
				// Fill in a "blank" model
				madDefinitionsModel = new MadDefinitionListModel( new Vector<MadDefinition<?,?>>(), new MadDefinitionComparator());
			}
		}

		return madDefinitionsModel;
	}

	@Override
	public void registerRackTabbedPane( final GuiTabbedPane rackTabbedPane )
	{
		this.tabbedPane = rackTabbedPane;
	}

	@Override
	public void addContainerTab( final ContainerTab containerTab, final boolean isClosable )
	{
		if( tabbedPane != null )
		{
			tabbedPane.addNewContainerTab( containerTab, isClosable );
		}
	}

	@Override
	public void removeContainerTab( final ContainerTab subrackTab )
	{
		if( tabbedPane != null )
		{
			tabbedPane.removeContainerTab( subrackTab );
		}
	}

	private synchronized void checkYnDialog()
	{
		if( yesNoQuestionDialog == null )
		{
			yesNoQuestionDialog = new YesNoQuestionDialog();
		}
	}

	@Override
	public void showYesNoQuestionDialog( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String[] options,
			final String defaultChoice,
			final YesNoQuestionDialogCallback callback )
	{
		if( log.isTraceEnabled() )
		{
			log.trace("Would attempt to show yes no dialog with message " + message );
		}
		checkYnDialog();

		yesNoQuestionDialog.setValues( parentComponent,
				message,
				title,
				messageType,
				options,
				defaultChoice,
				callback );
		yesNoQuestionDialog.go();
	}

	private synchronized void checkTiDialog()
	{
		if( textInputDialog == null )
		{
			textInputDialog = new TextInputDialog();
		}
	}

	@Override
	public void showTextInputDialog( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String initialValue,
			final TextInputDialogCallback callback )
	{
		checkTiDialog();
		textInputDialog.setValues( parentComponent, message, title, messageType, initialValue, callback );
		textInputDialog.go();
	}

	private synchronized void checkMeDialog()
	{
		if( messageDialog == null )
		{
			messageDialog = new MessageDialog();
		}
	}

	@Override
	public void showMessageDialog( final Component parentComponent, final String message,
			final String title,
			final int messageType,
			final MessageDialogCallback callback )
	{
		if( log.isTraceEnabled() )
		{
			log.trace("Would attempt to show message dialog with message " + message );
		}
		checkMeDialog();
		messageDialog.setValues( parentComponent,
				message,
				title,
				messageType,
				callback );
		messageDialog.go();
	}

	private synchronized void checkDsDialog()
	{
		if( directorySelectionDialog == null )
		{
			directorySelectionDialog = new DirectorySelectionDialog();
		}
	}

	@Override
	public void showDirectorySelectionDialog( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final DirectorySelectionDialogCallback callback )
	{
		checkDsDialog();
		directorySelectionDialog.setValues( parentComponent, message, title, messageType, callback );
		directorySelectionDialog.go();
	}
}
