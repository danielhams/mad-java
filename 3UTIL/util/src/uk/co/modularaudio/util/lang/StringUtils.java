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

package uk.co.modularaudio.util.lang;

public class StringUtils
{
	public final static String ucInitialChar( final String inputString )
	{
		final StringBuilder retVal = new StringBuilder( inputString.length() );
		retVal.append( inputString.substring( 0, 1 ).toUpperCase() );
		retVal.append( inputString.substring( 1 ) );
		return retVal.toString();
	}

	public final static void appendFormattedInt( final StringBuilder sb, final int numSigFigures, final int val )
	{
		int iVal = (val < 0 ? -val : val);
		final char[] tmpChars = new char[numSigFigures];
		for( int i = 0 ; i < numSigFigures ; ++i )
		{
			final int fig = iVal % 10;
			tmpChars[i] = (char)('0' + fig);
			iVal = iVal / 10;
		}
		ArrayUtils.reverse( tmpChars );
		if( val < 0 )
		{
			tmpChars[0] = '-';
		}
		sb.append( tmpChars );
	}

	public final static void appendFormattedLong( final StringBuilder sb, final int numSigFigures, final long val )
	{
		long iVal = (val < 0 ? -val : val );
		final char[] tmpChars = new char[numSigFigures];
		for( int i = 0 ; i < numSigFigures ; ++i )
		{
			final int fig = (int)(iVal % 10);
			tmpChars[i] = (char)('0' + fig);
			iVal = iVal / 10;
		}
		ArrayUtils.reverse( tmpChars );
		if( val < 0 )
		{
			tmpChars[0] = '-';
		}
		sb.append( tmpChars );
	}

	public static boolean isEmpty( final String v )
	{
		return v == null || v.length() == 0;
	}
}
