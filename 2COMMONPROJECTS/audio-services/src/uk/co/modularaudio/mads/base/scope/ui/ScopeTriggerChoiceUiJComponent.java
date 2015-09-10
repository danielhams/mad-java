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

package uk.co.modularaudio.mads.base.scope.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.scope.mu.ScopeMadDefinition;
import uk.co.modularaudio.mads.base.scope.mu.ScopeMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class ScopeTriggerChoiceUiJComponent
	extends JPanel
	implements IMadUiControlInstance<ScopeMadDefinition, ScopeMadInstance, ScopeMadUiInstance>
{
	private static final long serialVersionUID = 3897074819507155924L;

//	private static Log log = LogFactory.getLog( ScopeTriggerChoiceUiJComponent.class.getName() );

	public enum TriggerChoice
	{
		NONE( "None" ),
		ON_RISE( "On Rise" ),
		ON_FALL( "On Fall" );

		private TriggerChoice( final String label )
		{
			this.label = label;
		}

		private String label;

		public String getLabel()
		{
			return label;
		}
	};

	public final static TriggerChoice DEFAULT_TRIGGER_CHOICE = TriggerChoice.NONE;

	private final static Map<String, TriggerChoice> LABEL_TO_MAPPING = new HashMap<>();

	static
	{
		LABEL_TO_MAPPING.put( TriggerChoice.NONE.getLabel(), TriggerChoice.NONE );
		LABEL_TO_MAPPING.put( TriggerChoice.ON_RISE.getLabel(), TriggerChoice.ON_RISE );
		LABEL_TO_MAPPING.put( TriggerChoice.ON_FALL.getLabel(), TriggerChoice.ON_FALL );
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public ScopeTriggerChoiceUiJComponent( final ScopeMadDefinition definition,
			final ScopeMadInstance instance,
			final ScopeMadUiInstance uiInstance,
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

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Trigger:" );
		label.setBorder( BorderFactory.createEmptyBorder() );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		add( label, "align center, right" );

		model = new DefaultComboBoxModel<String>();
		model.addElement( TriggerChoice.NONE.getLabel() );
		model.addElement( TriggerChoice.ON_RISE.getLabel() );
		model.addElement( TriggerChoice.ON_FALL.getLabel() );

		model.setSelectedItem( DEFAULT_TRIGGER_CHOICE.getLabel() );

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
				final String curVal = (String)model.getSelectedItem();
				final TriggerChoice triggerChoice = LABEL_TO_MAPPING.get( curVal );
				uiInstance.setDesiredTrigger( triggerChoice );
			}
		} );

		this.add( rotaryChoice, "grow" );
	}

	@Override
	public JComponent getControl()
	{
		return this;
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
