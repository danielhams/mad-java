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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.spectraldisplay.freqscale.FrequencyScaleComputer;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.mvc.displayrotary.SimpleRotaryIntToFloatConverter;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class SpectralAmpFreqMaxDialUiJComponent
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>,
	SampleRateListener, FreqAxisChangeListener
{
//	private static Log log = LogFactory.getLog( SpectralAmpFreqMaxDialUiJComponent.class.getName() );

	private final RotaryDisplayModel model;
	private final RotaryDisplayView view;

	// Look into making this something in the preferences
	public static final float TARGET_PLAYER_DB = -12.0f;

	private final static FreqDialColours DC = new FreqDialColours();

	public static final float DEFAULT_FREQ_MAX = 22050.0f;

	private float currentMinFreq = SpectralAmpFreqMinDialUiJComponent.DEFAULT_FREQ_MIN;

	public SpectralAmpFreqMaxDialUiJComponent( final SpectralAmpMadDefinition definition,
			final SpectralAmpMadInstance instance,
			final SpectralAmpMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new RotaryDisplayModel(
				SpectralAmpFreqMinDialUiJComponent.DEFAULT_FREQ_MIN,
				DEFAULT_FREQ_MAX,
				DEFAULT_FREQ_MAX,
				200,
				32,
				new SimpleRotaryIntToFloatConverter(),
				5,
				0,
				"hz");

		final RotaryDisplayController controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.UNIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"Max:",
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
				uiInstance.setDesiredMaxFrequency( newValue );
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
	public void receiveSampleRateChange( final int sampleRate )
	{
		model.setMaxValue( sampleRate / 2.0f );
	}

	@Override
	public void receiveFreqScaleChange( final FrequencyScaleComputer freqScaleComputer )
	{
		final float minFreq = freqScaleComputer.getMinFrequency();
		if( minFreq != currentMinFreq )
		{
			currentMinFreq = minFreq;
			// Stop collisions by forcing a ten hz difference
			model.setMinValue( minFreq + SpectralAmpMadUiDefinition.MIN_FREQ_DIFF );
		}
	}

	@Override
	public void receiveFftSizeChange( final int desiredFftSize )
	{
		// Don't care
	}
}
