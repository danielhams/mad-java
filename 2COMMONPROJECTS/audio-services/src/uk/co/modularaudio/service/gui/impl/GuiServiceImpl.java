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
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.SubrackTab;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.service.gui.impl.guirackpanel.RackServiceToBackActionAdaptor;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
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
import uk.co.modularaudio.util.swing.dialog.message.MessageDialog;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialog;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialog;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class GuiServiceImpl implements ComponentWithLifecycle, GuiService
{
	private static Log log = LogFactory.getLog( GuiServiceImpl.class.getName() );

	private GuiComponentFactoryService guiComponentFactoryService = null;
	private MadComponentService componentService = null;
	private RackService rackService = null;
	private BufferedImageAllocationService bufferedImageAllocationService = null;

	private MadDefinitionListModel typesComboModel = null;

	private GuiTabbedPane tabbedPane = null;

	private YesNoQuestionDialog yesNoQuestionDialog = null;
	private TextInputDialog textInputDialog = null;
	private MessageDialog messageDialog = null;

	@Override
	public void init() throws ComponentConfigurationException
	{
		yesNoQuestionDialog = new YesNoQuestionDialog();
		textInputDialog = new TextInputDialog();
		messageDialog = new MessageDialog();
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

	public void setGuiComponentFactoryService(GuiComponentFactoryService guiComponentFactoryService)
	{
		this.guiComponentFactoryService = guiComponentFactoryService;
	}

	public void setRackService(RackService rackService)
	{
		this.rackService = rackService;
	}

	public void setComponentService( MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setBufferedImageAllocationService(
			BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView( UserPreferencesMVCController userPrefsModelController )
			throws DatastoreException
	{
		return new UserPreferencesMVCView( userPrefsModelController );
	}

	@Override
	public RackModelRenderingComponent createGuiForRackDataModel( RackDataModel rackDataModel )
			throws DatastoreException
	{
		GuiRackPanel guiRackPanel = new GuiRackPanel( guiComponentFactoryService,
				this,
				rackService,
				componentService,
				bufferedImageAllocationService,
				new RackServiceToBackActionAdaptor( rackService, rackDataModel ),
				rackDataModel );
		return guiRackPanel;
	}

	@Override
	public MadDefinitionListModel getComponentTypesModel() throws DatastoreException
	{
		if( typesComboModel == null )
		{
			// Obtain the list of available component types from the component service
			try
			{
				typesComboModel = componentService.listDefinitionsAvailable();
			}
			catch (DatastoreException e)
			{
				String msg = "Exception caught creating the component type list model: " + e.toString();
				log.error( msg, e );
				// Fill in a "blank" model
				typesComboModel = new MadDefinitionListModel( new Vector<MadDefinition<?,?>>(), new MadDefinitionComparator());
			}
		}

		return typesComboModel;
	}

	@Override
	public void registerRackTabbedPane( GuiTabbedPane rackTabbedPane )
	{
		this.tabbedPane = rackTabbedPane;
	}

	@Override
	public void addSubrackTab( SubrackTab subrackTab, boolean isClosable )
	{
		if( tabbedPane != null )
		{
			tabbedPane.addNewSubrackTab( subrackTab, isClosable );
		}
	}

	@Override
	public void removeSubrackTab( SubrackTab subrackTab )
	{
		if( tabbedPane != null )
		{
			tabbedPane.removeSubrackTab( subrackTab );
		}
	}

	@Override
	public void showYesNoQuestionDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String[] options,
			String defaultChoice,
			YesNoQuestionDialogCallback callback )
	{
		log.debug("Would attempt to show yes no dialog with message " + message );
		yesNoQuestionDialog.setValues( parentComponent,
				message,
				title,
				messageType,
				options,
				defaultChoice,
				callback );
		yesNoQuestionDialog.go();
	}

	@Override
	public void showTextInputDialog( Component parentComponent,
			String message,
			String title,
			int messageType,
			String initialValue,
			TextInputDialogCallback callback )
	{
		textInputDialog.setValues( parentComponent, message, title, messageType, initialValue, callback );
		textInputDialog.go();
	}

	@Override
	public void showMessageDialog( Component parentComponent, String message,
			String title,
			int messageType,
			MessageDialogCallback callback )
	{
		log.debug("Would attempt to show message dialog with message " + message );
		messageDialog.setValues( parentComponent,
				message,
				title,
				messageType,
				callback );
		messageDialog.go();
	}
}
