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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillis1To1000SliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class OscilloscopeCaptureLengthSliderUiJComponent
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
//	private static Log log = LogFactory.getLog( OscilloscopeCaptureLengthSliderUiJComponent.class.getName() );

	private final LogarithmicTimeMillis1To1000SliderModel model;
	private final SliderDisplayController controller;
	private final LWTCSliderDisplayView view;

	public OscilloscopeCaptureLengthSliderUiJComponent( final OscilloscopeMadDefinition definition,
			final OscilloscopeMadInstance instance,
			final OscilloscopeMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new LogarithmicTimeMillis1To1000SliderModel();

		controller = new SliderDisplayController( model );

		view = new LWTCSliderDisplayView(
				model,
				controller,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				LWTCControlConstants.SLIDER_VIEW_COLORS,
				"Capture Time:",
				false,
				true );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.setCaptureTimeMillis( newValue );
			}
		} );
	}

	@Override
	public JComponent getControl()
	{
		return view;
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
