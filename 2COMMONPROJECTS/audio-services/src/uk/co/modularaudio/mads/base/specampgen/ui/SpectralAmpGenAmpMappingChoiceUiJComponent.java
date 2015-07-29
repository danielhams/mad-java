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

package uk.co.modularaudio.mads.base.specampgen.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadDefinition;
import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;
import uk.co.modularaudio.util.swing.lwtc.LWTCRotaryChoice;

public class SpectralAmpGenAmpMappingChoiceUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D, I>,
	U extends SpectralAmpGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D,I,U>
{
	private static final long serialVersionUID = -4110818101779866393L;

//	private static Log log = LogFactory.getLog( SpectralAmpAmpMappingChoiceUiJComponent.class.getName() );

	public enum AmpMapping
	{
		LINEAR( "Lin" ),
		LOG( "Log" ),
		DB( "dB" );

		private AmpMapping( final String label )
		{
			this.label = label;
		}

		private String label;

		public String getLabel()
		{
			return label;
		}
	};

	public final static AmpMapping DEFAULT_AMP_MAPPING = AmpMapping.LOG;

	private final static Map<String, AmpMapping> LABEL_TO_MAPPING = new HashMap<>();

	static
	{
		LABEL_TO_MAPPING.put( AmpMapping.LINEAR.getLabel(), AmpMapping.LINEAR );
		LABEL_TO_MAPPING.put( AmpMapping.LOG.getLabel(), AmpMapping.LOG );
		LABEL_TO_MAPPING.put( AmpMapping.DB.getLabel(), AmpMapping.DB );
	}

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public SpectralAmpGenAmpMappingChoiceUiJComponent( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex,
			final boolean labelAbove )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		if( labelAbove )
		{
			msh.addRowConstraint( "[grow 0][fill]" );
		}
		else
		{
			msh.addColumnConstraint( "[grow 0][fill]" );
		}

		setLayout( msh.createMigLayout() );

		final LWTCLabel label = new LWTCLabel( LWTCControlConstants.STD_LABEL_COLOURS, "Scale:" );
		label.setBorder( BorderFactory.createEmptyBorder() );
		label.setFont( LWTCControlConstants.LABEL_FONT );
		if( labelAbove )
		{
			add( label, "align center, bottom, wrap" );
		}
		else
		{
			add( label, "align center, right" );
		}

		model = new DefaultComboBoxModel<String>();
		model.addElement( AmpMapping.LINEAR.getLabel() );
		model.addElement( AmpMapping.LOG.getLabel() );
		model.addElement( AmpMapping.DB.getLabel() );

		model.setSelectedItem( DEFAULT_AMP_MAPPING.getLabel() );

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
				final AmpMapping mapping = LABEL_TO_MAPPING.get( curVal );
				uiInstance.setDesiredAmpMapping( mapping );
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
