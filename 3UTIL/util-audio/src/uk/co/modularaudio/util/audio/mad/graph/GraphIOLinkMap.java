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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelDefinition.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class GraphIOLinkMap
{
	private static Log log = LogFactory.getLog( GraphIOLinkMap.class.getName() );

	private static final boolean RUNTIME_CHECKING = false;

	private final Map<MadChannelInstance, Collection<MadChannelInstance>> graphConsumerChannelToMadChannelInstanceMap =
			new HashMap<MadChannelInstance, Collection<MadChannelInstance>>();

	private final Map<MadChannelInstance, MadChannelInstance> graphProducerChannelToMadChannelInstanceMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();

	private final Map<MadChannelInstance, MadChannelInstance> madChannelInstanceToGraphConsumerMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();

	private final Map<MadChannelInstance, Collection<MadChannelInstance>> madChannelInstanceToGraphProducerMap =
			new HashMap<MadChannelInstance, Collection<MadChannelInstance>>();

	public GraphIOLinkMap()
	{
	}

	public void addMadInstance( final MadInstance<?, ?> instance )
	{
	}

	public void removeMadInstance( final MadGraphInstance<?,?> graph,
			final MadInstance<?,?> instanceToRemove )
		throws RecordNotFoundException
	{
		try
		{
			final MadChannelInstance[] instanceChannels = instanceToRemove.getChannelInstances();
			for( final MadChannelInstance mci : instanceChannels )
			{
				switch( mci.definition.direction )
				{
					case CONSUMER:
					{
						final MadChannelInstance graphChannel = madChannelInstanceToGraphConsumerMap.get( mci );
						if( graphChannel != null )
						{
							unmapConsumerChannel( graphChannel, mci );
						}
						break;
					}
					default:
					{
						final Collection<MadChannelInstance> graphChannels = madChannelInstanceToGraphProducerMap.get( mci );
						if( graphChannels != null )
						{
							final ArrayList<MadChannelInstance> localCopy = new ArrayList<MadChannelInstance>( graphChannels );
							for( final MadChannelInstance gci : localCopy )
							{
								unmapProducerChannel( gci, mci );
							}
						}
						break;
					}
				}
			}
		}
		catch( final MAConstraintViolationException cve )
		{
			throw new RecordNotFoundException("Unable to remove instance io links due to MACVE: " + cve.toString(),
					cve );
		}
	}

	public void mapConsumerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
			throws MAConstraintViolationException
	{
		Collection<MadChannelInstance> mcis = graphConsumerChannelToMadChannelInstanceMap.get( graphChannelInstance );

		if( RUNTIME_CHECKING )
		{
			if( graphChannelInstance.definition.direction != MadChannelDirection.CONSUMER ||
				channelInstanceToExpose.definition.direction != MadChannelDirection.CONSUMER )
			{
				throw new MAConstraintViolationException("Consumer channel mapping directions incorrect");
			}

			if( mcis != null && mcis.contains( channelInstanceToExpose ) )
			{
				throw new MAConstraintViolationException("Consumer channel mapping failed as already mapped");
			}

			if( madChannelInstanceToGraphConsumerMap.containsKey( channelInstanceToExpose ) )
			{
				throw new MAConstraintViolationException("Consumer channel mapping failed mci checks");
			}
		}

		if( mcis == null )
		{
			mcis = new ArrayList<MadChannelInstance>();
			graphConsumerChannelToMadChannelInstanceMap.put( graphChannelInstance, mcis );
		}

		mcis.add( channelInstanceToExpose );

		madChannelInstanceToGraphConsumerMap.put( channelInstanceToExpose, graphChannelInstance );

//		log.debug("Mapped graph consumer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceToExpose.instance.getInstanceName() + " \"" +
//				channelInstanceToExpose.toString() + "\"");
	}

	public Map<MadChannelInstance, Collection<MadChannelInstance>> getGraphConsumerChannelInstanceMap()
	{
		return graphConsumerChannelToMadChannelInstanceMap;
	}

	public void unmapConsumerChannel( final MadChannelInstance graphChannelInstance,
				final MadChannelInstance channelInstanceExposed )
				throws RecordNotFoundException, MAConstraintViolationException
	{
		final Collection<MadChannelInstance> mcis = graphConsumerChannelToMadChannelInstanceMap.get( graphChannelInstance );

		if( RUNTIME_CHECKING )
		{
			if( !graphConsumerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Consumer channel unmapping failed as not mapped");
			}

			if( mcis == null || !mcis.contains( channelInstanceExposed ) )
			{
				throw new MAConstraintViolationException( "Consumer channel unmapping failed to find existing map" );
			}

			if( !madChannelInstanceToGraphConsumerMap.containsKey( channelInstanceExposed ) )
			{
				throw new MAConstraintViolationException( "Consumer channel unmapping failed to find specific channel" );
			}
		}

		mcis.remove( channelInstanceExposed );
		if( mcis.size() == 0 )
		{
			graphConsumerChannelToMadChannelInstanceMap.remove( graphChannelInstance );
		}

		madChannelInstanceToGraphConsumerMap.remove( channelInstanceExposed );

//		log.debug("Unmapped graph consumer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceExposed.instance.getInstanceName() + " \"" +
//				channelInstanceExposed.toString() + "\"");
	}

	public void mapProducerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
			throws MAConstraintViolationException
	{
		if( RUNTIME_CHECKING )
		{
			if( graphChannelInstance.definition.direction != MadChannelDirection.PRODUCER ||
					channelInstanceToExpose.definition.direction != MadChannelDirection.PRODUCER )
			{
				throw new MAConstraintViolationException("Producer channel mapping directions incorrect");
			}

			if( graphProducerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new MAConstraintViolationException("Producer channel mapping failed as channel already mapped");
			}
		}

		graphProducerChannelToMadChannelInstanceMap.put( graphChannelInstance, channelInstanceToExpose );

		Collection<MadChannelInstance> mcis = madChannelInstanceToGraphProducerMap.get( channelInstanceToExpose );

		if( mcis == null )
		{
			mcis = new ArrayList<MadChannelInstance>();
			madChannelInstanceToGraphProducerMap.put( channelInstanceToExpose, mcis );
		}
		mcis.add( graphChannelInstance );

//		log.debug("Mapped graph producer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceToExpose.instance.getInstanceName() + " \"" +
//				channelInstanceToExpose.toString() + "\"");
	}

	public Map<MadChannelInstance, MadChannelInstance> getGraphProducerChannelInstanceMap()
	{
		return graphProducerChannelToMadChannelInstanceMap;
	}

	public boolean isProducerChannelExposed( final MadChannelInstance auci )
	{
		final Collection<MadChannelInstance> mgcs = madChannelInstanceToGraphProducerMap.get( auci );
		return mgcs != null && mgcs.size() > 0;
	}

	public void unmapProducerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
			throws RecordNotFoundException, MAConstraintViolationException
	{
		final Collection<MadChannelInstance> gcis = madChannelInstanceToGraphProducerMap.get( channelInstanceExposed );

		if( RUNTIME_CHECKING )
		{
			if( !graphProducerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Producer channel unmapping failed as not mapped");
			}

			if( gcis == null || !gcis.contains( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Producer channel unmapping failed as not mapped");
			}
		}

		graphProducerChannelToMadChannelInstanceMap.remove( graphChannelInstance );

		gcis.remove( graphChannelInstance );
		if( gcis.size() == 0 )
		{
			madChannelInstanceToGraphProducerMap.remove( channelInstanceExposed );
		}

//		log.debug("Unmapped graph producer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceExposed.instance.getInstanceName() + " \"" +
//				channelInstanceExposed.toString() + "\"");
	}

	public Collection<MadChannelInstance> getGraphChannelsExposedForProducerChannel( final MadChannelInstance auci )
	{
		return madChannelInstanceToGraphProducerMap.get( auci );
	}

	public void clear()
	{
		graphConsumerChannelToMadChannelInstanceMap.clear();
		madChannelInstanceToGraphConsumerMap.clear();
		graphProducerChannelToMadChannelInstanceMap.clear();
		madChannelInstanceToGraphProducerMap.clear();
	}

	public void debug()
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Graph IO link map contains:");

			for( final Map.Entry<MadChannelInstance, MadChannelInstance> gcmc : graphProducerChannelToMadChannelInstanceMap.entrySet() )
			{
				log.debug("GraphProducerToMadChannel: " + gcmc.getKey().toString() + " " +
						gcmc.getValue().instance.getInstanceName() + " " +
						gcmc.getValue().toString() );
			}

			for( final Map.Entry<MadChannelInstance, Collection<MadChannelInstance>> gpma : graphConsumerChannelToMadChannelInstanceMap.entrySet() )
			{
				final Collection<MadChannelInstance> mcs = gpma.getValue();
				if( mcs != null )
				{
					for( final MadChannelInstance ci : mcs )
					{
						log.debug("GraphConsumerToMadChannel: " +
								gpma.getKey().toString() + " " +
								ci.instance.getInstanceName() + " " + ci.toString() );
					}
				}
			}

			for( final Map.Entry<MadChannelInstance, Collection<MadChannelInstance>> mcga : madChannelInstanceToGraphProducerMap.entrySet() )
			{
				final Collection<MadChannelInstance> gcs = mcga.getValue();
				if( gcs != null )
				{
					for( final MadChannelInstance gi : gcs )
					{
						log.debug("MadChannelToGraphProducerChannel: " +
								mcga.getKey().instance.getInstanceName() + " " +
								mcga.getKey().toString() + " " +
								gi.toString() );
					}
				}
			}

			for( final Map.Entry<MadChannelInstance, MadChannelInstance> mcgc : madChannelInstanceToGraphConsumerMap.entrySet() )
			{
				log.debug("MadChannelToGraphConsumerChannel: " +
						mcgc.getKey().instance.getInstanceName() + " " +
						mcgc.getKey().toString() + " " +
						mcgc.getValue().toString());
			}
		}
	}
}
