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

package uk.co.modularaudio.util.audio.spectraldisplay.ampscale;

import uk.co.modularaudio.util.audio.math.AudioMath;


public class LinearAmpScaleComputer implements AmpScaleComputer
{
//	private static Log log = LogFactory.getLog( LinearAmpScaleComputer.class.getName() );

	@Override
	public float scaleIt(final float valForBin)
	{
		return valForBin / 500.0f;
	}

	private int lastBucketIndex = 199;

	private float minDb = -96.0f;
	private float minValue = AudioMath.dbToLevelF( minDb );
	private float maxDb = 0.0f;
	private float maxValue = AudioMath.dbToLevelF( maxDb );

	private float rangeValue = maxValue - minValue;

	@Override
	public void setMinMaxDb( final float minValueDb, final float maxValueDb )
	{
		this.minDb = minValueDb;
		minValue = AudioMath.dbToLevelF( minDb );
		this.maxDb = maxValueDb;
		maxValue = AudioMath.dbToLevelF( maxDb );
		rangeValue = maxValue - minValue;
	}

	@Override
	public int rawToMappedBucketMinMax( final int numBuckets, final float iRawValue )
	{
		float rawValue = (iRawValue < minValue ? 0.0f : iRawValue - minValue );
		rawValue = (rawValue >= rangeValue ? 1.0f : (rawValue / rangeValue ));
		return (int)(((numBuckets - 1) * rawValue) + 0.5f);
	}

	@Override
	public float mappedBucketToRawMinMax( final int numBuckets, final int bucket )
	{
		final float normalisedBucketNum = bucket / (float)(numBuckets - 1);
		return minValue + (normalisedBucketNum * rangeValue);
	}

	@Override
	public void setParameters( final int numBuckets, final float minValueDb, final float maxValueDb )
	{
		lastBucketIndex = numBuckets - 1;
		this.minDb = minValueDb;
		minValue = AudioMath.dbToLevelF( minDb );
		this.maxDb = maxValueDb;
		maxValue = AudioMath.dbToLevelF( maxDb );
		rangeValue = maxValue - minValue;
	}

	@Override
	public final int rawToMappedBucket( final float iRawValue )
	{
		float rawValue = (iRawValue < minValue ? 0.0f : iRawValue - minValue );
		rawValue = (rawValue >= rangeValue ? 1.0f : (rawValue / rangeValue ));
		return (int)((lastBucketIndex * rawValue) + 0.5f);
	}

	@Override
	public final float mappedBucketToRaw( final int bucket )
	{
		final float normalisedBucketNum = bucket / (float)lastBucketIndex;
		return minValue + (normalisedBucketNum * rangeValue);
	}

}
