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

package uk.co.modularaudio.util.audio.mad.buffer;

import java.util.concurrent.atomic.AtomicInteger;

import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;

public class MultiChannelBackendToFrontendDataRingBuffer
{
//	private static Log log = LogFactory.getLog( BackendToFrontendDataRingBuffer.class.getName() );

	protected int numSamplesQueued;

	private final int capacity;
	private final int bufferLength;
	private final int numChannels;
	private final float[][] buffers;
	private final AtomicInteger readPosition = new AtomicInteger(0);
	private final AtomicInteger writePosition = new AtomicInteger(0);

	public MultiChannelBackendToFrontendDataRingBuffer( final int numChannels, final int capacity )
	{
		this.capacity = capacity;
		this.bufferLength = capacity + 1;
		this.numChannels = numChannels;
		buffers = new float[numChannels][];
		for( int i = 0 ; i < numChannels ; ++i )
		{
			buffers[i] = new float[ bufferLength ];
		}
	}

	public int getWritePosition()
	{
		return writePosition.get();
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

	public int frontEndGetNumReadableWithWriteIndex( final int writePosition )
	{
		final int curReadPosition = readPosition.get();
		return calcNumReadable(curReadPosition, writePosition);
	}

	public void clear()
	{
		numSamplesQueued = 0;
		readPosition.set( 0 );
		writePosition.set( 0 );
	}

	public int getBufferLength()
	{
		return bufferLength;
	}

	public int frontEndReadToRingWithWriteIndex( final int rwritePosition,
			final UnsafeFloatRingBuffer[] targetRings,
			final int numToRead )
	{
		final int rreadPosition = readPosition.get();
		final int numReadable = calcNumReadable( rreadPosition, rwritePosition );
		// Use the first ring buffer for position calculations
		final UnsafeFloatRingBuffer targetRing = targetRings[0];
		final int numTargetWriteable = targetRing.getNumWriteable();

		if( numTargetWriteable < numToRead || numReadable < numToRead )
		{
			return 0;
		}
		else
		{
			final int numTargetWriteableAtOnce = (targetRing.readPosition <= targetRing.writePosition ?
					targetRing.bufferLength - targetRing.writePosition :
					(targetRing.readPosition - 1) - targetRing.writePosition );
			final int numReadableAtOnce = (rreadPosition < rwritePosition ?
					(rwritePosition - rreadPosition) :
						(bufferLength - rreadPosition) );

			int newTargetWritePosition = targetRing.writePosition + numToRead;

			if( numTargetWriteableAtOnce >= numToRead )
			{
				// All in one blob for write
				if( numReadableAtOnce >= numToRead )
				{
					// All at once
					for( int c = 0 ; c < numChannels ; ++c )
					{
						System.arraycopy( buffers[c], rreadPosition, targetRings[c].buffer, targetRing.writePosition, numToRead);
					}
//					log.trace("RTRWWI Case1");
				}
				else
				{
					// Two bits
					final int firstSize = numReadableAtOnce;
					final int secondSize = numToRead - firstSize;

					for( int c = 0 ; c < numChannels ; ++c )
					{
						System.arraycopy( buffers[c], rreadPosition, targetRings[c].buffer, targetRing.writePosition, firstSize );

						System.arraycopy( buffers[c], 0, targetRings[c].buffer, targetRing.writePosition + firstSize, secondSize );
					}

					newTargetWritePosition = newTargetWritePosition % targetRing.bufferLength;
//					log.trace("RTRWWI Case2");
				}
			}
			else
			{
				// In three bits - either we can't read enough for part one
				// or we can't write enough
				if( numTargetWriteableAtOnce >= numReadableAtOnce )
				{
					// Driven by read size
					final int firstSize = numReadableAtOnce;
					final int secondSize = numTargetWriteableAtOnce - firstSize;
					final int firstAndSecondSize = firstSize + secondSize;
					final int thirdSize = numToRead - firstAndSecondSize;

					for( int c = 0 ; c < numChannels ; ++c )
					{
						System.arraycopy( buffers[c], rreadPosition, targetRings[c].buffer, targetRing.writePosition, firstSize );

						System.arraycopy( buffers[c], 0, targetRings[c].buffer, targetRing.writePosition + firstSize, secondSize );
						if( thirdSize > 0 )
						{
							System.arraycopy( buffers[c], 0 + secondSize, targetRings[c].buffer, 0, thirdSize );
						}
					}

//					log.trace("RTRWWI Case3");
				}
				else
				{
					// Driven by write size
					final int firstSize = numTargetWriteableAtOnce;
					final int secondSize = numReadableAtOnce - firstSize;
					final int firstAndSecondSize = firstSize + secondSize;
					final int thirdSize = numToRead - firstAndSecondSize;

					for( int c = 0 ; c < numChannels ; ++c )
					{
						System.arraycopy( buffers[c], rreadPosition, targetRings[c].buffer, targetRing.writePosition, firstSize );

						System.arraycopy( buffers[c], rreadPosition + firstSize, targetRings[c].buffer, 0, secondSize );

						if( thirdSize > 0 )
						{
							System.arraycopy( buffers[c], 0, targetRings[c].buffer, secondSize, thirdSize );
						}
					}

//					log.trace("RTRWWI Case4");
				}
				newTargetWritePosition = newTargetWritePosition % targetRing.bufferLength;
			}
			for( int c = 0 ; c < numChannels ; ++c )
			{
				targetRings[c].writePosition = newTargetWritePosition;
			}
		}
		final int newReadPosition = rreadPosition + numToRead;
		readPosition.set( newReadPosition % bufferLength );
		return numToRead;
	}

	public int read( final float targets[][], final int pos, final int length )
	{
		return internalRead( readPosition.get(), writePosition.get(), targets, pos, length, true, false );
	}

	protected int internalRead( final int rp, final int wp,
			final float[][] targets, final int pos, final int length,
			final boolean move,
			final boolean zero )
	{
		int amountRead = 0;
		final int curReadPosition = rp;
		final int curWritePosition = wp;

		final int numReadable = calcNumReadable( curReadPosition, curWritePosition );

		if( numReadable < length )
		{
			return -1;
		}
		else
		{
			// Case where the read position might loop over the end of the buffer
			if( curReadPosition + length > bufferLength )
			{
				final int numToReadFromEnd = bufferLength - curReadPosition;
				final int numToReadFromStart = length - numToReadFromEnd;
				for( int c = 0 ; c < numChannels ; ++c )
				{
					System.arraycopy( buffers[c], curReadPosition, targets[c], pos, numToReadFromEnd );
					System.arraycopy( buffers[c], 0, targets[c], pos + numToReadFromEnd, numToReadFromStart );
					if( zero )
					{
						for( int i = 0 ; i < numToReadFromEnd ; i++ )
						{
							buffers[c][ curReadPosition + i ] = 0.0f;
						}
						for( int i = 0 ; i < numToReadFromStart ; i++ )
						{
							buffers[c][ i ] = 0.0f;
						}
					}
				}
			}
			else
			{
				// Fits before the end of the ring
				for( int c = 0 ; c < numChannels ; ++c )
				{
					System.arraycopy( buffers[c], curReadPosition, targets[c], pos, length );
					if( zero )
					{
						for( int i = 0 ; i < length ; i++ )
						{
							buffers[c][ curReadPosition + i ] = 0.0f;
						}
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

	public int backEndGetNumSamplesQueued()
	{
		return numSamplesQueued;
	}

	public void backEndClearNumSamplesQueued()
	{
		numSamplesQueued = 0;
	}

	public int backEndWrite( final float[][] sourceBuffers, final int pos, final int length )
	{
		final int numWritten = internalWrite( readPosition.get(), writePosition.get(), sourceBuffers, pos, length, true );
		numSamplesQueued += length;
		return numWritten;
	}

	protected int internalWrite( final int rp, final int wp, final float sourceBuffers[][], final int pos, final int length, final boolean move )
	{
		if( length > capacity )
		{
			return -1;
		}
		int amountWritten = 0;

		final int curReadPosition = rp;
		final int curWritePosition = wp;
		final int numWriteable = calcNumWriteable( curReadPosition, curWritePosition );
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
				final int numToWriteAtEnd = bufferLength - curWritePosition;
				final int numToWriteAtStart = length - numToWriteAtEnd;
				for( int c = 0 ; c < numChannels ; ++c )
				{
					System.arraycopy( sourceBuffers[c], pos, buffers[c], curWritePosition, numToWriteAtEnd );
					System.arraycopy( sourceBuffers[c], pos + numToWriteAtEnd, buffers[c], 0, numToWriteAtStart );
				}
			}
			else
			{
				// Fits before the end of the ring
				for( int c = 0 ; c < numChannels ; ++c )
				{
					System.arraycopy( sourceBuffers[c], pos, buffers[c], curWritePosition, length );
				}
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

	public int getNumWriteable()
	{
		final int curReadPosition = readPosition.get();
		final int curWritePosition = writePosition.get();
		return calcNumWriteable(curReadPosition, curWritePosition);
	}
}
