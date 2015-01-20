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

public class UnsafeOverlapAndAddRingBuffer extends UnsafeFloatRingBuffer
{

	public UnsafeOverlapAndAddRingBuffer(int maxCapacity)
	{
		super(maxCapacity);
	}

	public int add(float[] data, int pos, int length)
	{
		return this.internalAdd( data, pos, length, true );
	}
	
	protected strictfp int internalAdd( float[] data, int pos, int length, boolean move )
	{
		if( length > capacity )
		{
			return -1;
		}
		int amountWritten = 0;

		int curReadPosition = readPosition;
		int curWritePosition = writePosition;
		int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
		int newPosition = curWritePosition + length;
		
		if( numWriteable < length )
		{
			return -1;
		}
		else
		{
			// Treat the cases
			// Case where the write position might loop over the end of the buffer
			
			if( newPosition > bufferLength )
			{
				int numToWriteAtEnd = bufferLength - curWritePosition;
				int numToWriteAtStart = length - numToWriteAtEnd;
				for( int i = 0; i < numToWriteAtEnd ; i++ )
				{
					buffer[ curWritePosition + i ] += data[ pos + i ];
				}
				for( int i = 0 ; i < numToWriteAtStart ; i++ )
				{
					buffer[ i ] += data[ numToWriteAtEnd + pos + i ];
				}
			}
			else
			{
				// Fits before the end of the ring
				for( int i = 0 ; i < length ; i++ )
				{
					buffer[ curWritePosition + i  ] += data[ pos + i ];
				}
			}
			amountWritten = length;
		}
		
		if( move )
		{
			while( newPosition > bufferLength )
			{
				newPosition -= bufferLength;
			}

			writePosition = newPosition;
		}
		return amountWritten;
	}

	public int addNoMove(float[] data, int pos, int length)
	{
		return this.internalAdd( data, pos, length, false );
	}

//	public float[] getBuffer()
//	{
//		return buffer;
//	}
//
//	public int getReadPosition()
//	{
//		return readPosition;
//	}
//	
//	public int getWritePosition()
//	{
//		return writePosition;
//	}
}
