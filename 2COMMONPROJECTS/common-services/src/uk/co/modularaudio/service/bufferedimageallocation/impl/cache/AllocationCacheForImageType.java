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
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	private GraphicsConfiguration graphicsConfiguration = null;
	private String cacheName = null;
//	private AllocationCacheConfiguration cacheConfiguration = null;
//	private AllocationLifetime allocationLifetime = null;
	private AllocationBufferType allocationBufferType = null;
	
	private Integer internalLock = new Integer(0);

	private int stdAllocImageWidth = -1;
	private int stdAllocImageHeight = -1;
	private int initialPages = -1;
	
	private long imageAllocationRawIdCounter = 0;
	private long imageAllocationAssignedIdCounter = 0;
	
	private Set<RawImage> rawImageSet = new HashSet<RawImage>();
	private OpenLongObjectHashMap<RawImage> rawImageIdToImageMap = new OpenLongObjectHashMap<RawImage>();
	private Set<UsedEntry> usedEntrySet = new HashSet<UsedEntry>();
	private Map<RawImage,HashSet<UsedEntry>> rawImageToUsedEntryMap = new HashMap<RawImage, HashSet<UsedEntry>>();
	private HashSet<FreeEntry> freeEntrySet = new HashSet<FreeEntry>();
	private Map<RawImage,HashSet<FreeEntry>> rawImageToFreeEntryMap = new HashMap<RawImage,HashSet<FreeEntry>>();
	
	private Decomposition decompositionForAllocation = new Decomposition();
	private Recomposition recompositionForFree = new Recomposition();

	public AllocationCacheForImageType( GraphicsConfiguration graphicsConfiguration,
			String name,
			AllocationCacheConfiguration cacheConfiguration,
			AllocationLifetime lifetime,
			AllocationBufferType allocationBufferType )
	{
		this.graphicsConfiguration = graphicsConfiguration;
		this.cacheName = name;
//		this.cacheConfiguration = cacheConfiguration;
//		this.allocationLifetime = lifetime;
		this.allocationBufferType = allocationBufferType;
		stdAllocImageWidth = cacheConfiguration.getStdAllocImageWidth();
		stdAllocImageHeight = cacheConfiguration.getStdAllocImageHeight();
		try
		{
			initialPages = cacheConfiguration.getInitialPagesForLifetimeAndType( lifetime, allocationBufferType );
			log.debug("Creating image cache named " + name +" of type " + allocationBufferType + " with " + initialPages + " initial pages.");
			
			populateInitialPages();
		}
		catch ( Exception e )
		{
			String msg = "Exception caught during initial page population of buffered image cache: " + e.toString();
			log.error( msg, e );
		}
	}
	
	private void populateInitialPages() throws DatastoreException
	{
		synchronized( internalLock )
		{
			for( int p = 0 ; p < initialPages ; p++ )
			{
				// Allocate raw image
				BufferedImage bufferedImage = doRealAllocation( stdAllocImageWidth,
						stdAllocImageHeight );

				Graphics2D rawGraphics = bufferedImage.createGraphics();
				
				long rawImageId = imageAllocationRawIdCounter++;
				RawImage rawImage = new RawImage( bufferedImage, rawGraphics, rawImageId );
				unsynchronisedAddRawImage( rawImageId, rawImage );
				
				// And create a free entry for it
				FreeEntry initialFreeEntry = new FreeEntry( rawImage, 0, 0, stdAllocImageWidth, stdAllocImageHeight );
				unsynchronisedAddFreeEntry( initialFreeEntry );
			}
		}
	}

	private BufferedImage doRealAllocation( int desiredWidth, int desiredHeight )
		throws DatastoreException
	{
		BufferedImage bufferedImage = null;
		
		switch( allocationBufferType.getJavaBufferedImageType() )
		{
			case BufferedImage.TYPE_INT_RGB:
			{
				bufferedImage = graphicsConfiguration.createCompatibleImage( desiredWidth, desiredHeight );
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				bufferedImage = graphicsConfiguration.createCompatibleImage( desiredWidth, desiredHeight, Transparency.TRANSLUCENT );
				break;
			}
			default:
			{
				String msg = "Unknown image type during allocation: " + allocationBufferType;
				throw new DatastoreException( msg );
			}
		}
//		new BufferedImage( stdAllocImageWidth,
//				stdAllocImageHeight,
//				allocationBufferType.getJavaBufferedImageType() );
//		GraphicsConfiguration
		return bufferedImage;
	}

	public TiledBufferedImage makeSubimageFromExistingFree(
			String allocationSource,
			AllocationMatch allocationMatchToUse,
			FreeEntry freeEntryToChopUp,
			int imageWidthToUse,
			int imageHeightToUse )
	{
		// Remove the existing free entry, it'll be replaced with what's necessary
		unsynchronisedRemoveFreeEntry( freeEntryToChopUp );
		
		long assignedId = imageAllocationAssignedIdCounter++;
		RawImage rawImage = freeEntryToChopUp.getSourceRawImage();
		
		int freeEntryStartX = freeEntryToChopUp.getX();
		int freeEntryWidth = freeEntryToChopUp.getWidth();
		int freeEntryStartY = freeEntryToChopUp.getY();
		int freeEntryHeight = freeEntryToChopUp.getHeight();
		
		BufferedImage rootBufferedImage = rawImage.getRootBufferedImage();

		// Clear the region we'll use
		rawImage.clearRegion( freeEntryStartX, freeEntryStartY, imageWidthToUse, imageHeightToUse );
		
		BufferedImage subImage = rootBufferedImage.getSubimage( freeEntryStartX,
				freeEntryStartY,
				imageWidthToUse,
				imageHeightToUse );

		UsedEntry usedEntry = new UsedEntry( allocationSource, rawImage, assignedId, freeEntryStartX, freeEntryStartY, imageWidthToUse, imageHeightToUse, subImage );
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
			FreeEntry smallerFe = new FreeEntry( rawImage,
					freeEntryStartX + decompositionForAllocation.smallerBlockX,
					freeEntryStartY + decompositionForAllocation.smallerBlockY,
					decompositionForAllocation.smallerBlockWidth,
					decompositionForAllocation.smallerBlockHeight );

			unsynchronisedAddFreeEntry( smallerFe );
			
			if( decompositionForAllocation.hasLarger )
			{
				FreeEntry largerFe = new FreeEntry( rawImage,
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
		TiledBufferedImageImpl theImpl = new TiledBufferedImageImpl( this, usedEntry, subImage );
		allocationMatchToUse.setFoundTile( theImpl );

		return allocationMatchToUse.getFoundTile();
	}

	public TiledBufferedImage allocateNewRawImageMakeSubimage(
			String allocationSource,
			AllocationMatch allocationMatchToUse,
			int imageWidthToUse,
			int imageHeightToUse )
		throws DatastoreException
	{
		synchronized (internalLock)
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
			}
			
			// Allocate raw image
			BufferedImage bufferedImage = doRealAllocation( widthToAlloc, heightToAlloc );

			Graphics2D rawGraphics = bufferedImage.createGraphics();
			
			long rawImageId = imageAllocationRawIdCounter++;
			RawImage rawImage = new RawImage( bufferedImage, rawGraphics, rawImageId );
			unsynchronisedAddRawImage( rawImageId, rawImage );
			
			// Create a subimage and used entry
			long assignedId = imageAllocationAssignedIdCounter++;
			BufferedImage subImage = bufferedImage.getSubimage( 0, 0, imageWidthToUse, imageHeightToUse );

			UsedEntry usedEntry = new UsedEntry( allocationSource, rawImage, assignedId, 0, 0, imageWidthToUse, imageHeightToUse, subImage );
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
				
				FreeEntry smallerFe = new FreeEntry( rawImage,
						decompositionForAllocation.smallerBlockX,
						decompositionForAllocation.smallerBlockY,
						decompositionForAllocation.smallerBlockWidth,
						decompositionForAllocation.smallerBlockHeight );

				unsynchronisedAddFreeEntry( smallerFe );
				
				if( decompositionForAllocation.hasLarger )
				{
					FreeEntry largerFe = new FreeEntry( rawImage,
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
			TiledBufferedImageImpl theImpl = new TiledBufferedImageImpl( this, usedEntry, subImage );
			allocationMatchToUse.setFoundTile( theImpl );
		}
		return allocationMatchToUse.getFoundTile();
	}

	public void freeTiledBufferedImage( TiledBufferedImageImpl realImpl )
	{
		synchronized (internalLock)
		{
			UsedEntry usedEntry = realImpl.getUsedEntry();
//			long assignedUsedEntryId = usedEntry.getAssignedId();
			RawImage usedRawImage = usedEntry.getRawImage();
			
			// Remove the "used" entry
			unsynchronisedRemoveUsedEntry( usedEntry );
			
			// Easy cases:
			// (1) Image was larger in one dimension than the standard allocation
			// If so, we just let it go and don't let it be re-used.
			// (2) Image exactly matches the standard allocation
			// so add it directly to the free entry set
			// (3) Image was a subimage - need to recompose it with any existing
			// free space for the raw image and possibly consolidate it
			int usedX = usedEntry.getX();
			int usedY = usedEntry.getY();
			int usedWidth = usedEntry.getWidth();
			int usedHeight = usedEntry.getHeight();
			boolean wasLargerThanStandard = (usedWidth > stdAllocImageWidth || usedHeight > stdAllocImageHeight );
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
				FreeEntry fe = new FreeEntry( usedRawImage, 0, 0, stdAllocImageWidth, stdAllocImageHeight );
				unsynchronisedAddFreeEntry( fe );
			}
			else
			{
				// Recompose (if possible) with existing free entries for the raw image
//				log.debug("Returned tiled buffered image was a sub image of what we alloc. Attempting to recompose with free entries.");
				// Get related free entries
				Set<FreeEntry> freeEntriesForRawImage = rawImageToFreeEntryMap.get( usedRawImage );
				if( BlockRecomposer.canRecomposeWithFreeEntries( usedRawImage, freeEntriesForRawImage, recompositionForFree, usedEntry ) )
				{
//					log.debug("Able to recompose: " + recompositionForFree.toString() );
					for( FreeEntry entryToRemove : recompositionForFree.freeEntriesToRemove )
					{
						unsynchronisedRemoveFreeEntry( entryToRemove );
					}
					// And add the new free entry
					FreeEntry newFreeEntry = new FreeEntry( usedRawImage, recompositionForFree.x, recompositionForFree.y, recompositionForFree.width, recompositionForFree.height );
					unsynchronisedAddFreeEntry( newFreeEntry );
				}
				else
				{
					// Can't recompose, just add a free entry for the size of the released image
//					log.debug("Unable to recompose - will add free entry for tiled image");
					FreeEntry fe = new FreeEntry( usedRawImage, usedX, usedY, usedWidth, usedHeight );
					unsynchronisedAddFreeEntry( fe );
				}
			}
		}
	}

	public int getNumRaw()
	{
		int retVal = 0;
		synchronized (internalLock)
		{
			retVal = rawImageSet.size();
		}
		return retVal;
	}

	public int getNumUsed()
	{
		int retVal = 0;
		synchronized (internalLock)
		{
			retVal = usedEntrySet.size();
		}
		return retVal;
	}
	
	public int getNumFree()
	{
		int retVal = 0;
		synchronized (internalLock)
		{
			retVal = freeEntrySet.size();
		}
		return retVal;
	}

	private void unsynchronisedAddRawImage( long rawImageId, RawImage rawImage )
	{
		rawImageSet.add( rawImage );
		rawImageIdToImageMap.put( rawImageId, rawImage );
		// Add free and used sets for it
		HashSet<FreeEntry> rawToFreeSet = new HashSet<FreeEntry>();
		rawImageToFreeEntryMap.put( rawImage, rawToFreeSet );
		HashSet<UsedEntry> ues = new HashSet<UsedEntry>();
		rawImageToUsedEntryMap.put( rawImage, ues );
	}

	private void unsynchronisedRemoveRawImage( RawImage rawImage )
	{
		rawImageIdToImageMap.removeKey( rawImage.getRawImageId() );
		rawImageSet.remove( rawImage );
		
		rawImageToFreeEntryMap.remove( rawImage );
		rawImageToUsedEntryMap.remove( rawImage );
	}

	private void unsynchronisedAddFreeEntry( FreeEntry freeEntry )
	{
		freeEntrySet.add( freeEntry );
		RawImage sourceRawImage = freeEntry.getSourceRawImage();
		Set<FreeEntry> freeEntriesForRaw = rawImageToFreeEntryMap.get( sourceRawImage );
		freeEntriesForRaw.add( freeEntry );
	}

	private void unsynchronisedRemoveFreeEntry( FreeEntry entryToRemove )
	{
		RawImage sourceRawImage = entryToRemove.getSourceRawImage();
		Set<FreeEntry> freeEntriesForRaw = rawImageToFreeEntryMap.get( sourceRawImage );
		if( !freeEntriesForRaw.remove( entryToRemove ) )
		{
			log.error("Attempted to remove a free entry but didn't find the free set entry!");
		}
		
		freeEntrySet.remove( entryToRemove );
	}

	private void unsychronisedAddUsedEntry( UsedEntry usedEntry )
	{
		usedEntrySet.add( usedEntry );
		RawImage rawImage = usedEntry.getRawImage();
		Set<UsedEntry> ues = rawImageToUsedEntryMap.get( rawImage );
		ues.add( usedEntry );
	}

	private void unsynchronisedRemoveUsedEntry( UsedEntry usedEntry )
	{
		usedEntrySet.remove( usedEntry );
		RawImage rawImage = usedEntry.getRawImage();
		Set<UsedEntry> ues = rawImageToUsedEntryMap.get( rawImage );
		if( !ues.remove( usedEntry ) )
		{
			log.error("Failed to remove used entry from raw image used entry set");
		}
	}

	public Set<RawImage> getRawImages()
	{
		synchronized (internalLock)
		{
			Set<RawImage> retSet = new HashSet<RawImage>();
			retSet.addAll( rawImageSet );
			return retSet;
		}
	}

	public RawImage getRawImageById( long rawImageId )
	{
		RawImage imgToReturn = null;
		synchronized (internalLock)
		{
			imgToReturn = rawImageIdToImageMap.get( rawImageId );
		}
		return imgToReturn;
	}

	public Set<FreeEntry> getRawImageFreeEntrySet( RawImage ri )
	{
		Set<FreeEntry> retVal = null;
		synchronized( internalLock )
		{
			retVal = rawImageToFreeEntryMap.get( ri );
		}
		return retVal;
	}

	public Set<UsedEntry> getRawImageUsedEntrySet( RawImage ri )
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
		for( FreeEntry fe : freeEntrySet )
		{
			log.debug("Have a free entry: " + fe.toString() );
		}
	}

	public void debugUsedEntries()
	{
		for( UsedEntry ue : usedEntrySet )
		{
			log.debug("Found a used entry: " + ue.toString() );
		}
	}
	
	public void errorUsedEntries()
	{
		for( UsedEntry ue : usedEntrySet )
		{
			log.error("Found a used entry: " + ue.toString() );
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
		
		for( FreeEntry fe : freeEntrySet )
		{
			for( FreeEntry fe2 : freeEntrySet )
			{
				if( fe2 != fe )
				{
					if( fe2.getSourceRawImage() == fe.getSourceRawImage() && intersection( fe, fe2 ) )
					{
						log.error("Found an inconsistency during allocation cache checks");
						log.error("FreeEntry(" + fe.toString() + ")");
						log.error("FreeEntry2(" + fe2.toString() + ")");

						debugFreeEntries();
						wasError = true;
					}
				}
			}
			for( UsedEntry ue : usedEntrySet )
			{
				if( ue.getRawImage() == fe.getSourceRawImage() && intersection( fe, ue ) )
				{
					log.error("Found an inconsistency during allocation cache checks");
					log.error("FreeEntry(" + fe.getX() + "," + fe.getY() + ":" + fe.getWidth() + "," + fe.getHeight() + ")");
					log.error("UsedEntry(" + ue.getX() + "," + ue.getY() + ":" + ue.getWidth() + "," + ue.getHeight() + ")");

					debugFreeEntries();
					debugUsedEntries();
					wasError = true;
				}
			}
		}
		if( wasError )
		{
			log.error("BufferedImageCache for " + cacheName + " inconsistent!");
		}
		else
		{
			log.trace("BufferedImageCache for " + cacheName + " consistent");
		}
	}
	
	private boolean intersection( FreeEntry fe, UsedEntry ue )
	{
		int fex = fe.getX();
		int fey = fe.getY();
		int few = fe.getWidth() - 1;
		int feh = fe.getHeight() - 1;
		int fex2 = fex + few;
		int fey2 = fey + feh;
		
		int uex = ue.getX();
		int uey = ue.getY();
		int uew = ue.getWidth() - 1;
		int ueh = ue.getHeight() - 1;
		int uex2 = uex + uew;
		int uey2 = uey + ueh;
		
//		boolean intersectsX = (Math.abs(fex - uex) * 2 < (few + uew));
//		boolean intersectsY = (Math.abs(fey - uey) * 2 < (feh + ueh));
//		
//		return intersectsX && intersectsY;
		return !( fex > uex2
				|| fex2 < uex
				|| fey > uey2
				|| fey2 < uey );
	}

	private boolean intersection( FreeEntry fe, FreeEntry fe2 )
	{
		int fex = fe.getX();
		int fey = fe.getY();
		int few = fe.getWidth() - 1;
		int feh = fe.getHeight() - 1;
		int fex2 = fex + few;
		int fey2 = fey + feh;
		
		int fe2x = fe2.getX();
		int fe2y = fe2.getY();
		int fe2w = fe2.getWidth() - 1;
		int fe2h = fe2.getHeight() - 1;
		int fe2x2 = fe2x + fe2w;
		int fe2y2 = fe2y + fe2h;
		
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
