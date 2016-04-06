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

package uk.co.modularaudio.util.timing;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.mahout.math.list.LongArrayList;

public class TestTimer
{
	private final ArrayList<String> sectorNames = new ArrayList<String>();
	private final LongArrayList sectorTimestamps = new LongArrayList();

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
		final long firstTs = sectorTimestamps.get(0);
		long prevTs = firstTs;
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
			sb.append( String.format("%15s", sectorNames.get( i ) ) );
			sb.append( " " );
			sb.append( NanosTimestampFormatter.formatTimestampForLogging( delta, true ) );
			sb.append( "\n" );
			prevTs = curTs;
		}
		final long total = prevTs - firstTs;
		sb.append( linePrefix );
		sb.append( " Total " );
		sb.append( NanosTimestampFormatter.formatTimestampForLogging( total, true ) );
		sb.append( "\n" );
		log.info( sb.toString() );
	}
}
