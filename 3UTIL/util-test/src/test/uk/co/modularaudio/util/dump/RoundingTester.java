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

package test.uk.co.modularaudio.util.dump;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.math.MathFormatter;

public class RoundingTester extends TestCase
{
	private static Log log = LogFactory.getLog( RoundingTester.class.getName() );

	private void adjustVal( final float testVal )
	{
		log.debug( "############# Incoming float is " + MathFormatter.fastFloatPrint( testVal, 3, true ) );
		int asInt = (int)testVal;
		log.debug( "As int, it is " + asInt );
		if( asInt >= 0 )
		{
			log.debug( "Seeing if we move int up" );
			asInt += asInt & 1;
		}
		else
		{
			log.debug( "Seeing if we move int down" );
			asInt -= 1;
		}
		log.debug( "After int opt it is " + asInt );
		float adjustedVal = testVal - asInt;
		log.debug("Adjusted with that, we have " + MathFormatter.fastFloatPrint( adjustedVal, 3, true ) );
		if( adjustedVal < 0.0f )
		{
			log.debug("Adjusting up one.");
			adjustedVal += 1.0f;
		}
		log.debug("So final val is " + MathFormatter.fastFloatPrint( adjustedVal, 3, true ) );
	}

	public void testIntRounding() throws Exception
	{
		float testVal = -2.11f;
		final float incr = 0.1f;

		do
		{

			adjustVal( testVal );
			testVal += incr;
		}
		while( testVal < 2.1f );
	}
}
