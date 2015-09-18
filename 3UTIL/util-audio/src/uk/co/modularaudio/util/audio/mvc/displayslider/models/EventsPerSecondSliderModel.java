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

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class EventsPerSecondSliderModel extends SliderDisplayModel
{
	public final static int MIN_EVENTS_PER_SECOND = 5;
	public final static int MAX_EVENTS_PER_SECOND = 1000;
	public final static int DEFAULT_EVENTS_PER_SECOND = 30;

	public EventsPerSecondSliderModel()
	{
		super( MIN_EVENTS_PER_SECOND,
				MAX_EVENTS_PER_SECOND,
				DEFAULT_EVENTS_PER_SECOND,
				DEFAULT_EVENTS_PER_SECOND,
				MAX_EVENTS_PER_SECOND - MIN_EVENTS_PER_SECOND,
				5,
				new SimpleSliderIntToFloatConverter(),
				4,
				0,
				"eps" );
	}
}
