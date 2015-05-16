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
		model.addElement( "DJ Cross Fader" );
		model.addElement( "DJ EQ Gain" );
		model.addElement( "DJ Deck Fader" );
		model.addElement( "Mastering Mixer Fader" );
		model.addElement( "Speed" );
		model.addElement( "Frequency" );
		model.addElement( "Left Right" );
		model.addElement( "Compression Threshold" );
		model.addElement( "Compression Ratio" );
		model.addElement( "Output Gain" );
		model.addElement( "Time (1->5000 ms)" );
		model.addElement( "SAO Scale" );
		model.addElement( "SAO Offset" );
		model.addElement( "Static Value" );

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
				final int index = model.getIndexOf( newValue );
				uiInstance.setValueModelIndex( index );
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
