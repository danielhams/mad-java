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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;

public class RackLinkImageRegistry
{
//	private static Log log = LogFactory.getLog( NewRackLinkImageRegistry.class.getName() );
	
	// Rack Link registry
	private ArrayList<RackLinkImage> rackLinkImages = new ArrayList<RackLinkImage>();
	private HashMap<RackLink, RackLinkImage> rackLinkToImageMap = new HashMap<RackLink, RackLinkImage>();
	private HashMap<RackComponent, ArrayList<RackLink>> componentToRackLinksMap = new HashMap<RackComponent, ArrayList<RackLink>>();
	
	private ArrayList<RackIOLinkImage> rackIOLinkImages = new ArrayList<RackIOLinkImage>();
	private Map<RackIOLink, RackIOLinkImage> rackIOLinkToImageMap = new HashMap<RackIOLink, RackIOLinkImage>();
	private Map<RackComponent, List<RackIOLink>> componentToRackIOLinksMap = new HashMap<RackComponent, List<RackIOLink>>();
	
	public RackLinkImageRegistry()
	{
	}
	
	public void addLinkToRegistry(RackLink link,
			RackLinkImage linkImage,
			RackComponent sourceRackComponent,
			MadChannelInstance sourceRackComponentChannel,
			RackComponent sinkRackComponent,
			MadChannelInstance sinkRackComponentChannel)
	{
		rackLinkToImageMap.put( link, linkImage );
		rackLinkImages.add( linkImage );
		addLinkToComponentsMap( link, sourceRackComponent );
		addLinkToComponentsMap( link, sinkRackComponent );
	}
	
	public void addIOLinkToRegistry( RackIOLink ioLink,
			RackIOLinkImage ioLinkImage,
			RackComponent sourceRackComponent,
			MadChannelInstance sourceRackComponentChannel,
			RackComponent sinkRackComponent,
			MadChannelInstance sinkRackComponentChannel )
	{
		rackIOLinkToImageMap.put( ioLink, ioLinkImage );
		rackIOLinkImages.add( ioLinkImage );
		addIOLinkToComponentsMap( ioLink, ioLink.getRackComponent() );
	}
	
	private void addLinkToComponentsMap( RackLink link, RackComponent component )
	{
		ArrayList<RackLink> linksForComponent = componentToRackLinksMap.get( component );
		if( linksForComponent == null )
		{
			linksForComponent = new ArrayList<RackLink>();
			componentToRackLinksMap.put( component, linksForComponent );
		}
		linksForComponent.add( link );
	}
	
	private void addIOLinkToComponentsMap( RackIOLink ioLink, RackComponent component )
	{
		List<RackIOLink> linksForComponent = componentToRackIOLinksMap.get( component );
		if( linksForComponent == null )
		{
			linksForComponent = new ArrayList<RackIOLink>();
			componentToRackIOLinksMap.put( component, linksForComponent );
		}
		linksForComponent.add( ioLink );
	}

	public void clear()
	{
		while( rackLinkImages.size() > 0 )
		{
			RackLinkImage nrli = rackLinkImages.get( 0 );
			RackLink rl = nrli.getRackLink();
//			log.debug("** Removing rack link image for link: " + rl.toString() );
			removeLink( rl );
		}

		while( rackIOLinkImages.size() > 0 )
		{
			RackIOLinkImage nrili = rackIOLinkImages.get( 0 );
			RackIOLink ril = nrili.getRackIOLink();
//			log.debug("** Removing rack io link image for io link: " + ril.toString() );
			removeIOLink( ril );
		}
	}

	public List<RackLinkImage> getRackLinkImages()
	{
		return rackLinkImages;
	}
	
	public List<RackIOLinkImage> getRackIOLinkImages()
	{
		return rackIOLinkImages;
	}
	
	private List<RackLink> emptyRackLinkList = new ArrayList<RackLink>();

	public List<RackLink> getLinksForComponent(RackComponent componentToFindLinksFor)
	{
		List<RackLink> retVal = null;
		retVal = componentToRackLinksMap.get( componentToFindLinksFor );
		if( retVal == null )
		{
			retVal = emptyRackLinkList;
		}
		return retVal;
	}
	
	private List<RackIOLink> emptyRackIOLinkList = new ArrayList<RackIOLink>();
	
	public List<RackIOLink> getIOLinksForComponent( RackComponent componentToFindLinksFor )
	{
		List<RackIOLink> retVal = null;
		retVal = componentToRackIOLinksMap.get( componentToFindLinksFor );
		if( retVal == null )
		{
			retVal = emptyRackIOLinkList;
		}
		return retVal;		
	}

	public void removeLink(RackLink oneLink)
	{
		RackLinkImage rli = rackLinkToImageMap.get( oneLink );
		rackLinkImages.remove( rli );
		rackLinkToImageMap.remove( oneLink );
		removeLinkForComponent( oneLink.getProducerRackComponent(), oneLink );
		removeLinkForComponent( oneLink.getConsumerRackComponent(), oneLink );
		
		rli.destroy();
	}
	
	public void removeLinkAt(int modelIndex)
	{
		RackLinkImage rli = rackLinkImages.get( modelIndex );
		removeLink( rli.getRackLink() );
	}
	
	private void removeLinkForComponent( RackComponent component,
			RackLink link )
	{
		List<RackLink> linksRegisteredToComponent = componentToRackLinksMap.get( component );
		if( linksRegisteredToComponent != null )
		{
			linksRegisteredToComponent.remove( link );
		}
	}

	public RackLinkImage getRackLinkImageForRackLink(RackLink rl)
	{
		return rackLinkToImageMap.get( rl );
	}

	public RackIOLinkImage getRackIOLinkImageForRackIOLink(RackIOLink rl)
	{
		return rackIOLinkToImageMap.get( rl );
	}

	public void removeIOLinkAt( int modelIndex )
	{
		RackIOLinkImage rili = rackIOLinkImages.get( modelIndex );
		removeIOLink( rili.getRackIOLink() );
	}

	public void removeIOLink( RackIOLink oneLink)
	{
		RackIOLinkImage rili = rackIOLinkToImageMap.get( oneLink );
		rackIOLinkImages.remove( rili );
		rackIOLinkToImageMap.remove( oneLink );
		removeIOLinkForComponent( oneLink.getRackComponent(), oneLink );
		rili.destroy();
	}

	private void removeIOLinkForComponent( RackComponent component, RackIOLink ioLink )
	{
		List<RackIOLink> ioLinksRegisteredToComponent = componentToRackIOLinksMap.get( component );
		if( ioLinksRegisteredToComponent != null )
		{
			ioLinksRegisteredToComponent.remove( ioLink );
		}
	}
	
}
