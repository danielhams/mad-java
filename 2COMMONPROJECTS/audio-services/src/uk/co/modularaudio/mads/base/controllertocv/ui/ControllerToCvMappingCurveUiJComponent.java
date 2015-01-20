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

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerEventMapping;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class ControllerToCvMappingCurveUiJComponent extends PacComboBox<String>
	implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private ControllerToCvMadUiInstance uiInstance = null;

	private Map<ControllerEventMapping, String> eventMappingToDisplayNameMap = new HashMap<ControllerEventMapping, String>();
	private Map<String, ControllerEventMapping> displayNameToEventMappingMap = new HashMap<String, ControllerEventMapping>();

	public ControllerToCvMappingCurveUiJComponent(
			ControllerToCvMadDefinition definition,
			ControllerToCvMadInstance instance,
			ControllerToCvMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;
		this.setOpaque( false );
		
		eventMappingToDisplayNameMap.put( ControllerEventMapping.LINEAR, "Linear" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.LOG, "Log" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.LOG_FREQUENCY, "Log Frequency" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.EXP, "Exp" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.EXP_FREQUENCY, "Exp Frequency" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.CIRC_Q1, "Circle, Quadrant 1" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.CIRC_Q2, "Circle, Quadrant 2" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.CIRC_Q3, "Circle, Quadrant 3" );
		eventMappingToDisplayNameMap.put( ControllerEventMapping.CIRC_Q4, "Circle, Quadrant 4" );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		ControllerEventMapping[] eventMappingValues = ControllerEventMapping.values();
		for( ControllerEventMapping eventMapping : eventMappingValues )
		{
			String displayName = eventMappingToDisplayNameMap.get( eventMapping );
			cbm.addElement( displayName );
			displayNameToEventMappingMap.put( displayName, eventMapping );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "Linear" );
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
			
			ControllerEventMapping mappingToUse = displayNameToEventMappingMap.get( name );
			
			uiInstance.sendMapping( mappingToUse );
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
