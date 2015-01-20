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
import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnsafePreallocatingGenericRingBuffer<A>
{
	private static Log log = LogFactory.getLog( UnsafePreallocatingGenericRingBuffer.class.getName() );
	
	protected int readPosition = 0;
	protected int writePosition = 0;
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected A[] buffer = null;
	
	protected BufferObjectCopier<A> copier = null;
	
	@SuppressWarnings("unchecked")
	public UnsafePreallocatingGenericRingBuffer( Class<A> clazz,  BufferObjectCopier<A> copier, int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		this.copier = copier;
		buffer = (A[])Array.newInstance( clazz, bufferLength );
		for( int i = 0 ; i < bufferLength ; i++ )
		{
			buffer[ i ] = copier.allocInstance();
		}
	}
	
	protected final int calcNumReadable( int curReadPosition, int curWritePosition )
	{
		int retVal = -1;

		if( curWritePosition == curReadPosition )
		{
			retVal = 0;
		}
		else if( curWritePosition > curReadPosition )
		{
			// Simple case, reading from start of buffer writing to end of it
			retVal = curWritePosition - curReadPosition;
		}
		else if( curReadPosition > curWritePosition )
		{
			// Case we are reading from end of buffer and writing at the start
			retVal = (bufferLength - curReadPosition) + curWritePosition;
		}
		return retVal;
	}
	
	protected final int calcNumWriteable( int curReadPosition, int curWritePosition )
	{
		int retVal = capacity - calcNumReadable(curReadPosition, curWritePosition);
		
		return retVal;
	}
	
	public int read( A target[], int pos, int length )
	{
		boolean success = false;
		// Keep trying until we are the one to successfully update
		// the read position with atomic CAS
		while( !success )
		{
			int numReadable = calcNumReadable( readPosition, writePosition );
			int newPosition;
			
			if( numReadable < length )
			{
				return 0;
			}
			else
			{
				newPosition = readPosition + length;
				// Treat the cases
				if( writePosition > readPosition )
				{
					// Copy from the ring buffer directly into the output and update the read position
					copyFromToLength( buffer, readPosition, target, pos, length );
				}
				else if( readPosition > writePosition )
				{
					// Case where the read position might loop over the end of the buffer
					if( newPosition > bufferLength )
					{
						int numToReadFromEnd = bufferLength - readPosition;
						int numToReadFromStart = length - numToReadFromEnd;
						copyFromToLength( buffer, readPosition, target, pos, numToReadFromEnd );
						copyFromToLength( buffer, 0, target, pos+ numToReadFromEnd, numToReadFromStart );
					}
					else
					{
						// Fits before the end of the ring
						copyFromToLength( buffer, readPosition, target, pos, length );
					}
				}
				else
				{
					log.error( "Case analysis error in ring read" );
					throw new BufferUnderflowException();
				}
			}
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			readPosition = newPosition;
		}
		return length;
	}
	
	public int readUpToMaxNum( A target[], int pos, int maxNum ) throws BufferUnderflowException
	{
		boolean success = false;
		int numRead = 0;
		while( !success )
		{
			int numReadable = calcNumReadable( readPosition, writePosition );
			if( numReadable == 0 )
			{
				return 0;
			}
			int newPosition = readPosition;
			
			int length = ( numReadable < maxNum ? numReadable : maxNum );
			
			if( writePosition > readPosition )
			{
				// Copy from the ring buffer directly into the output and update the read position
				copyFromToLength( buffer, readPosition, target, pos, length );
			}
			else if( readPosition > writePosition )
			{
				// Case where the read position might loop over the end of the buffer
				if( readPosition + length > bufferLength )
				{
					int numToReadFromEnd = bufferLength - readPosition;
					int numToReadFromStart = length - numToReadFromEnd;
					copyFromToLength( buffer, readPosition, target, pos, numToReadFromEnd );
					copyFromToLength( buffer, 0, target, pos+ numToReadFromEnd, numToReadFromStart );
				}
				else
				{
					// Fits before the end of the ring
					copyFromToLength( buffer, readPosition, target, pos, length );
				}
				
				numRead = length;
			}
			else
			{
				log.error( "Case analysis error in ring read" );
				throw new BufferUnderflowException();
			}
			
			newPosition += length;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			readPosition = newPosition;
		}
		return numRead;
	}
	
	public void readOneCopyToDest( A dest ) throws BufferUnderflowException
	{
		boolean success = false;
		while( !success )
		{
			int numReadable = calcNumReadable( readPosition, writePosition );
			int newPosition = readPosition;
			
			if( numReadable < 1 )
			{
				throw new BufferUnderflowException();
			}
			else
			{
				copier.copyValues( buffer[ readPosition ], dest );
			}
			
			newPosition += 1;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			readPosition = newPosition;
		}
	}
	
	protected final void copyFromToLength( A[] from, int curReadPosition, A[] to, int pos, int length )
	{
		for( int i = 0 ; i < length ; i++ )
		{
			copier.copyValues( from[ curReadPosition + i], to[ pos + i ] );
		}
	}
	
	public int writeOne( A entry )
	{
		int numWriteable = calcNumWriteable( readPosition, writePosition );
		int newPosition = writePosition;
		
		if( numWriteable < 1 )
		{
			return 0;
		}
		else
		{
			copier.copyValues( entry,  buffer[ writePosition ] );
		}
		
		newPosition += 1;
		
		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		writePosition = newPosition;
		return 1;
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
				copyFromToLength( source, pos, buffer, writePosition, numToWriteAtEnd );
				copyFromToLength( source, pos + numToWriteAtEnd, buffer, 0, numToWriteAtStart );
			}
			else
			{
				// Fits before the end of the ring
				copyFromToLength( source, pos, buffer, writePosition, length );
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
	
	public final int getCurrentFillLevel()
	{
		return calcNumReadable( readPosition, writePosition );
	}
	
	public final int getNumReadable()
	{
		return calcNumReadable( readPosition, writePosition);
	}
	
	public final int getNumWriteable()
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
}
