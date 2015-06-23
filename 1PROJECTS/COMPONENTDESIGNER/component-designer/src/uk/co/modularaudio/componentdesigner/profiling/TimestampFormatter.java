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

package uk.co.modularaudio.componentdesigner.profiling;

public class TimestampFormatter
{
	private final static int NUM_CHARS_FOR_NANOS_FORMATTING = 2 + 1 + 3 + 1 + 3 + 1 + 3;

	public static String formatNanos( final long nanos )
	{
		final char output[] = new char[NUM_CHARS_FOR_NANOS_FORMATTING];

		long residual = nanos;
		int currentChar = 0;

		for( int i = 0 ; i < 3 ; ++i )
		{

			for( int d = 0 ; d < 3 ; ++d )
			{
				final int remainder = (int)residual % 10;
				output[currentChar++] = (char)('0' + remainder);
				residual /= 10;
			}
			output[currentChar++] = '.';
		}

		int remainder = (int)residual % 10;
		output[currentChar++] = (char)('0' + remainder);
		residual /= 10;
		remainder = (int)residual % 10;
		output[currentChar++] = (char)('0' + remainder);

		// Now reverse it
		final StringBuilder sb = new StringBuilder();
		for( int i = NUM_CHARS_FOR_NANOS_FORMATTING - 1 ; i >= 0 ; --i )
		{
			sb.append( output[i] );
		}

		return sb.toString();
	}

}
