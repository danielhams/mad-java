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

package test.uk.co.modularaudio.util.audio.dsp;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.LimiterCrude;
import uk.co.modularaudio.util.audio.dsp.LimiterTanh;
import uk.co.modularaudio.util.audio.dsp.LimiterTanhApprox;
import uk.co.modularaudio.util.math.MathFormatter;
import uk.co.modularaudio.util.timing.TestTimer;

public class LimiterTest extends TestCase
{
	private static Log log = LogFactory.getLog( LimiterTest.class.getName() );

//	private static final float TEST_KNEE = 0.5f;
//	private static final float TEST_FALLOFF = 1.0f;
	private static final float TEST_KNEE = 0.98f;
	private static final float TEST_FALLOFF = 20.0f;

	private static final int NUM_PERF_VALUES = 8192 * 2;
//	private static final int NUM_PERF_LOOPS = 8192 * 10;
	private static final int NUM_PERF_LOOPS = 1;

	public LimiterTest()
	{
	}

	public void testLimits()
		throws Exception
	{
		final float[] sourceValues = {
				0.0f,
				0.01f,
				0.5f,
				0.9f,
				0.979f,
				0.98f,
				0.981f,
				0.982f,
				0.99999f,
				0.5f,
				0.6f,
				0.7f,
				0.8f,
				0.9f,
				0.99999f,
				1.0f,
				1.00001f,
				1.01f,
				2.0f,
				10.0f,
				100.0f,
				1000.0f,
				10000.0f,
				};

		final float[] testValues = new float[ sourceValues.length * 2];
		System.arraycopy( sourceValues, 0, testValues, 0, sourceValues.length );
		for( int s = 0 ; s < sourceValues.length ; ++s )
		{
			testValues[sourceValues.length + s] = -1 * testValues[s];
		}

		final float[] resultsCrude = new float[ testValues.length ];
		final float[] resultsTanhApprox = new float[ testValues.length ];
		final float[] resultsTanh = new float[ testValues.length ];

		System.arraycopy( testValues, 0, resultsCrude, 0, testValues.length );
		System.arraycopy( testValues, 0, resultsTanhApprox, 0, testValues.length );
		System.arraycopy( testValues, 0, resultsTanh, 0, testValues.length );

		final LimiterCrude limiterCrude = new LimiterCrude( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanhApprox limiterTanhApprox = new LimiterTanhApprox( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanh limiterTanh = new LimiterTanh( TEST_KNEE, TEST_FALLOFF );

		limiterCrude.filter( resultsCrude, 0, testValues.length );
		limiterTanhApprox.filter( resultsTanhApprox, 0, testValues.length );
		limiterTanh.filter( resultsTanh, 0, testValues.length );

		for( int i = 0 ; i < testValues.length ; ++i )
		{
			final float sourceVal = testValues[i];
			final float resultCrude = resultsCrude[i];
			final float resultTanhApprox = resultsTanhApprox[i];
			final float resultTanh = resultsTanh[i];

			assertTrue( boundaryCheck( resultCrude ) );
			assertTrue( boundaryCheck( resultTanhApprox ) );
			assertTrue( boundaryCheck( resultTanh ) );

			log.debug("Mapped value " +
					MathFormatter.slowFloatPrint( sourceVal, 18, true ) +
					" to C(" +
					MathFormatter.slowFloatPrint( resultCrude, 18, true ) +
					") TA(" +
					MathFormatter.slowFloatPrint( resultTanhApprox, 18, true ) +
					") T(" +
					MathFormatter.slowFloatPrint( resultTanh, 18, true ) +
					")");
		}
	}

	private boolean boundaryCheck( final float val )
	{
		final float absVal = Math.abs( val );
		return( absVal >= 0.0f && absVal <= 1.0f );
	}

	public void testLimiterPerformance()
			throws Exception
	{
		final float[] testValues = new float[ NUM_PERF_VALUES ];
		fillTestValues( testValues );

		final float[] crudeResults = new float[ NUM_PERF_VALUES ];
		System.arraycopy( testValues, 0, crudeResults, 0, NUM_PERF_VALUES );
		float crudeCum = 0.0f;
		final float[] taResults = new float[ NUM_PERF_VALUES ];
		System.arraycopy( testValues, 0, taResults, 0, NUM_PERF_VALUES );
		float taCum = 0.0f;
		final float[] tResults = new float[ NUM_PERF_VALUES ];
		System.arraycopy( testValues, 0, tResults, 0, NUM_PERF_VALUES );
		float tCum = 0.0f;

		final LimiterCrude limiterCrude = new LimiterCrude( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanhApprox limiterTanhApprox = new LimiterTanhApprox( TEST_KNEE, TEST_FALLOFF );
		final LimiterTanh limiterTanh = new LimiterTanh( TEST_KNEE, TEST_FALLOFF );

		final TestTimer tt = new TestTimer();
		tt.markBoundary( "Begin" );

		for( int l = 0 ; l < NUM_PERF_LOOPS ; ++l )
		{
			limiterCrude.filter( crudeResults, 0, NUM_PERF_VALUES );
			crudeCum += crudeResults[0];
		}
		tt.markBoundary( "Crude" );

		for( int l = 0 ; l < NUM_PERF_LOOPS ; ++l )
		{
			limiterTanhApprox.filter( taResults, 0, NUM_PERF_VALUES );
			taCum += taResults[0];
		}
		tt.markBoundary( "Tanh Approx" );

		for( int l = 0 ; l < NUM_PERF_LOOPS ; ++l )
		{
			limiterTanh.filter( tResults, 0, NUM_PERF_VALUES );
			tCum += tResults[0];
		}
		tt.markBoundary( "Tanh" );

		tt.logTimes( "Limiter performance tests", log );
		final StringBuilder dontOptResults = new StringBuilder();
		dontOptResults.append( Float.toString( crudeCum ) );
		dontOptResults.append( Float.toString( taCum ) );
		dontOptResults.append( Float.toString( tCum ) );
	}

	private void fillTestValues( final float[] testValues )
	{
		final int numTestValues = testValues.length;
		for( int i = 0 ; i < numTestValues ; ++i )
		{
			final double randValue = Math.random();
			final double val = (randValue * 2.0) - 1.0;
			testValues[i] = (float)val;
		}
	}
}
