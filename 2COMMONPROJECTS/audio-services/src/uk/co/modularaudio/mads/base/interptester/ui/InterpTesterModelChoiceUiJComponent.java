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

package uk.co.modularaudio.mads.base.interptester.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadDefinition;
import uk.co.modularaudio.mads.base.interptester.mu.InterpTesterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class InterpTesterModelChoiceUiJComponent extends JPanel
	implements IMadUiControlInstance<InterpTesterMadDefinition, InterpTesterMadInstance, InterpTesterMadUiInstance>
{
	private static final long serialVersionUID = 28004477652791854L;

	private final DefaultComboBoxModel<String> model;

	public enum ModelChoice
	{
		DJ_CROSS_FADER("DJ Cross Fader"),
		DJ_EQ_GAIN("DJ EQ Gain"),
		DJ_DECK_FADER("DJ Deck Fader"),
		MASTERING_MIXER_FADER("Mastering Mixer Fader"),
		SPEED("Speed"),
		FREQUENCY("Frequency"),
		LEFT_RIGHT("Left Right"),
		COMPRESSION_THRESHOLD("Compression Threshold"),
		COMPRESSION_RATIO("Compression Ratio"),
		OUTPUT_GAIN("Output Gain"),
		TIME_5K_MS("Time (1->5000 ms)"),
		SAO_SCALE("SAO Scale"),
		SAO_OFFSET("SAO Offset"),
		STATIC_VALUE("Static Value");

		private String label;

		private ModelChoice( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	};

	public final static ModelChoice DEFAULT_MODEL_CHOICE = ModelChoice.DJ_CROSS_FADER;

	private final static Map<String, ModelChoice> LABEL_TO_MODEL = new HashMap<>();

	static
	{
		for( final ModelChoice choice : ModelChoice.values() )
		{
			LABEL_TO_MODEL.put( choice.getLabel(), choice );
		}
	}

	public InterpTesterModelChoiceUiJComponent(
			final InterpTesterMadDefinition definition,
			final InterpTesterMadInstance instance,
			final InterpTesterMadUiInstance uiInstance,
			final int controlIndex )
	{
		super();
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 3" );
		msh.addColumnConstraint( "[][grow]" );
		setLayout( msh.createMigLayout() );

		final LWTCLabel modelLabel = new LWTCLabel( "Model:" );
		modelLabel.setFont( LWTCControlConstants.LABEL_FONT );
		modelLabel.setForeground( Color.BLACK );
		modelLabel.setBorder( BorderFactory.createEmptyBorder() );
		add( modelLabel, "");

		model = new DefaultComboBoxModel<String>();
		for( final ModelChoice choice : ModelChoice.values() )
		{
			model.addElement( choice.getLabel() );
		}
		model.setSelectedItem( DEFAULT_MODEL_CHOICE.getLabel() );

		final LWTCRotaryChoice choice = new LWTCRotaryChoice(
				LWTCControlConstants.STD_ROTARY_CHOICE_COLOURS,
				model,
				false );

		add( choice, "grow");

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
				final String newValue = (String)model.getSelectedItem();
				final ModelChoice choice = LABEL_TO_MODEL.get( newValue );
				uiInstance.setModelChoice( choice );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
