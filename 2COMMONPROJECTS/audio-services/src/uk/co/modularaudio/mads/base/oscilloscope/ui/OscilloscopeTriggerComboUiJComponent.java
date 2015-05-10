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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureTriggerEnum;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class OscilloscopeTriggerComboUiJComponent
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private final Map<String, OscilloscopeCaptureTriggerEnum> triggerNameToEnumMap = new HashMap<String, OscilloscopeCaptureTriggerEnum>();

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public OscilloscopeTriggerComboUiJComponent(
			final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		model.addElement( "None" );
		model.addElement( "On Rise" );
		model.addElement( "On Fall" );

		triggerNameToEnumMap.put( "None", OscilloscopeCaptureTriggerEnum.NONE );
		triggerNameToEnumMap.put( "On Rise", OscilloscopeCaptureTriggerEnum.ON_RISE );
		triggerNameToEnumMap.put( "On Fall", OscilloscopeCaptureTriggerEnum.ON_FALL );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		model.setSelectedItem( "None" );

		model.addListDataListener( new ListDataListener()
		{

			@Override
			public void intervalRemoved( final ListDataEvent e )
			{
			}

			@Override
			public void intervalAdded( final ListDataEvent e )
			{
			}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
				final String name = (String)model.getSelectedItem();
				final OscilloscopeCaptureTriggerEnum ev = triggerNameToEnumMap.get( name );
				uiInstance.sendTriggerChoice( ev );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return rotaryChoice;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
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

	@Override
	public String getControlValue()
	{
		return (String)model.getSelectedItem();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setSelectedItem( value );
	}
}
