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
			0.00000123f,
			0.00009512f,
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

		for( int dec = 0 ; dec < 13 ; ++dec )
		{
			for( final float val : testVals )
			{
//				log.debug("Conversion tests of: " + MathFormatter.slowFloatPrint( val, dec, true) +
//						" with " + dec + " decimal places" );
				final String sval = MathFormatter.slowFloatPrint( val, dec, true );
				final String fval = MathFormatter.fastFloatPrint( val, dec, true );
//				log.debug("Old fast version: " + fval );
				String nfval = MathFormatter.newFastFloatPrint( val, dec, true );
//				log.debug("New fast version: " + nfval );

				if( !sval.equals( nfval ) )
				{
					log.error("Failed new fast format verification of " + val );
					nfval = MathFormatter.newFastFloatPrint( val, dec, true );
				}

				if( sval.equals( nfval ) && !fval.equals( nfval ) )
				{
					log.info( "New fast print worked where the old one failed:");
					log.info( "New(" + nfval + ")");
					log.info( "Old(" + fval + ")");
				}
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
		log.debug( "For fast formatter, did it in " + diff + " which is per iter " + numPerIter );

		timeBefore = System.nanoTime();
		for( int i = 0 ; i < NUM_ITERS ; i++ )
		{
			final String thing = MathFormatter.newFastFloatPrint( 1.642673f, 4, true );
		}
		timeAfter = System.nanoTime();
		diff = timeAfter - timeBefore;
		numPerIter = diff / NUM_ITERS;
		log.debug( "For new fast formatter, did it in " + diff + " which is per iter " + numPerIter );
}

}
