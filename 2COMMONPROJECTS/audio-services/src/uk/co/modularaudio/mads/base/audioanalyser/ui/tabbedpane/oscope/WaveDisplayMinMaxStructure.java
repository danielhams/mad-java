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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;


public class WaveDisplayMinMaxStructure
{
	public float minLowValue;
	public float maxLowValue;
	public float previousMinLowValue;
	public float previousMaxLowValue;
	public float minMidValue;
	public float maxMidValue;
	public float previousMinMidValue;
	public float previousMaxMidValue;
	public float minHiValue;
	public float maxHiValue;
	public float previousMinHiValue;
	public float previousMaxHiValue;

	public WaveDisplayMinMaxStructure()
	{
		resetAll();
	}

	public final void resetAll()
	{
		resetMinMax();
		resetPrevious();
	}

	public final void resetMinMax()
	{
		minLowValue = Float.MAX_VALUE;
		maxLowValue = -minLowValue;

		minMidValue = Float.MAX_VALUE;
		maxMidValue = -minMidValue;

		minHiValue = Float.MAX_VALUE;
		maxHiValue = -minHiValue;
	}

	public final void resetPrevious()
	{
		previousMinLowValue = 0.0f;
		previousMaxLowValue = 0.0f;

		previousMinMidValue = 0.0f;
		previousMaxMidValue = 0.0f;

		previousMinHiValue = 0.0f;
		previousMaxHiValue = 0.0f;
	}

	public void moveToNext()
	{
		previousMinLowValue = minLowValue;
		previousMaxLowValue = maxLowValue;

		previousMinMidValue = minMidValue;
		previousMaxMidValue = maxMidValue;

		previousMinHiValue = minHiValue;
		previousMaxHiValue = maxHiValue;

	}

	public void extendWithPrevious()
	{
		if( previousMaxLowValue < minLowValue )
		{
			minLowValue = previousMaxLowValue;
		}
		if( previousMinLowValue > maxLowValue )
		{
			maxLowValue = previousMinLowValue;
		}
		if( previousMaxMidValue < minMidValue )
		{
			minMidValue = previousMaxMidValue;
		}
		if( previousMinMidValue > maxMidValue )
		{
			maxMidValue = previousMinMidValue;
		}
		if( previousMaxHiValue < minHiValue )
		{
			minHiValue = previousMaxHiValue;
		}
		if( previousMinHiValue > maxHiValue )
		{
			maxHiValue = previousMinHiValue;
		}
	}

	public void processSample( final float lowSample, final float midSample, final float hiSample )
	{
		if( lowSample < minLowValue )
		{
			minLowValue = lowSample;
		}
		if( lowSample > maxLowValue )
		{
			maxLowValue = lowSample;
		}
		if( midSample < minMidValue )
		{
			minMidValue = midSample;
		}
		if( midSample > maxMidValue )
		{
			maxMidValue = midSample;
		}
		if( hiSample < minHiValue )
		{
			minHiValue = hiSample;
		}
		if( hiSample > maxHiValue )
		{
			maxHiValue = hiSample;
		}
	}
}
