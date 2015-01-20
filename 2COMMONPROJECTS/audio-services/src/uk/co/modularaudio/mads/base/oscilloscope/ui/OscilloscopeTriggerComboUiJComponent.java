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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureTriggerEnum;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComboBox;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class OscilloscopeTriggerComboUiJComponent extends PacComboBox<String>
		implements
		IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private OscilloscopeMadUiInstance uiInstance = null;

	private Map<String, OscilloscopeCaptureTriggerEnum> triggerNameToEnumMap = new HashMap<String, OscilloscopeCaptureTriggerEnum>();

	public OscilloscopeTriggerComboUiJComponent(
			OscilloscopeMadDefinition definition,
			OscilloscopeMadInstance instance,
			OscilloscopeMadUiInstance uiInstance,
			int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( false );

		triggerNameToEnumMap.put( "None", OscilloscopeCaptureTriggerEnum.NONE );
		triggerNameToEnumMap.put( "On Rise", OscilloscopeCaptureTriggerEnum.ON_RISE );
		triggerNameToEnumMap.put( "On Fall", OscilloscopeCaptureTriggerEnum.ON_FALL );

		DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
		for (String triggerName : triggerNameToEnumMap.keySet())
		{
			cbm.addElement( triggerName );
		}
		this.setModel( cbm );

		setFont( this.getFont().deriveFont( 9f ) );

		this.setSelectedItem( "None" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
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
			String name = (String) getSelectedItem();
			OscilloscopeCaptureTriggerEnum ev = triggerNameToEnumMap.get( name );
			uiInstance.sendTriggerChoice( ev );
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
