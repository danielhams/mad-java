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

package uk.co.modularaudio.service.guicompfactory.impl.cache.raw;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentImageCache;
import uk.co.modularaudio.service.guicompfactory.impl.cache.GuiComponentPainter;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;

public class AbstractComponentImageRCache implements GuiComponentImageCache
{
	private static Log log = LogFactory.getLog( AbstractComponentImageRCache.class.getName() );
	
	private final GuiComponentPainter painter;

	private HashMap<MadDefinition<?,?>, 
		OpenLongObjectHashMap<BufferedImage>> madDefinitionToMapOfDimToImage = new HashMap<MadDefinition<?,?>,
			OpenLongObjectHashMap<BufferedImage>>();
	
	public AbstractComponentImageRCache( GuiComponentPainter painter )
	{
		this.painter = painter;
	}
	
	public BufferedImage getImageForRackComponent( RackComponent rackComponent, int width, int height, boolean isFront )
	{
//		log.debug("Creating image for " + rackComponent.getInstance().getDefinition().getName() + " of (" + width + ", " + height + ")");

		MadInstance<?, ?> madInstance = rackComponent.getInstance();
		MadDefinition<?,?> madDefinition = madInstance.getDefinition();
		long compoundKey = buildCompoundKey( width, height );

		OpenLongObjectHashMap<BufferedImage> dimensionsToImageMap = madDefinitionToMapOfDimToImage.get( madDefinition );
		
		BufferedImage bufferedImage = null;
		if( dimensionsToImageMap != null && dimensionsToImageMap.containsKey( compoundKey ) )
		{
			bufferedImage = dimensionsToImageMap.get( compoundKey );
		}
		if( bufferedImage == null )
		{
			try
			{
				bufferedImage = new BufferedImage( width, height,
						AllocationBufferType.TYPE_INT_ARGB.getJavaBufferedImageType() );
						
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
					dimensionsToImageMap = new OpenLongObjectHashMap<BufferedImage>();
					madDefinitionToMapOfDimToImage.put( madDefinition, dimensionsToImageMap );
				}
				dimensionsToImageMap.put( compoundKey, bufferedImage );
			}
			catch ( Exception e )
			{
				String msg = "Exception caught creating cached gui front image: " + e.toString();
				log.error( msg, e );
			}
		}
		return bufferedImage;
	}

	private long buildCompoundKey( int width, int height )
	{
		return ((long)width << 32) | (long)height;
	}

	public void destroy()
	{
		Collection<OpenLongObjectHashMap<BufferedImage>> typeToBufs = madDefinitionToMapOfDimToImage.values();
		for( OpenLongObjectHashMap<BufferedImage> imsForType : typeToBufs )
		{
			imsForType.clear();
		}
		madDefinitionToMapOfDimToImage.clear();
	}
}
