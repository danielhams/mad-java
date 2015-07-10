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
import uk.co.modularaudio.util.mvc.displayslider.SliderIntToFloatConverter;

public class LogarithmicDbFaderIntToFloatConverter implements SliderIntToFloatConverter
{
	private final float linearHighestDb;
	private final float linearLowestDb;
	private final float compressedHighestDb;
	private final float compressedLowestDb;

	private final int numTotalSteps;
	private final int numLinearSteps;
	private final int numCompressedSteps;

	private final float linearDynamicRange;
	private final float compressedDynamicRange;

	public LogarithmicDbFaderIntToFloatConverter( final float linearHighestDb,
			final float linearLowestDb,
			final float compressedHighestDb,
			final float compressedLowestDb )
	{
		this.linearHighestDb = linearHighestDb;
		this.linearLowestDb = linearLowestDb;
		this.compressedHighestDb = compressedHighestDb;
		this.compressedLowestDb = compressedLowestDb;

		this.linearDynamicRange = linearHighestDb - linearLowestDb;
		this.compressedDynamicRange = compressedHighestDb - compressedLowestDb;

		// Use less steps per db in compressed than
		// for the non-compression section
		numCompressedSteps = ((int)compressedDynamicRange) * 2;
		numLinearSteps = ((int)linearDynamicRange) * 4;

		// Add an extra step to represent -INF
		numTotalSteps = numLinearSteps + numCompressedSteps + 1;
	}

	@Override
	public int floatValueToSliderIntValue( final SliderDisplayModel sdm, final float inValue )
	{
		return toSliderIntFromDb( inValue );
	}

	@Override
	public float sliderIntValueToFloatValue( final SliderDisplayModel sdm, final int sliderIntValue )
	{
		return toDbFromSliderInt( sliderIntValue );
	}


	public float toDbFromSliderInt( final int sliderIntValue )
	{
		if( sliderIntValue <= 0 )
		{
			return Float.NEGATIVE_INFINITY;
		}
		else
		{
			// Take off one step for -INF
			int scaledIntVal = sliderIntValue - 1;

			if( scaledIntVal < numCompressedSteps )
			{
				// Map to compressed values
				return compressedLowestDb + ((float)scaledIntVal / numCompressedSteps) * compressedDynamicRange;
			}
			else
			{
				// Take off num compressed values, and map to linear values
				scaledIntVal -= numCompressedSteps;
				final float outputDb = linearLowestDb + ( ((float)scaledIntVal / numLinearSteps) * linearDynamicRange);
				return outputDb;
			}
		}
	}

	public int toSliderIntFromDb( final float db )
	{
		if( db == Float.NEGATIVE_INFINITY )
		{
			return 0;
		}
		else if( db < compressedHighestDb )
		{
			// How much over the lowest db value is it?
			final float amountOverLowest = db - compressedLowestDb;
			if( amountOverLowest < 0.0f )
			{
				return 0;
			}
			else
			{
				final float scaledValue = amountOverLowest / compressedDynamicRange;
				final float linearStepValue = Math.round( scaledValue * numCompressedSteps);
	//			log.debug("Return an int of " + MathFormatter.fastFloatPrint( linearStepValue, 5, true ) );
				return (int)linearStepValue + 1;
			}
		}
		else
		{
			// How much over the lowest linear db value is it?
			final float amountOverLowest = db - linearLowestDb;
			final float scaledValue = amountOverLowest / linearDynamicRange;
			final float linearStepValue = Math.round(scaledValue * numLinearSteps);
//			log.debug("Return an int of " + MathFormatter.fastFloatPrint( linearStepValue, 5, true ) );
			return 1 + numCompressedSteps + (int)linearStepValue;
		}
	}

	public int getNumTotalSteps()
	{
		return numTotalSteps;
	}

	public int getNumLinearSteps()
	{
		return numLinearSteps;
	}

	public int getNumCompressedSteps()
	{
		return numCompressedSteps;
	}

	public float getLinearDynamicRange()
	{
		return linearDynamicRange;
	}

	public float getCompressedDynamicRange()
	{
		return compressedDynamicRange;
	}

	public float getLinearHighestDb()
	{
		return linearHighestDb;
	}

	public float getLinearLowestDb()
	{
		return linearLowestDb;
	}

	public float getCompressedHighestDb()
	{
		return compressedHighestDb;
	}

	public float getCompressedLowestDb()
	{
		return compressedLowestDb;
	}

}
