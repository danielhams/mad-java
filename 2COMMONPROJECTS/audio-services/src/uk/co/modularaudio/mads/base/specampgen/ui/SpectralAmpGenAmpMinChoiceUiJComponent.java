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

public class SpectralAmpGenAmpMinChoiceUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
	I extends SpectralAmpGenMadInstance<D, I>,
	U extends SpectralAmpGenMadUiInstance<D, I>>
	extends JPanel
	implements IMadUiControlInstance<D, I, U>
{
	private static final long serialVersionUID = -3615905365787164682L;

	private final DefaultComboBoxModel<String> model;
	private final LWTCRotaryChoice rotaryChoice;

	public enum AmpMin
	{
		M_70_DB( "-70dB", -70.0f ),
		M_80_DB( "-80dB", -80.0f ),
		M_96_DB( "-96dB", -96.0f ),
		M_100_DB( "-100dB", -100.0f ),
		M_120_DB( "-120dB", -120.0f ),
		M_144_DB( "-144dB", -144.0f );

		private AmpMin( final String name, final float db )
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

	public final static AmpMin DEFAULT_AMP_MIN = AmpMin.M_96_DB;

	private static final Map<String, AmpMin> NAME_TO_WAVESCALE_MAP = new HashMap<String, AmpMin> ();

	static
	{
		for( final AmpMin ws : AmpMin.values() )
		{
			NAME_TO_WAVESCALE_MAP.put( ws.getName(), ws );
		}
	}

	public SpectralAmpGenAmpMinChoiceUiJComponent( final D definition,
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

		final LWTCLabel label = new LWTCLabel( "Floor:" );
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
		model.addElement( AmpMin.M_70_DB.getName() );
		model.addElement( AmpMin.M_80_DB.getName() );
		model.addElement( AmpMin.M_96_DB.getName() );
		model.addElement( AmpMin.M_100_DB.getName() );
		model.addElement( AmpMin.M_120_DB.getName() );
		model.addElement( AmpMin.M_144_DB.getName() );

		model.setSelectedItem( DEFAULT_AMP_MIN.getName() );

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
				final AmpMin ws = NAME_TO_WAVESCALE_MAP.get( value );
				uiInstance.setDesiredAmpMin( ws );
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
