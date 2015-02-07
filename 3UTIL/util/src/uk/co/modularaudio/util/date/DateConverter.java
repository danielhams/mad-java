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

package uk.co.modularaudio.util.date;
import java.sql.Timestamp;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * @author dan
 *
 */
public class DateConverter
{

	// Standard format dates for to and from oracle.
	public final static String MA_ORACLE_DATE_TIME_FORMAT = "dd/MM/yyyy:HH:mm:ss";

	public final static String MA_USER_DATE_FORMAT = "dd/MM/yyyy";

	public final static String MA_USER_TIME_FORMAT = "HH:mm:ss";

	public final static String MA_USER_TIME_MILLIS_FORMAT = "HH:mm:ss:SSS";

	public final static String MA_USER_DATE_TIME_FORMAT =
		MA_USER_DATE_FORMAT + " " + MA_USER_TIME_FORMAT;

	public final static String MA_USER_DATE_TIME_MILLIS_FORMAT =
			MA_USER_DATE_FORMAT + " " + MA_USER_TIME_MILLIS_FORMAT;

    /**
	 * <P>Convert an incoming date string from the database to a java.util.Date object.</P>
	 * <P>If the supplied string is null or empty, null is returned.</P>
	 * @param fromDB String
	 * @return Date
	 */
	public static java.util.Date oracleDateTimeStrToJavaDate(final String fromDB)
	{
		if (fromDB != null && fromDB.length() > 0)
		{
			final SimpleDateFormat dFormatter = new SimpleDateFormat(MA_ORACLE_DATE_TIME_FORMAT);
			return(dFormatter.parse(fromDB,new ParsePosition(0)));
		}
		else
		{
			return(null);
		}
	}


	public static java.sql.Timestamp oracleDateTimeStrToJavaTimestamp( final String fromDB)
	{
		java.sql.Timestamp retVal = null;
		final java.util.Date tmpDate = oracleDateTimeStrToJavaDate( fromDB );
		if (tmpDate != null)
		{
			retVal = new java.sql.Timestamp( tmpDate.getTime() );
		}
		return(retVal);
	}


	/**
	 * <P>Convert a java.util.Date object into the textual string that must be passed
	 * to Oracle in PL/SQL etc.</P>
	 * @param inDate java.util.Date
	 * @return String
	 */
	public static String javaDateToOracleDateTimeStr(final java.util.Date inDate)
	{
		return(javaDateToCustomDateTimeStr(inDate, MA_ORACLE_DATE_TIME_FORMAT));
	}


	/**
	 * <P>Turn a user entered date into the appropriate string for passing to the database.</P>
	 * <P>If the supplied string is null or empty, this function returns an empty string.</P>
	 * @param inDateStr User String
	 * @return String
	 */
	public static String userDateTimeStrToOracleDateTimeStr( final String inDateStr)
	{
		String retString = "";

		// First get it in Java format (if not null)
		if (inDateStr != null && inDateStr.length() > 0)
		{
			final java.util.Date jDate = userDateTimeStrToJavaDate( inDateStr );
			// Now convert this to DB format
			retString = javaDateToOracleDateTimeStr( jDate );
		}
		return(retString);
	}


	/**
	 * <P>Convert a date time string entered by a user into a java date.</P>
	 * <P>No validation is performed here, it is assumed to be in the correct format.</P>
	 * <P>null is returned if the string is invalid or badly formatted.</P>
	 * @param userDateTimeStr String
	 * @return Date
	 */
	public static java.util.Date userDateTimeStrToJavaDate( final String userDateTimeStr)
	{
		return(customDateTimeStrToJavaDate( userDateTimeStr, MA_USER_DATE_TIME_FORMAT));
	}


	/**
	 * <P>Convert a date time string entered by the user into the date time string required for
	 * passing to Oracle.</P>
	 * <P>No validation is performed here.</P>
	 * <P>If parsing fails, or no string is supplied, an empty string is returned.</P>
	 * @param inDateStr String
	 * @return String
	 */
	public static String userDateStrToOracleDateTimeStr( final String inDateStr)
	{
		String retString = "";

		// First get it in Java format (if not null)
		if (inDateStr != null && inDateStr.length() > 0)
		{
			final java.util.Date jDate = userDateStrToJavaDate( inDateStr );
			// Now convert this to DB format
			if (jDate != null)
			{
				retString = javaDateToOracleDateTimeStr( jDate );
			}
		}
		return(retString);
	}

	public static Timestamp userDateStrToJavaTimestamp( final String inDateStr )
	{
		java.sql.Timestamp retVal = null;

		final java.util.Date tmpDate = userDateStrToJavaDate( inDateStr );

		if (tmpDate != null)
		{
			retVal = new java.sql.Timestamp( tmpDate.getTime() );
		}

		return(retVal);
	}

	public static Timestamp userDateTimeStrToJavaTimestamp( final String inDateTimeStr )
	{
		java.sql.Timestamp retVal = null;

		final java.util.Date tmpDate = userDateTimeStrToJavaDate( inDateTimeStr );

		if (tmpDate != null)
		{
			retVal = new java.sql.Timestamp( tmpDate.getTime() );
		}
		return(retVal);
	}


