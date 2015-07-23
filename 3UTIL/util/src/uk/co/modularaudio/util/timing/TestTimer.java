package uk.co.modularaudio.util.timing;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

public class TestTimer
{
	private final ArrayList<String> sectorNames = new ArrayList<String>();
	private final ArrayList<Long> sectorTimestamps = new ArrayList<Long>();

	public TestTimer()
	{
	}

	public void markBoundary( final String boundaryName )
	{
		final long curTime = System.nanoTime();
		sectorNames.add( boundaryName );
		sectorTimestamps.add( curTime );
	}

	public void logTimes( final String linePrefix,
			final Log log )
	{
		final int numSectors = sectorTimestamps.size();
		final StringBuilder sb = new StringBuilder();
		long prevTs = sectorTimestamps.get(0);
		sb.append( linePrefix );
		sb.append( "\n" );

		for( int i = 0 ; i < numSectors ; ++i )
		{
			final long curTs = sectorTimestamps.get(i);
			final long delta = curTs - prevTs;
			sb.append( linePrefix );
			sb.append( " Sector " );
			sb.append( i );
			sb.append( ": " );
			sb.append( sectorNames.get( i ) );
			sb.append( " " );
			sb.append( Long.toString(delta) );
			sb.append( "\n" );
			prevTs = curTs;
		}
		log.info( sb.toString() );
	}
}
