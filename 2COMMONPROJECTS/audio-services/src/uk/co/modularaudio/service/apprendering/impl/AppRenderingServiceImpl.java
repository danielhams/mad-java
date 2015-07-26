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

package uk.co.modularaudio.service.apprendering.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.AppRenderingService;
import uk.co.modularaudio.service.apprendering.util.AppRenderingStructure;
import uk.co.modularaudio.service.apprendering.util.HotspotFrameTimeFactory;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class AppRenderingServiceImpl
	implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, AppRenderingService
{
	private static Log log = LogFactory.getLog( AppRenderingServiceImpl.class.getName() );

	private ConfigurationService configurationService;
	private MadComponentService componentService;
	private MadGraphService graphService;
	private RackService rackService;
	private RenderingPlanService renderingPlanService;
	private TimingService timingService;

	private final static String CONFIG_KEY_STARTUP_HOTSPOT = AppRenderingServiceImpl.class.getSimpleName() + ".StartupHotspot";
	private final static String CONFIG_KEY_PROFILE_RENDERING_JOBS = AppRenderingServiceImpl.class.getSimpleName() + ".ProfileRenderingJobs";
	private final static String CONFIG_KEY_PROFILE_TEMP_EVENT_STORAGE_CAPACITY = AppRenderingServiceImpl.class.getSimpleName() + ".TempEventStorageCapacity";
	private final static String CONFIG_KEY_PROFILE_RENDERING_JOB_QUEUE_CAPACITY = AppRenderingServiceImpl.class.getSimpleName() + ".RenderingJobQueueCapacity";
	private final static String CONFIG_KEY_MAX_WAIT_FOR_TRANSITION_MILLIS = AppRenderingServiceImpl.class.getSimpleName() + ".MaxWaitForTransitionMillis";

	private final static int HOTSPOT_SAMPLES_PER_RENDER_PERIOD = 1024;

	// Two seconds of hotspotting
	private static final long HOTSPOT_COMPILATION_TIME_MILLIS = 3000;

	private boolean doStartupHotspot;

	private boolean shouldProfileRenderingJobs;
	private int tempEventStorageCapacity;
	private int renderingJobQueueCapacity;
	private int maxWaitForTransitionMillis;

	public AppRenderingServiceImpl()
	{
		// Uses DI.
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				componentService == null ||
				graphService == null ||
				rackService == null ||
				renderingPlanService == null ||
				timingService == null )
		{
			final String msg = "Missing service dependencies. Check configuration.";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		final Map<String,String> errors = new HashMap<String,String>();

		doStartupHotspot = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_STARTUP_HOTSPOT, errors );

