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

package uk.co.modularaudio.util.audio.buffer;

import java.lang.reflect.Array;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnsafeGenericRingBuffer<A>
{
	private static Log log = LogFactory.getLog( UnsafeGenericRingBuffer.class.getName() );
	
	protected int readPosition;
	protected int writePosition;
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected A[] buffer = null;
	
	@SuppressWarnings("unchecked")
	public UnsafeGenericRingBuffer( Class<A> clazz,  int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = (A[])Array.newInstance( clazz, bufferLength );
		readPosition = 0;
		writePosition = 0;
	}
	
	public int read( A target[], int pos, int length ) throws InterruptedException
	{
		int numReadable = calcNumReadable( readPosition, writePosition );
		
		return internalRead( readPosition, writePosition, numReadable, target, pos, length );
	}

	protected int internalRead( int rp, int wp, int numReadable, A[] target, int pos, int length )
	{
		if( numReadable < length )
		{
			return 0;
		}
		else
		{
			// Treat the cases
			if( wp > rp )
			{
				// Copy from the ring buffer directly into the output and update the read position
				System.arraycopy( buffer, rp, target, pos, length );
			}
			else if( rp > wp )
			{
				// Case where the read position might loop over the end of the buffer
				if( rp + length > bufferLength )
				{
					int numToReadFromEnd = bufferLength - rp;
					int numToReadFromStart = length - numToReadFromEnd;
					System.arraycopy( buffer, rp, target, pos, numToReadFromEnd );
					System.arraycopy( buffer, 0, target, pos + numToReadFromEnd, numToReadFromStart );
				}
				else
				{
					// Fits before the end of the ring
					System.arraycopy( buffer, rp, target, pos, length );
				}
			}
			else
			{
				log.error( "Case analysis error in ring read" );
				throw new RuntimeException();
			}
		}
		
		rp += length;
		
		if( rp > bufferLength )
		{
			rp -= bufferLength;
		}
		readPosition = rp;
		return length;
	}
	
	public int write( A source[], int pos, int length )
	{
		int numWriteable = calcNumWriteable( readPosition, writePosition );
		int newPosition = writePosition;
		
		if( numWriteable < length )
		{
			return 0;
		}
		else
		{
			// Treat the cases

			// Case where the write position might loop over the end of the buffer
			if( writePosition + length > bufferLength )
			{
				int numToWriteAtEnd = bufferLength - writePosition;
				int numToWriteAtStart = length - numToWriteAtEnd;
				System.arraycopy( source, pos, buffer, writePosition, numToWriteAtEnd );
				System.arraycopy( source, pos + numToWriteAtEnd, buffer, 0, numToWriteAtStart );
			}
			else
			{
				// Fits before the end of the ring
				System.arraycopy( source, pos, buffer, writePosition, length );
			}
		}
		
		newPosition += length;
		
		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		writePosition = newPosition;

		return length;
	}
	
	public int getNumReadable()
	{
		return( calcNumReadable( readPosition, writePosition ) );
	}
	
	public int getNumWriteable()
	{
		return( calcNumWriteable( readPosition, writePosition ) );
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

	public A readOneOut()
	{
		int rp = readPosition;
		int wp = writePosition;
		int numReadable = calcNumReadable( rp, wp );
		
		return internalReadOneOut( rp, wp, numReadable );
	}

	protected A internalReadOneOut( int rp, int wp, int numReadable )
	{
		A retVal = null;
		if( numReadable < 1 )
		{
			log.error("Oops");
			return null;
		}
		else
		{
			retVal = buffer[ readPosition ];
		}
		
		rp += 1;
		
		if( rp >= bufferLength )
		{
			rp -= bufferLength;
		}
		readPosition = rp;
		return retVal;
	}

	public void writeOne( A object )
	{
		int numWriteable = getNumWriteable();
		int newPosition = writePosition;
		
		if( numWriteable < 1 )
		{
			log.error("Oops in writeone");
			return;
		}
		else
		{
			buffer[ writePosition ] = object;
		}
		
		newPosition += 1;
		
		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		writePosition = newPosition;

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
		int rp = readPosition;
		int wp = writePosition;
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
