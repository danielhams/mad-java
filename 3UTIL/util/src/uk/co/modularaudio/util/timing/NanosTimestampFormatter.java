package uk.co.modularaudio.util.timing;

public class NanosTimestampFormatter
{
	public static String formatTimestampForLogging( final long rawNanosTimestamp, final boolean displayHoursMinSeconds )
	{
		final long nanosPart = rawNanosTimestamp % 1000;
		final long totalMicros = rawNanosTimestamp / 1000;
		final long microsPart = totalMicros % 1000;
		final long totalMillis = totalMicros /  1000;
		final long millisPart = totalMillis % 1000;
		final long totalSeconds = totalMillis / 1000;
		final long secondsPart = totalSeconds % 60;
		final long totalMinutes = totalSeconds / 60;
		final long minutesPart = totalMinutes % 60;
		final long hoursPart = totalMinutes / 60;

		// HH:MM:SS_MIL_MIC_NAN
		// 12345678901234567890
		final int stringLength = ( displayHoursMinSeconds ? 20 : 14 );

		final StringBuilder sb = new StringBuilder( stringLength );
		if( displayHoursMinSeconds )
		{
			sb.append( String.format( "%02d", hoursPart ) );
			sb.append( ':' );
			sb.append( String.format( "%02d", minutesPart ) );
			sb.append( ':' );
		}
		sb.append( String.format( "%02d", secondsPart ) );
		sb.append( '.' );
		sb.append( String.format( "%03d", millisPart ) );
		sb.append( '.' );
		sb.append( String.format( "%03d", microsPart ) );
		sb.append( '.' );
		sb.append( String.format( "%03d", nanosPart ) );
		return sb.toString();
	}
}
