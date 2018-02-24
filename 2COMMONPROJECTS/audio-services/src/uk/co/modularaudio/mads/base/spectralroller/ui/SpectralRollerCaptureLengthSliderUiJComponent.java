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

package uk.co.modularaudio.mads.base.spectralroller.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadDefinition;
import uk.co.modularaudio.mads.base.spectralroller.mu.SpectralRollerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To5000SliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class SpectralRollerCaptureLengthSliderUiJComponent
	implements IMadUiControlInstance<SpectralRollerMadDefinition, SpectralRollerMadInstance, SpectralRollerMadUiInstance>
{
//	private static Log log = LogFactory.getLog( WaveRollerCaptureLengthSliderUiJComponent.class.getName() );

	private final LogarithmicTimeMillis1To5000SliderModel model;
	private final SliderDisplayController controller;
	private final LWTCSliderDisplayView view;

	public SpectralRollerCaptureLengthSliderUiJComponent( final SpectralRollerMadDefinition definition,
			final SpectralRollerMadInstance instance,
			final SpectralRollerMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new LogarithmicTimeMillis1To5000SliderModel();

		controller = new SliderDisplayController( model );

		view = new LWTCSliderDisplayView(
				model,
				controller,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				LWTCControlConstants.SLIDER_VIEW_COLORS,
				"Zoom:",
				false, true );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.setCaptureTime( newValue );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return view;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage ,
			final MadTimingParameters timingParameters ,
			final int U_currentGuiTime , final int framesSinceLastTick )
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String valueStr )
	{
		final float asFloat = Float.parseFloat( valueStr );
		controller.setValue( this, asFloat );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}