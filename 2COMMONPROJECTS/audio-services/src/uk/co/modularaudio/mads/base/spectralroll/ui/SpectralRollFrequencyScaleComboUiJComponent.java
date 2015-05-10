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

package uk.co.modularaudio.mads.base.spectralroll.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LinearFreqScaleComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.LogarithmicFreqScaleComputer;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class SpectralRollFrequencyScaleComboUiJComponent
	implements IMadUiControlInstance<SpectralRollMadDefinition, SpectralRollMadInstance, SpectralRollMadUiInstance>
{
	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	private final Map<String, FrequencyScaleComputer> freqScaleToCalculatorMap = new HashMap<String, FrequencyScaleComputer> ();

	public SpectralRollFrequencyScaleComboUiJComponent( final SpectralRollMadDefinition definition,
			final SpectralRollMadInstance instance,
			final SpectralRollMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		model.addElement( "Lin" );
		model.addElement( "Log" );

		model.setSelectedItem( "Log" );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		freqScaleToCalculatorMap.put( "Lin", new LinearFreqScaleComputer() );
		freqScaleToCalculatorMap.put( "Log", new LogarithmicFreqScaleComputer() );

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
				final FrequencyScaleComputer fsc = freqScaleToCalculatorMap.get( value );
				uiInstance.setDesiredFreqScaleComputer( fsc );
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
