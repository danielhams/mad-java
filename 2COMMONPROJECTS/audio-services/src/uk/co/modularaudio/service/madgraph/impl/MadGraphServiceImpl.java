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

public class MadGraphServiceImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, MadGraphService
{
	private static Log log = LogFactory.getLog( MadGraphServiceImpl.class.getName() );

	private MadComponentService componentService = null;

	private MadClassificationGroup codeGroup = new MadClassificationGroup( Visibility.CODE, "Code" );
	private MadClassificationGroup customisableUnitsGroup = new MadClassificationGroup( Visibility.PUBLIC, "Customisable Units" );
	private MadRootGraphDefinition<?,?> rootGraphDefinition = null;
	private MadAppGraphDefinition<?,?> appGraphDefinition = null;
	private MadSubGraphDefinition<?,?> subGraphDefinition = null;

	private FadeInOutLinkHelper fadeInOutLinkHelper = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentService == null )
		{
			String msg = "GraphServiceImpl is missing service dependencies - check configuration";
			throw new ComponentConfigurationException( msg );
		}
		MadClassification rootGraphPrivateClassification = new MadClassification( codeGroup,
				"root_graph",
				"Root Graph",
				"The root audio graph",
				ReleaseState.RELEASED );
		rootGraphDefinition = new MadRootGraphDefinition
			( "root_graph", "Root Graph",
				rootGraphPrivateClassification, new MadGraphQueueBridge() );

		MadClassification appGraphPrivateClassification = new MadClassification( codeGroup,
				"app_graph",
				"App Graph",
				"The applilcation audio graph",
				ReleaseState.RELEASED );
		appGraphDefinition = new MadAppGraphDefinition(  "app_graph", "App Graph",
				appGraphPrivateClassification, new MadGraphQueueBridge() );

		MadClassification subGraphPublicClassification = new MadClassification( customisableUnitsGroup,
				"sub_graph",
				"Sub Graph",
				"A customisable container graph for building re-usable mad components",
				ReleaseState.RELEASED );
		subGraphDefinition = new MadSubGraphDefinition(  "sub_graph", "Sub Graph",
				subGraphPublicClassification, new MadGraphQueueBridge() );

	}

	@Override
	public void postInit() throws DatastoreException
	{
		try
		{
			fadeInOutLinkHelper = new FadeInOutLinkHelper( componentService );
		}
		catch( Exception e )
		{
			String msg = "Unable to initialise fade in out helper: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public void preShutdown() throws DatastoreException
	{
	}

	@Override
	public void destroy()
	{
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadGraphInstance<?,?> createNewRootGraph( String name ) throws DatastoreException
	{
		MadGraphInstance<?,?> retVal = null;
		MadChannelConfiguration emptyChannelConfiguration = MadChannelConfiguration.getEmptyChannelConfiguration();
		retVal = new MadGraphInstance( "Root Graph Instance",
				rootGraphDefinition,
				new HashMap<MadParameterDefinition, String>(),
				emptyChannelConfiguration );

		return retVal;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MadGraphInstance<?,?> createNewParameterisedGraph( String name, GraphType graphType,
			int numInputAudioChannels, int numOutputAudioChannels,
			int numInputCvChannels, int numOutputCvChannels,
			int numInputNoteChannels, int numOutputNoteChannels )
			throws DatastoreException
	{
		MadGraphInstance<?,?> retVal = null;
		Map<MadParameterDefinition, String> emptyParamValues = new HashMap<MadParameterDefinition,String>();
		switch( graphType )
		{
			case ROOT_GRAPH:
			{
				MadChannelConfiguration emptyChannelConfiguration = MadChannelConfiguration.getEmptyChannelConfiguration();
				retVal = new MadGraphInstance( name,
						rootGraphDefinition,
						emptyParamValues,
						emptyChannelConfiguration );
				break;
			}
			case APP_GRAPH:
			{
				MadChannelConfiguration userChannelConfiguration = buildUserGraphChannelConfiguration( numInputAudioChannels,
						numOutputAudioChannels,
						numInputCvChannels,
						numOutputCvChannels,
						numInputNoteChannels,
						numOutputNoteChannels );

				retVal = new MadGraphInstance( name,
						appGraphDefinition,
						emptyParamValues,
						userChannelConfiguration );
				break;
			}
			case SUB_GRAPH:
			{
				MadChannelConfiguration userChannelConfiguration = buildUserGraphChannelConfiguration( numInputAudioChannels,
						numOutputAudioChannels,
						numInputCvChannels,
						numOutputCvChannels,
						numInputNoteChannels,
						numOutputNoteChannels );
				retVal = new MadGraphInstance( name,
						subGraphDefinition,
						emptyParamValues,
						userChannelConfiguration );
				break;
			}
			default:
				String msg = "Unknown graph type: " + graphType.toString();
				throw new DatastoreException( msg );
		}

		return retVal;
	}

	@Override
	public void destroyGraph( MadGraphInstance<?,?> graphInstanceToDestroy,
			boolean deleteLinks,
			boolean deleteMadInstances )
		throws DatastoreException
	{
//		log.debug("Destroying graph named: " + graphInstanceToDestroy.getInstanceName() );
		try
		{
			if( deleteLinks )
			{
				ArrayList<MadLink> links = new ArrayList<MadLink>( graphInstanceToDestroy.getLinks() );
				for( MadLink link : links )
				{
					graphInstanceToDestroy.deleteLink( link );
				}
			}
			ArrayList<MadInstance<?,?>> gins = new ArrayList<MadInstance<?,?>>( graphInstanceToDestroy.getInstances() );
			for( MadInstance<?,?> ins : gins )
			{
				graphInstanceToDestroy.removeInstance( ins );
				if( deleteMadInstances )
				{
					if( ins.isContainer() )
					{
						MadGraphInstance<?, ?>  subGraphInstance = (MadGraphInstance<?,?>)ins;
						this.destroyGraph( subGraphInstance, deleteLinks, deleteMadInstances );
					}
					else
					{
						componentService.destroyInstance(  ins );
					}
				}
			}
		}
		catch (RecordNotFoundException e)
		{
			// Shouldn't happen....
			String msg = "Caught a record not found cleaning up a graph...";
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public void addGraphListener( MadGraphInstance<?,?> g, MadGraphListener listener )
	{
		internalSingleAddGraphListener( g, listener );
	}

	private void internalSingleAddGraphListener( MadGraphInstance<?,?> graph, MadGraphListener listener )
	{
		graph.addGraphListener( listener );
	}

	@Override
	public void removeGraphListener( MadGraphInstance<?,?> graph, MadGraphListener listener )
	{
		graph.removeGraphListener( listener );
	}

	@Override
	public boolean checkCanAddInstanceToGraphWithName( MadGraphInstance<?,?> graph,
			String name )
	{
		return graph.checkCanAddInstanceWithName( name );
	}

	@Override
	public void addInstanceToGraphWithName( MadGraphInstance<?,?> graph, MadInstance<?,?> instance,
			String name )
			throws DatastoreException, MAConstraintViolationException
	{
		graph.addInstanceWithName( instance, name );
		fireGraphChanged( graph );
	}

	@Override
	public String getNameForNewComponentOfType( MadGraphInstance<?,?> graph,
			MadDefinition<?,?> definitionToAdd ) throws DatastoreException
	{
		return graph.getNameForNewComponentInstance( definitionToAdd );
	}

	@Override
	public MadInstance<?,?> findInstanceByName( MadGraphInstance<?,?> graph,
			String name ) throws DatastoreException, RecordNotFoundException
	{
		return graph.getInstanceByName( name );
	}

	@Override
	public Collection<MadInstance<?,?>> findAllInstances( MadGraphInstance<?,?> graph )
			throws DatastoreException
	{
		return graph.getInstances();
	}

	@Override
	public boolean renameInstance( MadGraphInstance<?,?> graph, String oldName,
			String newName,
			String newNameInGraph )
			throws DatastoreException, RecordNotFoundException,
			MAConstraintViolationException
	{
		MadInstance<?,?> aui = graph.getInstanceByName( oldName );
		graph.renameInstanceByName( oldName,  newName, newNameInGraph );
		aui.setInstanceName( newName );

		fireGraphChanged( graph );

		return true;
	}

	@Override
	public void removeInstanceFromGraph( MadGraphInstance<?,?> graph, MadInstance<?,?> instanceToRemove )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// Need to fade out any existing audio links if we are running
		if( graph.hasListeners() )
		{
			if( graph.containsInstance( instanceToRemove ) )
			{
				try
				{
					PFadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutDeleteMadInstance( graph, instanceToRemove );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removePFadeOutDeleteMadInstance( fadeOutInstance, graph, instanceToRemove );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finalisePFadeOut( fadeOutInstance );
				}
				catch (MadProcessingException e)
				{
					String msg = "MadProcessingException caught deleting link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				String msg = "MadInstance " + instanceToRemove.getInstanceName() + " is not in the graph " + graph.getInstanceName();
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
	public void addLink( MadGraphInstance<?,?> graph, MadLink link )
			throws DatastoreException, RecordNotFoundException,
			MAConstraintViolationException
	{
		// Make sure the channels are of the right type
		MadChannelDefinition consumerDefinition = link.getConsumerChannelInstance().definition;
		MadChannelType consumerChannelType = consumerDefinition.type;
		MadChannelDirection consumerDirection = consumerDefinition.direction;
		MadChannelDefinition producerDefinition = link.getProducerChannelInstance().definition;
		MadChannelType producerChannelType = producerDefinition.type;
		MadChannelDirection producerDirection = producerDefinition.direction;
		if( consumerChannelType != producerChannelType )
		{
			String msg = "Both the consumer and producer channel type must match";
			throw new MAConstraintViolationException( msg );
		}
		else if( consumerDirection == producerDirection )
		{
			String msg = "A link must be between a consumer and a producer";
			throw new MAConstraintViolationException( msg );
		}
		else
		{
			// Check if the graph has listeners - if it does, we need to do the old fade in trick on the link
			if( graph.hasListeners() && producerChannelType == MadChannelType.AUDIO )
			{
				try
				{
					FadeInMadInstance fadeInInstance = fadeInOutLinkHelper.fadeInAddLink( graph, link );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removeFadeInAddLink( fadeInInstance, graph, link );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finaliseFadeIn( fadeInInstance );
				}
				catch (MadProcessingException e)
				{
					String msg = "MadProcessingException caught adding link: " + e.toString();
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
	public void deleteLink( MadGraphInstance<?,?> graph, MadLink link )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		MadChannelType producerChannelType = link.getProducerChannelInstance().definition.type;

		// Check if the graph has listeners - if it does, do the fade out trick.
		if( graph.hasListeners() && producerChannelType == MadChannelType.AUDIO )
		{
			try
			{
				FadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutDeleteLink( graph, link );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removeFadeOutDeleteLink( fadeOutInstance, graph, link );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finaliseFadeOut( fadeOutInstance );
			}
			catch (MadProcessingException e)
			{
				String msg = "MadProcessingException caught deleting link: " + e.toString();
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
	public Collection<MadLink> findAllLinks( MadGraphInstance<?,?> graph ) throws DatastoreException
	{
		return graph.getLinks();
	}

	@Override
	public Set<MadLink> findAllLinksToInstance( MadGraphInstance<?,?> graph, MadInstance<?,?> instance ) throws DatastoreException
	{
		return graph.findAllLinksToInstance( instance );
	}

	@Override
	public Set<MadLink> findAllLinksFromInstance( MadGraphInstance<?,?> graph, MadInstance<?,?> instance ) throws DatastoreException
	{
		return graph.findAllLinksFromInstance( instance );
	}

	@Override
	public void dumpGraph( MadGraphInstance<?,?> g )
	{
		log.debug("Dump of the graph \"" + g.getInstanceName() + "\"");
		internalDumpGraph( g, "" );
	}

	private void internalDumpGraph( MadGraphInstance<?,?> g, String indentation )
	{
		// Graph channel instances
		MadChannelInstance[] graphChannelInstances = g.getChannelInstances();
		if( graphChannelInstances.length == 0 )
		{
			log.debug( indentation + "This graph has no inputs or outputs" );
		}
		else
		{
			Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphInputChannelInstanceMap = g.getGraphInputChannelInstanceMap();
			Map<MadChannelInstance, MadChannelInstance> graphOutputChannelInstanceMap = g.getGraphOutputChannelInstanceMap();

			for( MadChannelInstance gci : graphChannelInstances )
			{
				if( gci.definition.direction == MadChannelDirection.PRODUCER )
				{
					MadChannelInstance mappedTo = graphOutputChannelInstanceMap.get( gci );
					if( mappedTo != null )
					{
						log.debug( indentation + "Graph Channel Instance named \"" + gci.definition.name + "\" - mapped to mad \"" +
								mappedTo.instance.getInstanceName() + "\" channel \"" + mappedTo.definition.name + "\"");
					}
				}
				else
				{
					ArrayList<MadChannelInstance> mappedList = graphInputChannelInstanceMap.get( gci );
					if( mappedList != null )
					{
						for( MadChannelInstance auci : mappedList )
						{
							log.debug( indentation + "Graph Channel Instance named \"" + gci.definition.name + "\" - mapped to mad \"" +
									auci.instance.getInstanceName() + "\" channel \"" + auci.definition.name + "\"");
						}
					}
				}
			}
		}

		Collection<MadInstance<?,?>> ins = g.getInstances();
		// Standard (non-composite) units
		for( MadInstance<?,?> aui : ins )
		{
			if( aui instanceof MadGraphInstance )
			{
				// Will print it out after the standard units and links
			}
			else
			{
				String nameInGraph = g.getInstanceNameInGraph( aui );
				log.debug( indentation + "MadInstance: name in graph \"" + nameInGraph + "\" of type: \"" + aui.getDefinition().getName() +"\"");
			}
		}
		// Links
		Collection<MadLink> aulis = g.getLinks();
		for( MadLink aul : aulis )
		{
			log.debug( indentation + "MadLink: " + aul.toStringWithNamesInGraph( g ) );
		}
		// Graph (composite) units
		for( MadInstance<?,?> aui : ins )
		{
			if( aui instanceof MadGraphInstance )
			{
				String nameInGraph = g.getInstanceNameInGraph( aui );
				log.debug( indentation + "GraphInstance: name in graph \"" + nameInGraph + "\" of type: \"" + aui.getDefinition().getName() +"\"");
				log.debug( indentation + "Graph contents:");
				MadGraphInstance<?,?> subGraph = (MadGraphInstance<?,?>)aui;
				internalDumpGraph( subGraph, indentation + "    " );
			}
		}
	}

	private MadChannelConfiguration buildUserGraphChannelConfiguration(
			int numInputAudioChannels, int numOutputAudioChannels,
			int numInputCvChannels, int numOutputCvChannels,
			int numInputNoteChannels, int numOutputNoteChannels )
	{
		List<MadChannelDefinition> channelDefinitionArray = new ArrayList<MadChannelDefinition>();
		// Just audio channels for now
		for( int iac = 1 ; iac <= numInputAudioChannels ; iac++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Input Channel " + iac,
					MadChannelType.AUDIO,
					MadChannelDirection.CONSUMER,
					MadChannelPosition.MONO ) );
		}
		for( int oac = 1 ; oac <= numOutputAudioChannels ; oac++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition( "Output Channel " + oac,
					MadChannelType.AUDIO,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO ) );
		}
		for( int icc = 1 ; icc <= numInputCvChannels ; icc++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition("Input CV Channel " + icc,
					MadChannelType.CV,
					MadChannelDirection.CONSUMER,
					MadChannelPosition.MONO ) );
		}
		for( int occ = 1 ; occ <= numOutputCvChannels ; occ++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition("Output CV Channel " + occ,
					MadChannelType.CV,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO ) );
		}
		for( int inc = 1 ; inc <= numInputNoteChannels ; inc++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition("Input Note Channel " + inc,
					MadChannelType.NOTE,
					MadChannelDirection.CONSUMER,
					MadChannelPosition.MONO ) );
		}
		for( int onc = 1 ; onc <= numOutputNoteChannels ; onc++ )
		{
			channelDefinitionArray.add( new MadChannelDefinition("Output Note Channel " + onc,
					MadChannelType.NOTE,
					MadChannelDirection.PRODUCER,
					MadChannelPosition.MONO ) );
		}
		MadChannelDefinition[] userChannelDefinitions = channelDefinitionArray.toArray( new MadChannelDefinition[ channelDefinitionArray.size()] );
		MadChannelConfiguration retVal = new MadChannelConfiguration( userChannelDefinitions );
		return retVal;
	}

	@Override
	public void exposeAudioInstanceChannelAsGraphChannel( MadGraphInstance<?,?> graph,
			MadChannelInstance graphChannelInstance,
			MadChannelInstance channelInstanceToExpose )
			throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{

		// Assert that the channels are of the same type and direction
		MadChannelDefinition gcd = graphChannelInstance.definition;
		MadChannelDefinition aucd = channelInstanceToExpose.definition;
		MadChannelType channelType = aucd.type;
		if( gcd.type != channelType ||
				gcd.direction != aucd.direction )
		{
			String msg = "To expose a channel as a graph channel both the type and direction must match.";
			throw new MAConstraintViolationException( msg );
		}
		else
		{
			if( graph.hasListeners() && channelType == MadChannelType.AUDIO )
			{
				try
				{
					FadeInMadInstance fadeInInstance = fadeInOutLinkHelper.fadeInExposeAsGraphChannel( graph, graphChannelInstance, channelInstanceToExpose );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.removeFadeInExposeAsGraphChannel( fadeInInstance, graph, graphChannelInstance, channelInstanceToExpose );
					fireGraphChanged( graph );
					fadeInOutLinkHelper.finaliseFadeIn( fadeInInstance );
				}
				catch (MadProcessingException e)
				{
					String msg = "MadProcessingException caught adding link: " + e.toString();
					throw new DatastoreException( msg, e );
				}
			}
			else
			{
				graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance,
						channelInstanceToExpose );
				fireGraphChanged( graph );
			}
		}
	}

	@Override
	public void removeAudioInstanceChannelAsGraphChannel( MadGraphInstance<?,?> graph,
			MadChannelInstance graphChannelInstance,
			MadChannelInstance channelInstanceExposed )
			throws RecordNotFoundException, DatastoreException, MAConstraintViolationException
	{
		MadChannelType channelType = channelInstanceExposed.definition.type;

		if( graph.hasListeners() && channelType == MadChannelType.AUDIO )
		{
			try
			{
				FadeOutMadInstance fadeOutInstance = fadeInOutLinkHelper.fadeOutRemoveExposeAsGraphChannel( graph, graphChannelInstance, channelInstanceExposed );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removeFadeOutRemoveExposeAsGraphChannel( fadeOutInstance, graph, graphChannelInstance, channelInstanceExposed );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finaliseFadeOut( fadeOutInstance );
			}
			catch (MadProcessingException e)
			{
				String msg = "MadProcessingException caught adding link: " + e.toString();
				throw new DatastoreException( msg, e );
			}
		}
		else
		{
			graph.removeAudioInstanceChannelAsGraphChannel( graphChannelInstance,
					channelInstanceExposed );

			fireGraphChanged( graph );
		}
	}

	@Override
	public MadGraphInstance<?,?> flattenGraph( MadGraphInstance<?,?> graph ) throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
//		log.debug("Beginning subgraph flattening on graph "+ graph.getInstanceName() );
		MadGraphInstance<?,?> retVal = createNewRootGraph( graph.getInstanceName() );
		Collection<MadInstance<?,?>> curInstances = graph.getInstances();
		Set<MadGraphInstance<?,?>> foundSubGraphs = new HashSet<MadGraphInstance<?,?>>();
		Stack<MadGraphInstance<?,?>> graphStackToRoot = new Stack<MadGraphInstance<?,?>>();
		graphStackToRoot.push( graph );
		for( MadInstance<?,?> aui : curInstances )
		{
			if( aui instanceof MadGraphInstance )
			{
				MadGraphInstance<?,?> g = (MadGraphInstance<?,?>)aui;
				// We'll do it afterwards
				foundSubGraphs.add( g );
			}
			else
			{
//				log.debug("Straight copying mad \"" + aui.getInstanceName() +"\"");
				retVal.addInstanceWithName( aui, aui.getInstanceName() );
			}
		}
		for( MadGraphInstance<?,?> subGraph : foundSubGraphs )
		{
//			log.debug("Found a sub graph \"" + subGraph.getInstanceName() + "\"");
			graphStackToRoot.push( subGraph );
			recursiveAddGraphComponentsAssumeSiblingsExist( graphStackToRoot, graph, subGraph, retVal );
			graphStackToRoot.pop();
		}
		// Now the links
		processCurrentGraphLinks( graph, retVal, foundSubGraphs );
		return retVal;
	}

	private void recursiveAddGraphComponentsAssumeSiblingsExist( Stack<MadGraphInstance<?,?>> graphStackToRoot,
			MadGraphInstance<?,?> parentGraph,
			MadGraphInstance<?,?> currentGraphToProcess,
			MadGraphInstance<?,?> graphToInsertIn )
		throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		// Loop over the instances in the subgraph adding them to the parent
		// and rewire the links
		Collection<MadInstance<?,?>> currentGraphInstances = currentGraphToProcess.getInstances();
		Set<MadGraphInstance<?,?>> foundSubGraphs = new HashSet<MadGraphInstance<?,?>>();
		for( MadInstance<?,?> sbAui : currentGraphInstances )
		{
			if( sbAui instanceof MadGraphInstance )
			{
				MadGraphInstance<?,?> g = (MadGraphInstance<?,?>)sbAui;
				foundSubGraphs.add( g );
			}
			else
			{
				String uniqueInstanceNameWithPathInGraph = buildPathInGraphInstanceName( graphStackToRoot, sbAui );
//				log.debug("Straight copying mad \"" + sbAui.getInstanceName() + "\" as \"" + uniqueInstanceNameWithPathInGraph + "\"");
				graphToInsertIn.addInstanceWithName( sbAui, uniqueInstanceNameWithPathInGraph );
			}
		}
		// Now the graphs
		for( MadGraphInstance<?,?> subSubGraph : foundSubGraphs )
		{
//			log.debug("Found a sub graph \"" + subSubGraph.getInstanceName() + "\"");
			graphStackToRoot.push( subSubGraph );
			recursiveAddGraphComponentsAssumeSiblingsExist( graphStackToRoot, currentGraphToProcess,  subSubGraph, graphToInsertIn );
			graphStackToRoot.pop();
		}
		// Links
		processCurrentGraphLinks( currentGraphToProcess, graphToInsertIn,
				foundSubGraphs );
	}

	private String buildPathInGraphInstanceName( Stack<MadGraphInstance<?,?>> graphStackToRoot, MadInstance<?,?> sbAui )
	{
		StringBuilder retVal = new StringBuilder();
		int numGraphs = graphStackToRoot.size();
		for( int i = 0 ; i < numGraphs ; i++ )
		{
			MadGraphInstance<?,?> g = graphStackToRoot.get( i );
			retVal.append( g.getInstanceName() + ":" );
		}
		retVal.append( sbAui.getInstanceName() );
		return retVal.toString();
	}

	private void processCurrentGraphLinks( MadGraphInstance<?,?> currentGraphToProcess,
			MadGraphInstance<?,?> graphToInsertIn,
			Set<MadGraphInstance<?,?>> foundSubGraphs )
			throws MAConstraintViolationException
	{
		Collection<MadLink> currentGraphLinks = currentGraphToProcess.getLinks();
		for( MadLink sgl : currentGraphLinks )
		{
			MadChannelInstance producerChannelInstance = sgl.getProducerChannelInstance();
			MadChannelInstance consumerChannelInstance = sgl.getConsumerChannelInstance();
			MadInstance<?,?> linkProducerInstance = producerChannelInstance.instance;
			MadInstance<?,?> linkConsumerInstance = consumerChannelInstance.instance;
			boolean producerIsSubGraph = foundSubGraphs.contains( linkProducerInstance );
			boolean consumerIsSubGraph = foundSubGraphs.contains( linkConsumerInstance );

			if( producerIsSubGraph && consumerIsSubGraph )
			{
//				log.debug("Need replacement link(s) of producer and consumer for " + sgl.toString() );

				MadGraphInstance<?,?> producerSubGraph = (MadGraphInstance<?,?>)linkProducerInstance;
				MadChannelInstance producerAuci = recursiveFindConnectedProducer( foundSubGraphs, producerSubGraph, producerChannelInstance );

				MadGraphInstance<?,?> consumerSubGraph = (MadGraphInstance<?,?>)linkConsumerInstance;
				ArrayList<MadChannelInstance> consumerAucis = recursiveFindConnectedConsumers( foundSubGraphs, consumerSubGraph, consumerChannelInstance );

				if( producerAuci == null || consumerAucis.size() == 0 )
				{
					log.debug("The producer or the consumer wasn't connected to anything. Won't make a new link.");
					continue;
				}
				else
				{
					for( MadChannelInstance consumerAuci : consumerAucis )
					{
						MadLink replacementLink = new MadLink( producerAuci, consumerAuci );
//						log.debug("Replacing it with " + replacementLink.toString() );
						graphToInsertIn.addLink( replacementLink );
					}
				}
			}
			else if( producerIsSubGraph )
			{
//				log.debug("Need a replacement link of producer for " + sgl.toString() );
				MadChannelInstance subGraphChannelInstance = sgl.getProducerChannelInstance();
				MadGraphInstance<?,?> subGraph = (MadGraphInstance<?,?>)linkProducerInstance;

				MadChannelInstance foundChannelInstance = recursiveFindConnectedProducer( foundSubGraphs,
						subGraph,
						subGraphChannelInstance );
				if( foundChannelInstance == null )
				{
//					log.debug("The subgraph channel instance wasn't connected to anything. Won't make a new link");
				}
				else
				{
					MadLink replacementLink = new MadLink( foundChannelInstance,
							sgl.getConsumerChannelInstance() );
//					log.debug("Replacing it with " + replacementLink.toString() );
					graphToInsertIn.addLink( replacementLink );
				}
			}
			else if( consumerIsSubGraph )
			{
//				log.debug("Need replacement link(s) of consumer for " + sgl.toString() );
				MadChannelInstance subGraphChannelInstance = sgl.getConsumerChannelInstance();
				MadGraphInstance<?,?> subGraph = (MadGraphInstance<?,?>)linkConsumerInstance;

				ArrayList<MadChannelInstance> foundChannelInstances = recursiveFindConnectedConsumers( foundSubGraphs,
						subGraph,
						subGraphChannelInstance );

				if( foundChannelInstances.size() == 0 )
				{
//					log.debug("The subgraph channel instance wasn't connected to anything. Won't make a new link");
				}
				else
				{
					for( MadChannelInstance auci : foundChannelInstances )
					{
						MadLink replacementLink = new MadLink( sgl.getProducerChannelInstance(),
								auci );
//						log.debug("Replacing it with " + replacementLink.toString() );
						graphToInsertIn.addLink( replacementLink );
					}
				}
			}
			else
			{
				// It's a link between actually instances, just re-add it
//				log.debug("Straight copying link " + sgl.toString() );
				MadLink duplicatedLink = new MadLink( sgl.getProducerChannelInstance(), sgl.getConsumerChannelInstance() );
				graphToInsertIn.addLink( duplicatedLink );
			}
		}
	}

	private ArrayList<MadChannelInstance> recursiveFindConnectedConsumers(
			Set<MadGraphInstance<?, ?>> foundSubGraphs,
			MadGraphInstance<?, ?> graph,
			MadChannelInstance subGraphChannelInstance)
	{
		Map<MadChannelInstance, ArrayList<MadChannelInstance>> graphInputChannelMap = graph.getGraphInputChannelInstanceMap();
		ArrayList<MadChannelInstance> mappedChannelInstances = graphInputChannelMap.get( subGraphChannelInstance );
		ArrayList<MadChannelInstance> retVal = new ArrayList<MadChannelInstance>();
		if( mappedChannelInstances == null )
		{
			return retVal;
		}
		else
		{
			for( MadChannelInstance auci : mappedChannelInstances )
			{
				MadInstance<?,?> foundMad  = auci.instance;
				if( foundMad instanceof MadGraphInstance )
				{
					MadGraphInstance<?,?> graphToSearch = (MadGraphInstance<?,?>)foundMad;
					retVal.addAll( recursiveFindConnectedConsumers(foundSubGraphs, graphToSearch, auci ) );
				}
				else
				{
					retVal.add( auci );
				}
			}
			return retVal;
		}
	}

	private MadChannelInstance recursiveFindConnectedProducer(
			Set<MadGraphInstance<?,?>> foundSubGraphs,
			MadGraphInstance<?,?> graph,
			MadChannelInstance subGraphChannelInstance )
	{
		Map<MadChannelInstance, MadChannelInstance> graphOutputChannelMap = graph.getGraphOutputChannelInstanceMap();
		MadChannelInstance mappedChannelInstance = graphOutputChannelMap.get( subGraphChannelInstance );

		if( mappedChannelInstance == null )
		{
			return null;
		}
		else
		{
			MadInstance<?,?> foundMad = mappedChannelInstance.instance;
			if( foundMad instanceof MadGraphInstance )
			{
				MadGraphInstance<?,?> graphToSearch = (MadGraphInstance<?,?>)foundMad;
				return recursiveFindConnectedProducer(foundSubGraphs, graphToSearch, mappedChannelInstance);
			}
			else
			{
				return mappedChannelInstance;
			}
		}
	}

	private void fireGraphChanged( MadGraphInstance<?,?> graph )
	{
		graph.fireGraphChangeSignal();
	}

	public MadComponentService getComponentService()
	{
		return componentService;
	}

	public void setComponentService( MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	@Override
	public void addInstanceToGraphWithNameAndMapChannelsToGraphChannels( MadGraphInstance<?,?> graph,
			MadInstance<?,?> instanceToMap,
			String name,
			boolean warnAboutMissingChannels )
			throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		graph.addInstanceWithName(instanceToMap, name);

		if( graph.hasListeners() )
		{
			// Fade in time
			try
			{
				PFadeInMadInstance pFadeInInstance = fadeInOutLinkHelper.fadeInGraphChannelMap(graph, instanceToMap, warnAboutMissingChannels );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.removePFadeInGraphChannelMap( pFadeInInstance, graph, instanceToMap );
				fireGraphChanged( graph );
				fadeInOutLinkHelper.finalisePFadeIn( pFadeInInstance );
			}
			catch (MadProcessingException e)
			{
				String msg = "MADProcessingException caught mapping instance channels to graph channels: " + e.toString();
				throw new DatastoreException( msg, e );
			}
		}
		else
		{
			MadChannelInstance[] channelInstances = graph.getChannelInstances();

			for( int c = 0 ; c < channelInstances.length ; c++ )
			{
				MadChannelInstance graphChannelInstance = channelInstances[ c ];
				String graphChannelInstanceName = graphChannelInstance.definition.name;
				// Look for a channel with the same name on the sub rack
				MadChannelInstance channelInstance = instanceToMap.getChannelInstanceByNameReturnNull( graphChannelInstanceName );
				if( channelInstance != null )
				{
					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance,
							channelInstance );
				}
				else if( warnAboutMissingChannels )
				{
					log.warn( "Didn't find a channel to map!" );
				}
			}
			graph.fireGraphChangeSignal();
		}
	}
}
