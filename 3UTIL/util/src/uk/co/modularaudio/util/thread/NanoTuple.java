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

package uk.co.modularaudio.util.thread;

public class NanoTuple
{
//	private static Log log = LogFactory.getLog( NanoTuple.class.getName() );
	
	// A million nanoseconds in a millisecond
	public static final int MILLIS_TO_NANOS_FACTOR = 1000 * 1000;

	private long millis = -1;
	private int nanos = -1;
	
	private static final int BUF_LEN = 512;
//	private String ntName = null;
	private StringBuilder sb = new StringBuilder( BUF_LEN );
	
	public NanoTuple( String name )
	{
		this( name, System.nanoTime() );
	}
	
	public NanoTuple( String name, long nanos )
	{
//		this.ntName = name;
		resetToNanoTime(nanos);
	}

	public NanoTuple( String name, NanoTuple inNanos )
	{
//		this.ntName = name;
		this.millis = inNanos.millis;
		this.nanos = inNanos.nanos;
	}
	
	public NanoTuple( String name, long millis, int ananos )
	{
//		this.ntName = name;
		this.millis = millis;
		this.nanos = ananos;
	}

	public boolean nanoSleepIfNotPassed( long undersleep ) throws InterruptedException
	{
		long curNanoTime = System.nanoTime();
		long thisNanoTime = getFullNanosValue();
		// Diff nano time  +ve when nanoTime > curTime, -ve when nanoTime < curTime
		long diffNanoTime = thisNanoTime - curNanoTime;
		
		if( diffNanoTime > 0 )
		{
			// Real sleep +ve when nanoTime > curTime
			long realSleep = diffNanoTime - undersleep;
//			log.debug( ntName + ": Checked the sleep of ( " + diffNanoTime + " ) but with undersleep it is ( " + realSleep + " )");
			if( realSleep > 0 )
			{
//				log.debug("Doing a sleep");
				long localMillis = computeMillis( realSleep );
				int localNanos = computeNanos( realSleep, localMillis );
				Thread.sleep( localMillis, localNanos );
				return true;
			}
			else
			{
//				log.debug( ntName + ": Skipping the sleep as it is negative( " + realSleep + " )");
			}
		}
		else
		{
//			log.debug( ntName + ": Not doing the sleep as we've already passed it cur( " + curNanoTime + " ) and sleepTime( " + thisNanoTime + " )");
		}
		return false;
	}

	public boolean nanoSleepIfNotPassed() throws InterruptedException
	{
		return nanoSleepIfNotPassed( 0 );
	}

	public void resetToCurrent()
	{
		resetToNanoTime( System.nanoTime() );
	}

	public void resetToNanoTime( long inNanos )
	{
		millis = computeMillis( inNanos );
		nanos = computeNanos( inNanos, millis );
	}

	private int computeNanos( long inNanos, long relatedMillis )
	{
		return (int)(inNanos - ( relatedMillis * MILLIS_TO_NANOS_FACTOR));
	}

	private long computeMillis( long inNanos )
	{
		return (long)(inNanos / MILLIS_TO_NANOS_FACTOR);
	}

	public void resetToNanoTuple( NanoTuple arg )
	{
		this.millis = arg.millis;
		this.nanos = arg.nanos;
	}

	public void setToArgMinus( NanoTuple laterTimestamp, NanoTuple earlierTimestamp )
	{
		long later = (laterTimestamp.millis * MILLIS_TO_NANOS_FACTOR) + laterTimestamp.nanos;
		long earlier = (earlierTimestamp.millis * MILLIS_TO_NANOS_FACTOR) + earlierTimestamp.nanos;
		resetToNanoTime( later - earlier );
	}

	public void setToArgPlusNanos(NanoTuple baseTimestamp, long nanosToAdd)
	{
		long base = (baseTimestamp.millis * MILLIS_TO_NANOS_FACTOR) + baseTimestamp.nanos;
		resetToNanoTime( base + nanosToAdd );
	}
	
	@Override
	public String toString()
	{
		if( sb.length() > 0 )
		{
			sb.setLength( 0 );
		}
		sb.append( "m(" );
		sb.append( millis );
		sb.append( ")n(");
		sb.append( nanos );
		sb.append( ")" );
		return sb.toString();
	}

	public long getFullNanosValue()
	{
		return (millis * MILLIS_TO_NANOS_FACTOR) + nanos;
	}

	public void addNanos( long nanosToAdd )
	{
		long newVal = getFullNanosValue() + nanosToAdd;
		resetToNanoTime( newVal );
	}
}
