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

public class LogarithmicDbAmpScaleComputer implements AmpScaleComputer
{
//	private static Log log = LogFactory.getLog( LogarithmicAmpScaleComputer.class.getName() );

	private final static float SCALE_FACTOR = 9.08f;

//	public final static float MIN_DB_VALUE = -80.0f;
//	public final static float MIN_DB_VALUE = -96.0f;
//	public final static float MIN_DB_VALUE = -100.0f;
	public final static float MIN_DB_VALUE = -120.0f;
	public final static float MIN_RAW_VALUE = AudioMath.dbToLevelF( MIN_DB_VALUE );

	@Override
	public float scaleIt(final float valForBin)
	{
		float val = (valForBin * 10.0f) + 1.0f;
		val = (float)Math.log( val );
		val = val / SCALE_FACTOR;
		return val;
	}

	private int numBuckets = 199;
	private float minDb = -96.0f;
	private float minValue = AudioMath.dbToLevelF( minDb );
	private float maxDb = 0.0f;
	private float maxValue = AudioMath.dbToLevelF( maxDb );

	private float rangeDb = maxDb - minDb;

	@Override
	public void setParameters( final int numBuckets, final float minValueDb, final float maxValueDb )
	{
		this.numBuckets = numBuckets;
		this.minDb = minValueDb;
		minValue = AudioMath.dbToLevelF( minDb );
		this.maxDb = maxValueDb;
		maxValue = AudioMath.dbToLevelF( maxDb );
		this.rangeDb = maxDb - minDb;
	}

	@Override
	public final int rawToMappedBucket( final float rawValue )
	{
		if( rawValue >= maxValue )
		{
			return numBuckets;
		}
		else if( rawValue <= minValue )
		{
			return 0;
		}
		else
		{
			final float asDb = AudioMath.levelToDbF( rawValue );
			final float normalisedValue = (asDb - minDb) / rangeDb;

			return (int)( (normalisedValue * numBuckets) + 0.5f);
		}
	}

	@Override
	public final float mappedBucketToRaw( final int bucket )
	{
		final float normalisedValue = bucket / (float)numBuckets;

		final float dbValueInBucket = minDb + (normalisedValue * rangeDb);
		final float finalValue = AudioMath.dbToLevelF( dbValueInBucket );
		return finalValue;
	}
}
