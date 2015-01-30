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

package uk.co.modularaudio.util.audio.format;


public enum DataRate
{
	SR_1024, SR_22050, SR_32000, SR_44100, SR_48000, SR_88200, SR_96000, SR_192000;

	public final static DataRate CD_QUALITY = SR_44100;

	public int getValue()
	{
		switch( this )
		{
		case SR_1024:
			return 1024;
		case SR_22050:
			return 22050;
		case SR_32000:
			return 32000;
		case SR_44100:
			return 44100;
		case SR_48000:
			return 48000;
		case SR_88200:
			return 88200;
		case SR_96000:
			return 96000;
		case SR_192000:
			return 192000;
		default:
			return -1;
		}
	}

	// Calculated from sample rate and buffer size
	public double calculateLatency(final int bufferSize)
	{
		final double retVal = (double)bufferSize / (double)this.getValue();
		return retVal;
	}

	// Calculate a rounded integer number of samples for a number of milliseconds
	public int calculateSamplesForLatency( final int millis )
	{
		final int numSamplesPerSecond = this.getValue();
		final float numSamplesPerMilli = numSamplesPerSecond / 1000.0f;
		final float numSamplesForFadeOut = numSamplesPerMilli * millis;
		return Math.round( numSamplesForFadeOut );
	}

	public static DataRate fromFrequency( final int outputFrequency ) throws UnknownDataRateException
	{
		DataRate retVal = null;

		final DataRate values[] = DataRate.values();

		for( int i = 0 ; i < values.length ; i++ )
		{
			final DataRate toTest = values[i];
			final String asString = toTest.toString();
			final String testString = "SR_" + outputFrequency;
			if( asString.equals( testString ) )
			{
				retVal = toTest;
			}
		}

		if( retVal == null )
		{
			// Didn't find it
			final String msg = "Unable to translate frequency " + outputFrequency + " into a known sample rate.";
			throw new UnknownDataRateException( msg );
		}
		return retVal;
	}

	public int calculateUsecLatencyForFrames( final int numFrames )
	{
		final int samplesPerSecond = getValue();
		final double samplesPerMicrosecond = samplesPerSecond / 1000000.0;
		final double microSecondsPerSample = 1.0 / samplesPerMicrosecond;
		return (int)Math.round( microSecondsPerSample * numFrames );
	}

}
