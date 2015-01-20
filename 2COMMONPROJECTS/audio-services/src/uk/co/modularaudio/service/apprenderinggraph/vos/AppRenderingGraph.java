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

package uk.co.modularaudio.service.apprenderinggraph.vos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.masterio.mu.MasterInMadDefinition;
import uk.co.modularaudio.mads.masterio.mu.MasterInMadInstance;
import uk.co.modularaudio.mads.masterio.mu.MasterOutMadDefinition;
import uk.co.modularaudio.mads.masterio.mu.MasterOutMadInstance;
import uk.co.modularaudio.mads.masterio.mu.MasterOutMadInstance.FadeType;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.HotspotClockSourceJobQueueHelperThread;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.HotspotFrameTimeFactory;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.MTRenderingJobQueue;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.STRenderingJobQueue;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.service.rendering.vos.AbstractParallelRenderingJob;
import uk.co.modularaudio.service.rendering.vos.RenderingJobQueue;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.service.rendering.vos.profiling.JobProfileResult;
import uk.co.modularaudio.service.rendering.vos.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.hardwareio.IOBuffers;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AppRenderingGraph implements AppRenderingLifecycleListener
{
	private static Log log = LogFactory.getLog( AppRenderingGraph.class.getName() );

	private MadComponentService componentService = null;
	private MadGraphService graphService = null;
	private RenderingService renderingService = null;
	private TimingService timingService = null;

	private MadGraphInstance<?, ?> internalRootGraph = null;
	private MadGraphInstance<?, ?> internalHostingGraph = null;

	private MasterInMadInstance masterInInstance = null;
	private MasterOutMadInstance masterOutInstance = null;

	private final AtomicReference<RenderingPlan> renderingPlan = new AtomicReference<RenderingPlan>();

	private final RenderingJobQueue renderingJobQueue;
	private final int numHelperThreads;
	private final boolean shouldProfileRenderingJobs;
	private final RenderingJobQueueHelperThread threads[];
	private final int maxWaitForTransitionMillis;

	private DynamicRenderingPlanGraphListener dynamicRenderingPlanGraphListener = null;

	private MadGraphInstance<?, ?> emptyGraphWhenNotRendering = null;
	// The actual graph to render
	private MadGraphInstance<?, ?> currentRenderingGraph = null;
	// The externally set application graph
	private MadGraphInstance<?, ?> externalApplicationGraph = null;

	// The graph containing the audio system tester components
	private MadGraphInstance<?,?> audioSystemTesterGraph = null;

	private HotspotClockSourceJobQueueHelperThread hotspotClockSourceThread = null;

	private JobDataListComparator jobDataListComparator = new JobDataListComparator();

	public AppRenderingGraph( MadComponentService componentService,
			MadGraphService graphService,
			RenderingService renderingService,
			TimingService timingService,
			int numHelperThreads,
			boolean shouldProfileRenderingJobs,
			int maxWaitForTransitionMillis ) throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		this.componentService = componentService;
		this.graphService = graphService;
		this.renderingService = renderingService;
		this.timingService = timingService;

		internalRootGraph = graphService.createNewRootGraph( "Component Designer IO Graph" );
		internalHostingGraph = graphService.createNewParameterisedGraph( "Component Designer Hosting Graph",
				GraphType.APP_GRAPH,
				16, 16,
				16, 16,
				16, 16 );
		internalRootGraph.addInstanceWithName( internalHostingGraph, internalHostingGraph.getInstanceName() );
		addIOComponentsToRootGraph();

		// Temporary empty graph to render
		emptyGraphWhenNotRendering = graphService.createNewParameterisedGraph( "Graph When Not Rendering",
				GraphType.SUB_GRAPH,
				16, 16,
				16, 16,
				16, 16 );

		internalUseGraph( emptyGraphWhenNotRendering );

		audioSystemTesterGraph = AudioSystemTestGraphCreator.createAudioSystemTestGraph( graphService, componentService );

		if( numHelperThreads == 0 )
		{
			renderingJobQueue = new STRenderingJobQueue( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );
		}
		else
		{
			renderingJobQueue = new MTRenderingJobQueue( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );
		}

		this.numHelperThreads = numHelperThreads;
		this.threads = new RenderingJobQueueHelperThread[ numHelperThreads ];
		this.shouldProfileRenderingJobs = shouldProfileRenderingJobs;
		this.maxWaitForTransitionMillis = maxWaitForTransitionMillis;
	}

	public void destroy() throws DatastoreException
	{
		log.debug( "clearing up the graphs..");
		if( externalApplicationGraph != null )
		{
			throw new DatastoreException( "During cleanup the externally set application graph has not been unset" );
		}
		if( dynamicRenderingPlanGraphListener != null )
		{
			throw new DatastoreException( "During destruction the drgpl is not null!" );
		}
		try
		{
			graphService.destroyGraph( internalRootGraph, true, true );

			graphService.destroyGraph( audioSystemTesterGraph, true, true );
		}
		catch ( Exception e)
		{
			String msg = "Exception caught cleaning up root graph: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	};

	public void dumpRenderingPlan() throws DatastoreException
	{
		log.debug( "Root graph dump: " );
		log.debug( "####################### ROOT GRAPH BEGIN #########################" );
		graphService.dumpGraph( internalRootGraph );
		log.debug( "####################### ROOT GRAPH END #########################" );
		if( dynamicRenderingPlanGraphListener != null )
		{
			DataRate tmpDataRate = DataRate.SR_44100;
			HardwareIOOneChannelSetting dumpChannelSetting = new HardwareIOOneChannelSetting( tmpDataRate,  1024 );
			HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings(dumpChannelSetting, 40000, 1024 );
			MadFrameTimeFactory frameTimeFactory = new HotspotFrameTimeFactory();
			RenderingPlan renderingPlan = renderingService.createRenderingPlan( internalRootGraph, dataRateConfiguration, frameTimeFactory );
			renderingService.dumpRenderingPlan( renderingPlan );
		}
		else
		{
			log.warn("Unable to dump rendering plan as not currently rendering.");
		}
	}

	public void dumpProfileResults()
	{
		if( !shouldProfileRenderingJobs )
		{
			log.debug( "Asked for dump of profile results but profiling isn't activated!" );
			return;
		}
		RenderingPlan rp = renderingPlan.get();
		if( rp != null )
		{
			RenderingPlanProfileResults profileResults = new RenderingPlanProfileResults( rp.getAllJobs() );

			boolean success = rp.getProfileResultsIfFilled(profileResults);

			if( success )
			{
				long clockCallbackStart = profileResults.getClockCallbackStart();
				long clockCallbackPostProducer = profileResults.getClockCallbackPostProducer();
				long clockCallbackPostRpFetch = profileResults.getClockCallbackPostRpFetch();
				long clockCallbackPostLoop = profileResults.getClockCallbackPostLoop();
				long producerDuration = clockCallbackPostProducer - clockCallbackStart;
				long rpFetchDuration = clockCallbackPostRpFetch - clockCallbackPostProducer;
				long loopDuration = clockCallbackPostLoop - clockCallbackPostRpFetch;
				long totalDuration = clockCallbackPostLoop - clockCallbackStart;
				log.debug("Got rendering profile results - clockStart(" + clockCallbackStart + ") clockEnd(" +
						clockCallbackPostLoop + ") producerDuration(" + producerDuration + ") rpFetchDuration( " +
						rpFetchDuration + ") loopDuration(" + loopDuration + ") totalDuration(" + totalDuration + ")");
				HashMap<AbstractParallelRenderingJob, JobProfileResult> jobToProfileResultMap = profileResults.getJobToProfileResultMap();

				ArrayList<ParsedJobData> jobDataList = new ArrayList<ParsedJobData>();

				for( AbstractParallelRenderingJob rj : jobToProfileResultMap.keySet() )
				{
					JobProfileResult jr = jobToProfileResultMap.get( rj );
					long jobStartTimestamp = jr.getStartTimestamp();
					long jobEndTimestamp = jr.getEndTimestamp();
					long jobOffsetFromStart = jobStartTimestamp - clockCallbackStart;
					long jobLength = jobEndTimestamp - jobStartTimestamp;
					int jobThreadNum = jr.getJobThreadExecutor();
					String jobName = rj.toString();
					ParsedJobData pjd = new ParsedJobData( jobStartTimestamp, jobEndTimestamp, jobOffsetFromStart, jobLength, jobThreadNum, jobName );
					jobDataList.add( pjd );
				}

				Collections.sort( jobDataList, jobDataListComparator );

				for( ParsedJobData pjd : jobDataList )
				{
					log.debug( pjd.toString() );
				}
			}
			else
			{
				log.warn( "Failed to fetch profiling results");
			}
		}
	}

	public void setApplicationGraph( MadGraphInstance<?, ?> newGraphToRender )
			throws DatastoreException
	{
		if( externalApplicationGraph != null )
		{
			String msg = "Cannot set an application graph when one is already set.";
			throw new DatastoreException( msg );
		}
		else
		{
			log.debug("Setting application graph to: \"" + newGraphToRender.getInstanceName() + "\"");
			externalApplicationGraph = newGraphToRender;
		}
	}

	public void unsetApplicationGraph( MadGraphInstance<?, ?> oldGraphToUnset )
			throws DatastoreException
	{
		if ( externalApplicationGraph == oldGraphToUnset)
		{
			if( currentRenderingGraph == oldGraphToUnset )
			{
				String msg = "Cannot unset application graph whilst it is active.";
				throw new DatastoreException( msg );
			}
			else
			{
				externalApplicationGraph = null;
			}
		}
		else
		{
			String msg = "Attempting to unset application graph with an incorrect graph";
			throw new DatastoreException( msg );
		}
	}

	public boolean isApplicationGraphSet()
	{
		return externalApplicationGraph != null;
	}

	public void activateApplicationGraph()
			throws MadProcessingException
	{
		if( externalApplicationGraph == null )
		{
			String msg = "Unable to activate application graph when one isn't set!";
			throw new MadProcessingException( msg );
		}
		else if( currentRenderingGraph == externalApplicationGraph )
		{
			String msg = "Attempting to activate the application graph when it is already active.";
			throw new MadProcessingException( msg );
		}

		try
		{
			// Switch over to application graph
			internalUseGraph( externalApplicationGraph );
		}
		catch (Exception e)
		{
			String msg = "Exception caught activating application graph: " + e.toString();
			log.error( msg, e );
			throw new MadProcessingException( msg, e );
		}
	}

	public void deactivateApplicationGraph() throws MadProcessingException
	{
		if( externalApplicationGraph == null )
		{
			String msg = "Unable to deactivate application when one is not being used!";
			throw new MadProcessingException( msg );
		}
		else if( currentRenderingGraph != externalApplicationGraph )
		{
			String msg = "Unable to deactivate application when one is not being used!";
			throw new MadProcessingException( msg );
		}
		else
		{
			try
			{
				internalUseGraph( emptyGraphWhenNotRendering );
			}
			catch( DatastoreException de )
			{
				String msg = "DatastoreException caught attempting to switch to empty graph: " + de.toString();
				throw new MadProcessingException( msg, de );
			}
		}
	}

	public boolean isApplicationGraphActive()
	{
		return currentRenderingGraph != emptyGraphWhenNotRendering;
	}

	public MadGraphInstance<?, ?> getCurrentApplicationGraph()
	{
		return externalApplicationGraph;
	}

	@Override
	public void receiveEngineSignal( HardwareIOChannelSettings coreEngineChannelSettings,
			MadFrameTimeFactory frameTimeFactory,
			SignalType signalType,
			AppRenderingErrorQueue errorQueue )
		throws DatastoreException, MadProcessingException
	{
		log.debug("Received engine signal: " + signalType );
		switch( signalType )
		{
			case START_TEST:
			{
				// Switch to the tester graph
				try
				{
					graphService.removeInstanceFromGraph( internalHostingGraph, currentRenderingGraph );
					graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( internalHostingGraph, audioSystemTesterGraph,
							audioSystemTesterGraph.getInstanceName(), true );
				}
				catch (Exception e)
				{
					String msg = "Exception caught switching to testing graph: " + e.toString();
					log.error( msg, e );
					throw new DatastoreException( msg, e  );
				}
				break;
			}
			case STOP_TEST:
			{
				// Switch back from the tester graph to the current rendering graph;
				try
				{
					graphService.removeInstanceFromGraph( internalHostingGraph, audioSystemTesterGraph );
					graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( internalHostingGraph, currentRenderingGraph,
							currentRenderingGraph.getInstanceName(), true );
				}
				catch( Exception e )
				{
					String msg = "Exception caught switching back from testing graph: " + e.toString();
					log.error( msg, e );
					throw new DatastoreException( msg, e  );
				}
				break;
			}
			case PRE_START:
			{
				if( dynamicRenderingPlanGraphListener != null )
				{
					String msg = "Unable to add dynamic rendering plan listener as one is already set!";
					throw new DatastoreException( msg );
				}
				else
				{
					startThreads();

					dynamicRenderingPlanGraphListener = new DynamicRenderingPlanGraphListener( renderingService,
							this,
							internalRootGraph );

					// Make sure we are listening for changes to the graph
					graphService.addGraphListener( internalRootGraph, dynamicRenderingPlanGraphListener );

					// Get the listener to create the first rendering plan based on what
					// is in the current rack
//					drpgl.receiveGraphChangeSignal();
					dynamicRenderingPlanGraphListener.forcePlanCreation( coreEngineChannelSettings, frameTimeFactory );

					// Start a fade in
					masterOutInstance.setAndStartFade( FadeType.IN );
				}
				break;
			}
			case POST_START:
			{
				// Wait for the fade in to complete
				int numSamplesLatencyClockBuffer = coreEngineChannelSettings.getSampleFramesOutputLatency();
				long nanosOutputLatency = coreEngineChannelSettings.getNanosOutputLatency();
				int sleepWaitingForFadeMillis = (int)((nanosOutputLatency / 1000000) / 2);
				long endTimeMillis = System.currentTimeMillis() + maxWaitForTransitionMillis;
				long curTimeMillis;
				while( (curTimeMillis = System.currentTimeMillis()) < endTimeMillis )
				{
					if( !masterOutInstance.isFadeFinished( numSamplesLatencyClockBuffer ) )
					{
						try
						{
							Thread.sleep( sleepWaitingForFadeMillis );
						}
						catch( InterruptedException ie )
						{}
					}
					else
					{
						break;
					}
				}
				if( curTimeMillis >= endTimeMillis )
				{
					log.warn( "Failed waiting for post_start transition" );
				}
				masterOutInstance.setAndStartFade( FadeType.NONE );
				break;
			}
			case PRE_STOP:
			{
				int numSamplesLatencyClockBuffer = coreEngineChannelSettings.getSampleFramesOutputLatency();
				long nanosOutputLatency = coreEngineChannelSettings.getNanosOutputLatency();
				int sleepWaitingForFadeMillis = (int)((nanosOutputLatency / 1000000) / 2);

				masterOutInstance.setAndStartFade( FadeType.OUT );
				// Wait for fade out to finish
				long endTimeMillis = System.currentTimeMillis() + maxWaitForTransitionMillis;
				long curTimeMillis;
				while( (curTimeMillis = System.currentTimeMillis()) < endTimeMillis )
				{
					if( !masterOutInstance.isFadeFinished( numSamplesLatencyClockBuffer ) )
					{
						try
						{
							Thread.sleep( sleepWaitingForFadeMillis );
						}
						catch( InterruptedException ie )
						{}
					}
					else
					{
						break;
					}
				}
				if( curTimeMillis >= endTimeMillis )
				{
					log.warn( "Failed waiting for pre_stop transition" );
				}
				// Don't reset the fade type
				break;
			}
			case POST_STOP:
			{
				if( dynamicRenderingPlanGraphListener == null )
				{
					String msg = "Failed to process post stop - no plan creation listener currently being used.";
					throw new DatastoreException( msg );
				}
				else
				{
					graphService.removeGraphListener( internalRootGraph, dynamicRenderingPlanGraphListener );
					dynamicRenderingPlanGraphListener.destroy();
					dynamicRenderingPlanGraphListener = null;
				}
				stopThreads();
				break;
			}
		}

	}

	public void useNewRenderingPlanWithWaitDestroyPrevious( RenderingPlan newRenderingPlan ) throws MadProcessingException
	{
		RenderingPlan previousPlan = this.renderingPlan.get();
		if( previousPlan == null && newRenderingPlan == null )
		{
			// intentionally do nothing.
			log.warn( "Attempted to use new rendering plan that is null - and we don't have one either");
			return;
		}

		boolean needWaitForUse = false;
		int sleepWaitingForPlanMillis = 100;

//		log.debug("Will attempt to hot-swap out rendering plan.");
//		log.debug("New plan has " + newRenderingPlan.getTotalNumJobs() + " total rendering jobs");

		if( previousPlan == null && newRenderingPlan != null )
		{
			HardwareIOChannelSettings planChannelSettings = newRenderingPlan.getPlanChannelSettings();
			MadTimingParameters planTimingParameters = newRenderingPlan.getPlanTimingParameters();
			MadFrameTimeFactory planFrameTimeFactory = newRenderingPlan.getPlanFrameTimeFactory();
			// Start em all
			Set<MadInstance<?,?>> auis = newRenderingPlan.getAllInstances();
			for( MadInstance<?,?> aui : auis )
			{
				aui.internalEngineStartup( planChannelSettings, planTimingParameters, planFrameTimeFactory );
			}
		}
		else if( previousPlan != null && newRenderingPlan == null )
		{
			needWaitForUse = internalRootGraph.hasListeners();
			HardwareIOChannelSettings previousPlanChannelSettings = previousPlan.getPlanChannelSettings();
			long nanosOutputLatency = previousPlanChannelSettings.getNanosOutputLatency();
			sleepWaitingForPlanMillis = (int)((nanosOutputLatency / 1000000) / 2);
		}
		else // Both non-null
		{
			// Got to work out which components are to be stopped
			Set<MadInstance<?,?>> previousAuis = previousPlan.getAllInstances();
			Set<MadInstance<?,?>> newAuis = newRenderingPlan.getAllInstances();

			HardwareIOChannelSettings planChannelSettings = newRenderingPlan.getPlanChannelSettings();
			MadTimingParameters planTimingParameters = newRenderingPlan.getPlanTimingParameters();
			MadFrameTimeFactory planFrameTimeFactory = newRenderingPlan.getPlanFrameTimeFactory();
			for( MadInstance<?,?> newAui : newAuis )
			{
				if( !previousAuis.contains( newAui ) )
				{
					newAui.internalEngineStartup( planChannelSettings, planTimingParameters, planFrameTimeFactory );
				}
			}
			needWaitForUse = internalRootGraph.hasListeners();
			long nanosOutputLatency = planChannelSettings.getNanosOutputLatency();
			sleepWaitingForPlanMillis = (int)((nanosOutputLatency / 1000000) / 2);
		}

		this.renderingPlan.set( newRenderingPlan );

		if( needWaitForUse )
		{
			long startTime = System.currentTimeMillis();
			long curTime = startTime;
			while( curTime < startTime + maxWaitForTransitionMillis && !newRenderingPlan.getPlanUsed() )
			{
				try
				{
					Thread.sleep( sleepWaitingForPlanMillis );
				}
				catch (InterruptedException e)
				{
					log.error("InterruptedException during sleep waiting for plan usage: " + e.toString(), e );
				}
				curTime = System.currentTimeMillis();
			}
		}

		// Need to start components that are in the new rendering plan, but not in the old
		if( previousPlan == null && newRenderingPlan != null )
		{
		}
		else if( previousPlan != null && newRenderingPlan == null )
		{
			// Stop em all
			Set<MadInstance<?,?>> auis = previousPlan.getAllInstances();
			for( MadInstance<?,?> aui : auis )
			{
				aui.internalEngineStop();
			}
		}
		else // Both non-null
		{
			// Got to work out which components are to be stopped
			Set<MadInstance<?,?>> previousAuis = previousPlan.getAllInstances();
			Set<MadInstance<?,?>> newAuis = newRenderingPlan.getAllInstances();

			for( MadInstance<?,?> previousAui : previousAuis )
			{
				if( !newAuis.contains( previousAui ) )
				{
					previousAui.internalEngineStop();
				}
			}
		}

		if( previousPlan != null )
		{
			renderingService.destroyRenderingPlan( previousPlan );
		}
	}

	private void addIOComponentsToRootGraph()
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, MadProcessingException
	{
		MadDefinition<?, ?> masterInDef = componentService.findDefinitionById( MasterInMadDefinition.DEFINITION_ID );
		masterInInstance =
				(MasterInMadInstance) componentService.createInstanceFromDefinition( masterInDef,
						null,
						"Master In" );
		internalRootGraph.addInstanceWithName( masterInInstance, masterInInstance.getInstanceName() );

		MadChannelInstance[] micis = masterInInstance.getChannelInstances();
		for (int i = 0; i < micis.length; i++)
		{
			MadChannelInstance masterInputChannel = micis[i];
			String channelName = masterInputChannel.definition.name;
			String findChannelName = channelName.replaceAll( "Output", "Input" );
			MadChannelInstance subGraphInputChannel = internalHostingGraph.getChannelInstanceByName( findChannelName );
			MadLink masterInLink = new MadLink( masterInputChannel, subGraphInputChannel );
			internalRootGraph.addLink( masterInLink );
		}

		MadDefinition<?, ?> masterOutDef = componentService.findDefinitionById( MasterOutMadDefinition.DEFINITION_ID );
		masterOutInstance =
				(MasterOutMadInstance) componentService.createInstanceFromDefinition( masterOutDef,
						null,
						"Master Out" );

		internalRootGraph.addInstanceWithName( masterOutInstance,
				masterOutInstance.getInstanceName() );

		MadChannelInstance[] mocis = masterOutInstance.getChannelInstances();
		for (int o = 0; o < mocis.length; o++)
		{
			MadChannelInstance masterOutputChannel = mocis[o];
			String channelName = masterOutputChannel.definition.name;
			String findChannelName = channelName.replaceAll( "Input", "Output" );
			MadChannelInstance subGraphOutputChannel = internalHostingGraph.getChannelInstanceByName( findChannelName );
			MadLink masterOutLink = new MadLink( subGraphOutputChannel, masterOutputChannel );
			internalRootGraph.addLink( masterOutLink );
		}
	}

	private void internalUseGraph( MadGraphInstance<?, ?> newGraphToRender )
				throws DatastoreException
	{
		log.debug("internalUseGraph called with graph: \"" + newGraphToRender.getInstanceName() + "\"");
		try
		{
			// First time through this will be null
			// Otherwise we remove any set copy
			if (currentRenderingGraph != null)
			{
				graphService.removeInstanceFromGraph( internalHostingGraph,
						currentRenderingGraph );
			}

			// Move over the new one, and hook it up
			this.currentRenderingGraph = newGraphToRender;

			// Set the application graph and wire it up
			addGraphToHostingGraphAndHookupChannels( currentRenderingGraph );
		}
		catch (Exception e)
		{
			String msg = "Exception caught attempting to set app rack: "
					+ e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private void addGraphToHostingGraphAndHookupChannels( MadGraphInstance<?, ?> graphFromUser )
			throws MAConstraintViolationException, DatastoreException
	{
		log.debug("Hooking up channels of graph: \"" + graphFromUser.getInstanceName() + "\"");

		// Now loop around the appGraph channels looking for corresponding rack
		// graph channels
		try
		{
			graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( internalHostingGraph, graphFromUser,
					graphFromUser.getInstanceName(), true );
		}
		catch( RecordNotFoundException rnfe )
		{
			String msg = "RecordNotFoundException caught mapping app graph to hosting graph: " + rnfe.toString();
			log.error( msg, rnfe );
		}
	}

	private void startThreads()
	{
		log.debug("Starting helper threads.");
		for( int i = 0 ; i < numHelperThreads ; i++ )
		{
			threads[ i ] = new RenderingJobQueueHelperThread( i + 1, renderingJobQueue, shouldProfileRenderingJobs );
			threads[ i ].start();
		}
	}

	private void stopThreads()
	{
		log.debug("Stopping helper threads.");
		for( int i = 0 ; i < numHelperThreads ; i++ )
		{
			threads[ i ].halt();
		}
		try
		{
			Thread.sleep( 5 );
		}
		catch( InterruptedException ie )
		{
		}

		for( int i = 0 ; i < numHelperThreads ; i++ )
		{
			try
			{
				if( threads[i].isAlive() )
				{
					threads[i].forceHalt();
				}
				threads[ i ].join();
			}
			catch (Exception e)
			{
				String msg = "Exception caught stopping and joining helper thread: " + e.toString();
				log.error( msg, e );
			}
			threads[ i ] = null;
		}
	}

	public AtomicReference<RenderingPlan> getAtomicRenderingPlan()
	{
		return renderingPlan;
	}

	public RenderingJobQueue getRenderingJobQueue()
	{
		return renderingJobQueue;
	}

	public void startHotspotLooping( RenderingPlan renderingPlan )
	{
		hotspotClockSourceThread = new HotspotClockSourceJobQueueHelperThread( renderingPlan,
				renderingService,
				timingService,
				renderingJobQueue,
				shouldProfileRenderingJobs );
		hotspotClockSourceThread.start();
	}

	public void stopHotspotLooping()
	{
		// Let it exit gracefully
		try
		{
			hotspotClockSourceThread.halt();
			Thread.sleep( 200 );
		}
		catch(InterruptedException ie )
		{
		}
		if( hotspotClockSourceThread.isAlive() )
		{
			hotspotClockSourceThread.forceHalt();
		}
		try
		{
			hotspotClockSourceThread.join();
		}
		catch (InterruptedException ie)
		{
			String msg = "Interrupted waiting for join to hotspot thread: " + ie.toString();
			log.error( msg, ie );
		}
		hotspotClockSourceThread = null;
	}

	public IOBuffers getMasterInBuffers()
	{
		return masterInInstance.getMasterIOBuffers();
	}

	public IOBuffers getMasterOutBuffers()
	{
		return masterOutInstance.getMasterIOBuffers();
	}
}
