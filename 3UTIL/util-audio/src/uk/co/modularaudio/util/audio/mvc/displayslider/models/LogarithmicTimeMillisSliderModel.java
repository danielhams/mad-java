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

package uk.co.modularaudio.util.audio.mvc.displayslider.models;

import uk.co.modularaudio.util.mvc.displayslider.LogSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class LogarithmicTimeMillisSliderModel extends SliderDisplayModel
{
	public LogarithmicTimeMillisSliderModel()
	{
		super( 1.0f, 5000.0f, 60.0f,
				4999,
				100,
				new LogSliderIntToFloatConverter( 5000.0f ),
				5,
				3,
				"ms" );
	}
}