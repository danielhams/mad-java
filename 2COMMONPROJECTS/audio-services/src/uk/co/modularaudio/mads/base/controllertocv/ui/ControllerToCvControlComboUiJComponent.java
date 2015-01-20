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

package uk.co.modularaudio.mads.base.controllertocv.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ControllerToCvControlComboUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private ControllerToCvMadUiInstance uiInstance = null;

	private Map<Integer, String> controllerNumToDisplayNameMap = new HashMap<Integer, String>();
	private Map<String, Integer> displayNameToControllerNumMap = new HashMap<String, Integer>();

	public ControllerToCvControlComboUiJComponent(
			ControllerToCvMadDefinition definition,
			ControllerToCvMadInstance instance,
			ControllerToCvMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		
		for( int i = 0 ; i < 127 ; i++ )
		{
			String controllerName = "Controller " + i;
			controllerNumToDisplayNameMap.put( i, controllerName );
		}
		
		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for( Map.Entry<Integer,String> entry : controllerNumToDisplayNameMap.entrySet() )
		{
			String displayName = entry.getValue();
			cbm.addElement( displayName );
			displayNameToControllerNumMap.put( displayName, entry.getKey() );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Controller 0" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	protected void receiveIndexUpdate( int previousIndex, int newIndex )
	{
		if( previousIndex != newIndex )
		{
			// Figure what they changed, and update the component instance data with
			// the new table
			String name = (String) getSelectedItem();
			
			Integer controller = displayNameToControllerNumMap.get( name );
			
			uiInstance.sendSelectedController( controller );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
