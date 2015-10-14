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

package test.uk.co.modularaudio.util.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntLongHashMap;
import org.junit.Test;

import uk.co.modularaudio.util.math.Float16;
import uk.co.modularaudio.util.math.MathFormatter;

public class Float16Tester
{
	private static Log log = LogFactory.getLog( Float16Tester.class.getName() );

	@Test
	public void testFromFloat()
	{
		final float[] testFloats = new float[]
		{
			Float16.MAX_VALUE,
			Float16.MIN_VALUE,

			// Interesting values from an audio perspective
			-1.0f,
			-0.001f,
			-0.0001f,
			0.0f,
			0.0001f,
			0.001f,
			1.0f,
			-1.1f,
			-0.9f,
			0.9f,
			1.1f,

			// Some "steady" values
			0.1f,
			0.125f,
			0.2f,
			0.25f,
			0.3f,
			0.4f,
			0.5f,
			0.6f,
			0.7f,
			0.75f,
			0.8f,
			0.825f,

			// And some values to examine precision
			192000.0f,
			41000.0f,
			22050.0f,
			Float.NaN,
			Float.NEGATIVE_INFINITY,
			Float.POSITIVE_INFINITY,
		};

		for( final float testFloat : testFloats )
		{
			final Float16 f16 = new Float16( testFloat );

			final float andBack = f16.asFloat();

			log.debug( "OF(" +
					MathFormatter.slowFloatPrint( testFloat, 16, true ) +
					") F16(" +
					MathFormatter.slowFloatPrint( andBack, 16, true ) +
					")");
		}
	}

	@Test
	public void testHaveEnoughPrecision()
	{
		final int f16One = Float16.fromFloat( 1.0f );

		final float teensyFloat = 1.0f / 1000000.0f;

		int testf16;
		float curTestFloat = 1.0f;
		do
		{
			curTestFloat -= teensyFloat;
			testf16 = Float16.fromFloat( curTestFloat );
		}
		while( testf16 == f16One );

		final float nextLargestToOne = Float16.fromInt( testf16 );
		log.debug( "The next largest f16 near to 1.0f16 is " +
				MathFormatter.slowFloatPrint( nextLargestToOne, 12, true ) );

		final float minDiffNearOne = 1.0f - nextLargestToOne;
		log.debug("This is a diff of " +
				MathFormatter.slowFloatPrint( minDiffNearOne, 12, true ) );

		final OpenIntLongHashMap f16ToCountMap = new OpenIntLongHashMap( 50000 );

		final double delta = minDiffNearOne;

		double valueToCheck = 1.0;

		while( valueToCheck >= 0.0 )
		{
			final int f16 = Float16.fromFloat( (float)valueToCheck );
			final long curValue = f16ToCountMap.get( f16 );
			f16ToCountMap.put( f16, curValue+1 );

			valueToCheck -= delta;
		}
		final int f16Zero = Float16.fromFloat( 0.0f );
		final long curValue = f16ToCountMap.get( f16Zero );
		f16ToCountMap.put( f16Zero, curValue+1 );

		final IntArrayList keyList = f16ToCountMap.keys();

		final int numUniqueValues = keyList.size();
		log.debug( "Have " + numUniqueValues + " equidistant unique values between 0.0f16 and 1.0f16" );
		for( int i = 0 ; i < numUniqueValues ; ++i )
		{
			final int value = keyList.get( i );
			final float f16AsFloat = Float16.fromInt( value );
			final long count = f16ToCountMap.get( value );

			final boolean shouldPrint = (i < 10 || i > (numUniqueValues-10));
//			final boolean shouldPrint = count > 1;

			if( shouldPrint )
			{
				log.debug("For key f16(" +
						MathFormatter.slowFloatPrint( f16AsFloat, 12, true ) +
						") numHits(" +
						count +
						")");
			}
		}
	}

}
