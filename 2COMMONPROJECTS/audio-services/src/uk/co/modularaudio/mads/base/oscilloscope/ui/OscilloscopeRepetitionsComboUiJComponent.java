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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeCaptureRepetitionsEnum;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class OscilloscopeRepetitionsComboUiJComponent
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static Log log = LogFactory.getLog( OscilloscopeRepetitionsComboUiJComponent.class.getName() );

	private final Map<String, OscilloscopeCaptureRepetitionsEnum> repetitionsNameToEnumMap = new HashMap<String, OscilloscopeCaptureRepetitionsEnum>();

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public OscilloscopeRepetitionsComboUiJComponent(
			final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		model.addElement( "Continuous" );
		model.addElement( "Once" );

		repetitionsNameToEnumMap.put( "Continuous", OscilloscopeCaptureRepetitionsEnum.CONTINOUS );
		repetitionsNameToEnumMap.put( "Once", OscilloscopeCaptureRepetitionsEnum.ONCE );

		rotaryChoice = new LWTCRotaryChoice(
				LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		model.setSelectedItem( "Continuous" );

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
				final OscilloscopeCaptureRepetitionsEnum rv = repetitionsNameToEnumMap.get( name );
				uiInstance.sendRepetitionChoice( rv );

				if( rv == OscilloscopeCaptureRepetitionsEnum.CONTINOUS )
				{
					uiInstance.doRecapture();
				}
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
		if( repetitionsNameToEnumMap.containsKey( value ) )
		{
			model.setSelectedItem( value );
		}
 		else
		{
			if( log.isWarnEnabled() )
			{
				log.warn("Attempt to set control to unknown value: " + value);
			}
		}
	}
}
