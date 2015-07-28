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

import java.awt.Component;

import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadDefinition;
import uk.co.modularaudio.mads.base.specampgen.mu.SpectralAmpGenMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.SpectralAmpFreqRotaryDisplayModel;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class SpectralAmpGenFreqMinDialUiJComponent<D extends SpectralAmpGenMadDefinition<D, I>,
I extends SpectralAmpGenMadInstance<D, I>,
U extends SpectralAmpGenMadUiInstance<D, I>>
	implements IMadUiControlInstance<D, I, U>,
	FreqAxisChangeListener
{
//	private static Log log = LogFactory.getLog( SpectralAmpFreqMinDialUiJComponent.class.getName() );

	private final SpectralAmpFreqRotaryDisplayModel model;
	private final RotaryDisplayView view;

	// Look into making this something in the preferences
	public static final float TARGET_PLAYER_DB = -12.0f;

	private final static SpectralAmpDialColours DC = new SpectralAmpDialColours();

	public static final float DEFAULT_FREQ_MIN = 0.0f;

	private float currentMaxFreq = SpectralAmpGenFreqMaxDialUiJComponent.DEFAULT_FREQ_MAX;

	public SpectralAmpGenFreqMinDialUiJComponent( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		model = new SpectralAmpFreqRotaryDisplayModel(
				DEFAULT_FREQ_MIN,
				SpectralAmpGenFreqMaxDialUiJComponent.DEFAULT_FREQ_MAX - SpectralAmpGenMadUiDefinition.MIN_FREQ_DIFF,
				DEFAULT_FREQ_MIN,
				DEFAULT_FREQ_MIN );

		final RotaryDisplayController controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.UNIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"Frequency Min:",
				DC,
				false,
				true );

		view.setDiameter( 27 );

		uiInstance.addFreqAxisChangeListener( this );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.setDesiredMinFrequency( newValue );
			}
		} );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setValue( this, Float.parseFloat( value ) );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return view;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		final float maxFreq = freqScaleComputer.getMaxFrequency();
		if( maxFreq != currentMaxFreq )
		{
			currentMaxFreq = maxFreq;
			// Stop collisions by forcing a ten hz difference
			model.setMaxValue( maxFreq - SpectralAmpGenMadUiDefinition.MIN_FREQ_DIFF );
		}
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		// Don't care, ignore
	}
}
