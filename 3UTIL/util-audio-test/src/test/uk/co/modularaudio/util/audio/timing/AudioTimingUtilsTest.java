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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class AudioTimingUtilsTest
{
	private static Log log = LogFactory.getLog( AudioTimingUtilsTest.class.getName() );

	public AudioTimingUtilsTest()
	{
	}

	public void doit()
		throws Exception
	{
		log.debug("Doing timing tests.");
		final float newRatio22050And10 = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion(  22050, 10 );
		log.debug("For 22050 and 10 it is " + newRatio22050And10 );
		final float newRatio44100And20 = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion(  44100, 20 );
		log.debug("For 44100 and 20 it is " + newRatio44100And20 );
		final float newRatio44100And5 = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion(  44100, 5 );
		log.debug("For 44100 and 5 it is " + newRatio44100And5 );

		testOne( 44100, 10 );
		testOne( 44100, 20 );
		testOne( 44100, 40 );
		testOne( 22050, 10 );
		testOne( 22050, 20 );
		testOne( 22050, 40 );
		testOne( 11025, 10 );
		testOne( 11025, 20 );
		testOne( 11025, 40 );
	}

	public void testOne( final int sampleRate, final int millis )
	{
		final float testValue= AudioTimingUtils.calculateNewValueRatioHandwaveyVersion(  sampleRate, millis );
		final float oldRatio = 1.0f - testValue;
		float value = 1.0f;
		int numSamplesToHalf = 0;

		while( value >= 0.5f )
		{
			value = value * oldRatio;
			numSamplesToHalf++;
		}
		log.debug("The number of samples to half at " + sampleRate +" and " + millis + "ms is " + numSamplesToHalf );
		final int numSamplesPerMilli = sampleRate / 1000;
		final float numMillisToHalf = numSamplesToHalf / (float)numSamplesPerMilli;
		log.debug("This is " + numMillisToHalf + " milliseconds to half");
		final int numSamplesForMillisAtSampleRate = sampleRate / 1000 * millis;
		log.debug("And " + millis + "ms at " + sampleRate + " is " + numSamplesForMillisAtSampleRate + " samples");
		value = 1.0f;
		for( int i = 0 ; i < numSamplesForMillisAtSampleRate ; i++ )
		{
			value = value * oldRatio;
		}
		log.debug("And after this many samples, the value is " + value );
	}

	/**
	 * @param args
	 */
	public static void main( final String[] args )
		throws Exception
	{
		final AudioTimingUtilsTest atut = new AudioTimingUtilsTest();
		atut.doit();
	}

}
