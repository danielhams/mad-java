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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.exception.DatastoreException;

public class GuiRackToolbar extends JToolBar
{
	private static Log log = LogFactory.getLog( GuiRackToolbar.class.getName() );
	/**
	 *
	 */
	private static final long serialVersionUID = -387065135870575057L;

	private JComboBox<MadDefinition<?,?>> componentComboBox = null;
	private JButton addComponentButton = null;
	private JToggleButton rackRotateCheckbox = null;

	private GuiRackActions rackGuiActions = null;
	private GuiService guiService = null;

	public GuiRackToolbar( GuiRackActions rackGuiActions, GuiService guiService )
	{
		this.rackGuiActions = rackGuiActions;
		this.guiService = guiService;
		this.add( getComponentComboBox() );
		this.add( getAddComponentButton() );
		this.add( getRackRotateCheckbox() );
		this.setFloatable( false );
	}

	public final JComboBox<MadDefinition<?,?>> getComponentComboBox()
	{
		if( componentComboBox == null )
		{
			MadDefinitionListModel componentTypes = null;
			try
			{
				componentTypes = guiService.getComponentTypesModel();
			}
			catch (DatastoreException e)
			{
				String msg = "Exception caught getting component types combo data: " + e.toString();
				log.error( msg, e );
			}
			componentComboBox = new ComponentTypeComboBox( componentTypes );
		}
		return componentComboBox;
	}

	public final JButton getAddComponentButton()
	{
		if( addComponentButton == null )
		{
			addComponentButton = new AddComponentButton( rackGuiActions );
		}
		return addComponentButton;
	}

	public final JToggleButton getRackRotateCheckbox()
	{
		if( rackRotateCheckbox == null )
		{
			rackRotateCheckbox = new RotateRackCheckbox( rackGuiActions );
		}
		return rackRotateCheckbox;
	}
}
