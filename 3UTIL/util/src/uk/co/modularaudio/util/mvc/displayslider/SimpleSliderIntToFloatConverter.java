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

package uk.co.modularaudio.util.mvc.displayslider;



public class SimpleSliderIntToFloatConverter implements SliderIntToFloatConverter
{
//	private static Log log = LogFactory.getLog( SimpleSliderIntToFloatConverter.class.getName() );
		
	@Override
	public int floatValueToSliderIntValue( SliderDisplayModel sdm, float inValue )
	{
//		log.debug("Converting slider float value " + inValue );
		int numSteps = sdm.getNumSliderSteps();
		float maxValue = sdm.getMaxValue();
		float minValue = sdm.getMinValue();
		float diffValue = maxValue - minValue;
		float normalisedVal = (inValue - minValue ) / diffValue;
		float scaledVal = (normalisedVal * numSteps) + 0.5f;
		int intVal = (int)(scaledVal);
//		log.debug("Rescaled value as int is " + intVal );
		return intVal;
	}

	@Override
	public float sliderIntValueToFloatValue( SliderDisplayModel sdm, int sliderIntValue )
	{
//		log.debug("Converting slider int value " + sliderIntValue );
		int numSteps = sdm.getNumSliderSteps();
		float maxValue = sdm.getMaxValue();
		float minValue = sdm.getMinValue();
		float diffValue = maxValue - minValue;
		
		float normalisedSliderVal = sliderIntValue / (float)numSteps;
		float rescaledVal = minValue + (normalisedSliderVal * diffValue );
//		log.debug("Rescaled value as float is " + rescaledVal );
		return rescaledVal;
	}

}
