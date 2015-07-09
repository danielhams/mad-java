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

package uk.co.modularaudio.mads.base.notetocv.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadDefinition;
import uk.co.modularaudio.mads.base.notetocv.mu.NoteToCvMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.LogarithmicTimeMillisMinZeroRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class NoteToCvGlideLengthSliderUiJComponent
	implements IMadUiControlInstance<NoteToCvMadDefinition, NoteToCvMadInstance, NoteToCvMadUiInstance>
{
//	private static Log log = LogFactory.getLog( NoteToCvGlideLengthSliderUiJComponent.class.getName() );

	public final static int GLISS_KNOB_DIAMETER = 27;

	private final LogarithmicTimeMillisMinZeroRotaryDisplayModel model;
//	private final SliderDisplayController controller;
//	private final LWTCSliderDisplayView view;
	private final RotaryDisplayController controller;
	private final RotaryDisplayView view;

	public NoteToCvGlideLengthSliderUiJComponent( final NoteToCvMadDefinition definition,
			final NoteToCvMadInstance instance,
			final NoteToCvMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new LogarithmicTimeMillisMinZeroRotaryDisplayModel();

		controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.UNIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"Gliss:",
				LWTCControlConstants.STD_ROTARY_VIEW_COLORS,
				false );

		view.setDiameter( GLISS_KNOB_DIAMETER );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendFrequencyGlideMillis( newValue );
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
