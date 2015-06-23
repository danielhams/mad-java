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

	protected final int capacity;
	protected final int bufferLength;
	protected final A[] buffer;

	@SuppressWarnings("unchecked")
	public UnsafeGenericRingBuffer( final Class<A> clazz,  final int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = (A[])Array.newInstance( clazz, bufferLength );
		readPosition = 0;
		writePosition = 0;
	}

	public int read( final A target[], final int pos, final int length ) throws InterruptedException
	{
		final int numReadable = calcNumReadable( readPosition, writePosition );

		return internalRead( readPosition, writePosition, numReadable, target, pos, length );
	}

	protected int internalRead( final int iRp, final int wp, final int numReadable, final A[] target, final int pos, final int length )
	{
		int rp = iRp;
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
			else // if( rp >= wp )
			{
				// Case where the read position might loop over the end of the buffer
				if( rp + length > bufferLength )
				{
					final int numToReadFromEnd = bufferLength - rp;
					final int numToReadFromStart = length - numToReadFromEnd;
					System.arraycopy( buffer, rp, target, pos, numToReadFromEnd );
					System.arraycopy( buffer, 0, target, pos + numToReadFromEnd, numToReadFromStart );
				}
				else
				{
					// Fits before the end of the ring
					System.arraycopy( buffer, rp, target, pos, length );
				}
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

	public int write( final A source[], final int pos, final int length )
	{
		final int numWriteable = calcNumWriteable( readPosition, writePosition );
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
				final int numToWriteAtEnd = bufferLength - writePosition;
				final int numToWriteAtStart = length - numToWriteAtEnd;
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
		return calcNumReadable( readPosition, writePosition );
	}

	public int getNumWriteable()
	{
		return calcNumWriteable( readPosition, writePosition );
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
		final int rp = readPosition;
		final int wp = writePosition;
		final int numReadable = calcNumReadable( rp, wp );

		return internalReadOneOut( rp, wp, numReadable );
	}

	protected A internalReadOneOut( final int iRp, final int wp, final int numReadable )
	{
		int rp = iRp;
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

	public void writeOne( final A object )
	{
		final int numWriteable = getNumWriteable();
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

	protected int calcNumReadable( final int curReadPosition, final int curWritePosition )
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
			retVal = bufferLength - curReadPosition + curWritePosition;
		}
//		log.debug("RingBuffer.cap(" + capacity + ").calcNumReadable(" + curReadPosition + ", " + curWritePosition + ") -> (" + retVal + ")");
		return retVal;
	}

	protected int calcNumWriteable( final int curReadPosition, final int curWritePosition )
	{
		int retVal = -1;

		if( curWritePosition >= curReadPosition )
		{
			// Simple case, reading from start of buffer writing to end of it
			retVal = bufferLength - curWritePosition + curReadPosition;
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
		final int rp = readPosition;
		final int wp = writePosition;
		final int numReadable = calcNumReadable( rp, wp );
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
