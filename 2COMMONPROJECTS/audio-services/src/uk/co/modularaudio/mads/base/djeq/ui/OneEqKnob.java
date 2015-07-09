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

package uk.co.modularaudio.mads.base.djeq.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.DjEQRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class OneEqKnob extends PacPanel
{
	private static final long serialVersionUID = 537398070381235844L;

	private static Log log = LogFactory.getLog( OneEqKnob.class.getName() );

	private final DjEQRotaryDisplayModel rdm;
	private final RotaryDisplayController rdc;
	private final RotaryDisplayView rdv;

	public OneEqKnob( final String label )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		setLayout( msh.createMigLayout() );

		rdm = new DjEQRotaryDisplayModel();

		rdc = new RotaryDisplayController( rdm );

		rdv = new RotaryDisplayView(
				rdm,
				rdc,
				KnobType.BIPOLAR,
				SatelliteOrientation.ABOVE,
				SatelliteOrientation.RIGHT,
				label,
				LWTCControlConstants.STD_ROTARY_VIEW_COLORS,
				false,
				true );

		rdv.setDiameter( 31 );

		this.add( rdv, "grow" );
	}

	public RotaryDisplayModel getModel()
	{
		return rdm;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( rdm.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		if( value != null && value.length() > 0 )
		{
			try
			{
				final float newFloatValue = Float.parseFloat( value );
				rdc.setValue( this, newFloatValue );
			}
			catch( final NumberFormatException nfe )
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Failed to parse OneEqKnob value: " + value );
				}
			}
		}
	}
}
