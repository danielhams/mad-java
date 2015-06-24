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

package uk.co.modularaudio.util.audio.mad.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class GraphLinkMap
{
	private static Log log = LogFactory.getLog( GraphLinkMap.class.getName() );

	private static final boolean RUNTIME_CHECKING = true;

	private final Collection<MadLink> instanceLinks = new ArrayList<MadLink>();
	private final Map<MadInstance<?,?>, Set<MadLink>> consumerInstanceLinks = new HashMap<MadInstance<?,?>, Set<MadLink>>();
	private final Map<MadInstance<?,?>, Set<MadLink>> producerInstanceLinks = new HashMap<MadInstance<?,?>, Set<MadLink>>();

	public GraphLinkMap()
	{
	}

	public Collection<MadLink> getLinks()
	{
		return instanceLinks;
	}

	public Set<MadLink> getProducerInstanceLinks( final MadInstance<?, ?> instance )
	{
		return producerInstanceLinks.get( instance );
	}

	public Set<MadLink> getConsumerInstanceLinks( final MadInstance<?, ?> instance )
	{
		return consumerInstanceLinks.get( instance );
	}

	public void removeMadInstance( final MadInstance<?, ?> instance )
	{
		final Set<MadLink> consumerLinks = consumerInstanceLinks.get( instance );
		if( consumerLinks != null )
		{
			final Collection<MadLink> toRemove = new ArrayList<MadLink>( consumerLinks );
			for( final MadLink link : toRemove )
			{
				instanceLinks.remove( link );
				final MadChannelInstance pci = link.getProducerChannelInstance();
				final MadInstance<?,?> mi = pci.instance;
				final Set<MadLink> mls = producerInstanceLinks.get( mi );
				mls.remove( link );
			}
			consumerInstanceLinks.remove( instance );
		}
		final Set<MadLink> producerLinks = producerInstanceLinks.get( instance );
		if( producerLinks != null )
		{
			final Collection<MadLink> toRemove = new ArrayList<MadLink>( producerLinks );
			for( final MadLink link : toRemove )
			{
				instanceLinks.remove( link );
				final MadChannelInstance cci = link.getConsumerChannelInstance();
				final MadInstance<?,?> mi = cci.instance;
				final Set<MadLink> mls = consumerInstanceLinks.get( mi );
				mls.remove( link );
			}
			producerInstanceLinks.remove( instance );
		}
	}

	public void deleteLink( final MadLink link ) throws RecordNotFoundException
	{
		final MadChannelInstance cci = link.getConsumerChannelInstance();
		final MadInstance<?,?> cmi = cci.instance;
		final MadChannelInstance pci = link.getProducerChannelInstance();
		final MadInstance<?,?> pmi = pci.instance;

		if( RUNTIME_CHECKING &&
				!instanceLinks.contains( link ) ||
				!consumerInstanceLinks.containsKey( cmi ) ||
				!producerInstanceLinks.containsKey( pmi ) )
		{
			throw new RecordNotFoundException("Removal of unknown link or unknown mads");
		}

		assert( instanceLinks.contains( link ) );

		final Set<MadLink> consumerLinks = consumerInstanceLinks.get( cmi );
		if( consumerLinks.contains( link ) )
		{
			consumerLinks.remove( link );
		}
		else
		{
			if( log.isErrorEnabled() )
			{
				log.error("Missing consumer link entry for " + cmi.getInstanceName() + " " + cci.definition.name);
			}
		}

		final Set<MadLink> producerLinks = producerInstanceLinks.get( pmi );
		if( producerLinks.contains( link ) )
		{
			producerLinks.remove( link );
		}
		else
		{
			if( log.isErrorEnabled() )
			{
				log.error("Missing producer link entry for " + pmi.getInstanceName() + " " + pci.definition.name);
			}
		}

		instanceLinks.remove( link );
	}

	public Set<MadLink> findProducerInstanceLinksReturnNull( final MadChannelInstance channelInstance )
	{
		return producerInstanceLinks.get( channelInstance.instance );
	}

	public void clear()
	{
		instanceLinks.clear();
		consumerInstanceLinks.clear();
		producerInstanceLinks.clear();
	}

	public void addLink( final MadLink link ) throws MAConstraintViolationException
	{
		final MadChannelInstance cci = link.getConsumerChannelInstance();
		final MadInstance<?,?> cmi = cci.instance;
		final MadChannelInstance pci = link.getProducerChannelInstance();
		final MadInstance<?,?> pmi = pci.instance;

		final Set<MadLink> consumerLinks = consumerInstanceLinks.get( cmi );
		if( RUNTIME_CHECKING &&
				consumerLinks.contains( link ) )
		{
			throw new MAConstraintViolationException("Existing link from entry found");
		}
		consumerLinks.add( link );

		final Set<MadLink> producerLinks = producerInstanceLinks.get( pmi );
		if( RUNTIME_CHECKING &&
				producerLinks.contains( link ) )
		{
			throw new MAConstraintViolationException("Existing link to entry found");
		}
		producerLinks.add( link );

		instanceLinks.add( link );
	}

	public void debug()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Graph link map contains:");

			for( final MadLink link : instanceLinks )
			{
				log.debug("Link: " + link.toString() );
			}

			for( final Map.Entry<MadInstance<?, ?>, Set<MadLink>> cils : consumerInstanceLinks.entrySet() )
			{
				log.debug("InstanceLinksFrom: " + cils.getKey().getInstanceName() + " count(" +
						cils.getValue().size() + ")" );
				for( final MadLink l : cils.getValue() )
				{
					log.debug( "\t" + l.toString() );
				}
			}

			for( final Map.Entry<MadInstance<?, ?>, Set<MadLink>> pils : producerInstanceLinks.entrySet() )
			{
				log.debug("InstanceLinksTo: " + pils.getKey().getInstanceName() + " count(" +
						pils.getValue().size() + ")" );
				for( final MadLink l : pils.getValue() )
				{
					log.debug( "\t" + l.toString() );
				}
			}
		}
	}

	public void addMadInstance( final MadInstance<?,?> instance )
	{
		Set<MadLink> consumerLinks = consumerInstanceLinks.get( instance );
		if( consumerLinks == null )
		{
			consumerLinks = new HashSet<MadLink>();
			consumerInstanceLinks.put( instance, consumerLinks );
		}
		Set<MadLink> producerLinks = producerInstanceLinks.get( instance );
		if( producerLinks == null )
		{
			producerLinks = new HashSet<MadLink>();
			producerInstanceLinks.put( instance, producerLinks );
		}
	}
}