		shouldProfileRenderingJobs = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_PROFILE_RENDERING_JOBS,
				errors );
		tempEventStorageCapacity = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_PROFILE_TEMP_EVENT_STORAGE_CAPACITY,
				errors );
		renderingJobQueueCapacity = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_PROFILE_RENDERING_JOB_QUEUE_CAPACITY,
				errors );

		maxWaitForTransitionMillis = ConfigurationServiceHelper.checkForIntKey(configurationService, CONFIG_KEY_MAX_WAIT_FOR_TRANSITION_MILLIS, errors);

		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		// Do any needed hotspot looping to get things compile hot
		if( doStartupHotspot )
		{
			log.debug("Beginning startup hotspot heating");
			try
			{
				startupHotspot();
			}
			catch( final DatastoreException | TableCellFullException | TableIndexOutOfBoundsException | MAConstraintViolationException | InterruptedException e )
			{
				final String msg = "Failure during startup hotspot looping: " + e.toString();
				log.error( msg, e );
			}
			log.debug("Completed startup hotspot heating");
		}
	}

	@Override
	public void preShutdown()
	{
		// No work to do.
	}

	@Override
	public void destroy()
	{
		// No work to do.
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setGraphService( final MadGraphService graphService )
	{
		this.graphService = graphService;
	}

	public void setRackService( final RackService rackService )
	{
		this.rackService = rackService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setRenderingPlanService( final RenderingPlanService renderingService )
	{
		this.renderingPlanService = renderingService;
	}

	public void setTimingService( final TimingService timingService )
	{
		this.timingService = timingService;
	}

	@Override
	public AppRenderingStructure createAppRenderingStructure( final int numHelperThreads ) throws DatastoreException
	{
		try
		{
			return new AppRenderingStructure( componentService,
					graphService,
					renderingPlanService,
					renderingJobQueueCapacity,
					numHelperThreads,
					tempEventStorageCapacity,
					shouldProfileRenderingJobs,
					maxWaitForTransitionMillis );
		}
		catch( final RecordNotFoundException rnfe )
		{
			throw new DatastoreException( "RecordNotFoundException creating app rendering structure: " + rnfe.toString(), rnfe );
		}
		catch( final MadProcessingException aupe )
		{
			throw new DatastoreException( "MadProcessingException creating app rendering structure: " + aupe.toString(), aupe );
		}
		catch (final MAConstraintViolationException ecve)
		{
			throw new DatastoreException( "ConstraintViolationException creating app rendering structure: " + ecve.toString(), ecve );
		}
	}

	@Override
	public boolean shouldProfileRenderingJobs()
	{
		return shouldProfileRenderingJobs;
	}

	@Override
	public void destroyAppRenderingStructure( final AppRenderingStructure renderingStructure )
		throws DatastoreException
	{
		// Don't need to do anything, the GC will take care of it
	}

	private HotspotRenderingContainer createHotspotRenderingContainer( final RenderingPlan renderingPlan ) throws DatastoreException
	{
		try
		{
			return new HotspotRenderingAppStructure( componentService,
					graphService,
					renderingPlanService,
					timingService,
					renderingJobQueueCapacity,
					tempEventStorageCapacity,
					shouldProfileRenderingJobs,
					maxWaitForTransitionMillis,
					renderingPlan );
		}
		catch( final RecordNotFoundException rnfe )
		{
			throw new DatastoreException( "RecordNotFoundException creating hotspot rendering structure: " + rnfe.toString(), rnfe );
		}
		catch( final MadProcessingException aupe )
		{
			throw new DatastoreException( "MadProcessingException creating hotspot rendering structure: " + aupe.toString(), aupe );
		}
		catch (final MAConstraintViolationException ecve)
		{
			throw new DatastoreException( "ConstraintViolationException creating hotspot rendering structure: " + ecve.toString(), ecve );
		}
	}

	private void startupHotspot() throws DatastoreException, TableCellFullException, TableIndexOutOfBoundsException, MAConstraintViolationException, InterruptedException
	{
		// Hotspot compile them in one big rack
		final MadDefinitionListModel allMadDefinitions = componentService.listDefinitionsAvailable();

		// Create a new rack with at least four rows per component type
		final int numDefinitions = allMadDefinitions.getSize();
		final RackDataModel cacheRack = rackService.createNewRackDataModel( "cachingrack",
				"",
				RackService.DEFAULT_RACK_COLS,
				RackService.DEFAULT_RACK_ROWS * numDefinitions,
				false );

		final Map<MadParameterDefinition,String> emptyParameterValues = new HashMap<MadParameterDefinition, String>();

		for( int i = 0 ; i < numDefinitions ; i++ )
		{
			final Object o = allMadDefinitions.getElementAt( i );
			final MadDefinition<?,?> md = (MadDefinition<?,?>)o;
			if( isDefinitionPublic( md ) )
			{
				if( md.isParametrable() )
				{
					// Attempt empty parameters creation
					try
					{
						final String name = md.getId() + i;
						rackService.createComponent( cacheRack, md, emptyParameterValues, name );
					}
					catch(final Exception e )
					{
						if( log.isInfoEnabled() )
						{
							log.info("Skipping hotspot of " + md.getId() + " as it needs parameters and no default didn't work." ); // NOPMD by dan on 01/02/15 07:29
						}
					}
				}
				else
				{
					try
					{
						final String name = md.getId() + i;
						rackService.createComponent( cacheRack, md, emptyParameterValues, name );
					}
					catch(final RecordNotFoundException rnfe )
					{
						if( log.isInfoEnabled() )
						{
							log.info( "Skipping hotspot of " + md.getId() + " - probably missing UI for it" ); // NOPMD by dan on 01/02/15 07:29
						}
					}
				}
			}
		}

		final MadGraphInstance<?,?> hotspotGraph = rackService.getRackGraphInstance( cacheRack );

		final int outputLatencyFrames = HOTSPOT_SAMPLES_PER_RENDER_PERIOD;

		final HardwareIOOneChannelSetting hotspotCelc= new HardwareIOOneChannelSetting( DataRate.SR_44100,
				outputLatencyFrames );

		final long outputLatencyNanos = AudioTimingUtils.getNumNanosecondsForBufferLength(DataRate.SR_44100.getValue(),
				outputLatencyFrames );

		final HardwareIOChannelSettings hotspotDrc = new HardwareIOChannelSettings( hotspotCelc, outputLatencyNanos, outputLatencyFrames );
		final MadFrameTimeFactory hotspotFrameTimeFactory = new HotspotFrameTimeFactory();
		final RenderingPlan renderingPlan = renderingPlanService.createRenderingPlan( hotspotGraph, hotspotDrc, hotspotFrameTimeFactory );

		// Now create a rendering plan from this rack
		log.debug("Peforming hotspot mad instance looping.");
		final HotspotRenderingContainer hotspotContainer = createHotspotRenderingContainer( renderingPlan );
		hotspotContainer.startHotspotLooping();
		Thread.sleep( HOTSPOT_COMPILATION_TIME_MILLIS );
		hotspotContainer.stopHotspotLooping();

		rackService.destroyRackDataModel( cacheRack );
	}

	private final static boolean isDefinitionPublic( final MadDefinition<?,?> definition )
	{
		final MadClassification auc = definition.getClassification();
		final MadClassificationGroup aug = auc.getGroup();
		return ( aug != null ? aug.getVisibility() == Visibility.PUBLIC : false );
	}
}
