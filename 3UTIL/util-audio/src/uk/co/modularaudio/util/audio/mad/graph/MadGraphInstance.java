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
	protected GraphLinkMap linkMap = new GraphLinkMap();

	// Instance within the graph can be mapped to the graph channels for IO
	protected GraphIOLinkMap ioLinkMap = new GraphIOLinkMap();

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
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
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
			linkMap.addLink( link );
		}
		else
		{
			final String msg = "Both the producer and consumer must exist in the graph before a link can be made.";
			throw new MAConstraintViolationException( msg );
		}
	}

	public void deleteLink( final MadLink link ) throws RecordNotFoundException
	{
		linkMap.deleteLink( link );
	}

	public Collection<MadLink> getLinks()
	{
		return linkMap.getLinks();
	}

	public Set<MadLink> getProducerInstanceLinks( final MadInstance<?,?> instance )
	{
		return linkMap.getProducerInstanceLinks( instance );
	}

	public Set<MadLink> getConsumerInstanceLinks( final MadInstance<?,?> instance )
	{
		return linkMap.getConsumerInstanceLinks( instance );
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

		linkMap.addMadInstance( instance );
		ioLinkMap.addMadInstance( instance );
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

		// Remove all links for this component
		final String nameInGraph = instanceToNameInGraphMap.get( instanceToRemove );
		nameToInstanceMap.remove( nameInGraph );
		instanceToNameInGraphMap.remove( instanceToRemove );
		instances.remove( instanceToRemove );

		linkMap.removeMadInstance( instanceToRemove );
		ioLinkMap.removeMadInstance( this, instanceToRemove );
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

		if( graphChannelInstance.definition.direction == MadChannelDirection.PRODUCER )
		{
			ioLinkMap.mapProducerChannel( graphChannelInstance, channelInstanceToExpose );
		}
		else
		{
			ioLinkMap.mapConsumerChannel( graphChannelInstance, channelInstanceToExpose );
		}
	}

	public void removeAudioInstanceChannelAsGraphChannel( final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
		throws RecordNotFoundException, MAConstraintViolationException
	{
		if( graphChannelInstance.definition.direction == MadChannelDirection.PRODUCER )
		{
			ioLinkMap.unmapProducerChannel( graphChannelInstance, channelInstanceExposed );
		}
		else
		{
			ioLinkMap.unmapConsumerChannel( graphChannelInstance, channelInstanceExposed );
		}
	}

	public Map<MadChannelInstance, MadChannelInstance> getGraphOutputChannelInstanceMap()
	{
		return ioLinkMap.getGraphProducerChannelInstanceMap();
	}

	public Map<MadChannelInstance, ArrayList<MadChannelInstance>> getGraphInputChannelInstanceMap()
	{
		return ioLinkMap.getGraphConsumerChannelInstanceMap();
	}

	public Set<MadLink> findProducerInstanceLinksReturnNull( final MadChannelInstance channelInstance )
	{
		return linkMap.findProducerInstanceLinksReturnNull( channelInstance );
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
		int counter = 1;
		while( true )
		{
			final String newName = definitionToAdd.getName() + " " + counter;
			if( nameToInstanceMap.get( newName ) == null )
			{
				return newName;
			}
			counter++;
		}
	}

	public void addGraphListener( final MadGraphListener listener )
	{
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Adding graph listener " + listener.getName() + " to graph \"" + instanceName + "\"");
//		}

		this.listeners.add( listener );
		for( final MadGraphInstance<?,?> subGraph : subGraphs )
		{
			subGraph.addGraphListener( listener );
		}
	}

	public void removeGraphListener( final MadGraphListener listener )
	{
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Removing graph listener " + listener.getName() + " from graph \"" + instanceName + "\"");
//		}

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
		linkMap.clear();
		ioLinkMap.clear();
		listeners.clear();
		subGraphs.clear();
	}

	public ArrayList<MadChannelInstance> getGraphChannelsExposedForProducerChannel( final MadChannelInstance auci )
	{
		return ioLinkMap.getGraphChannelsExposedForProducerChannel( auci );
	}

	public void debugLinks()
	{
		linkMap.debug();
		ioLinkMap.debug();
	}

	public boolean isProducerChannelExposedOnGraph( final MadChannelInstance auci )
	{
		return ioLinkMap.isProducerChannelExposed( auci );
	}
}
