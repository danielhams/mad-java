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

package uk.co.modularaudio.service.apprendering.util;

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
import uk.co.modularaudio.service.apprendering.util.jobqueue.MTRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.jobqueue.STRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.session.AppRenderingLifecycleListener;
import uk.co.modularaudio.service.apprendering.util.structure.AudioSystemTestGraphCreator;
import uk.co.modularaudio.service.apprendering.util.structure.DynamicRenderingPlanGraphListener;
import uk.co.modularaudio.service.apprendering.util.structure.JobDataListComparator;
import uk.co.modularaudio.service.apprendering.util.structure.ParsedJobData;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.renderingplan.RenderingJob;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.service.renderingplan.profiling.JobProfileResult;
import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
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

/**
 * <p>The AppRenderingStructure is an object used to manage the lifecycle
 * of audio related state.</p>
 * <p>It is intended that one such object exists per application audio session.</p>
 * <p>An AppRenderingStructure contains:</p>
 * <ul>
 * <li>A root graph</li>
 * <li>A rendering plan related to the current state of the root graph</li>
 * <li>A rendering job queue on which jobs from the rendering plan can be placed</li>
 * <li>Zero or more rendering helper threads (currently zero due to JVM jitter)</li>
 * <li>A testing graph containing a component that outputs a test square wave signal</li>
 * </ul>
 * <p>The root graph contains:</p>
 * <ul>
 * <li>Components for performing audio IO such as audio and MIDI
 * <li>An internal hosting graph</li>
 * <li>Links from the audio IO components to the appropriate channels
 * of the internal hosting graph</li>
 * </ul>
 * <p>In turn, the internal hosting graph contains one of:</p>
 * <ul>
 * <li>An empty sub-graph when not rendering</li>
 * <li>A full subgraph of the applications audio components when rendering</li>
 * </ul>
 *
 * @author dan
 *
 */
public class AppRenderingStructure implements AppRenderingLifecycleListener
{
	private static Log log = LogFactory.getLog( AppRenderingStructure.class.getName() );

	private final MadComponentService componentService;
	private final MadGraphService graphService;
	private final RenderingPlanService renderingPlanService;

	private final MadGraphInstance<?, ?> internalRootGraph;
	private final MadGraphInstance<?, ?> internalHostingGraph;

	private MasterInMadInstance masterInInstance;
	private MasterOutMadInstance masterOutInstance;

	private final AtomicReference<RenderingPlan> renderingPlan = new AtomicReference<RenderingPlan>();

	private final AppRenderingJobQueue renderingJobQueue;

	private final boolean shouldProfileRenderingJobs;

	private final int maxWaitForTransitionMillis;

	private DynamicRenderingPlanGraphListener dynamicRenderingPlanGraphListener;

	private final MadGraphInstance<?, ?> emptyGraphWhenNotRendering;
	// The actual graph to render
	private MadGraphInstance<?, ?> currentRenderingGraph;
	// The externally set application graph
	private MadGraphInstance<?, ?> externalApplicationGraph;

	// The graph containing the audio system tester components
	private final MadGraphInstance<?,?> audioSystemTesterGraph;

	private final JobDataListComparator jobDataListComparator = new JobDataListComparator();

	public AppRenderingStructure( final MadComponentService componentService,
			final MadGraphService graphService,
			final RenderingPlanService renderingService,
			final int renderingJobQueueCapacity,
			final int numHelperThreads,
			final int tempEventStorageCapacity,
			final boolean shouldProfileRenderingJobs,
			final int maxWaitForTransitionMillis )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		this.componentService = componentService;
		this.graphService = graphService;
		this.renderingPlanService = renderingService;

		this.shouldProfileRenderingJobs = shouldProfileRenderingJobs;
		this.maxWaitForTransitionMillis = maxWaitForTransitionMillis;

