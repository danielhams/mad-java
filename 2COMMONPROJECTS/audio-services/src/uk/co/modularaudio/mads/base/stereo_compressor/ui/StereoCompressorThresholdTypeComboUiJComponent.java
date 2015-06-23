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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class StereoCompressorThresholdTypeComboUiJComponent
	implements IMadUiControlInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>
{
	private static final Map<String,ThresholdTypeEnum> DS_TO_ENUM_MAP = createDsToEnumMap();

	private static final Map<String,ThresholdTypeEnum> createDsToEnumMap()
	{
		final HashMap<String,ThresholdTypeEnum> map = new HashMap<String,ThresholdTypeEnum>();
		for( final ThresholdTypeEnum e : ThresholdTypeEnum.values() )
		{
			map.put( e.getDisplayString(), e );
		}

		return map;
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice choice;

	public StereoCompressorThresholdTypeComboUiJComponent(
			final StereoCompressorMadDefinition definition,
			final StereoCompressorMadInstance instance,
			final StereoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		for( final ThresholdTypeEnum e : ThresholdTypeEnum.values() )
		{
			model.addElement( e.getDisplayString() );
		}
		model.setSelectedItem( ThresholdTypeEnum.RMS.getDisplayString() );

		choice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

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
				final String value = (String)model.getSelectedItem();
				final ThresholdTypeEnum tte = DS_TO_ENUM_MAP.get( value );
				uiInstance.sendThresholdType( tte.ordinal() );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return choice;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
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
