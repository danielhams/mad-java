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

package uk.co.modularaudio.mads.base.bandlimitedoscillator.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadDefinition;
import uk.co.modularaudio.mads.base.bandlimitedoscillator.mu.BandLimitedOscillatorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.oscillatortable.OscillatorWaveShape;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class BandLimitedOscillatorShapeChoiceUiJComponent
	extends JPanel
	implements IMadUiControlInstance<BandLimitedOscillatorMadDefinition, BandLimitedOscillatorMadInstance, BandLimitedOscillatorMadUiInstance>
{
	private static final long serialVersionUID = -6447604891826738340L;

	public enum WaveChoice
	{
		SINE( "Sine", OscillatorWaveShape.SINE ),
		SAW( "Saw", OscillatorWaveShape.SAW ),
		SQUARE( "Square", OscillatorWaveShape.SQUARE ),
		TRIANGLE( "Triangle", OscillatorWaveShape.TRIANGLE ),
		TEST1( "Test1", OscillatorWaveShape.TEST1 ),
		JUNO( "Juno", OscillatorWaveShape.JUNO );

		private String label;
		private OscillatorWaveShape shape;

		private WaveChoice( final String label, final OscillatorWaveShape shape )
		{
			this.label = label;
			this.shape = shape;
		}

		public String getLabel()
		{
			return label;
		}

		public OscillatorWaveShape getShape()
		{
			return shape;
		}
	};

	public final static WaveChoice DEFAULT_WAVE_CHOICE = WaveChoice.SAW;

	private final static Map<String, WaveChoice> LABEL_TO_MAPPING = new HashMap<>();

	static
	{
		LABEL_TO_MAPPING.put( WaveChoice.SINE.getLabel(), WaveChoice.SINE );
		LABEL_TO_MAPPING.put( WaveChoice.SAW.getLabel(), WaveChoice.SAW );
		LABEL_TO_MAPPING.put( WaveChoice.SQUARE.getLabel(), WaveChoice.SQUARE );
		LABEL_TO_MAPPING.put( WaveChoice.TRIANGLE.getLabel(), WaveChoice.TRIANGLE );
		LABEL_TO_MAPPING.put( WaveChoice.TEST1.getLabel(), WaveChoice.TEST1 );
		LABEL_TO_MAPPING.put( WaveChoice.JUNO.getLabel(), WaveChoice.JUNO );
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public BandLimitedOscillatorShapeChoiceUiJComponent( final BandLimitedOscillatorMadDefinition definition,
			final BandLimitedOscillatorMadInstance instance,
			final BandLimitedOscillatorMadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
		msh.addColumnConstraint( "[grow 0][fill]" );

		setLayout( msh.createMigLayout() );

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Wave:" );
		label.setBorder( BorderFactory.createEmptyBorder() );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		add( label, "align center, right" );

		model = new DefaultComboBoxModel<String>();
		for( final WaveChoice wc : WaveChoice.values() )
		{
			model.addElement( wc.getLabel() );
		}

		model.setSelectedItem( DEFAULT_WAVE_CHOICE.getLabel() );

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
				final WaveChoice wc = LABEL_TO_MAPPING.get( val );
				uiInstance.setWaveShape( wc.getShape() );
			}
		} );

		this.add( rotaryChoice, "grow");
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , int framesSinceLastTick )
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
