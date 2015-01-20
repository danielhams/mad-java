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

import java.nio.BufferUnderflowException;


public class BlockingWriteRingBuffer extends WriteSemaphoreLocklessRingBuffer implements BlockingRingBufferInterface
{
	private Integer internalLock = new Integer(0);

	public BlockingWriteRingBuffer(int capacity)
	{
		super(capacity);
	}

	public boolean readMaybeBlock(float[] target, int pos, int length) throws BufferUnderflowException, InterruptedException
	{
		boolean didBlock =  false;
		synchronized( internalLock )
		{
			int rp = readPosition.get();
			int wp = writePosition.get();
			int numReadable = calcNumReadable( rp, wp );
			if( numReadable >= length )
			{
				super.internalRead( rp, wp, target, pos, length, true, false );
				internalLock.notify();
			}
		}
		return didBlock;
	}

	public boolean writeMaybeBlock(float[] source, int pos, int length) throws InterruptedException
	{
		boolean didBlock = false;
		synchronized (internalLock)
		{
			int rp = readPosition.get();
			int wp = writePosition.get();
			int numWriteable = calcNumWriteable( rp, wp );
			while( numWriteable < length )
			{
				didBlock = true;
				internalLock.wait();
				rp = readPosition.get();
				wp = writePosition.get();
				numWriteable = calcNumWriteable( rp, wp );
			}
			super.internalWrite( rp, wp, source, pos, length, true );
		}
		return didBlock;
	}

}
