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
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class SpectralAmpRunningAverageChoiceUiJComponent
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>
{
	public enum RunningAverage
	{
		OFF( "Off" ),
		SHORT( "Short Average" ),
		LONG( "Long Average" ),
		FALL( "Fall" ),
		FAST_FALL( "Fast Fall" ),
		PEAK_HOLD( "Peak Hold" );

		private RunningAverage( final String label )
		{
			this.label = label;
		}

		private String label;

		public String getLabel()
		{
			return label;
		}
	};

	public final static RunningAverage DEFAULT_RUNNING_AVERAGE = RunningAverage.FAST_FALL;

	private final static Map<String, RunningAverage> LABEL_TO_MAPPING = new HashMap<>();

	static
	{
		LABEL_TO_MAPPING.put( RunningAverage.OFF.getLabel(), RunningAverage.OFF );
		LABEL_TO_MAPPING.put( RunningAverage.SHORT.getLabel(), RunningAverage.SHORT );
		LABEL_TO_MAPPING.put( RunningAverage.LONG.getLabel(), RunningAverage.LONG );
		LABEL_TO_MAPPING.put( RunningAverage.FALL.getLabel(), RunningAverage.FALL );
		LABEL_TO_MAPPING.put( RunningAverage.FAST_FALL.getLabel(), RunningAverage.FAST_FALL );
		LABEL_TO_MAPPING.put( RunningAverage.PEAK_HOLD.getLabel(), RunningAverage.PEAK_HOLD );
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public SpectralAmpRunningAverageChoiceUiJComponent( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new DefaultComboBoxModel<String>();
		model.addElement( RunningAverage.OFF.getLabel() );
		model.addElement( RunningAverage.SHORT.getLabel() );
		model.addElement( RunningAverage.LONG.getLabel() );
		model.addElement( RunningAverage.FALL.getLabel() );
		model.addElement( RunningAverage.FAST_FALL.getLabel() );
		model.addElement( RunningAverage.PEAK_HOLD.getLabel() );

		model.setSelectedItem( DEFAULT_RUNNING_AVERAGE.getLabel() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
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
				final String val = (String)model.getSelectedItem();
				final RunningAverage ra = LABEL_TO_MAPPING.get( val );
				uiInstance.setDesiredRunningAverage( ra );
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