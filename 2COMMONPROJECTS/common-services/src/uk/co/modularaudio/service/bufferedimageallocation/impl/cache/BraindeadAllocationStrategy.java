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

package uk.co.modularaudio.service.bufferedimageallocation.impl.cache;

import java.util.HashSet;

import uk.co.modularaudio.service.bufferedimageallocation.impl.AllocationCacheConfiguration;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;

public class BraindeadAllocationStrategy implements AllocationStrategy
{
//	private static Log log = LogFactory.getLog( BraindeadAllocationStrategy.class.getName() );

	@Override
	public AllocationMatch huntForTileToUse(
			String allocationSource,
			AllocationCacheForImageType allocationCache,
			AllocationCacheConfiguration cacheConfig,
			int imageWidthToUse,
			int imageHeightToUse,
			AllocationMatch allocationMatchToUse ) throws DatastoreException
	{
//		log.debug("Hunting for free entry of minimum size " + imageWidthToUse + ", " + imageHeightToUse );
		HashSet<FreeEntry> freeEntries = allocationCache.getFreeEntries();
		
		FreeEntry smallestFreeEntrySoFar = null;
		int curWidth = Integer.MAX_VALUE;
		int curHeight = Integer.MAX_VALUE;
		
		for( FreeEntry fe : freeEntries )
		{
			int testEntryWidth = fe.getWidth();
			int testEntryHeight = fe.getHeight();
//			log.debug("Checking free entry of size " + testEntryWidth + ", " + testEntryHeight );
			
			boolean shouldUseIt = false;
			if( testEntryWidth >= imageWidthToUse && testEntryHeight >= imageHeightToUse )
			{
//				log.debug("Is big enough");
				if( testEntryWidth <= curWidth && testEntryHeight <= curHeight )
				{
//					log.debug("Is small enough to be a new candidate");
					shouldUseIt = true;
				}
			}
			
			if( shouldUseIt )
			{
				smallestFreeEntrySoFar = fe;
				
				if( imageWidthToUse == testEntryWidth && imageHeightToUse == testEntryHeight )
				{
					// Use it, we won't find any better
//					log.debug("It's an exact match, we'll default to it");
					break;
				}
				
				curWidth = testEntryWidth;
				curHeight = testEntryHeight;
			}
		}
		// We iterated over all of them - see if the largest we found is good enough
		if( curWidth >= imageWidthToUse && curHeight >= imageHeightToUse && smallestFreeEntrySoFar != null )
		{
//			log.debug("After search we will make subimage from free entry of dimension " + curWidth + ", " + curHeight );
			// We'll use this one.
			TiledBufferedImage foundTile = allocationCache.makeSubimageFromExistingFree( allocationSource,
					allocationMatchToUse,
					smallestFreeEntrySoFar,
					imageWidthToUse,
					imageHeightToUse );
			
			allocationMatchToUse.setFoundTile( foundTile );
		}
		else
		{
//			log.debug("Didn't find a candidate large enough");
			// Never find an image to use.
			allocationMatchToUse.setFoundTile( null );
		}
		return allocationMatchToUse;
	}
}
