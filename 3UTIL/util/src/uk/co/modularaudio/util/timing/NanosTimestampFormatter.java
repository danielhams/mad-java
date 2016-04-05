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
