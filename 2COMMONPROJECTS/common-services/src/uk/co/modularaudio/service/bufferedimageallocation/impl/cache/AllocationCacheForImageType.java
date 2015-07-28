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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.impl.AllocationCacheConfiguration;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.exception.DatastoreException;

public class AllocationCacheForImageType
{
	private static Log log = LogFactory.getLog( AllocationCacheForImageType.class.getName() );

	private final String cacheName;
	private final AllocationBufferType allocationBufferType;

	private final ReentrantLock internalLock = new ReentrantLock();

	private final int stdAllocImageWidth;
	private final int stdAllocImageHeight;
	private final int initialPages;

	private long imageAllocationRawIdCounter = 0;
	private long imageAllocationAssignedIdCounter = 0;

	private final Set<RawImage> rawImageSet = new HashSet<RawImage>();
	private final OpenLongObjectHashMap<RawImage> rawImageIdToImageMap = new OpenLongObjectHashMap<RawImage>();
	private final Set<UsedEntry> usedEntrySet = new HashSet<UsedEntry>();
	private final Map<RawImage,HashSet<UsedEntry>> rawImageToUsedEntryMap = new HashMap<RawImage, HashSet<UsedEntry>>();
	private final HashSet<FreeEntry> freeEntrySet = new HashSet<FreeEntry>();
	private final Map<RawImage,HashSet<FreeEntry>> rawImageToFreeEntryMap = new HashMap<RawImage,HashSet<FreeEntry>>();

	private final Decomposition decompositionForAllocation = new Decomposition();
	private final Recomposition recompositionForFree = new Recomposition();

