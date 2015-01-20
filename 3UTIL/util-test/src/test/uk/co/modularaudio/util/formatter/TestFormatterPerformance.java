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

import uk.co.modularaudio.util.math.MathFormatter;

public class TestFormatterPerformance
{
	private static Log log = LogFactory.getLog( TestFormatterPerformance.class.getName() );

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		float[] testVals = { -13456.455734573457f, 123.634634f, 0.0f, 123f};
		
		for( float val : testVals )
		{
			String sval = MathFormatter.slowFloatPrint( val, 3, true );
			log.debug("There we go - for " + val + " got : " + sval );
			sval = MathFormatter.fastFloatPrint( val, 3, true );
			log.debug("New version - for " + val + " got : " + sval );
		}
		
		doIterationTests();
		doIterationTests();

	}

	@SuppressWarnings("unused")
	private static void doIterationTests()
	{
		long timeBefore = System.nanoTime();
		int NUM_ITERS = 100000;
		for( int i = 0 ; i < NUM_ITERS ; i++ )
		{
			String thing = MathFormatter.slowFloatPrint( 1.642673f, 5, true );
		}
		long timeAfter = System.nanoTime();
		long diff = timeAfter - timeBefore;
		long numPerIter = diff / NUM_ITERS;
		log.debug( "For java formatter, did it in " + diff + " which is per iter " + numPerIter );

		timeBefore = System.nanoTime();
		for( int i = 0 ; i < NUM_ITERS ; i++ )
		{
			String thing = MathFormatter.fastFloatPrint( 1.642673f, 5, true );
		}
		timeAfter = System.nanoTime();
		diff = timeAfter - timeBefore;
		numPerIter = diff / NUM_ITERS;
		log.debug( "For new formatter, did it in " + diff + " which is per iter " + numPerIter );
	}

}
