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

package uk.co.modularaudio.controller.apprendering.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.apprendering.AppRenderingController;
import uk.co.modularaudio.service.apprenderingstructure.AppRenderingStructureService;
import uk.co.modularaudio.service.apprenderingstructure.HotspotRenderingContainer;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue;
import uk.co.modularaudio.service.audioproviderregistry.AudioProvider;
import uk.co.modularaudio.service.audioproviderregistry.AudioProviderRegistryService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rendering.RenderingPlan;
import uk.co.modularaudio.service.rendering.RenderingService;
import uk.co.modularaudio.util.audio.apprendering.AppRenderingSession;
import uk.co.modularaudio.util.audio.apprendering.HotspotFrameTimeFactory;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.AudioHardwareDevice;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.hardwareio.MidiHardwareDevice;
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

public class AppRenderingControllerImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, AppRenderingController
{
	private static Log log = LogFactory.getLog( AppRenderingControllerImpl.class.getName() );

	private final static String CONFIG_KEY_STARTUP_HOTSPOT = AppRenderingControllerImpl.class.getSimpleName() + ".StartupHotspot";

	private final static int HOTSPOT_SAMPLES_PER_RENDER_PERIOD = 1024;

	// Two seconds of hotspotting
	private static final long HOTSPOT_COMPILATION_TIME_MILLIS = 2000;

	private ConfigurationService configurationService;
	private MadComponentService componentService;
	private RackService rackService;
	private AudioProviderRegistryService audioProviderRegistryService;
	private AppRenderingStructureService appRenderingStructureService;
	private RenderingService renderingService;

	private boolean doStartupHotspot;

	private AppRenderingErrorQueue errorQueue;

	public AppRenderingControllerImpl()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( audioProviderRegistryService == null ||
				appRenderingStructureService == null ||
				renderingService == null ||
				configurationService == null ||
				componentService == null ||
				rackService == null
				)
		{
			throw new ComponentConfigurationException( "Service is missing dependencies" );
		}

		final Map<String,String> errors = new HashMap<String,String>();
		doStartupHotspot = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_STARTUP_HOTSPOT, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		errorQueue = new AppRenderingErrorQueue();
	}

	@Override
	public void destroy()
	{
		errorQueue.shutdown();
	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		// Do any needed hotspot looping to get things compile hot
		if( doStartupHotspot )
		{
			log.info("Beginning startup hotspot heating");
			try
			{
				startupHotspot();
			}
			catch( final DatastoreException | TableCellFullException | TableIndexOutOfBoundsException | MAConstraintViolationException | InterruptedException e )
			{
				final String msg = "Failure during startup hotspot looping: " + e.toString();
				log.error( msg, e );
			}
			log.info("Completed startup hotspot heating");
		}
	}

	@Override
	public void preShutdown()
	{
	}

	public void setAudioProviderRegistryService( final AudioProviderRegistryService audioProviderRegistryService )
	{
		this.audioProviderRegistryService = audioProviderRegistryService;
	}

	public void setAppRenderingStructureService( final AppRenderingStructureService appRenderingStructureService )
	{
		this.appRenderingStructureService = appRenderingStructureService;
	}

	public void setRenderingService( final RenderingService renderingService )
	{
		this.renderingService = renderingService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setRackService( final RackService rackService )
	{
		this.rackService = rackService;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.controller.apprendering.AppRenderingController#createAppRenderingSessionForConfiguration(uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration, uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback)
	 */
	@Override
	public AppRenderingSession createAppRenderingSessionForConfiguration( final HardwareIOConfiguration hardwareIOConfiguration,
			final AppRenderingErrorCallback errorCallback )
			throws DatastoreException, RecordNotFoundException
	{
		// Check all the appropriate things come from the same provider
		String firstFoundId = null;
		final AudioHardwareDevice phs = hardwareIOConfiguration.getProducerAudioDevice();
		if( phs != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = phs.getProviderId();
			}
		}
		final AudioHardwareDevice chs = hardwareIOConfiguration.getConsumerAudioDevice();
		if( chs != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = chs.getProviderId();
			}
			else if( !firstFoundId.equals( chs.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}
		final MidiHardwareDevice pmc = hardwareIOConfiguration.getProducerMidiDevice();
		if( pmc != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = pmc.getProviderId();
			}
			else if( !firstFoundId.equals( pmc.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}
		final MidiHardwareDevice cmc = hardwareIOConfiguration.getConsumerMidiDevice();
		if( cmc != null )
		{
			if( firstFoundId == null )
			{
				firstFoundId = cmc.getProviderId();
			}
			else if( !firstFoundId.equals( cmc.getProviderId() ) )
			{
				throw new DatastoreException("All hardware must come from the same provider.");
			}
		}

		if( phs == null && chs == null )
		{
			throw new DatastoreException("Either an input or output audio device must be used.");
		}

		// Now get the audio provider from the registry
		final AudioProvider provider = audioProviderRegistryService.getProviderById( firstFoundId );

		return provider.createAppRenderingSessionForConfiguration( hardwareIOConfiguration, errorQueue, errorCallback );
	}

	private void startupHotspot() throws DatastoreException, TableCellFullException, TableIndexOutOfBoundsException, MAConstraintViolationException, InterruptedException
	{
		// Now hotspot compile them in one big rack

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
		final RenderingPlan renderingPlan = renderingService.createRenderingPlan( hotspotGraph, hotspotDrc, hotspotFrameTimeFactory );

		// Now create a rendering plan from this rack
		log.debug("Peforming hotspot mad instance looping.");
		final HotspotRenderingContainer hotspotContainer = appRenderingStructureService.createHotspotRenderingContainer( renderingPlan );
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