	public static String javaTimestampToOracleDateTimeStr(final Timestamp inTimestamp)
	{
		// Convert the timestamp into a date, and then format it using the java date->userstr
		final java.util.Date tmpDate = new java.util.Date( inTimestamp.getTime() );

		return(javaDateToOracleDateTimeStr( tmpDate) );
	}


	public static String javaTimestampToUserDateTimeStr(final Timestamp inTimestamp)
	{
		String retVal = "";

		if (inTimestamp != null)
		{
			// Convert the timestamp into a date, and then format it using the java date->userstr
			final java.util.Date tmpDate = new java.util.Date( inTimestamp.getTime() );

			if (tmpDate != null)
			{
				retVal = javaDateToUserDateTimeStr( tmpDate );
			}
		}

		return( retVal );
	}


	public static String javaTimestampToUserDateStr(final Timestamp inTimestamp)
	{
		String retVal = "";

		// Convert the timestamp into a date, and then format it using the java date->userstr
		if (inTimestamp != null)
		{
			final java.util.Date tmpDate = new java.util.Date( inTimestamp.getTime() );

			if (tmpDate != null)
			{
				retVal = javaDateToUserDateStr( tmpDate );
			}
		}

		return( retVal );
	}


	/**
	 * <P>Convert a date string entered by a user into a java date.</P>
	 * <P>No validation is performed here, it is assumed to be in the correct format.</P>
	 * <P>null is returned if the string is invalid or badly formatted.</P>
	 * @param userDateStr String
	 * @return Date
	 */
	public static java.util.Date userDateStrToJavaDate( final String userDateStr)
	{
		return(customDateTimeStrToJavaDate( userDateStr, MA_USER_DATE_FORMAT));
	}

	/**
	 * Method customDateStrToJavaDate.
	 * @param customDateStr
	 * @param customDateFormat
	 * @return Date
	 */
	public static java.util.Date customDateTimeStrToJavaDate( final String customDateStr,
		final String customDateFormat)
	{
		java.util.Date retDate = null;
		if (customDateStr != null && customDateStr.length() > 0)
		{
			final SimpleDateFormat dFormatter = new SimpleDateFormat(customDateFormat);
			retDate = dFormatter.parse(customDateStr,new ParsePosition(0));
		}
		return(retDate);
	}


	/**
	 * <P>Turn a string from the oracle string format into the user visible date and time
	 * format.</P>
	 * <P>If the supplied string is empty or null, this function returns an empty string.</P>
	 * @param oracleDateStr String
	 * @return String
	 */
	public static String oracleDateTimeStrToUserDateTimeStr( final String oracleDateStr)
	{
		String retString = "";
		if (oracleDateStr != null && oracleDateStr.length() > 0)
		{
			final java.util.Date jDate = oracleDateTimeStrToJavaDate( oracleDateStr);
			retString = javaDateToUserDateTimeStr( jDate );
		}
		return(retString);
	}


	/**
	 * <P>Turn a Java date object into a date/time string that can be displayed to the user.</P>
	 * <P>If the supplied date is null, this function returns an empty string.</P>
	 * @param inDate java.util.Date
	 * @return String
	 */
	public static String javaDateToUserDateTimeStr( final java.util.Date inDate)
	{
		return(javaDateToCustomDateTimeStr( inDate, MA_USER_DATE_TIME_FORMAT));
	}

	/**
	 * Method javaDateToCustomDateTimeStr.
	 * @param inDate
	 * @param customDateFormat
	 * @return String
	 */
	public static String javaDateToCustomDateTimeStr( final java.util.Date inDate,
		final String customDateFormat)
	{
		String retString = "";
		if(inDate != null)
		{
			final SimpleDateFormat dFormatter = new SimpleDateFormat(customDateFormat);
			final StringBuffer retDate = new StringBuffer();
			retString = dFormatter.format(inDate, retDate, new FieldPosition(0)).toString();
		}
		return(retString);
	}


	/**
	 * <P>Turn a string from the oracle string format into the user visible date format.</P>
	 * <P>If the supplied string is empty or null, this function returns an empty string.</P>
	 * @param oracleDateStr String
	 * @return String
	 */
	public static String oracleDateTimeStrToUserDateStr( final String oracleDateStr)
	{
		String retString = "";
		if (oracleDateStr != null && oracleDateStr.length() > 0)
		{
			final java.util.Date jDate = oracleDateTimeStrToJavaDate( oracleDateStr);
			retString = javaDateToUserDateStr( jDate );
		}
		return(retString);
	}


	/**
	 * <P>Turn the supplied java date into a date string representation displayable to a user.</P>
	 * <P>If the supplied date is null, an empty string is returned.</P>
	 * @param inDate java.util.Date
	 * @return String
	 */
	public static String javaDateToUserDateStr( final java.util.Date inDate)
	{
		return(javaDateToCustomDateTimeStr( inDate, MA_USER_DATE_FORMAT));
	}

	public static String javaDateToUserTimestampStr( final java.util.Date inDate)
	{
		return(javaDateToCustomDateTimeStr( inDate, MA_USER_DATE_TIME_MILLIS_FORMAT));
	}
}
