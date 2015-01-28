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
import java.util.concurrent.locks.ReentrantLock;


public class BlockingWriteRingBuffer extends WriteSemaphoreLocklessRingBuffer implements BlockingRingBufferInterface
{
	private final ReentrantLock internalLock = new ReentrantLock();

	public BlockingWriteRingBuffer(final int capacity)
	{
		super(capacity);
	}

	@Override
	public boolean readMaybeBlock(final float[] target, final int pos, final int length) throws BufferUnderflowException, InterruptedException
	{
		final boolean didBlock =  false;
		internalLock.lock();
		try
		{
			final int rp = readPosition.get();
			final int wp = writePosition.get();
			final int numReadable = calcNumReadable( rp, wp );
			if( numReadable >= length )
			{
				super.internalRead( rp, wp, target, pos, length, true, false );
				internalLock.notify();
			}
		}
		finally
		{
			internalLock.unlock();
		}
		return didBlock;
	}

	@Override
	public boolean writeMaybeBlock(final float[] source, final int pos, final int length) throws InterruptedException
	{
		boolean didBlock = false;
		internalLock.lock();
		try
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
		finally
		{
			internalLock.unlock();
		}
		return didBlock;
	}

}
