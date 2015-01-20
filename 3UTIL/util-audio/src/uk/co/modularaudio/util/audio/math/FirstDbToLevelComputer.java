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
public class FirstDbToLevelComputer implements DbToLevelComputer
{
	public static final int LOWEST_NEGATIVE_DB = 50;
	public static final int MAX_OVERAMP_DB = 6;

	private static Log log = LogFactory.getLog( FirstDbToLevelComputer.class.getName() );
	
	private int numTotalSteps = -1;
	private int numScaledSteps = -1;
	
	private float totalDynamicRange = 0.0f;
	
	private float upperRangeBound = 0.0f;
	
	private float dbPerStep = 0.0f;
	private float stepsPerDb = 0.0f;
	private float zeroDBLevel = 0.0f;
	
	public FirstDbToLevelComputer( int numTotalSteps )
	{
		// We assume a required DB range from:
		// -INF, -96DB -> 0 -> +9
		this.totalDynamicRange = LOWEST_NEGATIVE_DB + MAX_OVERAMP_DB;
		
		this.numTotalSteps = numTotalSteps;
		this.numScaledSteps = numTotalSteps - 1;
		
		// We will be running from 0 -> 1 in amplitude,
		// but will reserve the bottom step for -INF on the DB scale
		upperRangeBound = numScaledSteps / totalDynamicRange;
		
		dbPerStep = totalDynamicRange / numScaledSteps;
		stepsPerDb = 1.0f / dbPerStep;
		
		zeroDBLevel = numTotalSteps - (stepsPerDb * MAX_OVERAMP_DB);
	}

	public float toDbFromNormalisedLevel( float level )
	{
		// Slider is from 0 -> 1
		// special case 0
		if( level == 0.0f )
		{
			return Float.NEGATIVE_INFINITY;
		}
		else
		{
			return (level * totalDynamicRange) - LOWEST_NEGATIVE_DB;
		}
	}
	
	public float toNormalisedSliderLevelFromDb( float db )
	{
		if( db == Float.NEGATIVE_INFINITY )
		{
			return 0.0f;
		}
		else
		{
			return (db + LOWEST_NEGATIVE_DB) / totalDynamicRange;
		}
	}
	
	
}
