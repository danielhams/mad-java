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

package uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay;

import java.awt.Color;

import javax.swing.SwingConstants;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModelAdaptor;
import uk.co.modularaudio.util.mvc.displayslider.SliderIntToFloatConverter;
import uk.co.modularaudio.util.swing.lwtc.LWTCSlider;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.SliderDisplayView.DisplayOrientation;

public class LWTCSliderDisplaySlider extends LWTCSlider
{
//	private static Log log = LogFactory.getLog( SliderDisplaySlider.class.getName() );

	private static final long serialVersionUID = 7532750303295733460L;

	private final SliderDisplayController controller;
	private SliderDisplayModel sdm;

	public LWTCSliderDisplaySlider( final SliderDisplayModel model,
			final SliderDisplayController controller,
			final DisplayOrientation displayOrientation,
			final Color bgColor,
			final Color foregroundColor,
			final boolean opaque )
	{
		super( ( displayOrientation == DisplayOrientation.HORIZONTAL ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL ) );

		this.setOpaque( opaque );

		this.controller = controller;
		this.sdm = model;

		this.setModel( new SliderDisplayModelAdaptor( this, model, controller ) );

		final int sliderMajorTickSpacing = model.getSliderMajorTickSpacing();
//		log.debug("Setting major tick spacing to " + sliderMajorTickSpacing + " with " + model.getNumSliderSteps() + " steps");
		this.setMajorTickSpacing( sliderMajorTickSpacing );
		this.setForeground( foregroundColor );
		this.setBackground( bgColor );
	}

	public void changeModel( final SliderDisplayModel newModel )
	{
		this.sdm = newModel;
		this.setModel( new SliderDisplayModelAdaptor( this, newModel, controller ) );

		final int sliderMajorTickSpacing = newModel.getSliderMajorTickSpacing();
		this.setMajorTickSpacing( sliderMajorTickSpacing );
	}

	public int getInitialValue()
	{
		final SliderIntToFloatConverter itfc = sdm.getSliderIntToFloatConverter();
		return itfc.floatValueToSliderIntValue( sdm, sdm.getInitialValue() );
	}
}
