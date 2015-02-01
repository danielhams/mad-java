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

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.exception.DatastoreException;

public class GuiRackToolbar extends JToolBar
{
	private static final long serialVersionUID = -387065135870575057L;

	private final JComboBox<MadDefinition<?,?>> componentComboBox;
	private final JButton addComponentButton;
	private final JToggleButton rackRotateCheckbox;

	public GuiRackToolbar( final GuiRackActions rackGuiActions, final GuiService guiService ) throws DatastoreException
	{
		final MadDefinitionListModel madDefinitions = guiService.getMadDefinitionsModel();
		componentComboBox = new MadDefinitionComboBox( madDefinitions );

		this.add( componentComboBox );
		addComponentButton = new AddComponentButton( rackGuiActions );
		this.add( addComponentButton );
		rackRotateCheckbox = new RotateRackCheckbox( rackGuiActions );
		this.add( rackRotateCheckbox );
		this.setFloatable( false );
	}
}
