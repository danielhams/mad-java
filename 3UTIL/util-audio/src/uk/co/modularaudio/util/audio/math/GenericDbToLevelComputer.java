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

package uk.co.modularaudio.util.audio.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("unused")
public class GenericDbToLevelComputer implements DbToLevelComputer
{
	private static Log log = LogFactory.getLog( GenericDbToLevelComputer.class.getName() );

	private float linearHighestDb = 0.0f;
	private float linearLowestDb = 0.0f;
	private float compressedHighestDb = 0.0f;
	private float compressedLowestDb = 0.0f;

	private int numTotalSteps = -1;
	private int numLinearSteps = -1;
	private int numCompressedSteps = -1;

	private float linearDynamicRange = 0.0f;
	private float compressedDynamicRange = 0.0f;

	public GenericDbToLevelComputer( final float linearHighestDb,
			final float linearLowestDb,
			final float compressedHighestDb,
			final float compressedLowestDb,
			final int numTotalSteps )
	{
		this.linearHighestDb = linearHighestDb;
		this.linearLowestDb = linearLowestDb;
		this.compressedHighestDb = compressedHighestDb;
		this.compressedLowestDb = compressedLowestDb;

		this.linearDynamicRange = linearHighestDb - linearLowestDb;
		this.compressedDynamicRange = compressedHighestDb - compressedLowestDb;

		this.numTotalSteps = numTotalSteps;
		this.numLinearSteps = (int)(0.7f * numTotalSteps);
		// Leave one last step for INF attenuation
		this.numCompressedSteps = numTotalSteps - numLinearSteps - 1;
	}

	@Override
	public float toDbFromNormalisedLevel( final float level )
	{
		// Slider is from 0 -> 1
		// special case 0
		if( level == 0.0f )
		{
			return Float.NEGATIVE_INFINITY;
		}
		else
		{
			// Take off one step for -INF
			int scaledIntVal = (int)(level * numTotalSteps) - 1;

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

	@Override
	public float toNormalisedSliderLevelFromDb( final float db )
	{
		if( db < compressedLowestDb )
		{
			return 0.0f;
		}
		else if( db < compressedHighestDb )
		{
			// How much over the lowest db value is it?
			final float amountOverLowest = db - compressedLowestDb;
			final float scaledValue = amountOverLowest / compressedDynamicRange;
			final float linearStepValue = scaledValue * numCompressedSteps;
			final float normalisedValue = linearStepValue / (numTotalSteps-1);
			return normalisedValue;
		}
		else
		{
			// How much over the lowest db value is it?
			final float amountOverLowest = db - linearLowestDb;
			final float scaledValue = amountOverLowest / linearDynamicRange;
			final float linearStepValue = scaledValue * numLinearSteps;
			final float normalisedValue = (linearStepValue + numCompressedSteps) / (numTotalSteps-1);
			return normalisedValue;
		}
	}

	public float toStepFromDb( final float db )
	{
		if( db < compressedLowestDb )
		{
			return 0.0f;
		}
		else if( db < compressedHighestDb )
		{
			// How much over the lowest db value is it?
			final float amountOverLowest = db - compressedLowestDb;
			final float scaledValue = amountOverLowest / compressedDynamicRange;
			return 1 + (scaledValue * numCompressedSteps);
		}
		else
		{
			// How much over the lowest db value is it?
			final float amountOverLowest = db - linearLowestDb;
			final float scaledValue = amountOverLowest / linearDynamicRange;
			return 1 + numCompressedSteps + (scaledValue * numLinearSteps);
		}
	}
}
