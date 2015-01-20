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

	protected int numSamplesQueued = 0;
	
	public BackendToFrontendDataRingBuffer( int ringLength )
	{
		super( ringLength );
	}
	
	public int getWritePosition()
	{
		return writePosition.get();
	}

	public int getNumReadableWithWriteIndex( int writePosition )
	{
		int curReadPosition = readPosition.get();
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
	
	public int readToRingWithWriteIndex( int rwritePosition, UnsafeFloatRingBuffer targetRing, int numToRead )
	{
		int rreadPosition = readPosition.get();
		int numReadable = calcNumReadable( rreadPosition, rwritePosition );
		int numTargetWriteable = targetRing.getNumWriteable();
		
		if( numTargetWriteable < numToRead || numReadable < numToRead )
		{
			return 0;
		}
		else
		{
			int numTargetWriteableAtOnce = (targetRing.readPosition < targetRing.writePosition ?
					targetRing.bufferLength - targetRing.writePosition :
					(targetRing.readPosition - 1) - targetRing.writePosition );
			int numReadableAtOnce = (rreadPosition < rwritePosition ?
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
					int firstSize = numReadableAtOnce;
					int secondSize = numToRead - firstSize;
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
					int firstSize = numReadableAtOnce;
					int secondSize = numTargetWriteableAtOnce - firstSize;
					int firstAndSecondSize = firstSize + secondSize;
					int thirdSize = numToRead - firstAndSecondSize;
					
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
					int firstSize = numTargetWriteableAtOnce;
					int secondSize = numReadableAtOnce - firstSize;
					int firstAndSecondSize = firstSize + secondSize;
					int thirdSize = numToRead - firstAndSecondSize;
					
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
		int newReadPosition = rreadPosition + numToRead;
		readPosition.set( newReadPosition % bufferLength );
		return numToRead;
	}

	public int getNumSamplesQueued()
	{
		return numSamplesQueued;
	}

	public void setNumSamplesQueued( int numSamplesQueued )
	{
		this.numSamplesQueued = numSamplesQueued;
	}
	
	
}
