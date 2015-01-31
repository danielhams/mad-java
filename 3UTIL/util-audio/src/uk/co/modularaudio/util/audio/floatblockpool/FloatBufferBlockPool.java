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

package uk.co.modularaudio.util.audio.floatblockpool;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FloatBufferBlockPool
{
	private static Log log = LogFactory.getLog( FloatBufferBlockPool.class.getName() );

	private final int capacity;
	private final int defaultBlockSize;
	private ArrayList<FloatBufferBlock> freeBlocks = null;
	private ArrayList<FloatBufferBlock> usedBlocks = null;

	public FloatBufferBlockPool( final BlockBufferingConfiguration blockBufferingConfiguration )
	{
		this( blockBufferingConfiguration.maxBlocksToBuffer,
				blockBufferingConfiguration.blockLengthInFloats );
	}

	private FloatBufferBlockPool( final int capacity, final int defaultBlockSize )
	{
		this.capacity = capacity;
		this.defaultBlockSize = defaultBlockSize;
		freeBlocks = new ArrayList<FloatBufferBlock>( capacity );
		usedBlocks = new ArrayList<FloatBufferBlock>( capacity );
	}

	public void allocate()
	{
		if( log.isTraceEnabled() )
		{
			log.trace( "Allocating " + capacity + " blocks of size " + defaultBlockSize );
		}
		// And create empty blocks and add them to the free list
		for( int i = 0 ; i < capacity ; i++ )
		{
			final float[] newArray = new float[ defaultBlockSize ];
			final FloatBufferBlock freshBlock = new FloatBufferBlock( newArray, defaultBlockSize );
			freeBlocks.add( freshBlock );
		}
		log.trace( "Allocation complete");
	}

	public void destroy()
	{
		usedBlocks.clear();
		freeBlocks.clear();
	}

	public FloatBufferBlock reserveBlock() throws BlockNotAvailableException
	{
		FloatBufferBlock retVal;
		try
		{
			retVal = freeBlocks.remove( freeBlocks.size() - 1 );
		}
		catch (final NoSuchElementException e)
		{
			throw new BlockNotAvailableException();
		}
		usedBlocks.add( retVal );
		return retVal;
	}

	public void returnBlock( final FloatBufferBlock usedBlock )
	{
		usedBlocks.remove( usedBlock );
		freeBlocks.add( usedBlock );
	}

	public int getNumFreeBlocks()
	{
		return freeBlocks.size();
	}

	public int getNumUsedBlocks()
	{
		return usedBlocks.size();
	}

	public int getNumTotalBlocks()
	{
		return capacity;
	}

	public int getNumFloatsInBlock()
	{
		return defaultBlockSize;
	}
}
