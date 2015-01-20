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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocklessPreallocatingGenericRingBuffer<A>
{
	private static Log log = LogFactory.getLog( LocklessPreallocatingGenericRingBuffer.class.getName() );
	
	protected AtomicInteger readPosition;
	protected AtomicInteger writePosition;
	
//	// Only one writer at a time
//	protected Semaphore writeSemaphore = new Semaphore(1);
	
	protected int capacity = -1;
	protected int bufferLength = -1;
	protected A[] buffer = null;
	
	protected BufferObjectCopier<A> copier = null;
	
	@SuppressWarnings("unchecked")
	public LocklessPreallocatingGenericRingBuffer( Class<A> clazz,  BufferObjectCopier<A> copier, int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		this.copier = copier;
		buffer = (A[])Array.newInstance( clazz, bufferLength );
		for( int i = 0 ; i < bufferLength ; i++ )
		{
			buffer[ i ] = copier.allocInstance();
		}
		readPosition = new AtomicInteger( 0 );
		writePosition = new AtomicInteger( 0 );
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
			int curReadPosition = readPosition.get();
			int curWritePosition = writePosition.get();
			int numReadable = calcNumReadable( curReadPosition, curWritePosition );
			int newPosition = curReadPosition;
			
			if( numReadable < length )
			{
				return 0;
			}
			else
			{
				// Treat the cases
				if( curWritePosition > curReadPosition )
				{
					// Copy from the ring buffer directly into the output and update the read position
					copyFromToLength( buffer, curReadPosition, target, pos, length );
				}
				else if( curReadPosition > curWritePosition )
				{
					// Case where the read position might loop over the end of the buffer
					if( curReadPosition + length > bufferLength )
					{
						int numToReadFromEnd = bufferLength - curReadPosition;
						int numToReadFromStart = length - numToReadFromEnd;
						copyFromToLength( buffer, curReadPosition, target, pos, numToReadFromEnd );
						copyFromToLength( buffer, 0, target, pos+ numToReadFromEnd, numToReadFromStart );
					}
					else
					{
						// Fits before the end of the ring
						copyFromToLength( buffer, curReadPosition, target, pos, length );
					}
				}
				else
				{
					log.error( "Case analysis error in ring read" );
					throw new BufferUnderflowException();
				}
			}
			
			newPosition += length;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			success = readPosition.compareAndSet( curReadPosition, newPosition );
		}
		return length;
	}
	
	public int readUpToMaxNum( A target[], int pos, int maxNum ) throws BufferUnderflowException
	{
		boolean success = false;
		int numRead = 0;
		while( !success )
		{
			int curReadPosition = readPosition.get();
			int curWritePosition = writePosition.get();
			int numReadable = calcNumReadable( curReadPosition, curWritePosition );
			if( numReadable == 0 )
			{
				return 0;
			}
			int newPosition = curReadPosition;
			
			int length = ( numReadable < maxNum ? numReadable : maxNum );
			
			if( curWritePosition > curReadPosition )
			{
				// Copy from the ring buffer directly into the output and update the read position
				copyFromToLength( buffer, curReadPosition, target, pos, length );
			}
			else if( curReadPosition > curWritePosition )
			{
				// Case where the read position might loop over the end of the buffer
				if( curReadPosition + length > bufferLength )
				{
					int numToReadFromEnd = bufferLength - curReadPosition;
					int numToReadFromStart = length - numToReadFromEnd;
					copyFromToLength( buffer, curReadPosition, target, pos, numToReadFromEnd );
					copyFromToLength( buffer, 0, target, pos+ numToReadFromEnd, numToReadFromStart );
				}
				else
				{
					// Fits before the end of the ring
					copyFromToLength( buffer, curReadPosition, target, pos, length );
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
			success = readPosition.compareAndSet( curReadPosition, newPosition );
		}
		return numRead;
	}
	
	public void readOneCopyToDest( A dest ) throws BufferUnderflowException
	{
		boolean success = false;
		while( !success )
		{
			int curReadPosition = readPosition.get();
			int curWritePosition = writePosition.get();
			int numReadable = calcNumReadable( curReadPosition, curWritePosition );
			int newPosition = curReadPosition;
			
			if( numReadable < 1 )
			{
				throw new BufferUnderflowException();
			}
			else
			{
				copier.copyValues( buffer[ curReadPosition ], dest );
			}
			
			newPosition += 1;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			success = readPosition.compareAndSet( curReadPosition, newPosition );
		}
	}
	
	protected final void copyFromToLength( A[] from, int curReadPosition, A[] to, int pos, int length )
	{
		for( int i = 0 ; i < length ; i++ )
		{
			copier.copyValues( from[ curReadPosition + i], to[ pos + i ] );
		}
	}
	
	public boolean writeOne( A entry )
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
		int newPosition = curWritePosition;
		
		if( numWriteable < 1 )
		{
			return false;
		}
		else
		{
			copier.copyValues( entry,  buffer[ curWritePosition ] );
		}
		
		newPosition += 1;
		
		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		while( !writePosition.compareAndSet( curWritePosition, newPosition ) )
		{
		}
		return true;
	}

	public int write( A source[], int pos, int length )
	{
//		try
//		{
//			writeSemaphore.acquire();
			int curReadPosition = readPosition.get();
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
				if( curWritePosition + length > bufferLength )
				{
					int numToWriteAtEnd = bufferLength - curWritePosition;
					int numToWriteAtStart = length - numToWriteAtEnd;
					copyFromToLength( source, pos, buffer, curWritePosition, numToWriteAtEnd );
					copyFromToLength( source, pos + numToWriteAtEnd, buffer, 0, numToWriteAtStart );
				}
				else
				{
					// Fits before the end of the ring
					copyFromToLength( source, pos, buffer, curWritePosition, length );
				}
			}
			
			newPosition += length;
			
			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			while( !writePosition.compareAndSet( curWritePosition, newPosition ) )
			{
			}
			return length;
//		}
//		catch( InterruptedException ie )
//		{
//			return 0;
//		}
//		finally
//		{
//			writeSemaphore.release();
//		}
	}
	
	public final int getCurrentFillLevel()
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		return calcNumReadable(curReadPosition, curWritePosition);
	}
	
	public final int getNumReadable()
	{
		int curReadPosition = readPosition.get();
		int curWritePosition = writePosition.get();
		return calcNumReadable(curReadPosition, curWritePosition);
	}
	
	public final int getNumWriteable()
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
}
