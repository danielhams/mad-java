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

package uk.co.modularaudio.util.audio.mad.hardwareio;

import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.buffer.LocklessPreallocatingGenericRingBuffer;

public class LocklessHardwareMidiNoteRingBuffer extends LocklessPreallocatingGenericRingBuffer<HardwareMidiNoteEvent>
{
	private static Log log = LogFactory.getLog( LocklessHardwareMidiNoteRingBuffer.class.getName() );

	private final static HardwareMidiNoteEventCopier NOTE_COPIER = new HardwareMidiNoteEventCopier();

	public LocklessHardwareMidiNoteRingBuffer( final int capacity )
	{
		super( HardwareMidiNoteEvent.class, NOTE_COPIER, capacity );
		// Now initialise empty objects inside the internal array
		for( int i = 0 ; i < capacity ; i++ )
		{
			buffer[ i ] = new HardwareMidiNoteEvent();
		}
	}

	@Override
	public int read( final HardwareMidiNoteEvent[] target, final int pos, final int length )
			throws BufferUnderflowException
	{
		return super.read( target, pos, length );
	}

	@Override
	public int write( final HardwareMidiNoteEvent[] source, final int pos, final int length )
	{
		return super.write( source, pos, length );
	}

	public int readUpToMaxNumAndFrameTime( final HardwareMidiNoteEvent[] target,
			final int pos,
			final int maxNum,
			final long endFrameTime )
	{
		boolean success = false;
		int numRead = 0;
		while( !success )
		{
			final int curReadPosition = readPosition.get();
			final int curWritePosition = writePosition.get();
			final int numReadable = calcNumReadable( curReadPosition, curWritePosition );
			if( numReadable == 0 )
			{
				return 0;
			}
			int length = ( numReadable < maxNum ? numReadable : maxNum );

			// Now check the timestamp to work out how many we will actually read out
			boolean done = false;
			int numToRead = 0;
			for( int c = 0 ; !done && c < length ; c++ )
			{
				int posToCheck = curReadPosition + c;
				if( posToCheck > (bufferLength-1) ) { posToCheck -= bufferLength; };
				final long bufferEventFrameTime = buffer[ posToCheck ].eventFrameTime;
				if( bufferEventFrameTime > endFrameTime )
				{
					if( c == 0 )
					{
						return 0;
					}
					else
					{
						done = true;
					}
				}
				else
				{
					numToRead++;
				}
			}

			if( numToRead > 0 )
			{
				length = numToRead;
			}

			if( curWritePosition > curReadPosition )
			{
				// Copy from the ring buffer directly into the output and update the read position
				copyFromToLength( buffer, curReadPosition, target, pos, length );
				numRead = length;
			}
			else if( curReadPosition > curWritePosition )
			{
				// Case where the read position might loop over the end of the buffer
				if( curReadPosition + length > bufferLength )
				{
					final int numToReadFromEnd = bufferLength - curReadPosition;
					final int numToReadFromStart = length - numToReadFromEnd;
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

			int newPosition = curReadPosition + length;

			if( newPosition >= bufferLength )
			{
				newPosition -= bufferLength;
			}
			success = readPosition.compareAndSet( curReadPosition, newPosition );
		}
		return numRead;
	}

}
