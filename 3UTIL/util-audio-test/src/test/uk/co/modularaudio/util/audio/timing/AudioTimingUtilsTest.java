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

package test.uk.co.modularaudio.util.audio.timing;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.math.MathFormatter;

public class AudioTimingUtilsTest extends TestCase
{
	private static Log log = LogFactory.getLog( AudioTimingUtilsTest.class.getName() );

	private static final float MAX_ROUNDTRIP_DIFF_MILLIS = 0.01f;

	public AudioTimingUtilsTest()
	{
	}

	public void testTimingMillisRoundtrip() throws Exception
	{
		final DataRate dataRate = DataRate.CD_QUALITY;
		final int sampleRate = dataRate.getValue();
		final float[] testMilliFloats = new float[] {
			1.0f,
			0.72389f,
			10.0f,
			20.0f,
			123.11265f,
			1.9f,
			0.1f
		};

		for( final float testMillis : testMilliFloats )
		{
			final float numSamplesFloat = AudioTimingUtils.getNumSamplesFloatForMillisAtSampleRate( sampleRate, testMillis );
			final int numSamples = (int)numSamplesFloat;
			final long andBackNanos = AudioTimingUtils.getNumNanosecondsForBufferLengthFloat( sampleRate, numSamplesFloat );
			final float andBackMillis = (float)(andBackNanos / 1000000.0);

			final float roundtripDiff = Math.abs(andBackMillis - testMillis);

			if(! (roundtripDiff < MAX_ROUNDTRIP_DIFF_MILLIS ) )
			{
				log.error("Failed round trip of " + testMillis + " with diff " +
						MathFormatter.slowFloatPrint( roundtripDiff, 12, true ) );
			}

			assertTrue( roundtripDiff < MAX_ROUNDTRIP_DIFF_MILLIS );

			final float andBackToSamplesFloat = AudioTimingUtils.getNumSamplesFloatForNanosAtSampleRate( sampleRate, andBackNanos );
			final int andBackToSamplesInt = (int)andBackToSamplesFloat;

			if( andBackToSamplesInt != numSamples )
			{
				log.error("Failed round trip nanos to samples of " + testMillis + " with back to samples " +
						andBackToSamplesInt + " expected " + numSamples );
			}

			assertTrue( andBackToSamplesInt == numSamples );
		}
	}
}
