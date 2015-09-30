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

package test.uk.co.modularaudio.util.audio.controlinterpolation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.LinearInterpolator;
import uk.co.modularaudio.util.math.MathFormatter;

public class LinearInterpolationTester
{
	private static Log log = LogFactory.getLog( LinearInterpolationTester.class.getName() );

	public LinearInterpolationTester()
	{
	}

	public void go() throws Exception
	{
		log.info( "Off we go" );

		final LinearInterpolator li = new LinearInterpolator();
		li.resetLowerUpperBounds( 0.0f, 1.0f );

		final int TEST_SAMPLE_RATE = 48000;
		final int TEST_PERIOD_LENGTH = 4;
		final int TEST_INTERPOLATION_LENGTH = 10;

		li.resetSampleRateAndPeriod( TEST_SAMPLE_RATE, TEST_PERIOD_LENGTH, TEST_INTERPOLATION_LENGTH );

		li.hardSetValue( 1.0f );
		li.notifyOfNewValue( 0.0f );

		final float[] results = new float[TEST_PERIOD_LENGTH];

		final int NUM_ITERATIONS = 6;

		for( int i = 0 ; i < NUM_ITERATIONS ; ++i )
		{
			li.generateControlValues( results, 0, TEST_PERIOD_LENGTH );
			log.info( "Last value in iteration " + i + " is " +
					MathFormatter.fastFloatPrint( results[TEST_PERIOD_LENGTH-1], 8, true ) );
		}

		li.notifyOfNewValue( 0.1f );

		for( int i = 0 ; i < NUM_ITERATIONS ; ++i )
		{
			li.generateControlValues( results, 0, TEST_PERIOD_LENGTH );
			log.info( "Last value in iteration " + i + " is " +
					MathFormatter.fastFloatPrint( results[TEST_PERIOD_LENGTH-1], 8, true ) );
		}

		li.notifyOfNewValue( 1.0f );

		for( int i = 0 ; i < NUM_ITERATIONS ; ++i )
		{
			li.generateControlValues( results, 0, TEST_PERIOD_LENGTH );
			log.info( "Last value in iteration " + i + " is " +
					MathFormatter.fastFloatPrint( results[TEST_PERIOD_LENGTH-1], 8, true ) );
		}

	}

	public static void main( final String[] args ) throws Exception
	{
		final LinearInterpolationTester t = new LinearInterpolationTester();
		t.go();
	}

}
