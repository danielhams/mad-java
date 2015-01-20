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

package uk.co.modularaudio.service.gui.impl.racktable.back;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.gui.valueobjects.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.gui.valueobjects.plugs.GuiChannelPlug;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class RackLinkPainter
{
	private static Log log = LogFactory.getLog( RackLinkPainter.class.getName() );
	
	private BufferedImageAllocationService bufferedImageAllocationService = null;
	
	private final static boolean DEBUG = false;
	// Master image that is full width and height of the rack
	// we'll clear it and subimage it when we need to paint a real one
	private AllocationMatch masterImageAllocationMatch = new AllocationMatch();
	private TiledBufferedImage compositeRackLinksTiledBufferedImage = null;
	private Rectangle compositeImageRectangle = null;
	private BufferedImage compositeRackLinksImage = null;
	
	private RackDataModel dataModel = null;
	private RackTableWithLinks tableWithLinks = null;
	private RackLinkImageRegistry rackLinkRegistry = null;
	private RackLinkCompositeLinksGuiComponent compositeLinksGuiComponent = new RackLinkCompositeLinksGuiComponent();

	public RackLinkPainter( BufferedImageAllocationService bufferedImageAllocationService, RackDataModel dataModel, RackTableWithLinks tableWithLinks )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
		
		this.dataModel = dataModel;
		this.tableWithLinks = tableWithLinks;
		rackLinkRegistry = new RackLinkImageRegistry();
		tableWithLinks.setLayer( compositeLinksGuiComponent, RackTableWithLinks.LPT_STATICWIRE_LAYER );
		tableWithLinks.add( compositeLinksGuiComponent );
	}

	public void createIndividualRackLinkImages()
	{
		for( int i = 0 ; i < dataModel.getNumLinks() ; i++ )
		{
			RackLink rl = dataModel.getLinkAt( i );
			
			drawOneRackLinkImageAndAdd( rl );
		}
	}

	public void createIndividualRackIOLinkImages()
	{
		for( int i = 0 ; i < dataModel.getNumIOLinks() ; i++ )
		{
			RackIOLink rl = dataModel.getIOLinkAt( i );
			
			drawOneRackIOLinkImageAndAdd( rl );
		}
	}

	public void drawOneRackLinkImageAndAdd( RackLink rl )
	{
		// For now lets assume some magic about how the end points of the cable are computed.
		RackComponent sourceRackComponent = rl.getProducerRackComponent();
		AbstractGuiAudioComponent sourceGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sourceRackComponent );
		MadChannelInstance sourceRackComponentChannel = rl.getProducerChannelInstance();
		GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );
		Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent, 
				sourceGuiPlug );
		
		RackComponent sinkRackComponent = rl.getConsumerRackComponent();
		AbstractGuiAudioComponent sinkGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sinkRackComponent );
		MadChannelInstance sinkRackComponentChannel = rl.getConsumerChannelInstance();
		GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );
		
		// Now allocate and draw a buffered image from this source point to the sink point.
		RackLinkImage rli = new RackLinkImage( "NewRackLinkPainter", bufferedImageAllocationService, rl, sourcePoint, sinkPoint );
		
		// Now add this into the registry
		rackLinkRegistry.addLinkToRegistry( rl, rli,
				sourceRackComponent, sourceRackComponentChannel,
				sinkRackComponent, sinkRackComponentChannel );
	}
	
	public void drawOneRackIOLinkImageAndAdd( RackIOLink ril )
	{
		// Bit of a hack....
		RackComponent masterIOComponent = dataModel.getContentsAtPosition( 0,  0 );
		MadChannelInstance ioChannelInstance = ril.getRackChannelInstance();
		RackComponent rackComponent = ril.getRackComponent();
		MadChannelInstance rackComponentChannelInstance = ril.getRackComponentChannelInstance();
		MadChannelDirection direction = rackComponentChannelInstance.definition.direction;
		
		// For now lets assume some magic about how the end points of the cable are computed.
		RackComponent sourceRackComponent = (direction == MadChannelDirection.PRODUCER ? rackComponent : masterIOComponent );
		AbstractGuiAudioComponent sourceGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sourceRackComponent );
		MadChannelInstance sourceRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? rackComponentChannelInstance : ioChannelInstance );
		GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );

		Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent, 
				sourceGuiPlug );
		
		RackComponent sinkRackComponent = (direction == MadChannelDirection.PRODUCER ? masterIOComponent : rackComponent );
		AbstractGuiAudioComponent sinkGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sinkRackComponent );
		MadChannelInstance sinkRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? ioChannelInstance : rackComponentChannelInstance );
		GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );
		
		// Now allocate and draw a buffered image from this source point to the sink point.
		RackIOLinkImage rilo;
		try
		{
			rilo = new RackIOLinkImage( bufferedImageAllocationService, ril, sourcePoint, sinkPoint );
			// Now add this into the registry
			rackLinkRegistry.addIOLinkToRegistry( ril, rilo, sourceRackComponent, sourceRackComponentChannel, sinkRackComponent, sinkRackComponentChannel );
		}
		catch ( Exception e )
		{
			String msg = "Exception caught creating buffered image: " + e.toString();
			log.error( msg, e );
		}
		
	}

	public void createCompositeRackLinksImageAndRedisplay()
	{
		//log.debug("Creating composite rack links image and redisplaying");
		compositeImageRectangle = workOutCompositeRectangle();
		if( compositeImageRectangle != null )
		{
			// if we already have a buffered image, release it back to the allocation service before we ask for a new one.
			if( compositeRackLinksImage != null )
			{
				// Only release if it's different
				if( compositeImageRectangle.width != compositeRackLinksImage.getWidth() ||
						compositeImageRectangle.height != compositeRackLinksImage.getHeight() )
				{
					// Dimensions don't match, release it
					freeCompositeRackLinksTiledImage();
				}
			}
			
			if( compositeRackLinksImage == null )
			{
				allocateNewCompositeTiledImage();
			}
			
			Graphics2D clearG = compositeRackLinksImage.createGraphics();
			clearG.setComposite( AlphaComposite.getInstance( AlphaComposite.CLEAR, 0.0f ) );
			clearG.fillRect( 0, 0, compositeImageRectangle.width, compositeImageRectangle.height );
			clearG.dispose();
			
			Graphics2D crliG2d = compositeRackLinksImage.createGraphics();

			if( DEBUG)
			{
				crliG2d.drawRect( 0, 0, compositeImageRectangle.width - 1, compositeImageRectangle.height - 1 );
			}
			
			// Now for each individual image paint it at the appropriate offset
			for(RackIOLinkImage oneIOLinkImage : rackLinkRegistry.getRackIOLinkImages() )
			{
				Rectangle oneIOLinkRectangle = oneIOLinkImage.getRectangle();
				BufferedImage oneIOLinkBufferedImage = oneIOLinkImage.getBufferedImage();
				int oneImageXOffset = oneIOLinkRectangle.x - compositeImageRectangle.x;
				int oneImageYOffset = oneIOLinkRectangle.y - compositeImageRectangle.y;
				crliG2d.drawImage( oneIOLinkBufferedImage,
						oneImageXOffset,
						oneImageYOffset,
						null );
			}
			for(RackLinkImage oneLinkImage : rackLinkRegistry.getRackLinkImages() )
			{
				Rectangle oneLinkRectangle = oneLinkImage.getRectangle();
				BufferedImage oneLinkBufferedImage = oneLinkImage.getBufferedImage();
				int oneImageXOffset = oneLinkRectangle.x - compositeImageRectangle.x;
				int oneImageYOffset = oneLinkRectangle.y - compositeImageRectangle.y;
				crliG2d.drawImage( oneLinkBufferedImage,
						oneImageXOffset,
						oneImageYOffset,
						null );
			}
		}
		else
		{
			freeCompositeRackLinksTiledImage();
		}
		updateCompositeComponentInTable();
//		log.debug("Asking for a full refresh");
		tableWithLinks.repaint();
	}

	private void allocateNewCompositeTiledImage()
	{
		if( compositeRackLinksTiledBufferedImage != null || compositeRackLinksImage != null )
		{
			log.error("Allocating new composite tiled image but old one still exists!");
		}
		try
		{
			compositeRackLinksTiledBufferedImage = bufferedImageAllocationService.allocateBufferedImage( "NewRackLinkPainterCompositeTiledImageAllocation",
					masterImageAllocationMatch,
					AllocationLifetime.SHORT,
					AllocationBufferType.TYPE_INT_ARGB,
					compositeImageRectangle.width,
					compositeImageRectangle.height );
			
			compositeRackLinksImage = compositeRackLinksTiledBufferedImage.getUnderlyingBufferedImage();
		}
		catch ( Exception e )
		{
			String msg = "Exception caught allocating new rack links composite image: " + e.toString();
			log.error( msg, e );
		}
	}

	private void freeCompositeRackLinksTiledImage()
	{
		try
		{
			if( compositeRackLinksTiledBufferedImage != null )
			{
				compositeRackLinksImage = null;
				bufferedImageAllocationService.freeBufferedImage( compositeRackLinksTiledBufferedImage );
				compositeRackLinksTiledBufferedImage = null;
			}
		}
		catch( Exception e )
		{
			String msg = "Exception caught freeing rack links composite image: " + e.toString();
			log.error( msg, e );
		}
	}

	private Rectangle workOutCompositeRectangle()
	{
		// Iterate over the individual rack link images working out the dimensions that are needed for a "minimum fit"
		// image containing all of the links
		// Makes for a smaller image, and less to paint = better performance
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		
		for( RackIOLinkImage oneIOLinkImage : rackLinkRegistry.getRackIOLinkImages() )
		{
			Rectangle olir = oneIOLinkImage.getRectangle();
			if( olir.x < minX )
			{
				minX = olir.x;
			}
			if( olir.x + olir.width > maxX )
			{
				maxX = olir.x + olir.width;
			}
			if( olir.y < minY )
			{
				minY = olir.y;
			}
			if( olir.y + olir.height > maxY )
			{
				maxY = olir.y + olir.height;
			}
		}
		for( RackLinkImage oneLinkImage : rackLinkRegistry.getRackLinkImages() )
		{
			Rectangle olir = oneLinkImage.getRectangle();
			if( olir.x < minX )
			{
				minX = olir.x;
			}
			if( olir.x + olir.width > maxX )
			{
				maxX = olir.x + olir.width;
			}
			if( olir.y < minY )
			{
				minY = olir.y;
			}
			if( olir.y + olir.height > maxY )
			{
				maxY = olir.y + olir.height;
			}
		}
		
		if( 0 == maxX || 0 == maxY )
		{
			return null;
		}
		else
		{
			return new Rectangle( minX, minY, maxX - minX, maxY - minY );
		}
	}

	public void clear()
	{
		rackLinkRegistry.clear();
		freeCompositeRackLinksTiledImage();
		compositeImageRectangle = null;
		compositeRackLinksImage = null;
	}

	public void updateRackLinkImageForLink( RackLink rl )
	{
		RackLinkImage rli = rackLinkRegistry.getRackLinkImageForRackLink( rl );
		
		RackComponent sourceRackComponent = rl.getProducerRackComponent();
		AbstractGuiAudioComponent sourceGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sourceRackComponent );
		MadChannelInstance sourceRackComponentChannel = rl.getProducerChannelInstance();
		GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );
		Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug( tableWithLinks,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent,
				sourceGuiPlug );
		
		RackComponent sinkRackComponent = rl.getConsumerRackComponent();
		AbstractGuiAudioComponent sinkGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sinkRackComponent );
		MadChannelInstance sinkRackComponentChannel = rl.getConsumerChannelInstance();
		GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );
	
		rli.redrawWireWithNewPoints( sourcePoint, sinkPoint );

		updateCompositeComponentInTable();
	}
	
	public void updateRackIOLinkImageForLink( RackIOLink rl )
	{
		RackIOLinkImage rili = rackLinkRegistry.getRackIOLinkImageForRackIOLink( rl );

		// Bit of a hack....
		RackComponent masterIOComponent = dataModel.getContentsAtPosition( 0,  0 );
		MadChannelInstance ioChannelInstance = rl.getRackChannelInstance();
		RackComponent rackComponent = rl.getRackComponent();
		MadChannelInstance rackComponentChannelInstance = rl.getRackComponentChannelInstance();
		MadChannelDirection direction = rackComponentChannelInstance.definition.direction;
		
		// For now lets assume some magic about how the end points of the cable are computed.
		RackComponent sourceRackComponent = (direction == MadChannelDirection.PRODUCER ? rackComponent : masterIOComponent );
		AbstractGuiAudioComponent sourceGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sourceRackComponent );
		MadChannelInstance sourceRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? rackComponentChannelInstance : ioChannelInstance );
		GuiChannelPlug sourceGuiPlug = sourceGuiComponent.getPlugFromMadChannelInstance( sourceRackComponentChannel );

		Point sourcePoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sourceGuiComponent,
				sourceRackComponent, 
				sourceGuiPlug );
		
		RackComponent sinkRackComponent = (direction == MadChannelDirection.PRODUCER ? masterIOComponent : rackComponent );
		AbstractGuiAudioComponent sinkGuiComponent = tableWithLinks.getGuiComponentFromTableModel( sinkRackComponent );
		MadChannelInstance sinkRackComponentChannel = (direction == MadChannelDirection.PRODUCER ? ioChannelInstance : rackComponentChannelInstance );
		GuiChannelPlug sinkGuiPlug = sinkGuiComponent.getPlugFromMadChannelInstance( sinkRackComponentChannel );
		Point sinkPoint = RackWirePositionHelper.calculateCenterForComponentPlug(
				tableWithLinks,
				dataModel,
				sinkGuiComponent,
				sinkRackComponent,
				sinkGuiPlug );
		
		rili.redrawWireWithNewPoints( sourcePoint, sinkPoint );

		updateCompositeComponentInTable();
	}
	
	public void removeRackLinkImageAt( int modelIndex )
	{
//		log.debug("Asked to remove link image at " + modelIndex );
		rackLinkRegistry.removeLinkAt( modelIndex );
	}
	
	public List<RackLink> getLinksForComponent(RackComponent componentToFindLinksFor)
	{
		// Return a copy so we don't get concurrent modification exceptions when removing / adding
		return new ArrayList<RackLink>(rackLinkRegistry.getLinksForComponent( componentToFindLinksFor ));
	}

	public void updateLink( RackLink oneLink )
	{
		// Generate a new link and then re-create the composite image
		updateRackLinkImageForLink( oneLink );
//		createCompositeRackLinksImageAndRedisplay();
	}
	
	public void updateIOLink( RackIOLink oneLink )
	{
		// Generate a new link and then re-create the composite image
		updateRackIOLinkImageForLink( oneLink );
//		createCompositeRackLinksImageAndRedisplay();
	}
	
	public List<RackIOLink> getIOLinksForComponent( RackComponent componentToFindLinksFor )
	{
		// Return a copy so we don't get concurrent modification exceptions when removing / adding
		return new ArrayList<RackIOLink>(rackLinkRegistry.getIOLinksForComponent( componentToFindLinksFor ));
	}

	public void setDataModel(RackDataModel dataModel)
	{
		this.dataModel = dataModel;
	}
	
	public void updateCompositeComponentInTable()
	{
		if( compositeImageRectangle != null && compositeRackLinksImage != null )
		{
			compositeLinksGuiComponent.setBoundsAndImage( compositeImageRectangle, compositeRackLinksImage );
		}
		else
		{
			compositeLinksGuiComponent.setBoundsAndImage( compositeImageRectangle, null );
		}
	}

	public void removeRackIOLinkImageAt( int modelIndex )
	{
		rackLinkRegistry.removeIOLinkAt( modelIndex );
	}

	public void destroy()
	{
		dataModel = null;
		freeCompositeRackLinksTiledImage();
	}
}
