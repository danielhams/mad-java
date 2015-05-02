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

package uk.co.modularaudio.service.samplecaching.impl;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.samplecaching.impl.SampleCacheBlock.SampleCacheBlockEnum;
import uk.co.modularaudio.util.audio.floatblockpool.BlockBufferingConfiguration;
import uk.co.modularaudio.util.audio.floatblockpool.BlockNotAvailableException;
import uk.co.modularaudio.util.audio.floatblockpool.FloatBufferBlock;
import uk.co.modularaudio.util.audio.floatblockpool.FloatBufferBlockPool;

public class TemperatureBufferBlockMap
{
	private static Log log = LogFactory.getLog( TemperatureBufferBlockMap.class.getName() );

	private final FloatBufferBlockPool allocationPool;

	private final OpenLongObjectHashMap<SampleCacheBlock> blockMap = new OpenLongObjectHashMap<SampleCacheBlock>();

	private final HashSet<SampleCacheBlock> hotBlocks = new HashSet<SampleCacheBlock>();
	private final Queue<SampleCacheBlock> warmBlocks = new ArrayDeque<SampleCacheBlock>();

	public TemperatureBufferBlockMap( final BlockBufferingConfiguration blockBufferingConfiguration )
	{
		allocationPool = new FloatBufferBlockPool( blockBufferingConfiguration );
	}

	public void allocate()
	{
		allocationPool.allocate();
	}

	public void destroy()
	{
		allocationPool.destroy();
	}

	public HashSet<SampleCacheBlock> getHotBlocks()
	{
		return hotBlocks;
	}

	public SampleCacheBlock getBlockById( final long blockID )
	{
		return blockMap.get( blockID );
	}

	public SampleCacheBlock getWarmOrFreeBlockCopyID( final long blockID ) throws BlockNotAvailableException
	{
		SampleCacheBlock retVal = null;

		final int numFreeBlocks = allocationPool.getNumFreeBlocks();
		if( numFreeBlocks > 0 )
		{
//			log.debug("Using a free block");
			final FloatBufferBlock block = allocationPool.reserveBlock();

			retVal = new SampleCacheBlock( blockID, block );
		}
		else
		{
//			log.debug("Repurposing a warm block");
			return repurposeWarmBlock( blockID );
		}
		return retVal;
	}

	public SampleCacheBlock moveBlockFromHotToWarmQueue( final long blockID ) throws BlockNotAvailableException
	{
		final SampleCacheBlock retVal = blockMap.get( blockID );

		if( retVal == null )
		{
			final String msg = "Failed to find hot block to move to warm queue with ID " + blockID;
			log.error( msg );
			throw new BlockNotAvailableException( msg );
		}

		hotBlocks.remove( retVal );

		final SampleCacheBlockEnum curStatus = retVal.useStatus.get();

		if( curStatus == SampleCacheBlockEnum.WARM )
		{
			final String msg = "Expected hot block - but is warm! " + blockID;
			log.error( msg );
			throw new BlockNotAvailableException( msg );
		}

		while( !retVal.useStatus.compareAndSet( curStatus, SampleCacheBlockEnum.WARM ) )
		{
		}

		warmBlocks.add( retVal );

		return retVal;
	}

	public void setBlockMakeHot( final long blockId, final SampleCacheBlock blockToUse )
	{
		blockToUse.useStatus.set( SampleCacheBlockEnum.HOT );
		blockMap.put( blockId, blockToUse );
		hotBlocks.add( blockToUse );
	}

	public void reheatBlock( final SampleCacheBlock curBlock )
	{
		warmBlocks.remove( curBlock );
		assert curBlock.useStatus.get() == SampleCacheBlockEnum.WARM;
		curBlock.useStatus.set( SampleCacheBlockEnum.HOT );
		hotBlocks.add( curBlock );
	}

	private SampleCacheBlock repurposeWarmBlock( final long replacementBlockID )
		throws BlockNotAvailableException
	{
		SampleCacheBlock retVal = null;
		try
		{
			retVal = warmBlocks.remove();
		}
		catch( final NoSuchElementException nsee )
		{
			throw new BlockNotAvailableException();
		}

		// Remove it from the block map first before we reset it's status
		blockMap.removeKey( retVal.blockID );

		// Loop around attempting to set it to moving
		assert retVal.useStatus.get() == SampleCacheBlockEnum.WARM;
		retVal.useStatus.set( SampleCacheBlockEnum.MOVING );
		retVal.blockID = replacementBlockID;

		return retVal;
	}

	public void dumpDetails()
	{
		final int numFloatsInBlock = allocationPool.getNumFloatsInBlock();
		if( log.isDebugEnabled() )
		{
			log.debug("Temperature Buffer Block Map Details (each block is " + numFloatsInBlock + " floats):");
			log.debug( "Total(" +  allocationPool.getNumTotalBlocks() + ") Free(" + allocationPool.getNumFreeBlocks() + ") Used(" + allocationPool.getNumUsedBlocks() +")");
			log.debug( "Hot(" + hotBlocks.size() + ") Warm(" + warmBlocks.size() + ")");
		}
	}
}
