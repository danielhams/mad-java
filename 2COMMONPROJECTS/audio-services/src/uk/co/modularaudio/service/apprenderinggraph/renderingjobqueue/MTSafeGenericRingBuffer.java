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

package uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MTSafeGenericRingBuffer<A>
{
	private static Log log = LogFactory.getLog( MTSafeGenericRingBuffer.class.getName() );
	
	protected AtomicInteger readPosition = new AtomicInteger( 0 );
	protected AtomicInteger writePosition = new AtomicInteger( 0 );
	
	protected ReentrantLock writeLock = new ReentrantLock( true );
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected A[] buffer = null;
	
	@SuppressWarnings("unchecked")
	public MTSafeGenericRingBuffer( Class<A> clazz,  int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = (A[])Array.newInstance( clazz, bufferLength );
	}
	
	public int write( A source[], int pos, int length )
	{
		int curReadPosition = readPosition.get();
		writeLock.lock();
		try
		{
			int curWritePosition = writePosition.get();
			int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
			int newPosition = curWritePosition;
			
			if( numWriteable < length )
			{
				return 0;
			}
			else
			{
				// Treat the cases
	
				// Case where the write position might loop over the end of the buffer
				if( newPosition + length > bufferLength )
				{
					int numToWriteAtEnd = bufferLength - newPosition;
					int numToWriteAtStart = length - numToWriteAtEnd;
					System.arraycopy( source, pos, buffer, newPosition, numToWriteAtEnd );
					System.arraycopy( source, pos + numToWriteAtEnd, buffer, 0, numToWriteAtStart );
				}
				else
				{
					// Fits before the end of the ring
					System.arraycopy( source, pos, buffer, newPosition, length );
				}
			}
			
			newPosition += length;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			writePosition.set( newPosition );
	
			return length;
		}
		finally
		{
			writeLock.unlock();
		}
	}
	
	public int getNumReadable()
	{
		return( calcNumReadable( readPosition.get(), writePosition.get() ) );
	}
	
	public int getNumWriteable()
	{
		return( calcNumWriteable( readPosition.get(), writePosition.get() ) );
	}

	public void clear()
	{
		// Just reset the write position to be equal to the read position
		writePosition = readPosition;
	}
	
	public int size()
	{
		return capacity;
	}

	public A readOneOut() throws InterruptedException
	{
		int rp = readPosition.get();
		int wp = writePosition.get();
		int numReadable = calcNumReadable( rp, wp );
		
		return internalReadOneOut( rp, wp, numReadable );
	}

	protected A internalReadOneOut( int rp, int wp, int numReadable )
	{
		A retVal = null;
		while( true )
		{
			if( numReadable < 1 )
			{
				return null;
			}
			else
			{
				retVal = buffer[ rp ];
			}
			
			int newRp = rp + 1;
			
			if( newRp >= bufferLength )
			{
				newRp -= bufferLength;
			}
			boolean success = readPosition.compareAndSet( rp, newRp );
			if( success )
			{
				// Fast track exit, stop re-evaluating the while
				return retVal;
			}
			else
			{
				rp = readPosition.get();
				wp = writePosition.get();
				numReadable = calcNumReadable( rp, wp );
			}
		}
	}

	public void writeOne( A object )
	{
		int curReadPosition = readPosition.get();
		writeLock.lock();
		try
		{
			int curWritePosition = writePosition.get();
			int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
			int newPosition = curWritePosition;
			
			if( numWriteable < 1 )
			{
				log.error("Oops in writeone");
				return;
			}
			else
			{
				buffer[ curWritePosition ] = object;
			}
			
			newPosition += 1;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			writePosition.set( newPosition );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected int calcNumReadable( int curReadPosition, int curWritePosition )
		{
			int retVal = -1;
	
			if( curWritePosition >= curReadPosition )
			{
				// Simple case, reading from start of buffer writing to end of it
				retVal = curWritePosition - curReadPosition;
			}
			else if( curReadPosition > curWritePosition )
			{
				// Case we are reading from end of buffer and writing at the start
				retVal = (bufferLength - curReadPosition) + curWritePosition;
			}
	//		log.debug("RingBuffer.cap(" + capacity + ").calcNumReadable(" + curReadPosition + ", " + curWritePosition + ") -> (" + retVal + ")");
			return retVal;
		}

	protected int calcNumWriteable( int curReadPosition, int curWritePosition )
	{
		int retVal = -1;
	
		if( curWritePosition >= curReadPosition )
		{
			// Simple case, reading from start of buffer writing to end of it
			retVal = (bufferLength - curWritePosition) + curReadPosition;
		}
		else if( curReadPosition > curWritePosition )
		{
			// Case we are reading from end of buffer and writing at the start
			retVal = curReadPosition - curWritePosition;
		}
		return retVal - 1;
	}

	public A readOneOrNull()
	{
		int rp = readPosition.get();
		int wp = writePosition.get();
		int numReadable = calcNumReadable( rp, wp );
		if( numReadable > 0 )
		{
			return internalReadOneOut( rp, wp, numReadable );
		}
		else
		{
			return null;
		}
	}
}
