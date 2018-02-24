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

package uk.co.modularaudio.util.timing;

import uk.co.modularaudio.util.lang.StringUtils;

public class NanosTimestampFormatter
{
	public static String formatTimestampForLogging( final long rawNanosTimestamp, final boolean displayHoursMinSeconds )
	{
		return formatTimestampForLogging( rawNanosTimestamp, displayHoursMinSeconds, ':', '.' );
	}

	public static String formatTimestampForLogging( final long rawNanosTimestamp, final boolean displayHoursMinSeconds,
			final char dateSep,
			final char timeSep )
	{
		final int nanosPart = (int)(rawNanosTimestamp % 1000);
		final long totalMicros = rawNanosTimestamp / 1000;
		final int microsPart = (int)(totalMicros % 1000);
		final long totalMillis = totalMicros /  1000;
		final int millisPart = (int)(totalMillis % 1000);
		final long totalSeconds = totalMillis / 1000;
		final int secondsPart = (int)(totalSeconds % 60);
		final long totalMinutes = totalSeconds / 60;
		final int minutesPart = (int)(totalMinutes % 60);
		final int hoursPart = (int)(totalMinutes / 60);

		// HH:MM:SS_MIL_MIC_NAN
		// 12345678901234567890
		final int stringLength = ( displayHoursMinSeconds ? 20 : 14 );

		final StringBuilder sb = new StringBuilder( stringLength );
		if( displayHoursMinSeconds )
		{
			StringUtils.appendFormattedInt( sb, 2, hoursPart );
			sb.append( dateSep );
			StringUtils.appendFormattedInt( sb, 2, minutesPart );
			sb.append( dateSep );
		}
		StringUtils.appendFormattedInt( sb, 2, secondsPart );
		sb.append( timeSep );
		StringUtils.appendFormattedInt( sb, 3, millisPart );
		sb.append( timeSep );
		StringUtils.appendFormattedInt( sb, 3, microsPart );
		sb.append( timeSep );
		StringUtils.appendFormattedInt( sb, 3, nanosPart );
		return sb.toString();
	}
}
