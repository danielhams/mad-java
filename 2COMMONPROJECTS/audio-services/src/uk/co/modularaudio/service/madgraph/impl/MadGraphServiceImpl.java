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

package uk.co.modularaudio.service.madgraph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadInstance;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.madgraph.MadSubGraphDefinition;
import uk.co.modularaudio.service.madgraph.impl.helper.FadeInOutLinkHelper;
import uk.co.modularaudio.service.madgraph.impl.mu.MadAppGraphDefinition;
import uk.co.modularaudio.service.madgraph.impl.mu.MadRootGraphDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelPosition;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphListener;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphQueueBridge;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.tuple.TwoTuple;

public class MadGraphServiceImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, MadGraphService
{
	private static Log log = LogFactory.getLog( MadGraphServiceImpl.class.getName() );

	private MadComponentService componentService;

	private final static MadClassificationGroup CODE_GROUP = new MadClassificationGroup( Visibility.CODE, "Code" );
	private final static MadClassificationGroup CUSTOM_GROUP = new MadClassificationGroup( Visibility.PUBLIC, "Customisable Units" );
	private MadRootGraphDefinition<?, ?> rootGraphDefinition;
	private MadAppGraphDefinition<?, ?> appGraphDefinition;
	private MadSubGraphDefinition<?, ?> subGraphDefinition;

	private FadeInOutLinkHelper fadeInOutLinkHelper;

	private final static Map<MadParameterDefinition, String> EMPTY_PARAM_VALUES = new HashMap<MadParameterDefinition, String>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init() throws ComponentConfigurationException
	{
		if (componentService == null)
		{
			final String msg = "GraphServiceImpl is missing service dependencies - check configuration";
			throw new ComponentConfigurationException( msg );
		}
		final MadClassification rootGraphPrivateClassification = new MadClassification( CODE_GROUP, "root_graph",
				"Root Graph", "The root audio graph", ReleaseState.RELEASED );
		rootGraphDefinition = new MadRootGraphDefinition( "root_graph", "Root Graph", rootGraphPrivateClassification,
				new MadGraphQueueBridge() );

		final MadClassification appGraphPrivateClassification = new MadClassification( CODE_GROUP, "app_graph",
				"App Graph", "The applilcation audio graph", ReleaseState.RELEASED );
		appGraphDefinition = new MadAppGraphDefinition( "app_graph", "App Graph", appGraphPrivateClassification,
				new MadGraphQueueBridge() );

		final MadClassification subGraphPublicClassification = new MadClassification( CUSTOM_GROUP, "sub_graph",
				"Sub Graph", "A customisable container graph for building re-usable mad components",
				ReleaseState.RELEASED );
		subGraphDefinition = new MadSubGraphDefinition( "sub_graph", "Sub Graph", subGraphPublicClassification,
				new MadGraphQueueBridge() );

	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		try
		{
			fadeInOutLinkHelper = new FadeInOutLinkHelper( componentService );
		}
		catch (final Exception e)
		{
			final String msg = "Unable to initialise fade in out helper: " + e.toString();
			throw new ComponentConfigurationException( msg, e );
		}
	}

	@Override
	public void preShutdown()
	{
		// No actions
	}

