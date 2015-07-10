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

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class MixdownSliderModel extends SliderDisplayModel
{

	private final static MixdownSliderIntToFloatConverter INT_TO_FLOAT_CONVERTER = new MixdownSliderIntToFloatConverter();

	public MixdownSliderModel()
	{
		super( Float.NEGATIVE_INFINITY, INT_TO_FLOAT_CONVERTER.getLinearHighestDb(),
				Float.NEGATIVE_INFINITY,
				0.0f,
				INT_TO_FLOAT_CONVERTER.getNumTotalSteps(),
				20,
				INT_TO_FLOAT_CONVERTER,
				3,
				3,
				"dB" );
	}

	@Override
	public void setValue( final Object source, final float iNewFloatValue )
	{
		float newFloatValue = iNewFloatValue;
		if( newFloatValue < INT_TO_FLOAT_CONVERTER.getCompressedLowestDb() )
		{
			newFloatValue = Float.NEGATIVE_INFINITY;
		}
		super.setValue( source, newFloatValue );
	}
}
