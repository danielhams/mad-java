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

import uk.co.modularaudio.util.math.NormalisedValuesMapper;

public class LogSliderIntToFloatConverter implements
		SliderIntToFloatConverter
{
	private float maxMappedValue = 0.0f;
	
	public LogSliderIntToFloatConverter( float maxMappedValue )
	{
		this.maxMappedValue = maxMappedValue;
	}

	@Override
	public int floatValueToSliderIntValue( SliderDisplayModel sdm, float inValue )
	{
		int numSteps = sdm.getNumSliderSteps();
		float minValue = sdm.getMinValue();
		float maxValue = sdm.getMaxValue();
		float diffValue = maxValue - minValue;
		float normalisedInVal = (inValue - minValue) / diffValue;
		float normalisedFloatVal = NormalisedValuesMapper.logMinMaxMapF( normalisedInVal, 0.0f, maxMappedValue );
		int intVal = (int)( numSteps * normalisedFloatVal );
		return intVal;
	}

	@Override
	public float sliderIntValueToFloatValue( SliderDisplayModel sdm, int sliderIntValue )
	{
		int numSteps = sdm.getNumSliderSteps();
		float minValue = sdm.getMinValue();
		float maxValue = sdm.getMaxValue();
		float diffValue = maxValue - minValue;
		
		float normalisedSliderVal = sliderIntValue / (float)numSteps;
		float normalisedOutVal = NormalisedValuesMapper.expMinMaxMapF( normalisedSliderVal, 0.0f, maxMappedValue );
		return minValue + (normalisedOutVal * diffValue);
	}

}
