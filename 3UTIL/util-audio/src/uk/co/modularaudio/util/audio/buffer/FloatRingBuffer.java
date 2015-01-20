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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.concurrent.Semaphore;

public class FloatRingBuffer
{
//	private static Log log = LogFactory.getLog( LocklessRingBuffer.class.getName() );
	
	protected int readPosition;
	protected int writePosition;
	
	protected Semaphore writeSemaphore = new Semaphore( 1 );
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected float buffer[] = null;
	
	public FloatRingBuffer( int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = new float[ this.bufferLength ];
		readPosition = 0;
		writePosition = 0;
	}
	
	protected final int calcNumReadable( int curReadPosition, int curWritePosition )
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
	
	protected final int calcNumWriteable( int curReadPosition, int curWritePosition )
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
	
	public int read( float target[], int pos, int length ) throws BufferUnderflowException, InterruptedException
	{
		return internalRead( target, pos, length, true, false );
	}
	
	public void moveForward( int readAmount )
	{
		int curReadPosition = readPosition;
		int newPosition = curReadPosition;

		newPosition += readAmount;
		
		if( newPosition > bufferLength )
		{
			newPosition -= bufferLength;
		}
		readPosition = newPosition;
	}
	
	protected int internalRead( float[] target, int pos, int length, boolean move, boolean zero )
		throws BufferUnderflowException
	{
			int amountRead = 0;
			int curReadPosition = readPosition;
			int curWritePosition = writePosition;
			
			int numReadable = calcNumReadable( curReadPosition, curWritePosition );

			if( numReadable < length )
			{
				throw new BufferUnderflowException();
			}
			else
			{
				// Case where the read position might loop over the end of the buffer
				if( curReadPosition + length > bufferLength )
				{
					int numToReadFromEnd = bufferLength - curReadPosition;
					int numToReadFromStart = length - numToReadFromEnd;
					System.arraycopy( buffer, curReadPosition, target, pos, numToReadFromEnd );
					System.arraycopy( buffer, 0, target, pos + numToReadFromEnd, numToReadFromStart );
					if( zero )
					{
						for( int i = 0 ; i < numToReadFromEnd ; i++ )
						{
							buffer[ curReadPosition + i ] = 0.0f;
						}
						for( int i = 0 ; i < numToReadFromStart ; i++ )
						{
							buffer[ i ] = 0.0f;
						}
					}
				}
				else
				{
					// Fits before the end of the ring
					System.arraycopy( buffer, curReadPosition, target, pos, length );
					if( zero )
					{
						for( int i = 0 ; i < length ; i++ )
						{
							buffer[ curReadPosition + i ] = 0.0f;
						}
					}
				}
				amountRead = length;
			}
			
			if( move )
			{
				int newPosition = curReadPosition;
				newPosition += amountRead;
				
				if( newPosition > bufferLength )
				{
					newPosition -= bufferLength;
				}
				readPosition = newPosition;
			}
			
			return amountRead;
	}

	public int readNoMove( float target[], int pos, int length ) throws BufferUnderflowException
	{
		return internalRead( target, pos, length, false, false );
	}
	
	protected int internalWrite( float source[], int pos, int length, boolean move )
		throws BufferOverflowException, InterruptedException
	{
		if( length > capacity )
		{
			throw new BufferOverflowException();
		}
		int amountWritten = 0;
		try
		{
			int curReadPosition = readPosition;
			int curWritePosition = writePosition;
			int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
			int newPosition = curWritePosition;
			
			if( numWriteable < length )
			{
				throw new BufferOverflowException();
			}
			else
			{
				// Treat the cases
				// Case where the write position might loop over the end of the buffer
				if( curWritePosition + length > bufferLength )
				{
					int numToWriteAtEnd = bufferLength - curWritePosition;
					int numToWriteAtStart = length - numToWriteAtEnd;
					System.arraycopy( source, pos, buffer, curWritePosition, numToWriteAtEnd );
					System.arraycopy( source, pos + numToWriteAtEnd, buffer, 0, numToWriteAtStart );
				}
				else
				{
					// Fits before the end of the ring
					System.arraycopy( source, pos, buffer, curWritePosition, length );
				}
				amountWritten = length;
			}
			
			if( move )
			{
				newPosition += length;
			
				while( newPosition > bufferLength )
				{
					newPosition -= bufferLength;
				}

				writePosition = newPosition;
			}
			return amountWritten;
		}
		finally
		{
			writeSemaphore.release();
		}
	}
	
	public int writeNoMove( float[] source, int pos, int length )
		throws BufferOverflowException, InterruptedException
	{
		return internalWrite( source, pos, length, false );
	}

	public int write( float source[], int pos, int length ) throws BufferOverflowException, InterruptedException
	{
		return internalWrite( source, pos, length, true );
	}
	
	public int getNumReadable()
	{
		int curReadPosition = readPosition;
		int curWritePosition = writePosition;
		return calcNumReadable( curReadPosition, curWritePosition);
	}
	
	public int getNumWriteable()
	{
		int curReadPosition = readPosition;
		int curWritePosition = writePosition;
		return calcNumWriteable( curReadPosition, curWritePosition);
	}

	public void clear()
	{
		// Just reset the write position to be equal to the read position
		int curReadPosition = readPosition;
		writePosition = curReadPosition;
	}
	
	public int size()
	{
		return capacity;
	}
	
	public int readAndZero( float[] target, int pos, int length )
	{
		return internalRead( target, pos, length, true, true );
	}
}