		internalRootGraph = graphService.createNewRootGraph( "Component Designer IO Graph" );
		internalHostingGraph = graphService.createNewParameterisedGraph( "Component Designer Hosting Graph",
				GraphType.APP_GRAPH,
				16, 16,
				16, 16,
				16, 16 );
		graphService.addInstanceToGraphWithName( internalRootGraph, internalHostingGraph, internalHostingGraph.getInstanceName() );
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
		catch ( final DatastoreException e)
		{
			final String msg = "Exception caught cleaning up root graph: " + e.toString();
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
			final DataRate tmpDataRate = DataRate.SR_44100;
			final HardwareIOOneChannelSetting dumpChannelSetting = new HardwareIOOneChannelSetting( tmpDataRate,  1024 );
			final HardwareIOChannelSettings dataRateConfiguration = new HardwareIOChannelSettings(dumpChannelSetting, 40000, 1024 );
			final MadFrameTimeFactory frameTimeFactory = new HotspotFrameTimeFactory();
			final RenderingPlan renderingPlan = renderingPlanService.createRenderingPlan( internalRootGraph, dataRateConfiguration, frameTimeFactory );
			renderingPlanService.dumpRenderingPlan( renderingPlan );
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
		final RenderingPlan rp = renderingPlan.get();
		if( rp != null )
		{
			final RenderingPlanProfileResults profileResults = new RenderingPlanProfileResults( rp.getAllJobs() );

			final boolean success = rp.getProfileResultsIfFilled(profileResults);

			if( success )
			{
				final long clockCallbackStart = profileResults.getClockCallbackStart();
				final long clockCallbackPostProducer = profileResults.getClockCallbackPostProducer();
				final long clockCallbackPostRpFetch = profileResults.getClockCallbackPostRpFetch();
				final long clockCallbackPostLoop = profileResults.getClockCallbackPostLoop();
				final long producerDuration = clockCallbackPostProducer - clockCallbackStart;
				final long rpFetchDuration = clockCallbackPostRpFetch - clockCallbackPostProducer;
				final long loopDuration = clockCallbackPostLoop - clockCallbackPostRpFetch;
				final long totalDuration = clockCallbackPostLoop - clockCallbackStart;
				if( log.isDebugEnabled() )
				{
					log.debug("Got rendering profile results - clockStart(" + clockCallbackStart + ") clockEnd(" +
							clockCallbackPostLoop + ") producerDuration(" + producerDuration + ") rpFetchDuration( " +
							rpFetchDuration + ") loopDuration(" + loopDuration + ") totalDuration(" + totalDuration + ")");
				}
				final HashMap<RenderingJob, JobProfileResult> jobToProfileResultMap = profileResults.getJobToProfileResultMap();

				final ArrayList<ParsedJobData> jobDataList = new ArrayList<ParsedJobData>();

				for( final RenderingJob rj : jobToProfileResultMap.keySet() )
				{
					final JobProfileResult jr = jobToProfileResultMap.get( rj );
					final long jobStartTimestamp = jr.getStartTimestamp();
					final long jobEndTimestamp = jr.getEndTimestamp();
					final long jobOffsetFromStart = jobStartTimestamp - clockCallbackStart;
					final long jobLength = jobEndTimestamp - jobStartTimestamp;
					final int jobThreadNum = jr.getJobThreadExecutor();
					final String jobName = rj.toString();
					final ParsedJobData pjd = new ParsedJobData( jobOffsetFromStart, jobLength, jobThreadNum, jobName );
					jobDataList.add( pjd );
				}

				Collections.sort( jobDataList, jobDataListComparator );

				for( final ParsedJobData pjd : jobDataList )
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

	public RenderingPlanProfileResults getProfileResults() throws DatastoreException
	{
		if( !shouldProfileRenderingJobs )
		{
			throw new DatastoreException( "Asked for profile results but profiling isn't activated!" );
		}
		final RenderingPlan rp = renderingPlan.get();
		if( rp == null )
		{
			throw new DatastoreException( "Asked for profile results but profiling isn't activated!" );
		}
		else
		{
			final RenderingPlanProfileResults profileResults = new RenderingPlanProfileResults( rp.getAllJobs() );

			if( !rp.getProfileResultsIfFilled(profileResults) )
			{
				log.warn( "Failed to fetch profiling results");
			}

			return profileResults;
		}
	}

	public void setApplicationGraph( final MadGraphInstance<?, ?> newGraphToRender )
			throws DatastoreException
	{
		if( externalApplicationGraph != null )
		{
			final String msg = "Cannot set an application graph when one is already set.";
			throw new DatastoreException( msg );
		}
		else
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Setting application graph to: \"" + newGraphToRender.getInstanceName() + "\"");
			}
			externalApplicationGraph = newGraphToRender;
		}
	}

