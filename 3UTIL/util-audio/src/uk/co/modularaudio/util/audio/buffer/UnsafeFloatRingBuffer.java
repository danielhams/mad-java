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

import java.util.Arrays;

public class UnsafeFloatRingBuffer
{
//	private static Log log = LogFactory.getLog( UnsafeFloatRingBuffer.class.getName() );

	public int readPosition;
	public int writePosition;

	public int capacity;
	public int bufferLength;
	public float[] buffer;

	public UnsafeFloatRingBuffer( final int capacity )
	{
		this( capacity, false );
	}

	public UnsafeFloatRingBuffer( final int capacity, final boolean fillWithZeros )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		buffer = new float[ bufferLength ];
		readPosition = 0;
		if( fillWithZeros )
		{
			Arrays.fill( buffer,  0.0f );
			writePosition = bufferLength - 1;
		}
		else
		{
			writePosition = 0;
		}
	}

	public float readOne()
	{
		float retVal = 0.0f;
		final int numReadable = getNumReadable();
		int newPosition = readPosition;

		if( numReadable < 1 )
		{
			return 0.0f;
		}
		else
		{
			// Treat the cases
			if( writePosition > readPosition )
			{
				// Copy from the ring buffer directly into the output and update the read position
				retVal = buffer[ readPosition ];
			}
			else // if( readPosition >= writePosition )
			{
				// Case where the read position might loop over the end of the buffer
				if( readPosition + 1 > bufferLength )
				{
					retVal = buffer[ 0 ];
				}
				else
				{
					// Fits before the end of the ring
					retVal = buffer[ readPosition ];
				}
			}
		}

		newPosition += 1;

		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		readPosition = newPosition;
		return retVal;
	}

	public int read( final float[] target, final int pos, final int length )
	{
		return internalRead( readPosition, writePosition, target, pos, length, true, false );
	}

	public boolean writeOne( final float valToWrite )
	{
		final int numWriteable = getNumWriteable();
		int newPosition = writePosition;

		if( numWriteable < 1 )
		{
			return false;
		}
		else
		{
			// Treat the cases

			// Case where the write position might loop over the end of the buffer
			buffer[ writePosition ] = valToWrite;
		}

		newPosition += 1;

		if( newPosition >= bufferLength )
		{
			newPosition -= bufferLength;
		}
		writePosition = newPosition;
		return true;
	}

	public int write( final float[] source, final int pos, final int length )
	{
		final int numWriteable = getNumWriteable();
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
			retVal = (bufferLength - curReadPosition) + curWritePosition;
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
			retVal = (bufferLength - curWritePosition) + curReadPosition;
		}
		else if( curReadPosition > curWritePosition )
		{
			// Case we are reading from end of buffer and writing at the start
			retVal = curReadPosition - curWritePosition;
		}
		return retVal - 1;
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

	public void clearToZero()
	{
		Arrays.fill( buffer, 0.0f );
	}

	public int size()
	{
		return capacity;
	}

	public int readNoMove( final float[] target, final int pos, final int length )
	{
		return internalRead( readPosition, writePosition, target, pos, length, false, false );
	}

	public void moveForward( final int numToTake )
	{
		readPosition += numToTake;
		while( readPosition >= bufferLength )
		{
			readPosition -= bufferLength;
		}
	}

	public int readAndZero( final float[] target, final int pos, final int length )
	{
		return internalRead( readPosition, writePosition, target, pos, length, true, true );
	}

	protected int internalRead( final int rp, final int wp, final float[] target, final int pos, final int length, final boolean move, final boolean zero )
	{
		int amountRead = 0;
		final int curReadPosition = rp;
		final int curWritePosition = wp;

		final int numReadable = calcNumReadable( curReadPosition, curWritePosition );

		if( numReadable < length )
		{
			return 0;
		}
		else
		{
			// Case where the read position might loop over the end of the buffer
			if( curReadPosition + length > bufferLength )
			{
				final int numToReadFromEnd = bufferLength - curReadPosition;
				final int numToReadFromStart = length - numToReadFromEnd;
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
					Arrays.fill( buffer, curReadPosition, curReadPosition + length, 0.0f );
//					for( int i = 0 ; i < length ; i++ )
//					{
//						buffer[ curReadPosition + i ] = 0.0f;
//					}
				}
			}
			amountRead = length;
		}

		if( move )
		{
			int newPosition = curReadPosition + amountRead;

			if( newPosition > bufferLength )
			{
				newPosition -= bufferLength;
			}
			readPosition = newPosition;
		}

		return amountRead;
	}

}
