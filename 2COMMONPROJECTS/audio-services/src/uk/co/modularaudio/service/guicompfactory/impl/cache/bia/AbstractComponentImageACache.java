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

package uk.co.modularaudio.service.guicompfactory.impl.cache.bia;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class AbstractComponentImageACache implements GuiComponentImageCache
{
	private static Log log = LogFactory.getLog( AbstractComponentImageACache.class.getName() );
	
	private final String guiCacheAllocationName;

	private final BufferedImageAllocationService bufferedImageAllocationService;
	
	private final GuiComponentPainter painter;

	private HashMap<MadDefinition<?,?>, 
		OpenLongObjectHashMap<TiledBufferedImage>> madDefinitionToMapOfDimToImage = new HashMap<MadDefinition<?,?>,
			OpenLongObjectHashMap<TiledBufferedImage>>();
	
	private AllocationMatch allocationMatchToUse = new AllocationMatch();
	
	public AbstractComponentImageACache(
			String guiCacheAllocationName,
			BufferedImageAllocationService bufferedImageAllocationService,
			GuiComponentPainter painter )
	{
		this.guiCacheAllocationName = guiCacheAllocationName;
		
		this.bufferedImageAllocationService = bufferedImageAllocationService;
		
		this.painter = painter;
	}
	
	public BufferedImage getImageForRackComponent( RackComponent rackComponent, int width, int height, boolean isFront )
	{
//		log.debug("Creating image for " + rackComponent.getInstance().getDefinition().getName() + " of (" + width + ", " + height + ")");

		MadInstance<?, ?> madInstance = rackComponent.getInstance();
		MadDefinition<?,?> madDefinition = madInstance.getDefinition();
		long compoundKey = buildCompoundKey( width, height );

		OpenLongObjectHashMap<TiledBufferedImage> dimensionsToImageMap = madDefinitionToMapOfDimToImage.get( madDefinition );
		
		TiledBufferedImage tiledImage = null;
		if( dimensionsToImageMap != null && dimensionsToImageMap.containsKey( compoundKey ) )
		{
			tiledImage = dimensionsToImageMap.get( compoundKey );
		}
		if( tiledImage == null )
		{
			try
			{
				tiledImage = bufferedImageAllocationService.allocateBufferedImage(
						guiCacheAllocationName,
						allocationMatchToUse,
						AllocationLifetime.LONG,
						AllocationBufferType.TYPE_INT_ARGB,
						width, height );
				
				BufferedImage bufferedImage = tiledImage.getUnderlyingBufferedImage();
				painter.drawComponentImage( rackComponent, bufferedImage, width, height );
				
				// If the component isn't configurable we can dispose of the related image.
				MadUiDefinition<?, ?> uiDefinition = rackComponent.getUiDefinition();
				if( !uiDefinition.isParametrable() )
				{
					if( isFront )
					{
						uiDefinition.clearFrontBufferedImage();
					}
					else
					{
						uiDefinition.clearBackBufferedImage();
					}
				}
				
				if( dimensionsToImageMap == null )
				{
					dimensionsToImageMap = new OpenLongObjectHashMap<TiledBufferedImage>();
					madDefinitionToMapOfDimToImage.put( madDefinition, dimensionsToImageMap );
				}
				dimensionsToImageMap.put( compoundKey, tiledImage );
			}
			catch ( Exception e )
			{
				String msg = "Exception caught creating cached gui front image: " + e.toString();
				log.error( msg, e );
			}
		}
		return tiledImage.getUnderlyingBufferedImage();
	}

	private long buildCompoundKey( int width, int height )
	{
		return ((long)width << 32) | (long)height;
	}

	public void destroy()
	{
		Collection<OpenLongObjectHashMap<TiledBufferedImage>> typeToBufs = madDefinitionToMapOfDimToImage.values();
		for( OpenLongObjectHashMap<TiledBufferedImage> imsForType : typeToBufs )
		{
			Collection<TiledBufferedImage> bufsStillRemaining = imsForType.values();
			for( TiledBufferedImage toDestroy : bufsStillRemaining )
			{
				try
				{
					bufferedImageAllocationService.freeBufferedImage( toDestroy );
				}
				catch ( Exception e )
				{
					String msg = "Exception caught freeing cached front component image: " + e.toString();
					log.error( msg, e );
				}
			}
			imsForType.clear();
		}
		madDefinitionToMapOfDimToImage.clear();
	}
}
