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

package uk.co.modularaudio.util.hibernate.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableNamePrefixer
{
	private static Pattern tableDefPattern = null;

	private static Pattern sequenceDefPattern = null;

	static
	{
		tableDefPattern = Pattern.compile("\\stable=\\\"([^\\\"]+)\\\"");
		sequenceDefPattern = Pattern.compile("<param name=\\\"sequence\\\">([^<]+)</param>");
	}

	public static String prefixTableNames(String originalHbmString, String persistedBeanTablePrefix)
	{
		// If the table prefix is filled in,
		// Find each occurence of table="SOMETHING"
		// and replace with table="PREFIX_SOMETHING"
		if (persistedBeanTablePrefix != null && !persistedBeanTablePrefix.equals(""))
		{
			Pattern patternToMatchAndPrefix = tableDefPattern;
			
			String tmpString = findAndPrefixPatternInString(originalHbmString, persistedBeanTablePrefix,
					patternToMatchAndPrefix,
					" table=\"",
					"\"");
			
			patternToMatchAndPrefix = sequenceDefPattern;
			
			String newHbmString = findAndPrefixPatternInString( tmpString, persistedBeanTablePrefix,
					patternToMatchAndPrefix,
					"<param name=\"sequence\">",
					"</param>");

			return (newHbmString.toString());
		}
		else
		{
			return originalHbmString;
		}
	}

	protected static String findAndPrefixPatternInString(String originalHbmString,
			String persistedBeanTablePrefix, 
			Pattern patternToMatchAndPrefix,
			String replacementPrefix,
			String replacementPostfix)
	{
		StringBuilder retVal = new StringBuilder(originalHbmString.length());

		int curPos = 0;

		Matcher matcher = patternToMatchAndPrefix.matcher(originalHbmString);
		while (matcher.find(curPos))
		{
			// Found a match
			int matchStart = matcher.start();
			int matchEnd = matcher.end();

			String originalTableName = matcher.group(1);

			// Copy up to match start into the output
			retVal.append(originalHbmString.substring(curPos, matchStart));

			// Now append the new table string
			retVal.append( replacementPrefix );
			retVal.append(persistedBeanTablePrefix);
			retVal.append("_");
			retVal.append(originalTableName);
			retVal.append( replacementPostfix );

			// Now move the curPos up to matchEnd
			curPos = matchEnd;
		}

		if (curPos != originalHbmString.length())
		{
			retVal.append(originalHbmString.substring(curPos));
		}
		return retVal.toString();
	}

}
