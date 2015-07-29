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

public class SpectralAmpGenAmpMaxChoiceUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D, I>,
	U extends SpectralAmpGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D, I, U>
{
	private static final long serialVersionUID = -3615905365787164682L;

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum AmpMax
	{
		ZERO_DB( "0dB", 0.0f ),
		M_FIVE_DB( "-5dB", -5.0f ),
		M_TEN_DB( "-10dB", -10.0f ),
		M_FIFTEEN_DB( "-15dB", -15.0f ),
		M_TWENTY_DB( "-20dB", -20.0f ),
		M_THIRTY_DB( "-30dB", -30.0f ),
		M_FORTY_DB( "-40dB", -40.0f ),
		M_FIFTY_DB( "-50dB", -50.0f ),
		M_SIXTY_DB( "-60dB", -60.0f );

		private AmpMax( final String name, final float db )
		{
			this.name = name;
			this.db = db;
		}

		public String getName()
		{
			return name;
		}

		public float getDb()
		{
			return db;
		}

		private String name;
		private float db;
	};

	public static final AmpMax DEFAULT_AMP_MAX = AmpMax.ZERO_DB;

	private static final Map<String, AmpMax> NAME_TO_WAVESCALE_MAP = new HashMap<String, AmpMax> ();

	static
	{
		for( final AmpMax ws : AmpMax.values() )
		{
			NAME_TO_WAVESCALE_MAP.put( ws.getName(), ws );
		}
	}

	public SpectralAmpGenAmpMaxChoiceUiJComponent( final D definition,
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

		final LWTCLabel label = new LWTCLabel( "Amp Ceiling:" );
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
		model.addElement( AmpMax.ZERO_DB.getName() );
		model.addElement( AmpMax.M_FIVE_DB.getName() );
		model.addElement( AmpMax.M_TEN_DB.getName() );
		model.addElement( AmpMax.M_FIFTEEN_DB.getName() );
		model.addElement( AmpMax.M_TWENTY_DB.getName() );
		model.addElement( AmpMax.M_THIRTY_DB.getName() );
		model.addElement( AmpMax.M_FORTY_DB.getName() );
		model.addElement( AmpMax.M_FIFTY_DB.getName() );
		model.addElement( AmpMax.M_SIXTY_DB.getName() );

		model.setSelectedItem( DEFAULT_AMP_MAX.getName() );

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
				final String value = (String)model.getSelectedItem();
				final AmpMax ws = NAME_TO_WAVESCALE_MAP.get( value );
				uiInstance.setDesiredAmpMax( ws );
			}
		} );

		add( rotaryChoice, "grow" );
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
