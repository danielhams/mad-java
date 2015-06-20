package uk.co.modularaudio.util.audio.mad.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class GraphIOLinkMap
{
	private static Log log = LogFactory.getLog( GraphIOLinkMap.class.getName() );

	private static final boolean RUNTIME_CHECKING = false;

	private final Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphConsumerChannelToMadChannelInstanceMap =
			new HashMap<MadChannelInstance, ArrayList<MadChannelInstance>>();

	private final Map<MadChannelInstance, MadChannelInstance> graphProducerChannelToMadChannelInstanceMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();

	private final Map<MadChannelInstance, MadChannelInstance> madChannelInstanceToGraphConsumerMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();

	private final Map<MadChannelInstance, ArrayList<MadChannelInstance>> madChannelInstanceToGraphProducerMap =
			new HashMap<MadChannelInstance, ArrayList<MadChannelInstance>>();

	public GraphIOLinkMap()
	{
	}

	public Map<MadChannelInstance, ArrayList<MadChannelInstance>> getGraphConsumerChannelInstanceMap()
	{
		return graphConsumerChannelToMadChannelInstanceMap;
	}

	public Map<MadChannelInstance, MadChannelInstance> getGraphProducerChannelInstanceMap()
	{
		return graphProducerChannelToMadChannelInstanceMap;
	}

	public void mapConsumerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
			throws MAConstraintViolationException
	{
		if( RUNTIME_CHECKING )
		{
			if( graphChannelInstance.definition.direction != MadChannelDirection.CONSUMER ||
					channelInstanceToExpose.definition.direction != MadChannelDirection.CONSUMER )
				{
					throw new MAConstraintViolationException("Consumer channel mapping directions incorrect");
				}

			if( graphConsumerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new MAConstraintViolationException("Consumer channel mapping failed as already mapped");
			}
		}

		ArrayList<MadChannelInstance> mcis = graphConsumerChannelToMadChannelInstanceMap.get( graphChannelInstance );

		if( mcis == null )
		{
			mcis = new ArrayList<MadChannelInstance>();
			graphConsumerChannelToMadChannelInstanceMap.put( graphChannelInstance, mcis );
		}

		mcis.add( channelInstanceToExpose );


		if( RUNTIME_CHECKING )
		{
			if( madChannelInstanceToGraphConsumerMap.containsKey( channelInstanceToExpose ) )
			{
				throw new MAConstraintViolationException("Consumer channel mapping failed mci checks");
			}
		}

		madChannelInstanceToGraphConsumerMap.put( channelInstanceToExpose, graphChannelInstance );

//		log.debug("Mapped graph consumer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceToExpose.instance.getInstanceName() + " \"" +
//				channelInstanceToExpose.toString() + "\"");
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

		ArrayList<MadChannelInstance> mcis = madChannelInstanceToGraphProducerMap.get( channelInstanceToExpose );

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

	public void unmapConsumerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
			throws RecordNotFoundException, MAConstraintViolationException
	{
		if( RUNTIME_CHECKING )
		{
			if( graphChannelInstance.definition.direction != MadChannelDirection.CONSUMER ||
					channelInstanceExposed.definition.direction != MadChannelDirection.CONSUMER )
				{
					throw new MAConstraintViolationException("Consumer channel unmapping directions incorrect");
				}

			if( !graphConsumerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Consumer channel unmapping failed as not mapped");
			}
		}

		final ArrayList<MadChannelInstance> mcis = graphConsumerChannelToMadChannelInstanceMap.get( graphChannelInstance );

		if( RUNTIME_CHECKING )
		{
			if( mcis == null || !mcis.contains( channelInstanceExposed ) )
			{
				throw new MAConstraintViolationException( "Consumer channel unmapping failed to find existing map" );
			}
		}

		mcis.remove( channelInstanceExposed );
		if( mcis.size() == 0 )
		{
			graphConsumerChannelToMadChannelInstanceMap.remove( graphChannelInstance );
		}

		if( RUNTIME_CHECKING )
		{
			if( !madChannelInstanceToGraphConsumerMap.containsKey( channelInstanceExposed ) )
			{
				throw new MAConstraintViolationException( "Consumer channel unmapping failed to find specific channel" );
			}
		}

		madChannelInstanceToGraphConsumerMap.remove( channelInstanceExposed );

//		log.debug("Unmapped graph consumer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceExposed.instance.getInstanceName() + " \"" +
//				channelInstanceExposed.toString() + "\"");
	}

	public void unmapProducerChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
			throws RecordNotFoundException, MAConstraintViolationException
	{
		if( RUNTIME_CHECKING )
		{
			if( graphChannelInstance.definition.direction != MadChannelDirection.PRODUCER ||
					channelInstanceExposed.definition.direction != MadChannelDirection.PRODUCER )
			{
				throw new MAConstraintViolationException("Producer channel unmapping directions incorrect");
			}
		}

		if( RUNTIME_CHECKING )
		{
			if( !graphProducerChannelToMadChannelInstanceMap.containsKey( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Producer channel unmapping failed as not mapped");
			}
		}
		graphProducerChannelToMadChannelInstanceMap.remove( graphChannelInstance );

		final ArrayList<MadChannelInstance> gcis = madChannelInstanceToGraphProducerMap.get( channelInstanceExposed );

		if( RUNTIME_CHECKING )
		{
			if( gcis == null || !gcis.contains( graphChannelInstance ) )
			{
				throw new RecordNotFoundException("Producer channel unmapping failed as not mapped");
			}
		}

		gcis.remove( graphChannelInstance );
		if( gcis.size() == 0 )
		{
			madChannelInstanceToGraphProducerMap.remove( channelInstanceExposed );
		}

//		log.debug("Unmapped graph producer channel \"" + graphChannelInstance.toString() + "\" to ");
//		log.debug( channelInstanceExposed.instance.getInstanceName() + " \"" +
//				channelInstanceExposed.toString() + "\"");
	}

	public void clear()
	{
		graphConsumerChannelToMadChannelInstanceMap.clear();
		madChannelInstanceToGraphConsumerMap.clear();
		graphProducerChannelToMadChannelInstanceMap.clear();
		madChannelInstanceToGraphProducerMap.clear();
	}

	public ArrayList<MadChannelInstance> getGraphChannelsExposedForProducerChannel( final MadChannelInstance auci )
	{
		return madChannelInstanceToGraphProducerMap.get( auci );
	}

	public void debug()
	{
		log.debug("Graph IO link map contains:");

		for( final Map.Entry<MadChannelInstance, MadChannelInstance> gcmc : graphProducerChannelToMadChannelInstanceMap.entrySet() )
		{
			log.debug("GraphProducerToMadChannel: " + gcmc.getKey().toString() + " " +
					gcmc.getValue().instance.getInstanceName() + " " + gcmc.getValue().toString() );
		}

		for( final Map.Entry<MadChannelInstance, ArrayList<MadChannelInstance>> gpma : graphConsumerChannelToMadChannelInstanceMap.entrySet() )
		{
			final ArrayList<MadChannelInstance> mcs = gpma.getValue();
			if( mcs != null )
			{
				for( final MadChannelInstance ci : mcs )
				{
					log.debug("GraphConsumerToMadChannel: " + gpma.getKey().toString() + " " +
							ci.instance.getInstanceName() + " " + ci.toString() );
				}
			}
		}

		for( final Map.Entry<MadChannelInstance, ArrayList<MadChannelInstance>> mcga : madChannelInstanceToGraphProducerMap.entrySet() )
		{
			final ArrayList<MadChannelInstance> gcs = mcga.getValue();
			if( gcs != null )
			{
				for( final MadChannelInstance gi : gcs )
				{
					log.debug("MadChannelToGraphProducerChannel: " + mcga.getKey().instance.getInstanceName() + " " + mcga.getKey().toString() + " " +
							gi.toString() );
				}
			}
		}

		for( final Map.Entry<MadChannelInstance, MadChannelInstance> mcgc : madChannelInstanceToGraphConsumerMap.entrySet() )
		{
			log.debug("MadChannelToGraphConsumerChannel: " + mcgc.getKey().instance.getInstanceName() + " " + mcgc.getKey().toString() + " " +
					mcgc.getValue().toString());
		}
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
			final MadChannelInstance[] graphChannels = graph.getChannelInstances();
			for( final MadChannelInstance gci : graphChannels )
			{
				switch( gci.definition.direction )
				{
					case CONSUMER:
					{
						final ArrayList<MadChannelInstance> mic = graphConsumerChannelToMadChannelInstanceMap.get( gci );
						if( mic != null )
						{
							final ArrayList<MadChannelInstance> itr = new ArrayList<MadChannelInstance>( mic );
							for( final MadChannelInstance mci : itr )
							{
								if( mci.instance == instanceToRemove )
								{
									unmapConsumerChannel( gci, mci );
								}
							}
						}
						break;
					}
					default:
					{
						final MadChannelInstance mci = graphProducerChannelToMadChannelInstanceMap.get( gci );
						if( mci != null && mci.instance == instanceToRemove )
						{
							unmapProducerChannel( gci, mci );
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
}
