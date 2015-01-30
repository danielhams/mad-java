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

package uk.co.modularaudio.componentdesigner.controller.front.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.controller.front.RenderingStateListener;
import uk.co.modularaudio.componentdesigner.controller.guihelper.GuiHelperController;
import uk.co.modularaudio.controller.audioprovider.AudioProviderController;
import uk.co.modularaudio.controller.component.ComponentController;
import uk.co.modularaudio.controller.rack.RackController;
import uk.co.modularaudio.controller.rendering.RenderingController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.HotspotFrameTimeFactory;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.MTRenderingJobQueue;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingGraph;
import uk.co.modularaudio.service.audioproviderregistry.pub.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.pub.AppRenderingErrorQueue.AppRenderingErrorStruct;
import uk.co.modularaudio.service.audioproviderregistry.pub.AppRenderingErrorQueue.ErrorSeverity;
import uk.co.modularaudio.service.audioproviderregistry.pub.AppRenderingIO;
import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.valueobjects.UserPreferencesMVCView;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class ComponentDesignerFrontControllerImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, ComponentDesignerFrontController
{
	private static final long AUDIO_TEST_RUN_MILLIS = 4000;
	private static final int AUDIO_ENGINE_RESTART_PAUSE_MILLIS = 2000;

	private static Log log = LogFactory.getLog( ComponentDesignerFrontControllerImpl.class.getName() );

	private final static String CONFIG_KEY_FORCE_HOTSPOT_COMPILE = ComponentDesignerFrontControllerImpl.class.getSimpleName() + ".ForceHotspotCompile";
	private final static String CONFIG_KEY_RENDER_COMPONENT_IMAGES = ComponentDesignerFrontControllerImpl.class.getSimpleName() + ".RenderComponentImages";

	private static final long HOTSPOT_COMPILATION_TIME_MILLIS = 10000;
	private static final int HOTSPOT_SAMPLES_PER_RENDER_PERIOD = 512;

	private GuiHelperController guiHelperController;
	private RackController rackController;
	private ComponentController componentController;
	private RenderingController renderingController;
	private AudioProviderController audioProviderController;
	private UserPreferencesController userPreferencesController;
	private SampleCachingController sampleCachingController;
	private ConfigurationService configurationService;
	private BufferedImageAllocationService bufferedImageAllocationService;

	private TimingService timingService;

	private RackService rackService;

	private AppRenderingIO appRenderingIO;

	private RackDataModel userVisibleRack;

	private RackModelRenderingComponent guiRack;

	private boolean forceHotspotCompile;
	private boolean renderComponentImages;

	// Timer for driving the gui updates
	private GuiDrivingTimer guiDrivingTimer;
	private ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage;

	private boolean loggingEnabled = true;

	private boolean currentlyRendering;

	private String absolutePathToFilename;

	private final List<RenderingStateListener> renderingStateListeners = new ArrayList<RenderingStateListener>();

	private Priority previousLoggingThreshold;

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		final Map<String, String> errors = new HashMap<String, String>();
		forceHotspotCompile = ConfigurationServiceHelper.checkForBooleanKey(configurationService, CONFIG_KEY_FORCE_HOTSPOT_COMPILE, errors);
		renderComponentImages = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_RENDER_COMPONENT_IMAGES, errors );
		ConfigurationServiceHelper.errorCheck(errors);
		guiTemporaryEventStorage = new ThreadSpecificTemporaryEventStorage( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );
	}

	public void setRackController( final RackController rackController)
	{
		this.rackController = rackController;
	}

	public void setComponentController( final ComponentController componentController)
	{
		this.componentController = componentController;
	}

	public void setGuiHelperController( final GuiHelperController guiController)
	{
		this.guiHelperController = guiController;
	}

	public void setRenderingController( final RenderingController renderingController)
	{
		this.renderingController = renderingController;
	}

	public void setConfigurationService( final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public void setAudioProviderController( final AudioProviderController audioProviderController)
	{
		this.audioProviderController = audioProviderController;
	}

	public void setUserPreferencesController( final UserPreferencesController userPreferencesController)
	{
		this.userPreferencesController = userPreferencesController;
	}

	public void setSampleCachingController( final SampleCachingController sampleCachingController )
	{
		this.sampleCachingController = sampleCachingController;
	}

	public void setBufferedImageAllocationService( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	public void setTimingService( final TimingService timingService )
	{
		this.timingService = timingService;
	}

	public void setRackService( final RackService rackService )
	{
		this.rackService = rackService;
	}

	@Override
	public void dumpRack() throws DatastoreException
	{
		rackController.dumpRack( userVisibleRack );
		if( appRenderingIO != null )
		{
			appRenderingIO.getAppRenderingGraph().dumpRenderingPlan();
		}
		if( sampleCachingController != null )
		{
			sampleCachingController.dumpSampleCache();
		}
	}

	@Override
	public void dumpProfileResults() throws DatastoreException
	{
		if( appRenderingIO != null )
		{
			appRenderingIO.getAppRenderingGraph().dumpProfileResults();
		}
	}

	@Override
	public void toggleLogging()
	{
		loggingEnabled = (loggingEnabled ? false : true );
		final Logger rootLogger = LogManager.getRootLogger();
		final Appender appender = rootLogger.getAppender( "console" );
		if( appender instanceof ConsoleAppender )
		{
			final ConsoleAppender ca = (ConsoleAppender)appender;
			if( loggingEnabled )
			{
				if( previousLoggingThreshold != null )
				{
					ca.setThreshold( previousLoggingThreshold );
				}
				else
				{
					ca.setThreshold( Level.TRACE );
				}
			}
			else
			{
				previousLoggingThreshold  = ca.getThreshold();
				ca.setThreshold( Level.ERROR );
			}
		}
	}

	@Override
	public void toggleRendering()
	{
		try
		{
			if( currentlyRendering )
			{
				stopDisplayTick();
				appRenderingIO.getAppRenderingGraph().deactivateApplicationGraph();
			}
			else
			{
				appRenderingIO.getAppRenderingGraph().activateApplicationGraph();
				startDisplayTick();
			}
			currentlyRendering = !currentlyRendering;
			for( final RenderingStateListener l : renderingStateListeners )
			{
				l.receiveRenderingStateChange( currentlyRendering );
			}
		}
		catch(final Exception e)
		{
			final String msg = "Exception caught toggling rendering: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public boolean isRendering()
	{
		return currentlyRendering;
	}

	private void initialiseEmptyRack()
	{
		try
		{
			// Make sure we free up any resources consumed by components in the rack (IO)
			final RackDataModel previousRack = userVisibleRack;
			if( previousRack != null )
			{
				final MadGraphInstance<?,?> oldGraph = rackController.getRackGraphInstance( previousRack );
				if( appRenderingIO != null )
				{
					appRenderingIO.getAppRenderingGraph().unsetApplicationGraph( oldGraph );
				}
			}
			userVisibleRack = rackController.createNewRackDataModel( "Empty Application Rack",
					"",
					RackService.DEFAULT_RACK_COLS,
					RackService.DEFAULT_RACK_ROWS,
					true );
			guiRack.setRackDataModel( userVisibleRack );

			final MadGraphInstance<?,?> graphToRender = rackController.getRackGraphInstance( userVisibleRack );
			if( appRenderingIO != null )
			{
				appRenderingIO.getAppRenderingGraph().setApplicationGraph( graphToRender );
			}

			if( previousRack != null )
			{
				rackController.destroyRackDataModel( previousRack );
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught creating rack: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public void newRack() throws DatastoreException
	{
		// Initialise a new rack first, makes cleaning up the new one much quicker (no listeners)
		initialiseEmptyRack();
		absolutePathToFilename = null;
	}

	@Override
	public void loadRackFromFile(final String filename) throws DatastoreException, IOException
	{
		// Delete old rack contents to free up memory before we load a new one.
		newRack();

		absolutePathToFilename = filename;

		final RackDataModel newRack = rackController.loadRackFromFile(filename);
		if( newRack != null )
		{
			final RackDataModel oldModel = userVisibleRack;
			final MadGraphInstance<?,?> oldGraph = rackController.getRackGraphInstance( oldModel );
			if( appRenderingIO != null )
			{
				appRenderingIO.getAppRenderingGraph().unsetApplicationGraph( oldGraph );
			}
			userVisibleRack = newRack;
			guiRack.setRackDataModel( userVisibleRack );

			final MadGraphInstance<?,?> rackGraph = rackController.getRackGraphInstance( userVisibleRack );
			if( appRenderingIO != null )
			{
				appRenderingIO.getAppRenderingGraph().setApplicationGraph( rackGraph );
			}
			destroyExistingRack( oldModel );
		}
	}

	private void destroyExistingRack( final RackDataModel oldRackModel ) throws DatastoreException
	{
		if( oldRackModel != null )
		{
			try
			{
				rackController.destroyRackDataModel( oldRackModel );
			}
			catch( final Exception e )
			{
				final String msg = "Exception caught destroying rack: " + e.toString();
				throw new DatastoreException( msg, e );
			}
		}
	}

	@Override
	public void revertRack() throws DatastoreException, IOException
	{
		if( absolutePathToFilename != null && !absolutePathToFilename.equals("" ) )
		{
			loadRackFromFile( absolutePathToFilename );
		}
	}

	@Override
	public String getRackDataModelName()
	{
		return rackService.getRackName( userVisibleRack );
	}

	@Override
	public void saveRackToFile( final String filename, final String rackName ) throws DatastoreException, IOException
	{
		absolutePathToFilename = filename;
		rackService.setRackName( userVisibleRack, rackName );
		rackController.saveRackToFile( userVisibleRack, filename );
	}

	@Override
	public boolean isRackDirty()
	{
		return rackService.isRackDirty( userVisibleRack );
	}

	@Override
	public void saveRack() throws DatastoreException, FileNotFoundException, IOException
	{
		if( absolutePathToFilename == null )
		{
			throw new FileNotFoundException();
		}
		else
		{
			rackController.saveRackToFile( userVisibleRack, absolutePathToFilename );
			rackService.setRackDirty( userVisibleRack, false );
		}
	}

	@Override
	public void ensureRenderingStoppedBeforeExit() throws DatastoreException, MadProcessingException
	{
		if( isRendering() )
		{
			toggleRendering();
		}
	}

	@Override
	public void addRenderingStateListener(final RenderingStateListener renderingStateListener)
	{
		renderingStateListeners.add( renderingStateListener );
	}

	@Override
	public void removeRenderingStateListener(final RenderingStateListener renderingStateListener)
	{
		renderingStateListeners.remove( renderingStateListener );
	}

	@Override
	public void postInit() throws DatastoreException
	{
		// Create an empty rack during init
		final RackDataModel tmpRack = rackController.createNewRackDataModel( "Init rack",
				"",
				RackService.DEFAULT_RACK_COLS,
				RackService.DEFAULT_RACK_ROWS,
				true );
		guiRack = guiHelperController.createGuiForRackDataModel( tmpRack );
		doStartupDuties();
	}

	@Override
	public void preShutdown() throws DatastoreException
	{
		guiRack.destroy();
		doExitDuties();
	}

	protected void startDisplayTick()
	{
		final MadTimingSource timingSource = timingService.getTimingSource();
		final MadTimingParameters timingParameters = timingSource.getTimingParameters();

		final int millisBetweenFrames = (int)(timingParameters.getNanosPerFrontEndPeriod() / 1000000);
		if( guiDrivingTimer == null )
		{
			guiDrivingTimer = new GuiDrivingTimer( millisBetweenFrames, new GuiTickActionListener( this ) );
		}

		if( guiDrivingTimer != null && !guiDrivingTimer.isRunning() )
		{
			guiDrivingTimer.start();
			log.debug("GUITIMER Starting gui driving timer");
		}
		else
		{
			log.error("Unable to start display tick!");
		}
	}

	protected void stopDisplayTick()
	{
		if( guiDrivingTimer != null && guiDrivingTimer.isRunning() )
		{
			log.debug("GUITIMER Stopping gui driving timer");
			guiDrivingTimer.stop();
			guiDrivingTimer = null;
		}
		else
		{
			log.error("Unable to stop display tick!");
		}
	}

	private boolean frontPreviouslyShowing = true;

	@Override
	public void receiveDisplayTick()
	{
		final MadTimingParameters timingParameters = timingService.getTimingSource().getTimingParameters();

		final long currentGuiFrameTime = appRenderingIO.getCurrentUiFrameTime();
//		log.debug("Estimated GUI frame time is " + currentGuiFrameTime );

		final List<RackComponent> rackComponents = userVisibleRack.getEntriesAsList();

		boolean doAll = false;

		if( guiRack.isFrontShowing() && guiRack.getJComponent().isVisible() )
		{
			doAll = true;
			frontPreviouslyShowing = true;
		}
		else
		{
			if( frontPreviouslyShowing )
			{
				doAll = true;
			}
			else
			{
			}
			frontPreviouslyShowing = false;
		}

		for( int i = 0 ; i < rackComponents.size() ; i++)
		{
			final RackComponent rc = rackComponents.get( i );
			final AbstractMadUiInstance<?, ?> uiInstance = rc.getUiInstance();
			if( doAll || uiInstance instanceof SubRackMadUiInstance )
			{
//				log.debug("Calling rdt on " + uiInstance.getInstance().getInstanceName() );

				rc.receiveDisplayTick( guiTemporaryEventStorage, timingParameters, currentGuiFrameTime );
			}
		}
	}

	private boolean isDefinitionPublic( final MadDefinition<?,?> definition )
	{
		final MadClassification auc = definition.getClassification();
		final MadClassificationGroup aug = auc.getGroup();
		return ( aug != null ? aug.getVisibility() == Visibility.PUBLIC : false );
	}

	private void doStartupDuties() throws DatastoreException
	{
		if( forceHotspotCompile || renderComponentImages )
		{
			try
			{
				if( renderComponentImages )
				{
					paintOneRackPerComponent();
				}

				if( forceHotspotCompile )
				{
					// Now hotspot compile them in one big rack

					final MadDefinitionListModel allMadDefinitions = componentController.listDefinitionsAvailable();

					// Create a new rack with at least four rows per component type
					final int numDefinitions = allMadDefinitions.getSize();
					final RackDataModel cacheRack = rackController.createNewRackDataModel( "cachingrack",
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
									rackController.createComponent( cacheRack, md, emptyParameterValues, name );
								}
								catch(final Exception e )
								{
									if( log.isInfoEnabled() )
									{
										log.info("Skipping hotspot of " + md.getId() + " as it needs parameters and no default didn't work." );
									}
								}
							}
							else
							{
								try
								{
									final String name = md.getId() + i;
									rackController.createComponent( cacheRack, md, emptyParameterValues, name );
								}
								catch(final RecordNotFoundException rnfe )
								{
									if( log.isInfoEnabled() )
									{
										log.info( "Skipping hotspot of " + md.getId() + " - probably missing UI for it" );
									}
								}
							}
						}
					}

					final MadGraphInstance<?,?> hotspotGraph = rackController.getRackGraphInstance( cacheRack );

					final int outputLatencyFrames = HOTSPOT_SAMPLES_PER_RENDER_PERIOD;

					final HardwareIOOneChannelSetting hotspotCelc= new HardwareIOOneChannelSetting( DataRate.SR_44100,
							outputLatencyFrames );

					final long outputLatencyNanos = AudioTimingUtils.getNumNanosecondsForBufferLength(DataRate.SR_44100.getValue(),
							outputLatencyFrames );

					final HardwareIOChannelSettings hotspotDrc = new HardwareIOChannelSettings( hotspotCelc, outputLatencyNanos, outputLatencyFrames );
					final MadFrameTimeFactory hotspotFrameTimeFactory = new HotspotFrameTimeFactory();
					final RenderingPlan renderingPlan = renderingController.createRenderingPlan( hotspotGraph, hotspotDrc, hotspotFrameTimeFactory );

					// Now create a rendering plan from this rack
					log.debug("Peforming hotspot mad instance looping.");
					final AppRenderingGraph hotspotAppGraph = renderingController.createAppRenderingGraph();
					hotspotAppGraph.startHotspotLooping( renderingPlan );
					Thread.sleep( HOTSPOT_COMPILATION_TIME_MILLIS );
					hotspotAppGraph.stopHotspotLooping();

					rackController.destroyRackDataModel( cacheRack );
				}
			}
			catch( final Exception e )
			{
				final String msg = "Exception caught forcing hotspot compilation: " + e.toString();
				log.error( msg, e );
				throw new DatastoreException( msg, e );
			}
		}

		initialiseEmptyRack();
	}

	private void paintOneRackPerComponent()
		throws DatastoreException, ContentsAlreadyAddedException, TableCellFullException,
			TableIndexOutOfBoundsException, MAConstraintViolationException, RecordNotFoundException
	{
		final MadDefinitionListModel defs = componentController.listDefinitionsAvailable();

		final Map<MadParameterDefinition,String> emptyParameterValues = new HashMap<MadParameterDefinition, String>();

		final int numDefs = defs.getSize();
		for( int i = 0 ; i < numDefs ; ++i )
		{
			final MadDefinition<?,?> def = defs.getElementAt( i );
			if( isDefinitionPublic( def ) )
			{
				final Span curComponentCellSpan = componentController.getUiSpanForDefinition( def );

				// If it's the channel 8 mixer, paint the rack master with it
				final boolean paintRackMasterToo = ( def.getId().equals( MixerMadDefinition.DEFINITION_ID ) );
				final int rackWidthToUse = ( paintRackMasterToo ? RackService.DEFAULT_RACK_COLS : curComponentCellSpan.x );
				final int rackHeightToUse = ( paintRackMasterToo ? RackService.DEFAULT_RACK_ROWS : curComponentCellSpan.y + 2 );

				final RackDataModel cacheRack = rackController.createNewRackDataModel( "cachingrack",
						"",
						rackWidthToUse,
						rackHeightToUse,
						paintRackMasterToo );

				final String name = def.getId() + i;
				rackController.createComponent( cacheRack, def, emptyParameterValues, name );

				forceHotspotRackPainting( cacheRack, def.getId() );

				rackController.destroyRackDataModel( cacheRack );
			}
		}
	}

	private void doExitDuties()
	{
		log.debug( "Unsetting application graph" );
		try
		{
			if( appRenderingIO != null )
			{
				final AppRenderingGraph appRenderingGraph = appRenderingIO.getAppRenderingGraph();

				if( appRenderingGraph.isApplicationGraphActive() )
				{
					log.debug( "Will first deactivate the application graph");
					appRenderingGraph.deactivateApplicationGraph();
				}

				if( appRenderingGraph.isApplicationGraphSet() )
				{
					final MadGraphInstance<?,?> graphToUnset = rackController.getRackGraphInstance( userVisibleRack );
					appRenderingGraph.unsetApplicationGraph( graphToUnset );
				}
			}

			destroyExistingRack( userVisibleRack );
		}
		catch ( final Exception e)
		{
			final String msg = "Exception caught unsetting application graph: " + e.toString();
			log.error( msg, e );
		}
	}

	private void forceHotspotRackPainting( final RackDataModel cacheRack, final String componentNameBeingDrawn )
			throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Performing rack painting for " + componentNameBeingDrawn);
		}
		// Now create a temporary gui for it and get them to render their front and back into it
		// This will populate the image buffer cache and clear up the stuff read from disk
		RackModelRenderingComponent hotspotRenderingComponent = guiHelperController.createGuiForRackDataModel( cacheRack );
		hotspotRenderingComponent.setForceRepaints( true );

		final JComponent renderingJComponent = hotspotRenderingComponent.getJComponent();
		final Dimension renderingPreferredSize = renderingJComponent.getPreferredSize();
		renderingJComponent.setSize( renderingPreferredSize );
		layoutComponent( renderingJComponent );

		// Paint the front
		final AllocationMatch localAllocationMatch = new AllocationMatch();
		final TiledBufferedImage hotspotPaintTiledImage = bufferedImageAllocationService.allocateBufferedImage( this.getClass().getSimpleName(),
				localAllocationMatch,
				AllocationLifetime.SHORT,
				AllocationBufferType.TYPE_INT_RGB,
				renderingPreferredSize.width,
				renderingPreferredSize.height );
		final BufferedImage imageToRenderInto = hotspotPaintTiledImage.getUnderlyingBufferedImage();
		final Graphics hotspotPaintGraphics = imageToRenderInto.createGraphics();
		final CellRendererPane crp = new CellRendererPane();
		crp.add( renderingJComponent );
		crp.paintComponent( hotspotPaintGraphics, renderingJComponent, crp, 0, 0, renderingPreferredSize.width, renderingPreferredSize.height, true );

		// And now the back
		hotspotRenderingComponent.rotateRack();
		crp.paintComponent( hotspotPaintGraphics, renderingJComponent, crp, 0, 0, renderingPreferredSize.width, renderingPreferredSize.height, true );

		final RackDataModel emptyRack = rackController.createNewRackDataModel( "", "", 2, 2, false );

		// Remove references to the data model passed in
		hotspotRenderingComponent.setRackDataModel( emptyRack );

		// And clear it up
		hotspotRenderingComponent.destroy();
		hotspotRenderingComponent = null;

		bufferedImageAllocationService.freeBufferedImage( hotspotPaintTiledImage );

	}

	private void layoutComponent( final Component renderingJComponent )
	{
        synchronized (renderingJComponent.getTreeLock()) {
        	renderingJComponent.doLayout();
            if (renderingJComponent instanceof Container)
                for (final Component child : ((Container) renderingJComponent).getComponents())
                    layoutComponent(child);
        }
	}

	@Override
	public UserPreferencesMVCView getUserPreferencesMVCView() throws DatastoreException
	{
		return userPreferencesController.getUserPreferencesMVCView();
	}

	@Override
	public void applyUserPreferencesChanges()
	{
		try
		{
			userPreferencesController.applyUserPreferencesChanges();

			if( isAudioEngineRunning() )
			{
				// And reset the audio IO too
				stopAudioEngine();
				Thread.sleep( AUDIO_ENGINE_RESTART_PAUSE_MILLIS );
			}
			startAudioEngine();
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught saving user preferences: " + e.toString();
			log.error( msg, e );
		}
	}

	@Override
	public boolean testUserPreferencesChanges()
	{
		try
		{
			final boolean retVal = callCheckOrStartAudioEngine( false );

			return retVal;
		}
		catch(final DatastoreException de)
		{
			final String msg = "DatastoreException caught testing user preferences changes: " + de.toString();
			log.error( msg, de );
			return false;
		}
	}

	@Override
	public void cancelUserPreferencesChanges()
	{
		userPreferencesController.cancelUserPreferencesChanges();
	}

	@Override
	public void reloadUserPreferences()
	{
		try
		{
			userPreferencesController.reloadUserPreferences();
		}
		catch( final DatastoreException de )
		{
			if( log.isErrorEnabled() )
			{
				log.error( "DatastoreException caught reloading user preferences: " + de.toString(), de );
			}
		}
	}

	@Override
	public boolean startAudioEngine()
	{
		try
		{
			return( callCheckOrStartAudioEngine( true ) );
		}
		catch( final Exception de )
		{
			final String msg = "DatatoreException caught starting audio engine: " + de.toString();
			log.error( msg, de );
			return false;
		}
	}

	@Override
	public boolean isAudioEngineRunning()
	{
		return (appRenderingIO == null ? false : appRenderingIO.isRendering() );
	}

	@Override
	public void stopAudioEngine()
	{
		if( appRenderingIO != null )
		{
			try
			{
				final AppRenderingGraph appRenderingGraph = appRenderingIO.getAppRenderingGraph();
				if( appRenderingGraph.isApplicationGraphActive() )
				{
					appRenderingGraph.deactivateApplicationGraph();
				}
				final MadGraphInstance<?,?> rgi = rackService.getRackGraphInstance( userVisibleRack );
				appRenderingGraph.unsetApplicationGraph( rgi );
				appRenderingIO.stopRendering();
				appRenderingIO.destroy();
				appRenderingIO = null;
			}
			catch( final Exception e )
			{
				final String msg = "Exception caught stopping audio engine: " + e.toString();
				log.error( msg, e );
			}
		}
	}

	private boolean callCheckOrStartAudioEngine( final boolean isStart )
			throws DatastoreException
	{
		boolean retVal = false;

		final UserPreferencesMVCController userPreferencesMVCController = userPreferencesController.getUserPreferencesMVCController();
		final UserPreferencesMVCModel prefsModel = userPreferencesMVCController.getModel();

		final HardwareIOConfiguration hardwareIOConfiguration = PrefsModelToHardwareIOConfigurationBridge.modelToConfiguration( prefsModel );

		final AppRenderingErrorCallback errorCallback = new AppRenderingErrorCallback()
		{

			@Override
			public void errorCallback( final AppRenderingErrorStruct error )
			{
//				log.error( "AppRenderingErrorCallback happened: " + error.severity.toString() + " " + error.msg );

				if( error.severity == ErrorSeverity.FATAL )
				{
					if( isRendering() )
					{
						toggleRendering();
					}
					stopAudioEngine();
				}
				else
				{
					log.warn(error.msg);
				}
			}

			@Override
			public String getName()
			{
				return "ApplicationAppRenderingErrorCallback(Anonymous)";
			}
		};

		try
		{
			if( isStart )
			{
				if( appRenderingIO != null )
				{
					throw new DatastoreException( "Attempting to replace magical audio IO when one already exists!");
				}
				appRenderingIO = audioProviderController.createAppRenderingIOForConfiguration( hardwareIOConfiguration, errorCallback );
				final MadGraphInstance<?,?> rgi = rackService.getRackGraphInstance( userVisibleRack );
				appRenderingIO.getAppRenderingGraph().setApplicationGraph( rgi );

				appRenderingIO.startRendering();
				retVal = true;
			}
			else
			{
				// is a config test
				boolean wasRunningBeforeTest = false;
				if( appRenderingIO != null && appRenderingIO.isRendering())
				{
					wasRunningBeforeTest = true;
					appRenderingIO.stopRendering();
					appRenderingIO.destroy();
					appRenderingIO = null;
					try
					{
						Thread.currentThread();
						Thread.sleep( AUDIO_ENGINE_RESTART_PAUSE_MILLIS );
					}
					catch (final InterruptedException e)
					{
						log.error( e );
					}
				}

				final AppRenderingIO testAppRenderingIO = audioProviderController.createAppRenderingIOForConfiguration( hardwareIOConfiguration, errorCallback );
				retVal = testAppRenderingIO.testRendering( AUDIO_TEST_RUN_MILLIS );

				if( wasRunningBeforeTest && !retVal)
				{
					try
					{
						Thread.sleep( AUDIO_ENGINE_RESTART_PAUSE_MILLIS );
					}
					catch (final InterruptedException e)
					{
						log.error( e );
					}
					callCheckOrStartAudioEngine( true );
				}
			}
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught calling check or start audio engine: " + e.toString();
			log.error( msg, e );
			if( appRenderingIO != null )
			{
				appRenderingIO.stopRendering();
				appRenderingIO.destroy();
				appRenderingIO = null;
			}
		}
		return retVal;
	}

	@Override
	public RackModelRenderingComponent getGuiRack()
	{
		return guiRack;
	}

	@Override
	public void registerRackTabbedPane( final GuiTabbedPane rackTabbedPane )
	{
		guiHelperController.registerRackTabbedPane( rackTabbedPane );
	}

	@Override
	public void showYesNoQuestionDialog( final Component parentComponent,
			final String message,
			final String title,
			final int messageType,
			final String[] options,
			final String defaultChoice,
			final YesNoQuestionDialogCallback callback )
	{
		guiHelperController.showYesNoQuestionDialog( parentComponent, message, title, messageType,
				options, defaultChoice, callback );
	}

	@Override
	public void showTextInputDialog( final Component parentComponent, final String message,
			final String title, final int messageType, final String initialValue,
			final TextInputDialogCallback callback )
	{
		guiHelperController.showTextInputDialog( parentComponent, message, title,
				messageType, initialValue, callback );
	}

	@Override
	public void showMessageDialog( final Component parentComponent, final String message,
			final String title,
			final int messageType,
			final MessageDialogCallback callback )
	{
		guiHelperController.showMessageDialog( parentComponent, message, title, messageType,
				callback );
	}

	@Override
	public RackDataModel getUserRack()
	{
		return userVisibleRack;
	}

}
