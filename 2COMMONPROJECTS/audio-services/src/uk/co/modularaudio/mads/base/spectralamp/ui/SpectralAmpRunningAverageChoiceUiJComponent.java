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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.FallComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.FastFallComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.LongAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.NoAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.PeakHoldComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.RunningAverageComputer;
import uk.co.modularaudio.util.audio.spectraldisplay.runav.ShortAverageComputer;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class SpectralAmpRunningAverageChoiceUiJComponent
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	private final PeakHoldComputer peakHoldComputer = new PeakHoldComputer();

	private final Map<String, RunningAverageComputer> runAvToCalculatorMap = new HashMap<String, RunningAverageComputer> ();
	private final Map<RunningAverageComputer, String> calculatorToNameMap = new HashMap<RunningAverageComputer, String> ();

	public SpectralAmpRunningAverageChoiceUiJComponent( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		model.addElement( "Off" );
		model.addElement( "Short Average" );
		model.addElement( "Long Average" );
		model.addElement( "Fall" );
		model.addElement( "Fast Fall" );
		model.addElement( "Peak Hold" );

		model.setSelectedItem( "Fast Fall" );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		runAvToCalculatorMap.put( "Off", new NoAverageComputer() );
		runAvToCalculatorMap.put( "Short Average", new ShortAverageComputer() );
		runAvToCalculatorMap.put( "Long Average", new LongAverageComputer() );
		runAvToCalculatorMap.put( "Fall", new FallComputer() );
		runAvToCalculatorMap.put( "Fast Fall", new FastFallComputer() );
		runAvToCalculatorMap.put( "Peak Hold", peakHoldComputer );

		for( final String name : runAvToCalculatorMap.keySet() )
		{
			calculatorToNameMap.put( runAvToCalculatorMap.get( name ), name );
		}

		uiInstance.setPeakHoldComputer( peakHoldComputer );

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
				final RunningAverageComputer ac = runAvToCalculatorMap.get( val );
				uiInstance.setDesiredRunningAverageComputer( ac );
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
