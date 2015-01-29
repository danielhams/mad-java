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

package uk.co.modularaudio.service.gui.impl.guirackpanel.sub;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;

public class GuiRackActions
{
	public class RackRotateToggleAction extends AbstractAction
	{
		private static final long serialVersionUID = 6567077333146253552L;
		
//		private GuiService guiService = null;
		private GuiRackPanel guiRackPanel = null;
		
		public RackRotateToggleAction( GuiService guiService, GuiRackPanel guiRackPanel )
		{
//			this.guiService = guiService;
			this.guiRackPanel = guiRackPanel;
			this.putValue(NAME, "View Wires");
			this.putValue(SELECTED_KEY, "RackRotate.selected");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				guiRackPanel.rotateRack();
			}
			catch (Exception ex)
			{
				String msg = "Exception caught performing rotate rack action: " + ex.toString();
				log.error( msg, ex );
			}
		}

		public void destroy()
		{
			guiRackPanel = null;
		}
	}
	
	public class AddComponentAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7614791274456721383L;
		
		private GuiService guiService = null;
		private RackService rackService = null;
//		private ComponentService componentService = null;
		private GuiRackPanel guiRackPanel = null;
		private RackDataModel rackDataModel = null;
		
		public AddComponentAction( GuiService guiService,
				RackService rackService,
				MadComponentService componentService,
				GuiRackPanel guiRackPanel, RackDataModel rackDataModel )
		{
			this.guiService = guiService;
			this.rackService = rackService;
//			this.componentService = componentService;
			this.rackDataModel = rackDataModel;
			this.guiRackPanel = guiRackPanel;
			this.putValue(NAME, "Add Component To Rack");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{

//			log.debug("ActionPerformed on add component action");
			try
			{
				MadDefinitionListModel pctcm = guiService.getMadDefinitionsModel();
				MadDefinition<?,?> typeToAdd = (MadDefinition<?,?>) pctcm.getSelectedItem();
//				log.debug("Would attempt to add: " + typeToAdd.getId() );
				// Pop up a dialog allowing the use to enter a new name
				// We get the default to put in the dialog from the graph anyway.
				String defaultNewName = rackService.getNameForNewComponentOfType( rackDataModel, typeToAdd );
				String question = "Please enter the name for the new \"" + typeToAdd.getName() + "\"";
				String title = "Enter new component name";
				
				NewComponentNameConfirmedCallback componentNameConfirmedCallback = new NewComponentNameConfirmedCallback( rackService,
						guiService,
						rackDataModel,
						guiRackPanel,
						typeToAdd );
				
				guiService.showTextInputDialog( guiRackPanel,
						question,
						title,
						JOptionPane.QUESTION_MESSAGE,
						defaultNewName,
						componentNameConfirmedCallback );	
			}
			catch(Exception ex)
			{
				String msg = "Exception caught performing add component: " + ex.toString();
				log.error( msg, ex );
			}
		}

		public void setRackDataModel( RackDataModel rackDataModel )
		{
			this.rackDataModel = rackDataModel;
		}

		public void destroy()
		{
			this.rackDataModel = null;
			this.guiRackPanel = null;
		}
	}
	
	private static Log log = LogFactory.getLog( GuiRackActions.class.getName() );
	
//	private GuiService guiService = null;
//	private RackService rackService = null;
//	private ComponentService componentService = null;
//	private GuiRackPanel guiRackPanel = null;
//	private RackDataModel rackDataModel = null;
	
	private AddComponentAction addComponentAction = null;
	private RackRotateToggleAction rackRotateToggleAction = null;
	
	public GuiRackActions( GuiService guiService,
			RackService rackService,
			MadComponentService componentService,
			GuiRackPanel guiRackPanel, RackDataModel rackDataModel )
	{
//		this.guiService = guiService;
//		this.rackService = rackService;
//		this.componentService = componentService;
//		this.guiRackPanel = guiRackPanel;
//		this.rackDataModel = rackDataModel;
		addComponentAction = new AddComponentAction( guiService,
				rackService,
				componentService,
				guiRackPanel,
				rackDataModel );
		rackRotateToggleAction = new RackRotateToggleAction( guiService,
				guiRackPanel );
	}
	
	public Action getAddComponentAction()
	{
		return addComponentAction;
	}
	
	public Action getRackRotateToggleAction()
	{
		return rackRotateToggleAction;
	}
	
	public void setRackDataModel( RackDataModel rackDataModel )
	{
//		this.rackDataModel = rackDataModel;
		addComponentAction.setRackDataModel( rackDataModel );
	}

	public void destroy()
	{
		addComponentAction.destroy();
		rackRotateToggleAction.destroy();		
	}
}
