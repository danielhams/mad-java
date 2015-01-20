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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenerateTimestampFormatterLookupTable
{
	private static Log log = LogFactory.getLog( GenerateTimestampFormatterLookupTable.class.getName() );

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		StringBuilder sb = new StringBuilder();
		sb.append("const char ** leadingZeroLookup = { " );
		for( int i = 0 ; i < 62 ; ++i )
		{
			if( i > 0 )
			{
				sb.append(", " );
			}
			sb.append( "\"" + i + "\"" );
		}
		System.out.println( sb.toString() );

		int year = 2013;
		int thousands = year / 1000;
		int hundreds = (year % 1000) / 100;
		int tens = (year % 100) / 10;
		int units = year % 10;
		
		log.debug("So for " + year + " I get thos(" + thousands + ") hun(" + hundreds + ") ten(" + tens + ") units(" + units + ")");
	}

}
