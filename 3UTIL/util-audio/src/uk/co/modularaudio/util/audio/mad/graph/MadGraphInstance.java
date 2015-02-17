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

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class MadGraphInstance<D extends MadGraphDefinition<D,I>, I extends MadGraphInstance<D,I>>
	extends MadInstance<D,I>
{
	private static Log log = LogFactory.getLog( MadGraphInstance.class.getName() );

	// Mad instances within the graph
	protected final Collection<MadInstance<?,?>> instances = new ArrayList<MadInstance<?,?>>();
	protected final Map<String, MadInstance<?,?>> nameToInstanceMap = new HashMap<String, MadInstance<?,?>>();
	protected final Map<MadInstance<?,?>, String> instanceToNameInGraphMap = new HashMap<MadInstance<?,?>, String>();

	// Mad links between mad instances within the graph
	protected final Collection<MadLink> instanceLinks = new ArrayList<MadLink>();
	protected final Map<MadChannelInstance, MadLink> consumerChannelInstanceToLinkMap = new HashMap<MadChannelInstance, MadLink>();
	protected final Map<MadChannelInstance, ArrayList<MadLink>> producerChannelInstanceToLinksMap =
			new HashMap<MadChannelInstance, ArrayList<MadLink>>();
	protected final Map<MadInstance<?,?>, Set<MadLink>> instanceLinksFrom = new HashMap<MadInstance<?,?>, Set<MadLink>>();
	protected final Map<MadInstance<?,?>, Set<MadLink>> instanceLinksTo = new HashMap<MadInstance<?,?>, Set<MadLink>>();

	// Mapping from our graph channels to mad instances within the graph
	protected final Map<MadChannelInstance, MadChannelInstance> graphOutputChannelInstanceToAuChannelInstanceMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();
	protected final Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphInputChannelInstanceToAuChannelInstanceMap =
			new HashMap<MadChannelInstance, ArrayList<MadChannelInstance>>();

	// And back - from the mad instance channels to graph channels
	protected final Map<MadChannelInstance, MadChannelInstance> auChannelInstanceToGraphChannelInstanceMap =
			new HashMap<MadChannelInstance, MadChannelInstance>();

	// Listeners
	protected final Set<MadGraphListener> listeners = new HashSet<MadGraphListener>();

	// Sub graphs
	protected final Set<MadGraphInstance<?,?>> subGraphs = new HashSet<MadGraphInstance<?,?>>();

	public MadGraphInstance( final String graphName,
			final D graphDefinition,
			final Map<MadParameterDefinition, String> parameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( graphName, graphDefinition, parameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		for( final MadInstance<?,?> child : instances )
		{
			child.internalEngineStartup( hardwareChannelSettings, timingParameters, frameTimeFactory );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
		for( final MadInstance<?,?> child : instances )
		{
			child.internalEngineStop();
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , int frameOffset , final int numFrames  )
	{
		final String msg = "Graph instance does not support the process method. Create a render plan from it, and use that";
		log.error( msg );
		return RealtimeMethodReturnCodeEnum.FAIL_FATAL;
	}

	// Graph related methods
	public Collection<MadInstance<?,?>> getInstances()
	{
		return instances;
	}

	public String getInstanceNameInGraph( final MadInstance<?,?> instance )
	{
		return instanceToNameInGraphMap.get( instance );
	}

	public void addLink( final MadLink link )
		throws MAConstraintViolationException
	{
		final MadInstance<?, ?> producerAui = link.getProducerChannelInstance().instance;
		final MadInstance<?, ?> consumerAui = link.getConsumerChannelInstance().instance;
		if( instances.contains( producerAui )
				&&
				instances.contains( consumerAui ) )
		{
			instanceLinks.add( link );
			final Set<MadLink> linksFromProducerSet = instanceLinksFrom.get( producerAui );
			linksFromProducerSet.add( link );

			final Set<MadLink> linksToConsumerSet = instanceLinksTo.get( consumerAui );
			linksToConsumerSet.add( link );

			// Finally add these channels to our channel instance to link lookup map
			addProducerChannelInstanceLink( link );
			addConsumerChannelInstanceLink( link );
		}
		else
		{
			final String msg = "Both the producer and consumer must exist in the graph before a link can be made.";
			throw new MAConstraintViolationException( msg );
		}
	}

	private void addProducerChannelInstanceLink( final MadLink link )
	{
		final MadChannelInstance auci = link.getProducerChannelInstance();
		ArrayList<MadLink> foundLinks = producerChannelInstanceToLinksMap.get(auci );
		if( foundLinks == null )
		{
			foundLinks = new ArrayList<MadLink>();
			producerChannelInstanceToLinksMap.put( auci, foundLinks );
		}
		foundLinks.add( link );
	}

	private void addConsumerChannelInstanceLink( final MadLink link ) throws MAConstraintViolationException
	{
		final MadChannelInstance auci = link.getConsumerChannelInstance();
		if( consumerChannelInstanceToLinkMap.containsKey( auci ) )
		{
			throw new MAConstraintViolationException( "ConstraintViolationException attempting to add consumer channel instance link: " + auci.toString() );
		}
		else
		{
			consumerChannelInstanceToLinkMap.put( auci, link );
		}
	}

	private void removeProducerChannelInstanceLink( final MadLink link ) throws RecordNotFoundException
	{
		final MadChannelInstance auci = link.getProducerChannelInstance();
		final ArrayList<MadLink> foundLinks = producerChannelInstanceToLinksMap.get( auci );
		if( foundLinks == null )
		{
			throw new RecordNotFoundException("No such producer channel instance: " + auci.toString() );
		}
		else
		{
			foundLinks.remove( link );
		}
	}

	private void removeConsumerChannelInstanceLink( final MadLink link ) throws RecordNotFoundException
	{
		final MadChannelInstance auci = link.getConsumerChannelInstance();
		if( !consumerChannelInstanceToLinkMap.containsKey( auci ) )
		{
			throw new RecordNotFoundException("No such consumer channel instance: " + auci.toString() );
		}
		consumerChannelInstanceToLinkMap.remove( auci );
	}

	public void deleteLink( final MadLink link ) throws RecordNotFoundException
	{
		final MadInstance<?,?> producerAui = link.getProducerChannelInstance().instance;
		final MadInstance<?,?> consumerAui = link.getConsumerChannelInstance().instance;
		final Set<MadLink> linksFromProducerSet = instanceLinksFrom.get( producerAui );
		linksFromProducerSet.remove( link );
		final Set<MadLink> linksToConsumerSet = instanceLinksTo.get( consumerAui );
		linksToConsumerSet.remove( link );

		removeProducerChannelInstanceLink( link );
		removeConsumerChannelInstanceLink( link );

		instanceLinks.remove( link );
	}

	public Collection<MadLink> getLinks()
	{
		return instanceLinks;
	}

	public Set<MadLink> findAllLinksToInstance( final MadInstance<?,?> instance )
	{
		return instanceLinksTo.get(  instance );
	}

	public Set<MadLink> findAllLinksFromInstance( final MadInstance<?,?> instance )
	{
		return instanceLinksFrom.get(  instance );
	}

	public boolean checkCanAddInstanceWithName( final String nameInGraph )
	{
		return !nameToInstanceMap.containsKey( nameInGraph );
	}

	public void addInstanceWithName( final MadInstance<?,?> instance, final String nameInGraph )
			throws DatastoreException, MAConstraintViolationException
	{
		if( nameToInstanceMap.containsKey( nameInGraph ) )
		{
			final String msg = "An instance with the name " + nameInGraph + " already exists in this graph";
			throw new MAConstraintViolationException( msg );
		}
		instances.add(  instance );
		nameToInstanceMap.put( nameInGraph, instance );
		instanceToNameInGraphMap.put( instance, nameInGraph );
		final Set<MadLink> linksFrom = new HashSet<MadLink>();
		instanceLinksFrom.put( instance,  linksFrom );
		final Set<MadLink> linksTo = new HashSet<MadLink>();
		instanceLinksTo.put( instance,  linksTo );

		if( instance instanceof MadGraphInstance )
		{
			final MadGraphInstance<?,?> subGraphInstance = (MadGraphInstance<?,?>)instance;
			// Subscribe the graph to this new child so change signals are propogated too
			subGraphs.add( subGraphInstance );
			for( final MadGraphListener gl : listeners )
			{
				subGraphInstance.addGraphListener( gl );
			}
		}
	}

	public boolean containsInstance( final MadInstance<?,?> instance )
	{
		return instances.contains( instance );
	}

	public void removeInstanceByNameInGraph( final String instanceName )
		throws RecordNotFoundException
	{
		final MadInstance<?,?> instanceToRemove = nameToInstanceMap.get( instanceName );
		if( instanceToRemove == null )
		{
			throw new RecordNotFoundException();
		}
		else
		{
			nameToInstanceMap.remove( instanceName );
			removeInstance( instanceToRemove );
		}
	}

	public void removeInstance( final MadInstance<?,?> instanceToRemove )
		throws RecordNotFoundException
	{
		if( !instances.contains( instanceToRemove ) )
		{
			throw new RecordNotFoundException();
		}

		// Check if it's a subgraph first - if it is, we'll be removing the listener
		if( instanceToRemove instanceof MadGraphInstance<?,?> )
		{
			final MadGraphInstance<?,?> subGraph = (MadGraphInstance<?,?>)instanceToRemove;
			for( final MadGraphListener gl : listeners )
			{
				subGraph.removeGraphListener( gl );
			}
			subGraphs.remove( subGraph );
		}
		// Remove any graph channels it is connected to
		directRemoveAudioInstanceFromGraphChannels( instanceToRemove );

		// Remove all links for this component
		directRemoveInstanceLinks( instanceToRemove );
		final String nameInGraph = instanceToNameInGraphMap.get( instanceToRemove );
		nameToInstanceMap.remove( nameInGraph );
		instanceToNameInGraphMap.remove( instanceToRemove );
		instances.remove( instanceToRemove );
		instanceLinksFrom.remove( instanceToRemove );
		instanceLinksTo.remove( instanceToRemove );
	}

	private void directRemoveAudioInstanceFromGraphChannels( final MadInstance<?,?> instanceToRemove )
	{
		// Need to loop around the mapped mad instance channels looking to see any of them are exposed as graph channels
		// if they are remove them.
		// We build a list of what to remove before we remove them so the iterator doesn't become invalid
		final ArrayList<MadChannelInstance> aucisToRemove = new ArrayList<MadChannelInstance>();
		for( final Map.Entry<MadChannelInstance, MadChannelInstance> auci2gc : auChannelInstanceToGraphChannelInstanceMap.entrySet() )
		{
			final MadChannelInstance auci = auci2gc.getKey();
			if( auci.instance == instanceToRemove )
			{
				aucisToRemove.add( auci );
			}
		}

		for( final MadChannelInstance auci : aucisToRemove )
		{
			final MadChannelInstance graphChannel = auChannelInstanceToGraphChannelInstanceMap.get( auci );

			if( graphChannel.definition.direction == MadChannelDirection.PRODUCER )
			{
				graphOutputChannelInstanceToAuChannelInstanceMap.remove( graphChannel );
				auChannelInstanceToGraphChannelInstanceMap.remove( auci );
			}
			else
			{
				final ArrayList<MadChannelInstance> mappedChannelInstances = graphInputChannelInstanceToAuChannelInstanceMap.get( graphChannel );
				if( !mappedChannelInstances.contains( auci ) )
				{
					log.error("Missing channel in consumer list");
				}
				mappedChannelInstances.remove( auci );
				if( mappedChannelInstances.size() == 0 )
				{
					graphInputChannelInstanceToAuChannelInstanceMap.remove( graphChannel );
				}
				auChannelInstanceToGraphChannelInstanceMap.remove( auci );
			}
		}
	}

	public void exposeAudioInstanceChannelAsGraphChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose ) throws RecordNotFoundException, MAConstraintViolationException
	{
		// Check the graph channel exists
		if( !channelInstanceToIndexMap.containsKey( graphChannelInstance ) )
		{
			final String msg = "Unknown graph channel passed when attempting to expose mad instance channel as graph channel";
			throw new RecordNotFoundException( msg );
		}
		else if( graphChannelInstance.definition.direction != channelInstanceToExpose.definition.direction )
		{
			final String msg = "Graph channel must match mad instance channel direction when exposing it";
			throw new MAConstraintViolationException( msg );
		}
		// Check if it's already assigned
//		if( graphConsumerChannelInstanceToAuChannelInstanceMap.get( graphChannelInstance ) != null ||
//				graphProducerChannelInstanceToAuChannelInstanceMap.get( graphChannelInstance ) 1
		if( graphChannelInstance.definition.direction == MadChannelDirection.PRODUCER )
		{
			if( graphOutputChannelInstanceToAuChannelInstanceMap.get( graphChannelInstance ) != null )
			{
				final String msg = "The graph channel " + graphChannelInstance.definition.name + " is already mapped";
				throw new MAConstraintViolationException( msg );
			}
			else
			{
//				log.debug("Adding output channel instance to mad channel instance map for " + graphChannelInstance.toString() );
				graphOutputChannelInstanceToAuChannelInstanceMap.put( graphChannelInstance, channelInstanceToExpose);
				auChannelInstanceToGraphChannelInstanceMap.put( channelInstanceToExpose, graphChannelInstance );
			}
		}
		else
		{
			// Don't need to check if it's already mapped - many can bind
//			log.debug("Adding input channel instance to mad channel instance map for " + graphChannelInstance.toString() );
			ArrayList<MadChannelInstance> mappedAuChannels = graphInputChannelInstanceToAuChannelInstanceMap.get( graphChannelInstance );
			if( mappedAuChannels == null )
			{
				mappedAuChannels = new ArrayList<MadChannelInstance>();
				graphInputChannelInstanceToAuChannelInstanceMap.put( graphChannelInstance, mappedAuChannels );
			}
			mappedAuChannels.add( channelInstanceToExpose );
			auChannelInstanceToGraphChannelInstanceMap.put( channelInstanceToExpose, graphChannelInstance );
		}
	}

	public void removeAudioInstanceChannelAsGraphChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
		throws RecordNotFoundException
	{
		// Check it's really assigned
		final MadChannelInstance curGraphChan = auChannelInstanceToGraphChannelInstanceMap.get( channelInstanceExposed );
		if( curGraphChan == null )
		{
			final String msg = "The passed mad instance and channel are not currently assigned to the graph channel passed.";
			throw new RecordNotFoundException( msg );
		}
		else
		{
			if( curGraphChan.definition.direction == MadChannelDirection.PRODUCER )
			{
//				log.debug("Removing output channel instance to mad channel instance map for " + curGraphChan.toString() );
				graphOutputChannelInstanceToAuChannelInstanceMap.remove( curGraphChan );
				auChannelInstanceToGraphChannelInstanceMap.remove( channelInstanceExposed );
			}
			else
			{
//				log.debug("Removing input channel instance to mad channel instance map for " + curGraphChan.toString() );
				final ArrayList<MadChannelInstance> mappedChans = graphInputChannelInstanceToAuChannelInstanceMap.get( curGraphChan );
				if( mappedChans == null )
				{
					final String msg = "The passed mad instance and channel are not currently assigned to the graph channel passed.";
					throw new RecordNotFoundException( msg );
				}
				mappedChans.remove( channelInstanceExposed );
				if( mappedChans.size() == 0 )
				{
					graphInputChannelInstanceToAuChannelInstanceMap.remove( curGraphChan );
				}
				auChannelInstanceToGraphChannelInstanceMap.remove( channelInstanceExposed );
			}
		}
	}

	public Map<MadChannelInstance, MadChannelInstance> getGraphOutputChannelInstanceMap()
	{
		return graphOutputChannelInstanceToAuChannelInstanceMap;
	}

	public Map<MadChannelInstance, ArrayList<MadChannelInstance>> getGraphInputChannelInstanceMap()
	{
		return graphInputChannelInstanceToAuChannelInstanceMap;
	}

	public Map<MadChannelInstance, MadChannelInstance> getAuChannelInstanceToGraphChannelInstanceMap()
	{
		return auChannelInstanceToGraphChannelInstanceMap;
	}

	// Private methods
	private void directRemoveInstanceLinks( final MadInstance<?,?> instance ) throws RecordNotFoundException
	{
		final Set<MadLink> linksFrom = instanceLinksFrom.get( instance );
		final Set<MadLink> linksTo = instanceLinksTo.get( instance );
		Collection<MadLink> toRemove = new ArrayList<MadLink>( linksFrom );
		for( final MadLink link : toRemove )
		{
			deleteLink( link );
		}

		toRemove = new ArrayList<MadLink>( linksTo);
		for( final MadLink link : toRemove )
		{
			deleteLink( link );
		}
	}

	public MadLink findLinkForConsumerChannelInstanceReturnNull( final MadChannelInstance channelInstance )
	{
		MadLink retVal = null;
		retVal = consumerChannelInstanceToLinkMap.get( channelInstance );
		return retVal;
	}

	public ArrayList<MadLink> findLinksForProducerChannelInstanceReturnNull( final MadChannelInstance channelInstance )
	{
		ArrayList<MadLink> retVal = null;
		retVal = producerChannelInstanceToLinksMap.get( channelInstance );
		return retVal;
	}

	public MadInstance<?,?> getInstanceByName( final String name )
		throws RecordNotFoundException
	{
		final MadInstance<?,?> curInstance = nameToInstanceMap.get( name );
		if( curInstance == null )
		{
			final String msg = "A mad instance with the name " + name + " does not exist in this graph";
			throw new RecordNotFoundException( msg );
		}
		else
		{
			return curInstance;
		}
	}

	public void renameInstanceByName( final String oldName, final String newName, final String newNameInGraph )
		throws RecordNotFoundException
	{
		final MadInstance<?,?> instanceToRename = getInstanceByName( oldName );
		nameToInstanceMap.remove( oldName );
		nameToInstanceMap.put( newName, instanceToRename );
		instanceToNameInGraphMap.put( instanceToRename, newNameInGraph );
	}

	public String getNameForNewComponentInstance( final MadDefinition<?,?> definitionToAdd )
	{
		final String newName = definitionToAdd.getName() + " " + (instances.size() + 1);
		return newName;
	}

	public void addGraphListener( final MadGraphListener listener )
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Adding graph listener " + listener.getName() + " to graph \"" + instanceName + "\"");
		}

		this.listeners.add( listener );
		for( final MadGraphInstance<?,?> subGraph : subGraphs )
		{
			subGraph.addGraphListener( listener );
		}
	}

	public void removeGraphListener( final MadGraphListener listener )
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Removing graph listener " + listener.getName() + " from graph \"" + instanceName + "\"");
		}
		for( final MadGraphInstance<?,?> subGraph : subGraphs )
		{
			subGraph.removeGraphListener( listener );
		}
		this.listeners.remove( listener );
	}

	public void fireGraphChangeSignal()
	{
		for( final MadGraphListener listener : listeners )
		{
			listener.receiveGraphChangeSignal();
		}
	}

	public boolean hasListeners()
	{
		return listeners.size() > 0;
	}

	@Override
	public boolean isContainer()
	{
		return true;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		instances.clear();
		nameToInstanceMap.clear();
		instanceToNameInGraphMap.clear();
		instanceLinks.clear();
		consumerChannelInstanceToLinkMap.clear();
		producerChannelInstanceToLinksMap.clear();
		instanceLinksFrom.clear();
		instanceLinksTo.clear();
		graphOutputChannelInstanceToAuChannelInstanceMap.clear();
		graphInputChannelInstanceToAuChannelInstanceMap.clear();
		auChannelInstanceToGraphChannelInstanceMap.clear();
		listeners.clear();
		subGraphs.clear();
	}

}
