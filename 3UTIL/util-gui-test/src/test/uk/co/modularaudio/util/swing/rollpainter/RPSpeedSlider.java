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

package test.uk.co.modularaudio.util.swing.rollpainter;

import java.awt.Color;
import java.awt.Dimension;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView;

public class RPSpeedSlider extends SliderDisplayView
{
	private static final long serialVersionUID = 2284612408473584756L;

	public RPSpeedSlider( SliderDisplayModel model,
			SliderDisplayController controller,
			SatelliteOrientation labelOrientation,
			DisplayOrientation displayOrientation,
			SatelliteOrientation textboxOrientation,
			String labelText,
			Color labelColor,
			Color unitsColor,
			boolean opaque )
	{
		super( model,
				controller,
				labelOrientation,
				displayOrientation,
				textboxOrientation,
				labelText,
				labelColor,
				unitsColor,
				opaque );
		this.setMinimumSize( new Dimension( 400, 50 ) );
	}

}
