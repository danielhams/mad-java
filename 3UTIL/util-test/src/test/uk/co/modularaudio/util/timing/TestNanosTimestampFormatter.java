/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package test.uk.co.modularaudio.util.timing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.timing.NanosTimestampFormatter;

public class TestNanosTimestampFormatter
{
	private static Log log = LogFactory.getLog( TestNanosTimestampFormatter.class.getName() );

	@Test
	public void test()
	{
		final long zeros = 0;
		final String zerosString = NanosTimestampFormatter.formatTimestampForLogging( zeros, true );

		log.trace( "For zeros got the string '" + zerosString + "'" );

		final long timestampValue = System.nanoTime();
		final String timestampString = NanosTimestampFormatter.formatTimestampForLogging( timestampValue, true );

		log.trace( "For the value " + timestampValue + " got '" + timestampString + "'" );


	}

}
