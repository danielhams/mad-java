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
	private final ArrayList<RackLinkImage> rackLinkImages = new ArrayList<RackLinkImage>();
	private final HashMap<RackLink, RackLinkImage> rackLinkToImageMap = new HashMap<RackLink, RackLinkImage>();
	private final HashMap<RackComponent, ArrayList<RackLink>> componentToRackLinksMap = new HashMap<RackComponent, ArrayList<RackLink>>();

	private final ArrayList<RackIOLinkImage> rackIOLinkImages = new ArrayList<RackIOLinkImage>();
	private final Map<RackIOLink, RackIOLinkImage> rackIOLinkToImageMap = new HashMap<RackIOLink, RackIOLinkImage>();
	private final Map<RackComponent, List<RackIOLink>> componentToRackIOLinksMap = new HashMap<RackComponent, List<RackIOLink>>();

	public RackLinkImageRegistry()
	{
	}

	public void addLinkToRegistry(final RackLink link,
			final RackLinkImage linkImage,
			final RackComponent sourceRackComponent,
			final MadChannelInstance sourceRackComponentChannel,
			final RackComponent sinkRackComponent,
			final MadChannelInstance sinkRackComponentChannel)
	{
		rackLinkToImageMap.put( link, linkImage );
		rackLinkImages.add( linkImage );
		addLinkToComponentsMap( link, sourceRackComponent );
		addLinkToComponentsMap( link, sinkRackComponent );
	}

	public void addIOLinkToRegistry( final RackIOLink ioLink,
			final RackIOLinkImage ioLinkImage,
			final RackComponent sourceRackComponent,
			final MadChannelInstance sourceRackComponentChannel,
			final RackComponent sinkRackComponent,
			final MadChannelInstance sinkRackComponentChannel )
	{
		rackIOLinkToImageMap.put( ioLink, ioLinkImage );
		rackIOLinkImages.add( ioLinkImage );
		addIOLinkToComponentsMap( ioLink, ioLink.getRackComponent() );
	}

	private void addLinkToComponentsMap( final RackLink link, final RackComponent component )
	{
		ArrayList<RackLink> linksForComponent = componentToRackLinksMap.get( component );
		if( linksForComponent == null )
		{
			linksForComponent = new ArrayList<RackLink>();
			componentToRackLinksMap.put( component, linksForComponent );
		}
		linksForComponent.add( link );
	}

	private void addIOLinkToComponentsMap( final RackIOLink ioLink, final RackComponent component )
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
			final RackLinkImage nrli = rackLinkImages.get( 0 );
			final RackLink rl = nrli.getRackLink();
//			log.debug("** Removing rack link image for link: " + rl.toString() );
			removeLink( rl );
		}

		while( rackIOLinkImages.size() > 0 )
		{
			final RackIOLinkImage nrili = rackIOLinkImages.get( 0 );
			final RackIOLink ril = nrili.getRackIOLink();
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

	private final List<RackLink> emptyRackLinkList = new ArrayList<RackLink>();

	public List<RackLink> getLinksForComponent(final RackComponent componentToFindLinksFor)
	{
		List<RackLink> retVal = null;
		retVal = componentToRackLinksMap.get( componentToFindLinksFor );
		if( retVal == null )
		{
			retVal = emptyRackLinkList;
		}
		return retVal;
	}

	private final List<RackIOLink> emptyRackIOLinkList = new ArrayList<RackIOLink>();

	public List<RackIOLink> getIOLinksForComponent( final RackComponent componentToFindLinksFor )
	{
		List<RackIOLink> retVal = null;
		retVal = componentToRackIOLinksMap.get( componentToFindLinksFor );
		if( retVal == null )
		{
			retVal = emptyRackIOLinkList;
		}
		return retVal;
	}

	public void removeLink(final RackLink oneLink)
	{
		final RackLinkImage rli = rackLinkToImageMap.get( oneLink );
		rackLinkImages.remove( rli );
		rackLinkToImageMap.remove( oneLink );
		removeLinkForComponent( oneLink.getProducerRackComponent(), oneLink );
		removeLinkForComponent( oneLink.getConsumerRackComponent(), oneLink );

		rli.destroy();
	}

	public void removeLinkAt(final int modelIndex)
	{
		final RackLinkImage rli = rackLinkImages.get( modelIndex );
		removeLink( rli.getRackLink() );
	}

	private void removeLinkForComponent( final RackComponent component,
			final RackLink link )
	{
		final List<RackLink> linksRegisteredToComponent = componentToRackLinksMap.get( component );
		if( linksRegisteredToComponent != null )
		{
			linksRegisteredToComponent.remove( link );
		}
	}

	public RackLinkImage getRackLinkImageForRackLink(final RackLink rl)
	{
		return rackLinkToImageMap.get( rl );
	}

	public RackIOLinkImage getRackIOLinkImageForRackIOLink(final RackIOLink rl)
	{
		return rackIOLinkToImageMap.get( rl );
	}

	public void removeIOLinkAt( final int modelIndex )
	{
		final RackIOLinkImage rili = rackIOLinkImages.get( modelIndex );
		removeIOLink( rili.getRackIOLink() );
	}

	public void removeIOLink( final RackIOLink oneLink)
	{
		final RackIOLinkImage rili = rackIOLinkToImageMap.get( oneLink );
		rackIOLinkImages.remove( rili );
		rackIOLinkToImageMap.remove( oneLink );
		removeIOLinkForComponent( oneLink.getRackComponent(), oneLink );
		rili.destroy();
	}

	private void removeIOLinkForComponent( final RackComponent component, final RackIOLink ioLink )
	{
		final List<RackIOLink> ioLinksRegisteredToComponent = componentToRackIOLinksMap.get( component );
		if( ioLinksRegisteredToComponent != null )
		{
			ioLinksRegisteredToComponent.remove( ioLink );
		}
	}

}