	@Override
	public void destroy()
	{
		// No actions
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadGraphInstance<?, ?> createNewRootGraph( final String name ) throws DatastoreException
	{
		final MadChannelConfiguration emptyChannelConfiguration = MadChannelConfiguration
				.getEmptyChannelConfiguration();
		final MadGraphInstance<?, ?> retVal = new MadGraphInstance( "Root Graph Instance", rootGraphDefinition,
				new HashMap<MadParameterDefinition, String>(), emptyChannelConfiguration );

		return retVal;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadGraphInstance<?, ?> createNewParameterisedGraph( final String name, final GraphType graphType,
			final int numInputAudioChannels, final int numOutputAudioChannels, final int numInputCvChannels,
			final int numOutputCvChannels, final int numInputNoteChannels, final int numOutputNoteChannels )
			throws DatastoreException
	{
		switch (graphType)
		{
			case ROOT_GRAPH:
			{
				final MadChannelConfiguration emptyChannelConfiguration = MadChannelConfiguration.getEmptyChannelConfiguration();
				return new MadGraphInstance( name, rootGraphDefinition, EMPTY_PARAM_VALUES, emptyChannelConfiguration );
			}
			case APP_GRAPH:
			{
				final MadChannelConfiguration userChannelConfiguration = buildUserGraphChannelConfiguration(
						numInputAudioChannels, numOutputAudioChannels, numInputCvChannels, numOutputCvChannels,
						numInputNoteChannels, numOutputNoteChannels );

				return new MadGraphInstance( name, appGraphDefinition, EMPTY_PARAM_VALUES, userChannelConfiguration );
			}
			case SUB_GRAPH:
			{
				final MadChannelConfiguration userChannelConfiguration = buildUserGraphChannelConfiguration(
						numInputAudioChannels, numOutputAudioChannels, numInputCvChannels, numOutputCvChannels,
						numInputNoteChannels, numOutputNoteChannels );
				return new MadGraphInstance( name, subGraphDefinition, EMPTY_PARAM_VALUES, userChannelConfiguration );
			}
			default:
			{
				final String msg = "Unknown graph type: " + graphType.toString();
				throw new DatastoreException( msg );
			}
		}
	}

	@Override
	public void destroyGraph( final MadGraphInstance<?, ?> graphInstanceToDestroy, final boolean deleteLinks,
			final boolean deleteMadInstances ) throws DatastoreException
	{
		// log.debug("Destroying graph named: " +
		// graphInstanceToDestroy.getInstanceName() );
		try
		{
			if (deleteLinks)
			{
				final ArrayList<MadLink> links = new ArrayList<MadLink>( graphInstanceToDestroy.getLinks() );
				for (final MadLink link : links)
				{
					graphInstanceToDestroy.deleteLink( link );
				}
			}
			final ArrayList<MadInstance<?, ?>> gins = new ArrayList<MadInstance<?, ?>>(
					graphInstanceToDestroy.getInstances() );
			for (final MadInstance<?, ?> ins : gins)
			{
				graphInstanceToDestroy.removeInstance( ins );
				if (deleteMadInstances)
				{
					if (ins.isContainer())
					{
						final MadGraphInstance<?, ?> subGraphInstance = (MadGraphInstance<?, ?>) ins;
						this.destroyGraph( subGraphInstance, deleteLinks, deleteMadInstances );
					}
					else
					{
						componentService.destroyInstance( ins );
					}
				}
			}
		}
		catch (final RecordNotFoundException e)
		{
			// Shouldn't happen....
			final String msg = "Caught a record not found cleaning up a graph...";
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public void addGraphListener( final MadGraphInstance<?, ?> g, final MadGraphListener listener )
	{
		internalSingleAddGraphListener( g, listener );
	}

	private void internalSingleAddGraphListener( final MadGraphInstance<?, ?> graph, final MadGraphListener listener )
	{
		graph.addGraphListener( listener );
	}

	@Override
	public void removeGraphListener( final MadGraphInstance<?, ?> graph, final MadGraphListener listener )
	{
		graph.removeGraphListener( listener );
	}

	@Override
	public boolean graphHasListeners( final MadGraphInstance<?, ?> graph )
	{
		return graph.hasListeners();
	}

	@Override
	public boolean checkCanAddInstanceToGraphWithName( final MadGraphInstance<?, ?> graph, final String name )
	{
		return graph.checkCanAddInstanceWithName( name );
	}

	@Override
	public void addInstanceToGraphWithName( final MadGraphInstance<?, ?> graph, final MadInstance<?, ?> instance,
			final String name ) throws DatastoreException, MAConstraintViolationException
	{
		graph.addInstanceWithName( instance, name );
		fireGraphChanged( graph );
	}

	@Override
	public String getNameForNewComponentOfType( final MadGraphInstance<?, ?> graph,
			final MadDefinition<?, ?> definitionToAdd ) throws DatastoreException
	{
		return graph.getNameForNewComponentInstance( definitionToAdd );
	}

	@Override
	public MadInstance<?, ?> findInstanceByName( final MadGraphInstance<?, ?> graph, final String name )
			throws DatastoreException, RecordNotFoundException
	{
		return graph.getInstanceByName( name );
	}

	@Override
	public Collection<MadInstance<?, ?>> findAllInstances( final MadGraphInstance<?, ?> graph )
			throws DatastoreException
	{
		return graph.getInstances();
	}

	@Override
	public boolean renameInstance( final MadGraphInstance<?, ?> graph, final String oldName, final String newName,
			final String newNameInGraph )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final MadInstance<?, ?> aui = graph.getInstanceByName( oldName );
		graph.renameInstanceByName( oldName, newName, newNameInGraph );
		aui.setInstanceName( newName );

		fireGraphChanged( graph );

		return true;
	}

	@Override
	public void removeInstanceFromGraph( final MadGraphInstance<?, ?> graph, final MadInstance<?, ?> instanceToRemove )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// Need to fade out any existing audio links if we are running
		if (graph.hasListeners())
		{
			if (graph.containsInstance( instanceToRemove ))
			{
				try
				{
					final PFadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutDeleteMadInstance( graph,
							instanceToRemove );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removePFadeOutDeleteMadInstance( fadeOutInstance, graph, instanceToRemove );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finalisePFadeOut( fadeOutInstance );
				}
				catch (final MadProcessingException e)
				{
					final String msg = "MadProcessingException caught deleting link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				final String msg = "MadInstance " + instanceToRemove.getInstanceName() + " is not in the graph "
						+ graph.getInstanceName();
				throw new RecordNotFoundException( msg );
			}
		}
		else
		{
			graph.removeInstance( instanceToRemove );
			fireGraphChanged( graph );
		}
	}

	@Override
	public void addLink( final MadGraphInstance<?, ?> graph, final MadLink link )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// Make sure the channels are of the right type
		final MadChannelDefinition consumerDefinition = link.getConsumerChannelInstance().definition;
		final MadChannelType consumerChannelType = consumerDefinition.type;
		final MadChannelDirection consumerDirection = consumerDefinition.direction;
		final MadChannelDefinition producerDefinition = link.getProducerChannelInstance().definition;
		final MadChannelType producerChannelType = producerDefinition.type;
		final MadChannelDirection producerDirection = producerDefinition.direction;
		if (consumerChannelType != producerChannelType)
		{
			final String msg = "Both the consumer and producer channel type must match";
			throw new MAConstraintViolationException( msg );
		}
		else if (consumerDirection == producerDirection)
		{
			final String msg = "A link must be between a consumer and a producer";
			throw new MAConstraintViolationException( msg );
		}
		else
		{
			// Check if the graph has listeners - if it does, we need to do the
			// old fade in trick on the link
			if (graph.hasListeners() && producerChannelType == MadChannelType.AUDIO)
			{
				try
				{
					final FadeInMadInstance fadeInInstance = fadeInOutLinkHelper.fadeInAddLink( graph, link );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removeFadeInAddLink( fadeInInstance, graph, link );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finaliseFadeIn( fadeInInstance );
				}
				catch (final MadProcessingException e)
				{
					final String msg = "MadProcessingException caught adding link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				// Just go ahead and add it
				graph.addLink( link );
				fireGraphChanged( graph );
			}
		}
	}

	@Override
	public void deleteLink( final MadGraphInstance<?, ?> graph, final MadLink link )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final MadChannelType producerChannelType = link.getProducerChannelInstance().definition.type;

		// Check if the graph has listeners - if it does, do the fade out trick.
		if (graph.hasListeners() && producerChannelType == MadChannelType.AUDIO)
		{
			try
			{
				final FadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutDeleteLink( graph, link );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removeFadeOutDeleteLink( fadeOutInstance, graph, link );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finaliseFadeOut( fadeOutInstance );
			}
			catch (final MadProcessingException e)
			{
				final String msg = "MadProcessingException caught deleting link: " + e.toString();
				throw new DatastoreException( msg, e );
			}
		}
		else
		{
			graph.deleteLink( link );
			fireGraphChanged( graph );
		}
	}

	@Override
	public Collection<MadLink> findAllLinks( final MadGraphInstance<?, ?> graph ) throws DatastoreException
	{
		return graph.getLinks();
	}

	@Override
	public Set<MadLink> getProducerInstanceLinks( final MadGraphInstance<?, ?> graph, final MadInstance<?, ?> instance )
			throws DatastoreException
	{
		return graph.getProducerInstanceLinks( instance );
	}

	@Override
	public Set<MadLink> getConsumerInstanceLinks( final MadGraphInstance<?, ?> graph, final MadInstance<?, ?> instance )
			throws DatastoreException
	{
		return graph.getConsumerInstanceLinks( instance );
	}

	@Override
	public void dumpGraph( final MadGraphInstance<?, ?> g )
	{
		if (log.isDebugEnabled())
		{
			log.debug( "Dump of the graph \"" + g.getInstanceName() + "\"" );
			internalDumpGraph( g, "" );
		}
	}

	private void internalDumpGraph( final MadGraphInstance<?, ?> g, final String indentation )
	{
		if (log.isDebugEnabled())
		{
			// Graph channel instances
			final MadChannelInstance[] graphChannelInstances = g.getChannelInstances();
			if (graphChannelInstances.length == 0)
			{
				log.debug( indentation + "This graph has no inputs or outputs" );
			}
			else
			{
				final Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphInputChannelInstanceMap = g
						.getGraphInputChannelInstanceMap();
				final Map<MadChannelInstance, MadChannelInstance> graphOutputChannelInstanceMap = g
						.getGraphOutputChannelInstanceMap();

				for (final MadChannelInstance gci : graphChannelInstances)
				{
					if (gci.definition.direction == MadChannelDirection.PRODUCER)
					{
						final MadChannelInstance mappedTo = graphOutputChannelInstanceMap.get( gci );
						if (mappedTo != null)
						{
							log.debug( indentation + "Graph Channel Instance named \"" + gci.definition.name
									+ "\" - mapped to mad \"" + mappedTo.instance.getInstanceName() + "\" channel \""
									+ mappedTo.definition.name + "\"" );
						}
					}
					else
					{
						final ArrayList<MadChannelInstance> mappedList = graphInputChannelInstanceMap.get( gci );
						if (mappedList != null)
						{
							for (final MadChannelInstance auci : mappedList)
							{
								log.debug( indentation + "Graph Channel Instance named \"" + gci.definition.name
										+ "\" - mapped to mad \"" + auci.instance.getInstanceName() + "\" channel \""
										+ auci.definition.name + "\"" );
							}
						}
					}
				}
			}

			final Collection<MadInstance<?, ?>> ins = g.getInstances();
			// Standard (non-composite) units
			for (final MadInstance<?, ?> aui : ins)
			{
				if (aui instanceof MadGraphInstance)
				{
					// Will print it out after the standard units and links
				}
				else
				{
					final String nameInGraph = g.getInstanceNameInGraph( aui );
					log.debug( indentation + "MadInstance: name in graph \"" + nameInGraph + "\" of type: \""
							+ aui.getDefinition().getName() + "\"" );
				}
			}
			// Links
			final Collection<MadLink> aulis = g.getLinks();
			for (final MadLink aul : aulis)
			{
				log.debug( indentation + "MadLink: " + aul.toStringWithNamesInGraph( g ) );
			}
			// Graph (composite) units
			for (final MadInstance<?, ?> aui : ins)
			{
				if (aui instanceof MadGraphInstance)
				{
					final String nameInGraph = g.getInstanceNameInGraph( aui );
					log.debug( indentation + "GraphInstance: name in graph \"" + nameInGraph + "\" of type: \""
							+ aui.getDefinition().getName() + "\"" );
					log.debug( indentation + "Graph contents:" );
					final MadGraphInstance<?, ?> subGraph = (MadGraphInstance<?, ?>) aui;
					internalDumpGraph( subGraph, indentation + "    " );
				}
			}
		}
	}

	private MadChannelConfiguration buildUserGraphChannelConfiguration( final int numInputAudioChannels,
			final int numOutputAudioChannels, final int numInputCvChannels, final int numOutputCvChannels,
			final int numInputNoteChannels, final int numOutputNoteChannels )
	{
		final List<MadChannelDefinition> channelDefinitionArray = new ArrayList<MadChannelDefinition>();
		// Just audio channels for now
		for (int iac = 1; iac <= numInputAudioChannels; iac++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Input Channel " + iac, MadChannelType.AUDIO,
					MadChannelDirection.CONSUMER, MadChannelPosition.MONO ) );
		}
		for (int oac = 1; oac <= numOutputAudioChannels; oac++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Output Channel " + oac, MadChannelType.AUDIO,
					MadChannelDirection.PRODUCER, MadChannelPosition.MONO ) );
		}
		for (int icc = 1; icc <= numInputCvChannels; icc++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Input CV Channel " + icc, MadChannelType.CV,
					MadChannelDirection.CONSUMER, MadChannelPosition.MONO ) );
		}
		for (int occ = 1; occ <= numOutputCvChannels; occ++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Output CV Channel " + occ, MadChannelType.CV,
					MadChannelDirection.PRODUCER, MadChannelPosition.MONO ) );
		}
		for (int inc = 1; inc <= numInputNoteChannels; inc++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Input Note Channel " + inc, MadChannelType.NOTE,
					MadChannelDirection.CONSUMER, MadChannelPosition.MONO ) );
		}
		for (int onc = 1; onc <= numOutputNoteChannels; onc++)
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Output Note Channel " + onc, MadChannelType.NOTE,
					MadChannelDirection.PRODUCER, MadChannelPosition.MONO ) );
		}
		final MadChannelDefinition[] userChannelDefinitions = channelDefinitionArray
				.toArray( new MadChannelDefinition[channelDefinitionArray.size()] );
		final MadChannelConfiguration retVal = new MadChannelConfiguration( userChannelDefinitions );
		return retVal;
	}

	@Override
	public void exposeAudioInstanceChannelAsGraphChannel( final MadGraphInstance<?, ?> graph,
			final MadChannelInstance graphChannelInstance, final MadChannelInstance channelInstanceToExpose )
			throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{

		// Assert that the channels are of the same type and direction
		final MadChannelDefinition gcd = graphChannelInstance.definition;
		final MadChannelDefinition aucd = channelInstanceToExpose.definition;
		final MadChannelType channelType = aucd.type;
		if (gcd.type != channelType || gcd.direction != aucd.direction)
		{
			final String msg = "To expose a channel as a graph channel both the type and direction must match.";
			throw new MAConstraintViolationException( msg );
		}
		else
		{
			if (graph.hasListeners() && channelType == MadChannelType.AUDIO)
			{
				try
				{
					final FadeInMadInstance fadeInInstance = fadeInOutLinkHelper.fadeInExposeAsGraphChannel( graph,
							graphChannelInstance, channelInstanceToExpose );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removeFadeInExposeAsGraphChannel( fadeInInstance, graph, graphChannelInstance,
							channelInstanceToExpose );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finaliseFadeIn( fadeInInstance );
				}
				catch (final MadProcessingException e)
				{
					final String msg = "MadProcessingException caught adding link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, channelInstanceToExpose );
				fireGraphChanged( graph );
			}
		}
	}

	@Override
	public void removeAudioInstanceChannelAsGraphChannel( final MadGraphInstance<?, ?> graph,
			final MadChannelInstance graphChannelInstance, final MadChannelInstance channelInstanceExposed )
			throws RecordNotFoundException, DatastoreException, MAConstraintViolationException
	{
		final MadChannelType channelType = channelInstanceExposed.definition.type;

		if (graph.hasListeners() && channelType == MadChannelType.AUDIO)
		{
			try
			{
				final FadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutRemoveExposeAsGraphChannel(
						graph, graphChannelInstance, channelInstanceExposed );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removeFadeOutRemoveExposeAsGraphChannel( fadeOutInstance, graph,
						graphChannelInstance, channelInstanceExposed );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finaliseFadeOut( fadeOutInstance );
			}
			catch (final MadProcessingException e)
			{
				final String msg = "MadProcessingException caught adding link: " + e.toString();
				throw new DatastoreException( msg, e );
			}
		}
		else
		{
			graph.removeAudioInstanceChannelAsGraphChannel( graphChannelInstance, channelInstanceExposed );

			fireGraphChanged( graph );
		}
	}

	@Override
	public MadGraphInstance<?, ?> flattenGraph( final MadGraphInstance<?, ?> graph )
			throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		// log.debug("Beginning subgraph flattening on graph "+
		// graph.getInstanceName() );
		final MadGraphInstance<?, ?> retVal = createNewRootGraph( graph.getInstanceName() );
		final Collection<MadInstance<?, ?>> curInstances = graph.getInstances();
		final Set<MadGraphInstance<?, ?>> foundSubGraphs = new HashSet<MadGraphInstance<?, ?>>();
		final Stack<MadGraphInstance<?, ?>> graphStackToRoot = new Stack<MadGraphInstance<?, ?>>();
		graphStackToRoot.push( graph );
		for (final MadInstance<?, ?> aui : curInstances)
		{
			if (aui instanceof MadGraphInstance)
			{
				final MadGraphInstance<?, ?> g = (MadGraphInstance<?, ?>) aui;
				// We'll do it afterwards
				foundSubGraphs.add( g );
			}
			else
			{
				// log.debug("Straight copying mad \"" + aui.getInstanceName()
				// +"\"");
				retVal.addInstanceWithName( aui, aui.getInstanceName() );
			}
		}
		for (final MadGraphInstance<?, ?> subGraph : foundSubGraphs)
		{
			// log.debug("Found a sub graph \"" + subGraph.getInstanceName() +
			// "\"");
			graphStackToRoot.push( subGraph );
			recursiveAddGraphComponentsAssumeSiblingsExist( graphStackToRoot, graph, subGraph, retVal );
			graphStackToRoot.pop();
		}
		// Now the links
		processCurrentGraphLinks( graph, retVal, foundSubGraphs );
		return retVal;
	}

	private void recursiveAddGraphComponentsAssumeSiblingsExist( final Stack<MadGraphInstance<?, ?>> graphStackToRoot,
			final MadGraphInstance<?, ?> parentGraph, final MadGraphInstance<?, ?> currentGraphToProcess,
			final MadGraphInstance<?, ?> graphToInsertIn )
			throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		// Loop over the instances in the subgraph adding them to the parent
		// and rewire the links
		final Collection<MadInstance<?, ?>> currentGraphInstances = currentGraphToProcess.getInstances();
		final Set<MadGraphInstance<?, ?>> foundSubGraphs = new HashSet<MadGraphInstance<?, ?>>();
		for (final MadInstance<?, ?> sbAui : currentGraphInstances)
		{
			if (sbAui instanceof MadGraphInstance)
			{
				final MadGraphInstance<?, ?> g = (MadGraphInstance<?, ?>) sbAui;
				foundSubGraphs.add( g );
			}
			else
			{
				final String uniqueInstanceNameWithPathInGraph = buildPathInGraphInstanceName( graphStackToRoot, sbAui );
				// log.debug("Straight copying mad \"" + sbAui.getInstanceName()
				// + "\" as \"" + uniqueInstanceNameWithPathInGraph + "\"");
				graphToInsertIn.addInstanceWithName( sbAui, uniqueInstanceNameWithPathInGraph );
			}
		}
		// Now the graphs
		for (final MadGraphInstance<?, ?> subSubGraph : foundSubGraphs)
		{
			// log.debug("Found a sub graph \"" + subSubGraph.getInstanceName()
			// + "\"");
			graphStackToRoot.push( subSubGraph );
			recursiveAddGraphComponentsAssumeSiblingsExist( graphStackToRoot, currentGraphToProcess, subSubGraph,
					graphToInsertIn );
			graphStackToRoot.pop();
		}
		// Links
		processCurrentGraphLinks( currentGraphToProcess, graphToInsertIn, foundSubGraphs );
	}

	private String buildPathInGraphInstanceName( final Stack<MadGraphInstance<?, ?>> graphStackToRoot,
			final MadInstance<?, ?> sbAui )
	{
		final StringBuilder retVal = new StringBuilder();
		final int numGraphs = graphStackToRoot.size();
		for (int i = 0; i < numGraphs; i++)
		{
			final MadGraphInstance<?, ?> g = graphStackToRoot.get( i );
			retVal.append( g.getInstanceName() + ":" );
		}
		retVal.append( sbAui.getInstanceName() );
		return retVal.toString();
	}

	private void processCurrentGraphLinks( final MadGraphInstance<?, ?> currentGraphToProcess,
			final MadGraphInstance<?, ?> graphToInsertIn, final Set<MadGraphInstance<?, ?>> foundSubGraphs )
			throws MAConstraintViolationException
	{
		final Collection<MadLink> currentGraphLinks = currentGraphToProcess.getLinks();
		for (final MadLink sgl : currentGraphLinks)
		{
			final MadChannelInstance producerChannelInstance = sgl.getProducerChannelInstance();
			final MadChannelInstance consumerChannelInstance = sgl.getConsumerChannelInstance();
			final MadInstance<?, ?> linkProducerInstance = producerChannelInstance.instance;
			final MadInstance<?, ?> linkConsumerInstance = consumerChannelInstance.instance;
			final boolean producerIsSubGraph = foundSubGraphs.contains( linkProducerInstance );
			final boolean consumerIsSubGraph = foundSubGraphs.contains( linkConsumerInstance );

			if (producerIsSubGraph && consumerIsSubGraph)
			{
				// log.debug("Need replacement link(s) of producer and consumer for "
				// + sgl.toString() );

				final MadGraphInstance<?, ?> producerSubGraph = (MadGraphInstance<?, ?>) linkProducerInstance;
				final MadChannelInstance producerAuci = recursiveFindConnectedProducer( foundSubGraphs,
						producerSubGraph, producerChannelInstance );

				final MadGraphInstance<?, ?> consumerSubGraph = (MadGraphInstance<?, ?>) linkConsumerInstance;
				final ArrayList<MadChannelInstance> consumerAucis = recursiveFindConnectedConsumers( foundSubGraphs,
						consumerSubGraph, consumerChannelInstance );

				if (producerAuci == null || consumerAucis.size() == 0)
				{
//					log.debug( "The producer or the consumer wasn't connected to anything. Won't make a new link." );
					continue;
				}
				else
				{
					for (final MadChannelInstance consumerAuci : consumerAucis)
					{
						final MadLink replacementLink = new MadLink( producerAuci, consumerAuci );
						// log.debug("Replacing it with " +
						// replacementLink.toString() );
						graphToInsertIn.addLink( replacementLink );
					}
				}
			}
			else if (producerIsSubGraph)
			{
				// log.debug("Need a replacement link of producer for " +
				// sgl.toString() );
				final MadChannelInstance subGraphChannelInstance = sgl.getProducerChannelInstance();
				final MadGraphInstance<?, ?> subGraph = (MadGraphInstance<?, ?>) linkProducerInstance;

				final MadChannelInstance foundChannelInstance = recursiveFindConnectedProducer( foundSubGraphs,
						subGraph, subGraphChannelInstance );
				if (foundChannelInstance == null)
				{
					// log.debug("The subgraph channel instance wasn't connected to anything. Won't make a new link");
				}
				else
				{
					final MadLink replacementLink = new MadLink( foundChannelInstance, sgl.getConsumerChannelInstance() );
					// log.debug("Replacing it with " +
					// replacementLink.toString() );
					graphToInsertIn.addLink( replacementLink );
				}
			}
			else if (consumerIsSubGraph)
			{
				// log.debug("Need replacement link(s) of consumer for " +
				// sgl.toString() );
				final MadChannelInstance subGraphChannelInstance = sgl.getConsumerChannelInstance();
				final MadGraphInstance<?, ?> subGraph = (MadGraphInstance<?, ?>) linkConsumerInstance;

				final ArrayList<MadChannelInstance> foundChannelInstances = recursiveFindConnectedConsumers(
						foundSubGraphs, subGraph, subGraphChannelInstance );

				if (foundChannelInstances.size() == 0)
				{
					// log.debug("The subgraph channel instance wasn't connected to anything. Won't make a new link");
				}
				else
				{
					for (final MadChannelInstance auci : foundChannelInstances)
					{
						final MadLink replacementLink = new MadLink( sgl.getProducerChannelInstance(), auci );
						// log.debug("Replacing it with " +
						// replacementLink.toString() );
						graphToInsertIn.addLink( replacementLink );
					}
				}
			}
			else
			{
				// It's a link between actually instances, just re-add it
				// log.debug("Straight copying link " + sgl.toString() );
				final MadLink duplicatedLink = new MadLink( sgl.getProducerChannelInstance(),
						sgl.getConsumerChannelInstance() );
				graphToInsertIn.addLink( duplicatedLink );
			}
		}
	}

	private ArrayList<MadChannelInstance> recursiveFindConnectedConsumers(
			final Set<MadGraphInstance<?, ?>> foundSubGraphs, final MadGraphInstance<?, ?> graph,
			final MadChannelInstance subGraphChannelInstance )
	{
		final Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphInputChannelMap = graph
				.getGraphInputChannelInstanceMap();
		final ArrayList<MadChannelInstance> mappedChannelInstances = graphInputChannelMap.get( subGraphChannelInstance );
		final ArrayList<MadChannelInstance> retVal = new ArrayList<MadChannelInstance>();
		if (mappedChannelInstances == null)
		{
			return retVal;
		}
		else
		{
			for (final MadChannelInstance auci : mappedChannelInstances)
			{
				final MadInstance<?, ?> foundMad = auci.instance;
				if (foundMad instanceof MadGraphInstance)
				{
					final MadGraphInstance<?, ?> graphToSearch = (MadGraphInstance<?, ?>) foundMad;
					retVal.addAll( recursiveFindConnectedConsumers( foundSubGraphs, graphToSearch, auci ) );
				}
				else
				{
					retVal.add( auci );
				}
			}
			return retVal;
		}
	}

	private MadChannelInstance recursiveFindConnectedProducer( final Set<MadGraphInstance<?, ?>> foundSubGraphs,
			final MadGraphInstance<?, ?> graph, final MadChannelInstance subGraphChannelInstance )
	{
		final Map<MadChannelInstance, MadChannelInstance> graphOutputChannelMap = graph
				.getGraphOutputChannelInstanceMap();
		final MadChannelInstance mappedChannelInstance = graphOutputChannelMap.get( subGraphChannelInstance );

		if (mappedChannelInstance == null)
		{
			return null;
		}
		else
		{
			final MadInstance<?, ?> foundMad = mappedChannelInstance.instance;
			if (foundMad instanceof MadGraphInstance)
			{
				final MadGraphInstance<?, ?> graphToSearch = (MadGraphInstance<?, ?>) foundMad;
				return recursiveFindConnectedProducer( foundSubGraphs, graphToSearch, mappedChannelInstance );
			}
			else
			{
				return mappedChannelInstance;
			}
		}
	}

	private void fireGraphChanged( final MadGraphInstance<?, ?> graph )
	{
		graph.fireGraphChangeSignal();
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	@Override
	public void addInstanceToGraphWithNameAndMapChannelsToGraphChannels( final MadGraphInstance<?, ?> graph,
			final MadInstance<?, ?> instanceToMap, final String name, final boolean warnAboutMissingChannels )
			throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		graph.addInstanceWithName( instanceToMap, name );

		if (graph.hasListeners())
		{
			// Fade in time
			try
			{
				final TwoTuple<PFadeInMadInstance, PFadeInMadInstance> prodConsFis =
						fadeInOutLinkHelper.fadeInGraphChannelMap( graph,
						instanceToMap, warnAboutMissingChannels );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removePFadeInGraphChannelMap( prodConsFis, graph, instanceToMap );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finalisePFadeIn( prodConsFis );
			}
			catch (final MadProcessingException e)
			{
				final String msg = "MADProcessingException caught mapping instance channels to graph channels: "
						+ e.toString();
				throw new DatastoreException( msg, e );
			}
		}
		else
		{
			final MadChannelInstance[] channelInstances = graph.getChannelInstances();

			for (int c = 0; c < channelInstances.length; c++)
			{
				final MadChannelInstance graphChannelInstance = channelInstances[c];
				final String graphChannelInstanceName = graphChannelInstance.definition.name;
				// Look for a channel with the same name on the sub rack
				final MadChannelInstance channelInstance = instanceToMap
						.getChannelInstanceByNameReturnNull( graphChannelInstanceName );
				if (channelInstance != null)
				{
					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, channelInstance );
				}
				else if (warnAboutMissingChannels)
				{
					log.warn( "Didn't find a channel to map!" );
				}
			}
			graph.fireGraphChangeSignal();
		}
	}
}
