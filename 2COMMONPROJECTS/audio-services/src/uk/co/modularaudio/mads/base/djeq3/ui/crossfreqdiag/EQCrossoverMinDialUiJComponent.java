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

import java.awt.Component;
import java.awt.Dimension;

import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.SpectralAmpFreqRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class EQCrossoverMinDialUiJComponent
{
//	private static Log log = LogFactory.getLog( SpectralAmpFreqMinDialUiJComponent.class.getName() );

	private final SpectralAmpFreqRotaryDisplayModel model;
	private final RotaryDisplayView view;

	// Look into making this something in the preferences
	public static final float TARGET_PLAYER_DB = -12.0f;

	public static final float DEFAULT_FREQ_MIN = 0.0f;
	public static final float MIN_FREQ_DIFF = 10.0f;

	private float currentMaxFreq = EQCrossoverMaxDialUiJComponent.DEFAULT_FREQ_MAX;

	public EQCrossoverMinDialUiJComponent( final EQCrossoverFreqInputPanel receiver )
	{
		model = new SpectralAmpFreqRotaryDisplayModel(
				DEFAULT_FREQ_MIN,
				EQCrossoverMaxDialUiJComponent.DEFAULT_FREQ_MAX - MIN_FREQ_DIFF,
				DEFAULT_FREQ_MIN,
				DEFAULT_FREQ_MIN );

		final RotaryDisplayController controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.UNIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"Low:",
				EQCrossoverFreqInputPanel.DC,
				false,
				true );

		view.setDiameter( 27 );
		view.setMinimumSize( new Dimension( 120, 30) );

		model.addChangeListener( new ValueChangeListener()
		{
			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				receiver.receiveMinFreqChange( newValue );
			}
		} );
	}

	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	public void receiveControlValue( final String value )
	{
		model.setValue( this, Float.parseFloat( value ) );
	}

	public Component getControl()
	{
		return view;
	}

	public void grabFocus()
	{
		view.getKnob().grabFocus();
	}

	public void receiveMaxFreqChange( final float maxFreq )
	{
		if( maxFreq != currentMaxFreq )
		{
			currentMaxFreq = maxFreq;
			// Stop collisions by forcing a ten hz difference
			model.setMaxValue( maxFreq - MIN_FREQ_DIFF );
		}
	}
}
