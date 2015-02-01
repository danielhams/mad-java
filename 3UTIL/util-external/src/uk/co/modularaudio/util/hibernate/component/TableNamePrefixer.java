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

package uk.co.modularaudio.util.hibernate.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableNamePrefixer
{
	private final static Pattern TABLE_DEF_PATTERN;

	private final static Pattern SEQUENCE_DEF_PATTERN;

	static
	{
		TABLE_DEF_PATTERN = Pattern.compile( "\\stable=\\\"([^\\\"]+)\\\"" );
		SEQUENCE_DEF_PATTERN = Pattern.compile( "<param name=\\\"sequence\\\">([^<]+)</param>" );
	}

	public static String prefixTableNames( final String originalHbmString, final String persistedBeanTablePrefix )
	{
		// If the table prefix is filled in,
		// Find each occurence of table="SOMETHING"
		// and replace with table="PREFIX_SOMETHING"
		if (persistedBeanTablePrefix != null && !persistedBeanTablePrefix.equals( "" ))
		{
			Pattern patternToMatchAndPrefix = TABLE_DEF_PATTERN;

			final String tmpString = findAndPrefixPatternInString( originalHbmString, persistedBeanTablePrefix,
					patternToMatchAndPrefix, " table=\"", "\"" );

			patternToMatchAndPrefix = SEQUENCE_DEF_PATTERN;

			final String newHbmString = findAndPrefixPatternInString( tmpString, persistedBeanTablePrefix,
					patternToMatchAndPrefix, "<param name=\"sequence\">", "</param>" );

			return (newHbmString.toString());
		}
		else
		{
			return originalHbmString;
		}
	}

	protected static String findAndPrefixPatternInString( final String originalHbmString, final String persistedBeanTablePrefix,
			final Pattern patternToMatchAndPrefix, final String replacementPrefix, final String replacementPostfix )
	{
		final StringBuilder retVal = new StringBuilder( originalHbmString.length() );

		int curPos = 0;

		final Matcher matcher = patternToMatchAndPrefix.matcher( originalHbmString );
		while (matcher.find( curPos ))
		{
			// Found a match
			final int matchStart = matcher.start();
			final int matchEnd = matcher.end();

			final String originalTableName = matcher.group( 1 );

			// Copy up to match start into the output
			retVal.append( originalHbmString.substring( curPos, matchStart ) );

			// Now append the new table string
			retVal.append( replacementPrefix );
			retVal.append( persistedBeanTablePrefix );
			retVal.append( "_" );
			retVal.append( originalTableName );
			retVal.append( replacementPostfix );

			// Now move the curPos up to matchEnd
			curPos = matchEnd;
		}

		if (curPos != originalHbmString.length())
		{
			retVal.append( originalHbmString.substring( curPos ) );
		}
		return retVal.toString();
	}

}