	public void unsetApplicationGraph( final MadGraphInstance<?, ?> oldGraphToUnset )
			throws DatastoreException
	{
		if ( externalApplicationGraph == oldGraphToUnset)
		{
			if( currentRenderingGraph == oldGraphToUnset )
			{
				final String msg = "Cannot unset application graph whilst it is active.";
				throw new DatastoreException( msg );
			}
			else
			{
				externalApplicationGraph = null;
			}
		}
		else
		{
			final String msg = "Attempting to unset application graph with an incorrect graph";
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
			final String msg = "Unable to activate application graph when one isn't set!";
			throw new MadProcessingException( msg );
		}
		else if( currentRenderingGraph == externalApplicationGraph )
		{
			final String msg = "Attempting to activate the application graph when it is already active.";
			throw new MadProcessingException( msg );
		}

		try
		{
			// Switch over to application graph
			internalUseGraph( externalApplicationGraph );
		}
		catch ( final DatastoreException e)
		{
			final String msg = "Exception caught activating application graph: " + e.toString();
			log.error( msg, e );
			throw new MadProcessingException( msg, e );
		}
	}

	public void deactivateApplicationGraph() throws MadProcessingException
	{
		if( externalApplicationGraph == null )
		{
			final String msg = "Unable to deactivate application when one is not being used!";
			throw new MadProcessingException( msg );
		}
		else if( currentRenderingGraph != externalApplicationGraph )
		{
			final String msg = "Unable to deactivate application when one is not being used!";
			throw new MadProcessingException( msg );
		}
		else
		{
			try
			{
				internalUseGraph( emptyGraphWhenNotRendering );
			}
			catch( final DatastoreException de )
			{
				final String msg = "DatastoreException caught attempting to switch to empty graph: " + de.toString();
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
	public void receiveEngineSignal( final HardwareIOChannelSettings coreEngineChannelSettings,
			final MadFrameTimeFactory frameTimeFactory,
			final SignalType signalType,
			final AppRenderingErrorQueue errorQueue )
		throws DatastoreException, MadProcessingException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Received engine signal: " + signalType );
		}
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
				catch (final Exception e)
				{
					final String msg = "Exception caught switching to testing graph: " + e.toString();
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
				catch( final Exception e )
				{
					final String msg = "Exception caught switching back from testing graph: " + e.toString();
					log.error( msg, e );
					throw new DatastoreException( msg, e  );
				}
				break;
			}
			case PRE_START:
			{
				if( dynamicRenderingPlanGraphListener != null )
				{
					final String msg = "Unable to add dynamic rendering plan listener as one is already set!";
					throw new DatastoreException( msg );
				}
				else
				{
					dynamicRenderingPlanGraphListener = new DynamicRenderingPlanGraphListener( renderingPlanService,
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
				final int numSamplesLatencyClockBuffer = coreEngineChannelSettings.getSampleFramesOutputLatency();
				final long nanosOutputLatency = coreEngineChannelSettings.getNanosOutputLatency();
				final int sleepWaitingForFadeMillis = (int)((nanosOutputLatency / 1000000) / 2);
				final long endTimeMillis = System.currentTimeMillis() + maxWaitForTransitionMillis;
				long curTimeMillis;
				while( (curTimeMillis = System.currentTimeMillis()) < endTimeMillis )
				{
					if( !masterOutInstance.isFadeFinished( numSamplesLatencyClockBuffer ) )
					{
						try
						{
							Thread.sleep( sleepWaitingForFadeMillis );
						}
						catch( final InterruptedException ie )
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
				final int numSamplesLatencyClockBuffer = coreEngineChannelSettings.getSampleFramesOutputLatency();
				final long nanosOutputLatency = coreEngineChannelSettings.getNanosOutputLatency();
				final int sleepWaitingForFadeMillis = (int)((nanosOutputLatency / 1000000) / 2);

				masterOutInstance.setAndStartFade( FadeType.OUT );
				// Wait for fade out to finish
				final long endTimeMillis = System.currentTimeMillis() + maxWaitForTransitionMillis;
				long curTimeMillis;
				while( (curTimeMillis = System.currentTimeMillis()) < endTimeMillis )
				{
					if( !masterOutInstance.isFadeFinished( numSamplesLatencyClockBuffer ) )
					{
						try
						{
							Thread.sleep( sleepWaitingForFadeMillis );
						}
						catch( final InterruptedException ie )
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
					final String msg = "Failed to process post stop - no plan creation listener currently being used.";
					throw new DatastoreException( msg );
				}
				else
				{
					graphService.removeGraphListener( internalRootGraph, dynamicRenderingPlanGraphListener );
					dynamicRenderingPlanGraphListener.destroy();
					dynamicRenderingPlanGraphListener = null;
				}
				break;
			}
		}
	}

	public void useNewRenderingPlanWithWaitDestroyPrevious( final RenderingPlan newRenderingPlan ) throws MadProcessingException
	{
		final RenderingPlan previousPlan = this.renderingPlan.get();
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
			final HardwareIOChannelSettings planChannelSettings = newRenderingPlan.getPlanChannelSettings();
			final MadTimingParameters planTimingParameters = newRenderingPlan.getPlanTimingParameters();
			final MadFrameTimeFactory planFrameTimeFactory = newRenderingPlan.getPlanFrameTimeFactory();
			// Start em all
			final Set<MadInstance<?,?>> auis = newRenderingPlan.getAllInstances();
			for( final MadInstance<?,?> aui : auis )
			{
				aui.internalEngineStartup( planChannelSettings, planTimingParameters, planFrameTimeFactory );
			}
		}
		else if( previousPlan != null && newRenderingPlan == null )
		{
			needWaitForUse = graphService.graphHasListeners( internalRootGraph );
			final HardwareIOChannelSettings previousPlanChannelSettings = previousPlan.getPlanChannelSettings();
			final long nanosOutputLatency = previousPlanChannelSettings.getNanosOutputLatency();
			sleepWaitingForPlanMillis = (int)((nanosOutputLatency / 1000000) / 2);
		}
		else // Both non-null
		{
			// Got to work out which components are to be stopped
			final Set<MadInstance<?,?>> previousAuis = previousPlan.getAllInstances();
			final Set<MadInstance<?,?>> newAuis = newRenderingPlan.getAllInstances();

			final HardwareIOChannelSettings planChannelSettings = newRenderingPlan.getPlanChannelSettings();
			final MadTimingParameters planTimingParameters = newRenderingPlan.getPlanTimingParameters();
			final MadFrameTimeFactory planFrameTimeFactory = newRenderingPlan.getPlanFrameTimeFactory();
			for( final MadInstance<?,?> newAui : newAuis )
			{
				if( !previousAuis.contains( newAui ) )
				{
					newAui.internalEngineStartup( planChannelSettings, planTimingParameters, planFrameTimeFactory );
				}
			}
			needWaitForUse = graphService.graphHasListeners( internalRootGraph );
			final long nanosOutputLatency = planChannelSettings.getNanosOutputLatency();
			sleepWaitingForPlanMillis = (int)((nanosOutputLatency / 1000000) / 2);
		}

		this.renderingPlan.set( newRenderingPlan );

		if( needWaitForUse )
		{
			final long startTime = System.currentTimeMillis();
			long curTime = startTime;
			while( curTime < startTime + maxWaitForTransitionMillis && !newRenderingPlan.getPlanUsed() )
			{
				try
				{
					Thread.sleep( sleepWaitingForPlanMillis );
				}
				catch (final InterruptedException e)
				{
					if( log.isErrorEnabled() )
					{
						log.error("InterruptedException during sleep waiting for plan usage: " + e.toString(), e );
					}
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
			final Set<MadInstance<?,?>> auis = previousPlan.getAllInstances();
			for( final MadInstance<?,?> aui : auis )
			{
				aui.internalEngineStop();
			}
		}
		else // Both non-null
		{
			// Got to work out which components are to be stopped
			final Set<MadInstance<?,?>> previousAuis = previousPlan.getAllInstances();
			final Set<MadInstance<?,?>> newAuis = newRenderingPlan.getAllInstances();

			for( final MadInstance<?,?> previousAui : previousAuis )
			{
				if( !newAuis.contains( previousAui ) )
				{
					previousAui.internalEngineStop();
				}
			}
		}

		if( previousPlan != null )
		{
			renderingPlanService.destroyRenderingPlan( previousPlan );
		}
	}

	private void addIOComponentsToRootGraph()
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, MadProcessingException
	{
		final MadDefinition<?, ?> masterInDef = componentService.findDefinitionById( MasterInMadDefinition.DEFINITION_ID );
		masterInInstance =
				(MasterInMadInstance) componentService.createInstanceFromDefinition( masterInDef,
						null,
						"Master In" );

		graphService.addInstanceToGraphWithName( internalRootGraph, masterInInstance,  masterInInstance.getInstanceName() );

		final MadChannelInstance[] micis = masterInInstance.getChannelInstances();
		for (int i = 0; i < micis.length; i++)
		{
			final MadChannelInstance masterInputChannel = micis[i];
			final String channelName = masterInputChannel.definition.name;
			final String findChannelName = channelName.replaceAll( "Output", "Input" );
			final MadChannelInstance subGraphInputChannel = internalHostingGraph.getChannelInstanceByName( findChannelName );
			final MadLink masterInLink = new MadLink( masterInputChannel, subGraphInputChannel );
			graphService.addLink( internalRootGraph, masterInLink );
		}

		final MadDefinition<?, ?> masterOutDef = componentService.findDefinitionById( MasterOutMadDefinition.DEFINITION_ID );
		masterOutInstance =
				(MasterOutMadInstance) componentService.createInstanceFromDefinition( masterOutDef,
						null,
						"Master Out" );

		graphService.addInstanceToGraphWithName( internalRootGraph, masterOutInstance, masterOutInstance.getInstanceName() );

		final MadChannelInstance[] mocis = masterOutInstance.getChannelInstances();
		for (int o = 0; o < mocis.length; o++)
		{
			final MadChannelInstance masterOutputChannel = mocis[o];
			final String channelName = masterOutputChannel.definition.name;
			final String findChannelName = channelName.replaceAll( "Input", "Output" );
			final MadChannelInstance subGraphOutputChannel = internalHostingGraph.getChannelInstanceByName( findChannelName );
			final MadLink masterOutLink = new MadLink( subGraphOutputChannel, masterOutputChannel );
			graphService.addLink( internalRootGraph, masterOutLink );
		}
	}

	private void internalUseGraph( final MadGraphInstance<?, ?> newGraphToRender )
				throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("internalUseGraph called with graph: \"" + newGraphToRender.getInstanceName() + "\"");
		}
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
		catch (final Exception e)
		{
			final String msg = "Exception caught attempting to set app rack: "
					+ e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	private void addGraphToHostingGraphAndHookupChannels( final MadGraphInstance<?, ?> graphFromUser )
			throws MAConstraintViolationException, DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Hooking up channels of graph: \"" + graphFromUser.getInstanceName() + "\"");
		}

		// Now loop around the appGraph channels looking for corresponding rack
		// graph channels
		try
		{
			graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( internalHostingGraph, graphFromUser,
					graphFromUser.getInstanceName(), true );
		}
		catch( final RecordNotFoundException rnfe )
		{
			final String msg = "RecordNotFoundException caught mapping app graph to hosting graph: " + rnfe.toString();
			log.error( msg, rnfe );
		}
	}

	public AtomicReference<RenderingPlan> getAtomicRenderingPlan()
	{
		return renderingPlan;
	}

	public AppRenderingJobQueue getRenderingJobQueue()
	{
		return renderingJobQueue;
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
