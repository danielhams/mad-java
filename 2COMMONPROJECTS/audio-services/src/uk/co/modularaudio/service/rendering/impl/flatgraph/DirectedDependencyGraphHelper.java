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

package uk.co.modularaudio.service.rendering.impl.flatgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class DirectedDependencyGraphHelper
{
//	private static Log log = LogFactory.getLog( FlatGraphHelper.class.getName() );
	
	public static DirectedDependencyGraph buildDirectedDependencyGraph( MadGraphService graphService, MadGraphInstance<?,?> graph )
		throws DatastoreException, RecordNotFoundException
	{
		DirectedDependencyGraph retVal = new DirectedDependencyGraph();
		
		Collection<MadInstance<?,?>> madInstances = graph.getInstances();
		
		// Working sets for the recursive marking
		List<MadInstance<?,?>> componentsToBeProcessed = new ArrayList<MadInstance<?,?>>( madInstances );
		List<MadInstance<?,?>> componentsMarkedProcessing = new ArrayList<MadInstance<?,?>>();
		List<MadInstance<?,?>> componentsDone = new ArrayList<MadInstance<?,?>>();
		
		for( MadInstance<?,?> drivingComponent : madInstances )
		{
			if( !componentsDone.contains( drivingComponent ) )
			{
				recursiveBuildDirectedDependencyGraph( graphService,
						graph,
						retVal,
						drivingComponent,
						componentsToBeProcessed,
						componentsMarkedProcessing,
						componentsDone );
			}
			else
			{
//				log.debug("Component " + drivingComponent.getName() + " already marked as done.");
			}
		}
		
		// Now fill in all the consumerComponentsWaitingForUs
		List<FlattenedRenderJob> flatJobs = retVal.jobs;
		for( FlattenedRenderJob flatJob : flatJobs )
		{
			Set<FlattenedRenderJob> jobsThisInstanceWaitsFor = flatJob.getProducerJobsWeWaitFor();
			for( FlattenedRenderJob jobProducingForUs : jobsThisInstanceWaitsFor )
			{
				jobProducingForUs.addConsumerJobWaitingForUs( flatJob );
			}
		}

		return retVal;
	}

	private static void recursiveBuildDirectedDependencyGraph( MadGraphService graphService,
			MadGraphInstance<?,?> graph,
			DirectedDependencyGraph flattenedGraph,
			MadInstance<?,?> drivingMadInstance,
			List<MadInstance<?,?>> componentsToBeProcessed,
			List<MadInstance<?,?>> componentsMarkedProcessing,
			List<MadInstance<?,?>> componentsDone)
		throws DatastoreException, RecordNotFoundException
	{
		// Check if this component needs to be processed - if it does check if it has things connected to it's sinks
		if( componentsMarkedProcessing.contains( drivingMadInstance ) )
		{
			String msg = "While recursively walking the tree the component " + drivingMadInstance.getInstanceName() + " is marked as processing.";
			throw new DatastoreException( msg );
		}
		else if( componentsDone.contains( drivingMadInstance ) )
		{
//			String msg = "Skipping flatten of " + drivingComponent.getName() + " - already marked as done.";
//			log.info( msg );
		}
		else
		{
//			log.info("Flatten called on " + drivingComponent.getName() );
			componentsToBeProcessed.remove( drivingMadInstance );
			componentsMarkedProcessing.add( drivingMadInstance );
			Set<FlattenedRenderJob> producerComponentsWeWaitFor = new HashSet<FlattenedRenderJob>();
			
			// Check for the components connected to the sinks of this instance
			Set<MadLink> linksTo = graphService.findAllLinksToInstance( graph,  drivingMadInstance );
			for( MadLink link : linksTo )
			{
				// Recurse on the source component
				MadInstance<?,?> producerMadInstance = link.getProducerChannelInstance().instance;

				recursiveBuildDirectedDependencyGraph( graphService,
						graph,
						flattenedGraph,
						producerMadInstance,
						componentsToBeProcessed,
						componentsMarkedProcessing,
						componentsDone);

				producerComponentsWeWaitFor.add( flattenedGraph.findJobByMadInstance( producerMadInstance ) );
				
			}
			
			if (drivingMadInstance instanceof MadGraphInstance )
			{
				String msg = "Sub-graphs are not implemented in the rendering service - you must provide a single graph!";
				throw new DatastoreException( msg  );
//				Graph innerGraph = (Graph)drivingMadInstance;

//				recursiveFlattenInnerGraph( graphService, graph, flattenedGraph, innerGraph, componentsToBeProcessed, componentsMarkedProcessing, componentsDone)
			}
			else
			{
				// Now add this component instance into the flat graph to be processed
				FlattenedRenderJob flattenedRenderJob = new FlattenedRenderJob( drivingMadInstance, producerComponentsWeWaitFor );
				flattenedGraph.addFlattenedRenderJob( flattenedRenderJob );
			}
			
			componentsMarkedProcessing.remove( drivingMadInstance );
			componentsDone.add( drivingMadInstance );
		}
	}

	public static void annotateDependencyGraph( DirectedDependencyGraph flatGraph )
	{
		List<FlattenedRenderJob> jobs = flatGraph.jobs;
		for( FlattenedRenderJob job : jobs )
		{
			recursiveAnnotedFlatJob( job );
		}
	}

	private static void recursiveAnnotedFlatJob(FlattenedRenderJob job)
	{
		// Check to see if all sink connected jobs have a cardinality - 
		// if they do we can fill ours in as max(sink_connected_cardinality) + 1
		// if they don't we recurse
		if( job.getCardinality() != FlattenedRenderJob.CARDINALITY_NOT_SET )
		{
			// Already processed
			return;
		}
		else
		{
			int maxCardinality = 0;
			Set<FlattenedRenderJob> producerJobsWeWaitFor = job.getProducerJobsWeWaitFor();
			for( FlattenedRenderJob producerJob : producerJobsWeWaitFor )
			{
				recursiveAnnotedFlatJob( producerJob );
				int dc = producerJob.getCardinality();
				if( dc > maxCardinality )
				{
					maxCardinality = dc;
				}
			}
			
			job.setCardinality( maxCardinality + 1 );
		}		
	}

}
