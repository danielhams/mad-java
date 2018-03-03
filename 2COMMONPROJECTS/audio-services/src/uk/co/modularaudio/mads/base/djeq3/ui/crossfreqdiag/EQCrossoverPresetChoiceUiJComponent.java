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

package uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class EQCrossoverPresetChoiceUiJComponent
	extends JPanel
{
	private static final long serialVersionUID = 5707248371035768443L;

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;
	private final LWTCButton loadPresetButton;

	public enum PresetChoice
	{
		DJ_EQ_BANDS1("DJ EQ 1", 120.0f, 2500.0f),
		THREEWAY_COMPRESSION("3 Band Compression", 457.460f, 2500.0f);

		private PresetChoice( final String name, final float lowFreq, final float highFreq )
		{
			this.name = name;
			this.lowFreq = lowFreq;
			this.highFreq = highFreq;
		}

		public String getName()
		{
			return name;
		}

		public float getLowFreq()
		{
			return lowFreq;
		}

		public float getHighFreq()
		{
			return highFreq;
		}

		private String name;
		private float lowFreq;
		private float highFreq;
	};

	public final static PresetChoice DEFAULT_PRESET = PresetChoice.THREEWAY_COMPRESSION;

	private static final Map<String, PresetChoice> NAME_TO_PRESET_MAP = new HashMap<String, PresetChoice> ();

	static
	{
		for( final PresetChoice pc : PresetChoice.values() )
		{
			NAME_TO_PRESET_MAP.put( pc.getName(), pc );
		}
	}

	public EQCrossoverPresetChoiceUiJComponent( final EQCrossoverFreqInputPanel receiver )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		msh.addColumnConstraint( "[fill][grow 0]" );

		setLayout( msh.createMigLayout() );

//		final LWTCLabel label = new LWTCLabel( "Preset:" );
//		label.setBorder( BorderFactory.createEmptyBorder() );
//		label.setFont( LWTCControlConstants.LABEL_FONT );
//		add( label, "align center, right" );

		model = new DefaultComboBoxModel<String>();
		model.addElement( PresetChoice.DJ_EQ_BANDS1.getName() );
		model.addElement( PresetChoice.THREEWAY_COMPRESSION.getName() );

		model.setSelectedItem( PresetChoice.DJ_EQ_BANDS1.getName() );

		rotaryChoice = new LWTCRotaryChoice( LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		model.addListDataListener( new ListDataListener()
		{
			@Override
			public void intervalRemoved( final ListDataEvent e ) {}

			@Override
			public void intervalAdded( final ListDataEvent e ) {}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
			}
		} );

		add( rotaryChoice, "grow" );

		loadPresetButton = new LWTCButton( LWTCControlConstants.STD_BUTTON_COLOURS,
				"Load Preset", false )
		{
			private static final long serialVersionUID = 3372322482420099520L;

			@Override
			public void receiveClick()
			{
				final String value = (String)model.getSelectedItem();
				final PresetChoice pc = NAME_TO_PRESET_MAP.get( value );
				receiver.receiveMinMaxFreqChange( pc.lowFreq, pc.highFreq );
			}
		};
		add( loadPresetButton, "" );

		this.setMinimumSize( new Dimension( 150, 30 ) );
	}

	public JComponent getControl()
	{
		return this;
	}


	public String getControlValue()
	{
		return (String)model.getSelectedItem();
	}

	public void receiveControlValue( final String value )
	{
		model.setSelectedItem( value );
	}
}
