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

import uk.co.modularaudio.util.audio.buffer.LocklessFloatRingBuffer;
import uk.co.modularaudio.util.audio.buffer.UnsafeFloatRingBuffer;

public class BackendToFrontendDataRingBuffer extends LocklessFloatRingBuffer
{
//	private static Log log = LogFactory.getLog( BackendToFrontendDataRingBuffer.class.getName() );

	protected int numSamplesQueued;

	public BackendToFrontendDataRingBuffer( final int ringLength )
	{
		super( ringLength );
	}

	public int getWritePosition()
	{
		return writePosition.get();
	}

	public int frontEndGetNumReadableWithWriteIndex( final int writePosition )
	{
		final int curReadPosition = readPosition.get();
		return calcNumReadable(curReadPosition, writePosition);
	}

	@Override
	public void clear()
	{
		numSamplesQueued = 0;
		super.clear();
	}

	public int getBufferLength()
	{
		return bufferLength;
	}

	public int frontEndReadToRingWithWriteIndex( final int rwritePosition, final UnsafeFloatRingBuffer targetRing, final int numToRead )
	{
		final int rreadPosition = readPosition.get();
		final int numReadable = calcNumReadable( rreadPosition, rwritePosition );
		final int numTargetWriteable = targetRing.getNumWriteable();

		if( numTargetWriteable < numToRead || numReadable < numToRead )
		{
			return 0;
		}
		else
		{
			final int numTargetWriteableAtOnce = (targetRing.readPosition < targetRing.writePosition ?
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
					System.arraycopy( buffer, rreadPosition, targetRing.buffer, targetRing.writePosition, numToRead);
//					log.trace("RTRWWI Case1");
				}
				else
				{
					// Two bits
					final int firstSize = numReadableAtOnce;
					final int secondSize = numToRead - firstSize;
					System.arraycopy( buffer, rreadPosition, targetRing.buffer, targetRing.writePosition, firstSize );

					System.arraycopy( buffer, 0, targetRing.buffer, targetRing.writePosition + firstSize, secondSize );

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

					System.arraycopy( buffer, rreadPosition, targetRing.buffer, targetRing.writePosition, firstSize );

					System.arraycopy( buffer, 0, targetRing.buffer, targetRing.writePosition + firstSize, secondSize );

					if( thirdSize > 0 )
					{
						System.arraycopy( buffer, 0 + secondSize, targetRing.buffer, 0, thirdSize );
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

					System.arraycopy( buffer, rreadPosition, targetRing.buffer, targetRing.writePosition, firstSize );

					System.arraycopy( buffer, rreadPosition + firstSize, targetRing.buffer, 0, secondSize );

					if( thirdSize > 0 )
					{
						System.arraycopy( buffer, 0, targetRing.buffer, secondSize, thirdSize );
					}
//					log.trace("RTRWWI Case4");
				}
				newTargetWritePosition = newTargetWritePosition % targetRing.bufferLength;
			}
			targetRing.writePosition = newTargetWritePosition;
		}
		final int newReadPosition = rreadPosition + numToRead;
		readPosition.set( newReadPosition % bufferLength );
		return numToRead;
	}

	public int backEndGetNumSamplesQueued()
	{
		return numSamplesQueued;
	}

	public void backEndClearNumSamplesQueued()
	{
		numSamplesQueued = 0;
	}

	public int backEndWrite( final float[] source, final int pos, final int length )
	{
		final int numWritten = super.write( source, pos, length );
		numSamplesQueued += length;
		return numWritten;
	}

	@Override
	public int write( final float[] s, final int p, final int l)
	{
		throw new RuntimeException("Use backEndWrite");
	}
}
