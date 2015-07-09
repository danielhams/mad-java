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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerEventMapping;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadDefinition;
import uk.co.modularaudio.mads.base.controllertocv.mu.ControllerToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class ControllerToCvMappingChoiceUiJComponent
implements IMadUiControlInstance<ControllerToCvMadDefinition, ControllerToCvMadInstance, ControllerToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( ControllerToCvMappingChoiceUiJComponent.class.getName() );

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public final static Map<String, ControllerEventMapping> MAPPING_LABEL_TO_ENUM = buildLabelToEnumMap();

	private static Map<String, ControllerEventMapping> buildLabelToEnumMap()
	{
		final Map<String, ControllerEventMapping> retVal = new HashMap<>();
		for( final ControllerEventMapping cem : ControllerEventMapping.values() )
		{
			retVal.put( cem.getLabel(), cem );
		}
		return retVal;
	}

	public final static ControllerEventMapping DEFAULT_EVENT_MAPPING = ControllerEventMapping.LINEAR;

	public ControllerToCvMappingChoiceUiJComponent(
			final ControllerToCvMadDefinition definition,
			final ControllerToCvMadInstance instance,
			final ControllerToCvMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new DefaultComboBoxModel<>();
		for( final ControllerEventMapping cem : ControllerEventMapping.values() )
		{
			model.addElement( cem.getLabel() );
		}

		model.setSelectedItem( DEFAULT_EVENT_MAPPING.getLabel() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS, model, false );

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
				final String val = (String)model.getSelectedItem();
				final ControllerEventMapping cem = MAPPING_LABEL_TO_ENUM.get( val );
				uiInstance.sendMapping( cem );
			}
		} );
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

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return rotaryChoice;
	}

	@Override
	public void destroy()
	{
	}
}