	public AllocationCacheForImageType( final String name,
			final AllocationCacheConfiguration cacheConfiguration,
			final AllocationLifetime lifetime,
			final AllocationBufferType allocationBufferType )
			throws DatastoreException
	{
		this.cacheName = name;
		this.allocationBufferType = allocationBufferType;
		stdAllocImageWidth = cacheConfiguration.getStdAllocImageWidth();
		stdAllocImageHeight = cacheConfiguration.getStdAllocImageHeight();
		try
		{
			initialPages = cacheConfiguration.getInitialPagesForLifetimeAndType( lifetime, allocationBufferType );
			if( log.isDebugEnabled() )
			{
				log.debug("Creating image cache named " + name +" of type " + allocationBufferType + " with " + initialPages + " initial pages.");
			}

			populateInitialPages();
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught during initial page population of buffered image cache: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private void populateInitialPages() throws DatastoreException
	{
		internalLock.lock();
		try{
			for( int p = 0 ; p < initialPages ; p++ )
			{
				// Allocate raw image
				final BufferedImage bufferedImage = doRealAllocation( stdAllocImageWidth,
						stdAllocImageHeight );

				final Graphics2D rawGraphics = bufferedImage.createGraphics();

				final long rawImageId = imageAllocationRawIdCounter++;
				final RawImage rawImage = new RawImage( bufferedImage, rawGraphics, rawImageId );
				unsynchronisedAddRawImage( rawImageId, rawImage );

				// And create a free entry for it
				final FreeEntry initialFreeEntry = new FreeEntry( rawImage, 0, 0, stdAllocImageWidth, stdAllocImageHeight );
				unsynchronisedAddFreeEntry( initialFreeEntry );
			}
		}
		finally
		{
			internalLock.unlock();
		}
	}

	private BufferedImage doRealAllocation( final int desiredWidth, final int desiredHeight )
		throws DatastoreException
	{
		BufferedImage bufferedImage = null;

		switch( allocationBufferType.getJavaBufferedImageType() )
		{
			case BufferedImage.TYPE_INT_RGB:
			{
				bufferedImage = new BufferedImage( desiredWidth, desiredHeight, BufferedImage.TYPE_INT_RGB );
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				bufferedImage = new BufferedImage( desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB );
				break;
			}
			default:
			{
				final String msg = "Unknown image type during allocation: " + allocationBufferType;
				throw new DatastoreException( msg );
			}
		}

		return bufferedImage;
	}

	public TiledBufferedImage makeSubimageFromExistingFree(
			final String allocationSource,
			final AllocationMatch allocationMatchToUse,
			final FreeEntry freeEntryToChopUp,
			final int imageWidthToUse,
			final int imageHeightToUse )
	{
		// Remove the existing free entry, it'll be replaced with what's necessary
		unsynchronisedRemoveFreeEntry( freeEntryToChopUp );

		final long assignedId = imageAllocationAssignedIdCounter++;
		final RawImage rawImage = freeEntryToChopUp.getSourceRawImage();

		final int freeEntryStartX = freeEntryToChopUp.getX();
		final int freeEntryWidth = freeEntryToChopUp.getWidth();
		final int freeEntryStartY = freeEntryToChopUp.getY();
		final int freeEntryHeight = freeEntryToChopUp.getHeight();

		final BufferedImage rootBufferedImage = rawImage.getRootBufferedImage();

		// Clear the region we'll use
		rawImage.clearRegion( freeEntryStartX, freeEntryStartY, imageWidthToUse, imageHeightToUse );

		final BufferedImage subImage = rootBufferedImage.getSubimage( freeEntryStartX,
				freeEntryStartY,
				imageWidthToUse,
				imageHeightToUse );

		final UsedEntry usedEntry = new UsedEntry( allocationSource, rawImage, assignedId, freeEntryStartX, freeEntryStartY, imageWidthToUse, imageHeightToUse, subImage );
		unsychronisedAddUsedEntry( usedEntry );

		// Create free entries for any left space
		if( imageWidthToUse !=  freeEntryWidth || imageHeightToUse != freeEntryHeight )
		{
			// Initial assignment is always top left
//			int widthLeft = freeEntryWidth - imageWidthToUse;
//			int heightLeft = freeEntryHeight - imageHeightToUse;
//			log.debug("Creating free entries after sub image of ( " + imageWidthToUse+", " + imageHeightToUse + " ) sizesleft ( " + widthLeft + ", " + heightLeft + " )");

			// One of the dimensions is probably smaller than the other - we want to create one big space and
			// one little one.
			BlockDecomposer.decomposeImage( decompositionForAllocation, freeEntryWidth, freeEntryHeight, imageWidthToUse, imageHeightToUse );

//			log.debug("Smaller block is " + decompositionForAllocation.smallBlockToString() );
//			log.debug("Larger block is " + decompositionForAllocation.largeBlockToString() );

			// Need to add back on the original offsets
			final FreeEntry smallerFe = new FreeEntry( rawImage,
					freeEntryStartX + decompositionForAllocation.smallerBlockX,
					freeEntryStartY + decompositionForAllocation.smallerBlockY,
					decompositionForAllocation.smallerBlockWidth,
					decompositionForAllocation.smallerBlockHeight );

			unsynchronisedAddFreeEntry( smallerFe );

			if( decompositionForAllocation.hasLarger )
			{
				final FreeEntry largerFe = new FreeEntry( rawImage,
						freeEntryStartX + decompositionForAllocation.largerBlockX,
						freeEntryStartY + decompositionForAllocation.largerBlockY,
						decompositionForAllocation.largerBlockWidth,
						decompositionForAllocation.largerBlockHeight );

				unsynchronisedAddFreeEntry( largerFe );
			}
		}
		else
		{
			// The allocation exactly matches the raw image, don't add any free entries for it.
//			log.debug("Allocation matches image - no free entry creation.");
		}
		// Return the necessary structure
		final TiledBufferedImageImpl theImpl = new TiledBufferedImageImpl( this, usedEntry, subImage );
		allocationMatchToUse.setFoundTile( theImpl );

		return allocationMatchToUse.getFoundTile();
	}

	public TiledBufferedImage allocateNewRawImageMakeSubimage(
			final String allocationSource,
			final AllocationMatch allocationMatchToUse,
			final int imageWidthToUse,
			final int imageHeightToUse )
		throws DatastoreException
	{
		internalLock.lock();
		try
		{

			// Check if the image falls in the bounds of what we cache.
			int widthToAlloc = -1;
			int heightToAlloc = -1;
			if( stdAllocImageWidth >= imageWidthToUse && stdAllocImageHeight >= imageHeightToUse )
			{
				widthToAlloc = stdAllocImageWidth;
				heightToAlloc = stdAllocImageHeight;
			}
			else
			{
				// The requested image is larger in one or both dimensions of what we cache.
				// Create a custom assignment
				widthToAlloc = imageWidthToUse;
				heightToAlloc = imageHeightToUse;
				if( log.isDebugEnabled() )
				{
					log.debug( "Allocating image larger than cache dimensions: " + imageWidthToUse + ", " + imageHeightToUse );
				}
			}

			// Allocate raw image
			final BufferedImage bufferedImage = doRealAllocation( widthToAlloc, heightToAlloc );

			final Graphics2D rawGraphics = bufferedImage.createGraphics();

			final long rawImageId = imageAllocationRawIdCounter++;
			final RawImage rawImage = new RawImage( bufferedImage, rawGraphics, rawImageId );
			unsynchronisedAddRawImage( rawImageId, rawImage );

			// Create a subimage and used entry
			final long assignedId = imageAllocationAssignedIdCounter++;
			final BufferedImage subImage = bufferedImage.getSubimage( 0, 0, imageWidthToUse, imageHeightToUse );

			final UsedEntry usedEntry = new UsedEntry( allocationSource, rawImage, assignedId, 0, 0, imageWidthToUse, imageHeightToUse, subImage );
			unsychronisedAddUsedEntry( usedEntry );

			// Create free entries for any left space
			if( imageWidthToUse != widthToAlloc || imageHeightToUse != heightToAlloc )
			{
				// Initial assignment is always top left
	//				int widthLeft = widthToAlloc - imageWidthToUse;
	//				int heightLeft = heightToAlloc - imageHeightToUse;
	//				log.debug("Creating free entries after sub image of ( " + imageWidthToUse+", " + imageHeightToUse + " ) sizesleft ( " + widthLeft + ", " + heightLeft + " )");

				// One of the dimensions is probably smaller than the other - we want to create one big space and
				// one little one.
				BlockDecomposer.decomposeImage( decompositionForAllocation, widthToAlloc, heightToAlloc, imageWidthToUse, imageHeightToUse );

	//				log.debug("Smaller block is " + decompositionForAllocation.smallBlockToString() );
	//				log.debug("Larger block is " + decompositionForAllocation.largeBlockToString() );

				final FreeEntry smallerFe = new FreeEntry( rawImage,
						decompositionForAllocation.smallerBlockX,
						decompositionForAllocation.smallerBlockY,
						decompositionForAllocation.smallerBlockWidth,
						decompositionForAllocation.smallerBlockHeight );

				unsynchronisedAddFreeEntry( smallerFe );

				if( decompositionForAllocation.hasLarger )
				{
					final FreeEntry largerFe = new FreeEntry( rawImage,
							decompositionForAllocation.largerBlockX,
							decompositionForAllocation.largerBlockY,
							decompositionForAllocation.largerBlockWidth,
							decompositionForAllocation.largerBlockHeight );

					unsynchronisedAddFreeEntry( largerFe );
				}
			}
			else
			{
				// The allocation exactly matches the raw image, don't add any free entries for it.
	//				log.debug("Allocation matches image - no free entry creation.");
			}

			// Return the necessary structure
			final TiledBufferedImageImpl theImpl = new TiledBufferedImageImpl( this, usedEntry, subImage );
			allocationMatchToUse.setFoundTile( theImpl );
		}
		finally
		{
			internalLock.unlock();
		}
		return allocationMatchToUse.getFoundTile();
	}

	public void freeTiledBufferedImage( final TiledBufferedImageImpl realImpl )
	{
		internalLock.lock();
		try
		{
			final UsedEntry usedEntry = realImpl.getUsedEntry();
//			long assignedUsedEntryId = usedEntry.getAssignedId();
			final RawImage usedRawImage = usedEntry.getRawImage();

			// Remove the "used" entry
			unsynchronisedRemoveUsedEntry( usedEntry );

			// Easy cases:
			// (1) Image was larger in one dimension than the standard allocation
			// If so, we just let it go and don't let it be re-used.
			// (2) Image exactly matches the standard allocation
			// so add it directly to the free entry set
			// (3) Image was a subimage - need to recompose it with any existing
			// free space for the raw image and possibly consolidate it
			final int usedX = usedEntry.getX();
			final int usedY = usedEntry.getY();
			final int usedWidth = usedEntry.getWidth();
			final int usedHeight = usedEntry.getHeight();
			final boolean wasLargerThanStandard = (usedWidth > stdAllocImageWidth || usedHeight > stdAllocImageHeight );
			if( wasLargerThanStandard )
			{
				// Do nothing.
//				log.debug("Returned tiled buffered image was larger than we alloc. Letting it go.");
				unsynchronisedRemoveRawImage( usedRawImage );
			}
			else if( usedWidth == stdAllocImageWidth && usedHeight == stdAllocImageHeight )
			{
				// Directly release it
//				log.debug("Returned tiled buffered image was an exact match for what we alloc. Creating free entry for complete image.");
				final FreeEntry fe = new FreeEntry( usedRawImage, 0, 0, stdAllocImageWidth, stdAllocImageHeight );
				unsynchronisedAddFreeEntry( fe );
			}
			else
			{
				// Recompose (if possible) with existing free entries for the raw image
//				log.debug("Returned tiled buffered image was a sub image of what we alloc. Attempting to recompose with free entries.");
				// Get related free entries
				final Set<FreeEntry> freeEntriesForRawImage = rawImageToFreeEntryMap.get( usedRawImage );
				if( BlockRecomposer.canRecomposeWithFreeEntries( usedRawImage, freeEntriesForRawImage, recompositionForFree, usedEntry ) )
				{
//					log.debug("Able to recompose: " + recompositionForFree.toString() );
					for( final FreeEntry entryToRemove : recompositionForFree.freeEntriesToRemove )
					{
						unsynchronisedRemoveFreeEntry( entryToRemove );
					}
					// And add the new free entry
					final FreeEntry newFreeEntry = new FreeEntry( usedRawImage, recompositionForFree.x, recompositionForFree.y, recompositionForFree.width, recompositionForFree.height );
					unsynchronisedAddFreeEntry( newFreeEntry );
				}
				else
				{
					// Can't recompose, just add a free entry for the size of the released image
//					log.debug("Unable to recompose - will add free entry for tiled image");
					final FreeEntry fe = new FreeEntry( usedRawImage, usedX, usedY, usedWidth, usedHeight );
					unsynchronisedAddFreeEntry( fe );
				}
			}
		}
		finally
		{
			internalLock.unlock();
		}
	}

	public int getNumRaw()
	{
		int retVal = 0;
		internalLock.lock();
		try
		{
			retVal = rawImageSet.size();
		}
		finally
		{
			internalLock.unlock();
		}
		return retVal;
	}

	public int getNumUsed()
	{
		int retVal = 0;
		internalLock.lock();
		try
		{
			retVal = usedEntrySet.size();
		}
		finally
		{
			internalLock.unlock();
		}
		return retVal;
	}

	public int getNumFree()
	{
		int retVal = 0;
		internalLock.lock();
		try
		{
			retVal = freeEntrySet.size();
		}
		finally
		{
			internalLock.unlock();
		}
		return retVal;
	}

	private void unsynchronisedAddRawImage( final long rawImageId, final RawImage rawImage )
	{
		rawImageSet.add( rawImage );
		rawImageIdToImageMap.put( rawImageId, rawImage );
		// Add free and used sets for it
		final HashSet<FreeEntry> rawToFreeSet = new HashSet<FreeEntry>();
		rawImageToFreeEntryMap.put( rawImage, rawToFreeSet );
		final HashSet<UsedEntry> ues = new HashSet<UsedEntry>();
		rawImageToUsedEntryMap.put( rawImage, ues );
	}

	private void unsynchronisedRemoveRawImage( final RawImage rawImage )
	{
		rawImageIdToImageMap.removeKey( rawImage.getRawImageId() );
		rawImageSet.remove( rawImage );

		rawImageToFreeEntryMap.remove( rawImage );
		rawImageToUsedEntryMap.remove( rawImage );
	}

	private void unsynchronisedAddFreeEntry( final FreeEntry freeEntry )
	{
		freeEntrySet.add( freeEntry );
		final RawImage sourceRawImage = freeEntry.getSourceRawImage();
		final Set<FreeEntry> freeEntriesForRaw = rawImageToFreeEntryMap.get( sourceRawImage );
		freeEntriesForRaw.add( freeEntry );
	}

	private void unsynchronisedRemoveFreeEntry( final FreeEntry entryToRemove )
	{
		final RawImage sourceRawImage = entryToRemove.getSourceRawImage();
		final Set<FreeEntry> freeEntriesForRaw = rawImageToFreeEntryMap.get( sourceRawImage );
		if( !freeEntriesForRaw.remove( entryToRemove ) )
		{
			log.error("Attempted to remove a free entry but didn't find the free set entry!");
		}

		freeEntrySet.remove( entryToRemove );
	}

	private void unsychronisedAddUsedEntry( final UsedEntry usedEntry )
	{
		usedEntrySet.add( usedEntry );
		final RawImage rawImage = usedEntry.getRawImage();
		final Set<UsedEntry> ues = rawImageToUsedEntryMap.get( rawImage );
		ues.add( usedEntry );
	}

	private void unsynchronisedRemoveUsedEntry( final UsedEntry usedEntry )
	{
		usedEntrySet.remove( usedEntry );
		final RawImage rawImage = usedEntry.getRawImage();
		final Set<UsedEntry> ues = rawImageToUsedEntryMap.get( rawImage );
		if( !ues.remove( usedEntry ) )
		{
			log.error("Failed to remove used entry from raw image used entry set");
		}
	}

	public Set<RawImage> getRawImages()
	{
		internalLock.lock();
		try
		{
			final Set<RawImage> retSet = new HashSet<RawImage>();
			retSet.addAll( rawImageSet );
			return retSet;
		}
		finally
		{
			internalLock.unlock();
		}
	}

	public RawImage getRawImageById( final long rawImageId )
	{
		RawImage imgToReturn = null;
		internalLock.lock();
		try
		{
			imgToReturn = rawImageIdToImageMap.get( rawImageId );
		}
		finally
		{
			internalLock.unlock();
		}
		return imgToReturn;
	}

	public Set<FreeEntry> getRawImageFreeEntrySet( final RawImage ri )
	{
		Set<FreeEntry> retVal = null;
		internalLock.lock();
		try
		{
			retVal = rawImageToFreeEntryMap.get( ri );
		}
		finally
		{
			internalLock.unlock();
		}
		return retVal;
	}

	public Set<UsedEntry> getRawImageUsedEntrySet( final RawImage ri )
	{
		Set<UsedEntry> retVal = null;
		synchronized( internalLock )
		{
			retVal = rawImageToUsedEntryMap.get( ri );
		}
		return retVal;
	}

	protected HashSet<FreeEntry> getFreeEntries()
	{
		return freeEntrySet;
	}

	public void debugFreeEntries()
	{
		if( log.isDebugEnabled() )
		{
			for( final FreeEntry fe : freeEntrySet )
			{
				log.debug("Have a free entry: " + fe.toString() );
			}
		}
	}

	public void debugUsedEntries()
	{
		if( log.isDebugEnabled() )
		{
			for( final UsedEntry ue : usedEntrySet )
			{
				log.debug("Found a used entry: " + ue.toString() );
			}
		}
	}

	public void errorUsedEntries()
	{
		if( log.isErrorEnabled() )
		{
			for( final UsedEntry ue : usedEntrySet )
			{
				log.error("Found a used entry: " + ue.toString() );
			}
		}
	}

	public AllocationBufferType getAllocationBufferType()
	{
		return allocationBufferType;
	}

	public String getCacheName()
	{
		return cacheName;
	}

	public void doConsistencyChecks()
	{
		boolean wasError = false;
		// Brute force consistency check:
		// Basically loop around all of the free elements checking each one against the
		// used elements. If we have any intersections, there's a bug somewhere....

		for( final FreeEntry fe : freeEntrySet )
		{
			for( final FreeEntry fe2 : freeEntrySet )
			{
				if( fe2 != fe )
				{
					if( fe2.getSourceRawImage() == fe.getSourceRawImage() && intersection( fe, fe2 ) )
					{
						if( log.isErrorEnabled() )
						{
							log.error("Found an inconsistency during allocation cache checks");
							log.error("FreeEntry(" + fe.toString() + ")");
							log.error("FreeEntry2(" + fe2.toString() + ")");
						}

						debugFreeEntries();
						wasError = true;
					}
				}
			}
			for( final UsedEntry ue : usedEntrySet )
			{
				if( ue.getRawImage() == fe.getSourceRawImage() && intersection( fe, ue ) )
				{
					if( log.isErrorEnabled() )
					{
						log.error("Found an inconsistency during allocation cache checks");
						log.error("FreeEntry(" + fe.getX() + "," + fe.getY() + ":" + fe.getWidth() + "," + fe.getHeight() + ")");
						log.error("UsedEntry(" + ue.getX() + "," + ue.getY() + ":" + ue.getWidth() + "," + ue.getHeight() + ")");
					}

					debugFreeEntries();
					debugUsedEntries();
					wasError = true;
				}
			}
		}
		if( wasError )
		{
			if( log.isErrorEnabled() )
			{
				log.error("BufferedImageCache for " + cacheName + " inconsistent!");
			}
		}
		else
		{
			if( log.isTraceEnabled() )
			{
				log.trace("BufferedImageCache for " + cacheName + " consistent");
			}
		}
	}

	private boolean intersection( final FreeEntry fe, final UsedEntry ue )
	{
		final int fex = fe.getX();
		final int fey = fe.getY();
		final int few = fe.getWidth() - 1;
		final int feh = fe.getHeight() - 1;
		final int fex2 = fex + few;
		final int fey2 = fey + feh;

		final int uex = ue.getX();
		final int uey = ue.getY();
		final int uew = ue.getWidth() - 1;
		final int ueh = ue.getHeight() - 1;
		final int uex2 = uex + uew;
		final int uey2 = uey + ueh;

//		boolean intersectsX = (Math.abs(fex - uex) * 2 < (few + uew));
//		boolean intersectsY = (Math.abs(fey - uey) * 2 < (feh + ueh));
//
//		return intersectsX && intersectsY;
		return !( fex > uex2
				|| fex2 < uex
				|| fey > uey2
				|| fey2 < uey );
	}

	private boolean intersection( final FreeEntry fe, final FreeEntry fe2 )
	{
		final int fex = fe.getX();
		final int fey = fe.getY();
		final int few = fe.getWidth() - 1;
		final int feh = fe.getHeight() - 1;
		final int fex2 = fex + few;
		final int fey2 = fey + feh;

		final int fe2x = fe2.getX();
		final int fe2y = fe2.getY();
		final int fe2w = fe2.getWidth() - 1;
		final int fe2h = fe2.getHeight() - 1;
		final int fe2x2 = fe2x + fe2w;
		final int fe2y2 = fe2y + fe2h;

//		boolean intersectsX = (Math.abs(fex - uex) * 2 < (few + uew));
//		boolean intersectsY = (Math.abs(fey - uey) * 2 < (feh + ueh));
//
//		return intersectsX && intersectsY;
		return !( fex > fe2x2
				|| fex2 < fe2x
				|| fey > fe2y2
				|| fey2 < fe2y );
	}

}
