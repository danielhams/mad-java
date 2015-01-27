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

package uk.co.modularaudio.util.audio.timing;


public class AudioTimingUtils
{
	private final static int HARDCODED_RATE = 44100;
	private final static int HARDCODED_MILLIS = 10;
	private final static float HARDCODED_NEW_RATIO = 0.01f;

	private final static long HERTZ_TO_NANOSECONDS_RATIO = 1000 * 1000 * 1000;

	public static float calculateNewValueRatioForMillisAtSampleRate( final long inSampleRate, final float millisForChase )
	{
		if( millisForChase <= 0.0f )
		{
			return 1.0f;
		}
		else
		{
			final float samplesPerMilli = inSampleRate / 1000.0f;
			final float samplesForChase = samplesPerMilli * millisForChase;
			final float ratio = 1.0f / samplesForChase;

			return ratio;
		}
	}

	public static float calculateNewValueRatioHandwaveyVersion( final long inSampleRate, final float millisForChase )
	{
		if( millisForChase <= 0.0f )
		{
			return 1.0f;
		}
		else
		{
			final float sampleRateRatio = HARDCODED_RATE / (float)inSampleRate;
			final float millisRatio = HARDCODED_MILLIS / millisForChase;
			final float retVal = (HARDCODED_NEW_RATIO * millisRatio ) * sampleRateRatio;

			return retVal;
		}
	}

	public static int getNumSamplesForMillisAtSampleRate( final int sampleRate, final float millis )
	{
		// Assume we have a high enough sample rate that this returns sensible things
		return (int)((sampleRate / 1000.0) * millis);
	}

	public static long getNumNanosecondsForBufferLength( final int sampleRate, final int hardwareBufferLength )
	{
		final double numNanosecondsPerSample = (HERTZ_TO_NANOSECONDS_RATIO / (double)sampleRate);
		return (long)(numNanosecondsPerSample * hardwareBufferLength);
	}

	public static int getNumSamplesForNanosAtSampleRate( final int sampleRate, final long nanos )
	{
		final float timeInMillis = nanos / 1000000.0f;
		return getNumSamplesForMillisAtSampleRate( sampleRate, timeInMillis );
	}

	public static String formatTimestampForLogging( final long rawNanosTimestamp )
	{
		final long nanosPart = rawNanosTimestamp % 1000;
		final long totalMicros = rawNanosTimestamp / 1000;
		final long microsPart = totalMicros % 1000;
		final long totalMillis = totalMicros /  1000;
		final long millisPart = totalMillis % 1000;
		final long totalSeconds = totalMillis / 1000;
		final long secondsPart = totalSeconds % 60;

		final StringBuilder sb = new StringBuilder( 3 + 3 + 3 + 3 + 2 );
		sb.append( String.format( "%02d", secondsPart ) );
		sb.append( "." );
		sb.append( String.format( "%03d", millisPart ) );
		sb.append( "." );
		sb.append( String.format( "%03d", microsPart ) );
		sb.append( "." );
		sb.append( String.format( "%03d", nanosPart ) );
		return sb.toString();
	}

	public static float nanosToMillisFloat(final long currentGuiTime)
	{
		return (currentGuiTime) / 1000000.0f;
	}
}
