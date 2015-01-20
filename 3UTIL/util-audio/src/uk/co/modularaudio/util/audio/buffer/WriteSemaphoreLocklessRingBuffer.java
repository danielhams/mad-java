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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteSemaphoreLocklessRingBuffer
{
//	private static Log log = LogFactory.getLog( LocklessRingBuffer.class.getName() );
	
	protected AtomicInteger readPosition;
	protected AtomicInteger writePosition;
	
	protected Semaphore writeSemaphore = new Semaphore( 1 );
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected float buffer[] = null;
	
	public WriteSemaphoreLocklessRingBuffer( int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = new float[ this.bufferLength ];
		readPosition = new AtomicInteger( 0 );
		writePosition = new AtomicInteger( 0 );
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
	
	public int read( float target[], int pos, int length )
	{
		return internalRead( readPosition.get(), writePosition.get(), target, pos, length, true, false );
	}
	
	public void moveForward( int readAmount )
	{
		int curReadPosition = readPosition.get();
		int newPosition = curReadPosition;

		newPosition += readAmount;
		
		if( newPosition > bufferLength )
		{
			newPosition -= bufferLength;
		}
		while( !readPosition.compareAndSet( curReadPosition, newPosition ) )
		{
		}
	}
	
	protected int internalRead( int rp, int wp, float[] target, int pos, int length, boolean move, boolean zero )
	{
		int amountRead = 0;
		int curReadPosition = rp;
		int curWritePosition = wp;
		
		int numReadable = calcNumReadable( curReadPosition, curWritePosition );

		if( numReadable < length )
		{
			return -1;
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
			while( !readPosition.compareAndSet( curReadPosition, newPosition ) )
			{
			}
		}
		
		return amountRead;
	}

	public int readNoMove( float target[], int pos, int length )
	{
		return internalRead( readPosition.get(), writePosition.get(), target, pos, length, false, false );
	}
	
	protected int internalWrite( int rp, int wp, float source[], int pos, int length, boolean move )
	{
		if( length > capacity )
		{
			return -1;
		}
		int amountWritten = 0;
		try
		{
			writeSemaphore.acquire();
			int curReadPosition = rp;
			int curWritePosition = wp;
			int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
			int newPosition = curWritePosition;
			
			if( numWriteable < length )
			{
				return -1;
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

				while( !writePosition.compareAndSet( curWritePosition, newPosition ) )
				{
				}
			}
			return amountWritten;
		}
		catch( InterruptedException ie )
		{
			return -1;
		}
		finally
		{
			writeSemaphore.release();
		}
	}
	
	public int writeNoMove( float[] source, int pos, int length )
	{
		return internalWrite( readPosition.get(), writePosition.get(), source, pos, length, false );
	}

	public int write( float source[], int pos, int length )
	{
		return internalWrite( readPosition.get(), writePosition.get(), source, pos, length, true );
	}
	
	public int getNumReadable()
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		return calcNumReadable(curReadPosition, curWritePosition);
	}
	
	public int getNumWriteable()
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		return calcNumWriteable(curReadPosition, curWritePosition);
	}

	public void clear()
	{
		// Just reset the write position to be equal to the read position
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		writePosition.compareAndSet( curWritePosition, curReadPosition );
	}
	
	public int size()
	{
		return capacity;
	}
	
	public int readAndZero( float[] target, int pos, int length )
	{
		return internalRead( readPosition.get(), writePosition.get(), target, pos, length, true, true );
	}
}
