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

public class LogarithmicTimeMillis1To5000SliderModel extends SliderDisplayModel
{
	public static final float DEFAULT_MILLIS = 60.0f;
	public static final float MIN_MILLIS = 1.0f;
	public static final float MAX_MILLIS = 5000.0f;

	public LogarithmicTimeMillis1To5000SliderModel()
	{
		super( MIN_MILLIS,
				MAX_MILLIS,
				DEFAULT_MILLIS,
				DEFAULT_MILLIS,
				499,
				10,
				new LogSliderIntToFloatConverter( MAX_MILLIS ),
				3,
				3,
				"ms" );
	}
}
