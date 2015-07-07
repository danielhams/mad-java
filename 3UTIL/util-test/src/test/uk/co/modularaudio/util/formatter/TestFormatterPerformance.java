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

package test.uk.co.modularaudio.util.formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.MathDefines;
import uk.co.modularaudio.util.math.MathFormatter;

public class TestFormatterPerformance
{
	private static Log log = LogFactory.getLog( TestFormatterPerformance.class.getName() );

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final float[] testVals =
		{
			-13456.455734573457f,
			123.634634f,
			0.0f,
			123f,
			299.9f,
			299.99f,
			299.999f,
			299.9999f,
			123.999f,
			129.999f,
			129.9999f,
			120.1f,
			120.01f,
			120.001f,
			120.0001f,
			120.00001f,
			-123f,
			-123.999f,
			-129.999f,
			-129.9999f,
			-120.0001f,
			1.642673f,
			MathDefines.TWO_PI_F
		};

		for( final float val : testVals )
		{
			final String sval = MathFormatter.slowFloatPrint( val, 3, true );
			log.debug("There we go - for " + val + " got : " + sval );
			String fval = MathFormatter.fastFloatPrint( val, 3, true );
			log.debug("New version - for " + val + " got : " + fval );

			if( !sval.equals( fval ) )
			{
				log.error("Failed format verification of " + val );
				fval = MathFormatter.fastFloatPrint( val, 3, true );
			}

		}

		for( final float val : testVals )
		{
			final String sval = MathFormatter.slowFloatPrint( val, 4, true );
			log.debug("There we go - for " + val + " got : " + sval );
			String fval = MathFormatter.fastFloatPrint( val, 4, true );
			log.debug("New version - for " + val + " got : " + fval );

			if( !sval.equals( fval ) )
			{
				log.error("Failed format verification of " + val );
				fval = MathFormatter.fastFloatPrint( val, 4, true );
			}

		}

		doIterationTests();
		doIterationTests();

	}

	@SuppressWarnings("unused")
	private static void doIterationTests()
	{
		long timeBefore = System.nanoTime();
		final int NUM_ITERS = 1000000;
		for( int i = 0 ; i < NUM_ITERS ; i++ )
		{
			final String thing = MathFormatter.slowFloatPrint( 1.642673f, 4, true );
		}
		long timeAfter = System.nanoTime();
		long diff = timeAfter - timeBefore;
		long numPerIter = diff / NUM_ITERS;
		log.debug( "For string.format, did it in " + diff + " which is per iter " + numPerIter );

		timeBefore = System.nanoTime();
		for( int i = 0 ; i < NUM_ITERS ; i++ )
		{
			final String thing = MathFormatter.fastFloatPrint( 1.642673f, 4, true );
		}
		timeAfter = System.nanoTime();
		diff = timeAfter - timeBefore;
		numPerIter = diff / NUM_ITERS;
		log.debug( "For new formatter, did it in " + diff + " which is per iter " + numPerIter );
	}

}
