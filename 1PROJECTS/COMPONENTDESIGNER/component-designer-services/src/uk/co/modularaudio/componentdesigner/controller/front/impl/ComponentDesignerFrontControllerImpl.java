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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.controller.front.RenderingStateListener;
import uk.co.modularaudio.componentdesigner.controller.gui.GuiController;
import uk.co.modularaudio.controller.apprendering.AppRenderingController;
import uk.co.modularaudio.controller.rack.RackController;
import uk.co.modularaudio.controller.samplecaching.SampleCachingController;
import uk.co.modularaudio.controller.userpreferences.UserPreferencesController;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.apprendering.util.AppRenderingSession;
import uk.co.modularaudio.service.apprendering.util.jobqueue.MTRenderingJobQueue;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorCallback;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue.AppRenderingErrorStruct;
import uk.co.modularaudio.service.audioproviderregistry.AppRenderingErrorQueue.ErrorSeverity;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.gui.UserPreferencesMVCView;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.renderingplan.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCController;
import uk.co.modularaudio.service.userpreferences.mvc.UserPreferencesMVCModel;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingSource;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dialog.message.MessageDialogCallback;
import uk.co.modularaudio.util.swing.dialog.textinput.TextInputDialogCallback;
import uk.co.modularaudio.util.swing.dialog.yesnoquestion.YesNoQuestionDialogCallback;

public class ComponentDesignerFrontControllerImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, ComponentDesignerFrontController
{
	private static final long AUDIO_TEST_RUN_MILLIS = 4000;
	private static final int AUDIO_ENGINE_RESTART_PAUSE_MILLIS = 2000;

	private static Log log = LogFactory.getLog( ComponentDesignerFrontControllerImpl.class.getName() );

	private final static String CONFIG_KEY_LOG_ROOTS = ComponentDesignerFrontControllerImpl.class.getSimpleName() +
			".LoggingRoots";

	private GuiController guiController;
	private RackController rackController;
	private AppRenderingController appRenderingController;
	private UserPreferencesController userPreferencesController;
	private SampleCachingController sampleCachingController;
	private ConfigurationService configurationService;

	// TODO: Known violations of the component hierarchy
	private TimingService timingService;
	private RackService rackService;

	private AppRenderingSession appRenderingSession;

	private RackDataModel userVisibleRack;

	private RackModelRenderingComponent guiRack;

	// Timer for driving the gui updates
	private GuiDrivingTimer guiDrivingTimer;
	private ThreadSpecificTemporaryEventStorage guiTemporaryEventStorage;

	private final HashSet<String> loggingRoots = new HashSet<String>();
	private boolean loggingEnabled = true;

	private boolean currentlyRendering;

	private String absolutePathToFilename;

	private final List<RenderingStateListener> renderingStateListeners = new ArrayList<RenderingStateListener>();

	private Level previousLoggingLevel;
	private final Map<String, Level> previousLoggerLevels = new HashMap<String, Level>();

	private boolean frontPreviouslyShowing = true;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( guiController == null ||
				rackController == null ||
				appRenderingController == null ||
				userPreferencesController == null ||
				sampleCachingController == null ||
				configurationService == null ||
				timingService == null ||
				rackService == null )
		{
			throw new ComponentConfigurationException( "Front controller missing dependencies. Please check configuration" );
		}

		guiTemporaryEventStorage = new ThreadSpecificTemporaryEventStorage( MTRenderingJobQueue.RENDERING_JOB_QUEUE_CAPACITY );

		final Map<String,String> errors = new HashMap<String,String>();
		final String[] loggingRootsArray = ConfigurationServiceHelper.checkForCommaSeparatedStringValues( configurationService, CONFIG_KEY_LOG_ROOTS, errors );
		ConfigurationServiceHelper.errorCheck( errors );

		for( final String lr : loggingRootsArray )
		{
			loggingRoots.add( lr );
		}
	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		try
		{
			// Create an empty rack during init
			final RackDataModel tmpRack = rackController.createNewRackDataModel( "Init rack",
					"",
					RackService.DEFAULT_RACK_COLS,
					RackService.DEFAULT_RACK_ROWS,
					true );
			guiRack = guiController.createGuiForRackDataModel( tmpRack );
			initialiseEmptyRack();
		}
		catch( final DatastoreException de )
		{
			throw new ComponentConfigurationException( de );
		}
	}

	@Override
	public void preShutdown()
	{
		guiRack.destroy();
		stopRenderingCleanupGraph();
	}

	@Override
	public void destroy()
	{
	}

	public void setRackController( final RackController rackController)
	{
		this.rackController = rackController;
	}

	public void setGuiController( final GuiController guiController)
	{
		this.guiController = guiController;
	}

	public void setAppRenderingController( final AppRenderingController appRenderingController)
	{
		this.appRenderingController = appRenderingController;
	}

	public void setUserPreferencesController( final UserPreferencesController userPreferencesController)
	{
		this.userPreferencesController = userPreferencesController;
	}

	public void setSampleCachingController( final SampleCachingController sampleCachingController )
	{
		this.sampleCachingController = sampleCachingController;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
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
		if( appRenderingSession != null )
		{
			appRenderingSession.dumpRenderingPlan();
		}
	}

	@Override
	public void dumpSampleCache() throws DatastoreException
	{
		if( sampleCachingController != null )
		{
			sampleCachingController.dumpSampleCache();
		}
	}

	@Override
	public void dumpProfileResults() throws DatastoreException
	{
		if( appRenderingSession != null )
		{
			appRenderingSession.dumpProfileResults();
		}
	}

	@Override
	public RenderingPlanProfileResults getProfileResults() throws DatastoreException
	{
		return appRenderingSession.getProfileResults();
	}

	@Override
	public void toggleLogging()
	{
		loggingEnabled = (loggingEnabled ? false : true );
		final LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig( LogManager.ROOT_LOGGER_NAME );
		final Map<String, LoggerConfig> loggers = config.getLoggers();

		if( previousLoggingLevel == null )
		{
			previousLoggingLevel = loggerConfig.getLevel();
			loggerConfig.setLevel( Level.TRACE );
			for( final Map.Entry<String, LoggerConfig> logger : loggers.entrySet() )
			{
				final String loggerName = logger.getKey();
				if( loggingRoots.contains( loggerName ) )
				{
					final LoggerConfig lc = logger.getValue();
					previousLoggerLevels.put( loggerName, lc.getLevel() );
					lc.setLevel( Level.TRACE );
				}
			}
		}
		else
		{
			loggerConfig.setLevel( previousLoggingLevel );
			previousLoggingLevel = null;
			for( final Map.Entry<String, LoggerConfig> logger : loggers.entrySet() )
			{
				final String loggerName = logger.getKey();
				if( loggingRoots.contains( loggerName ) )
				{
					final LoggerConfig lc = logger.getValue();
					final Level oldLevel = previousLoggerLevels.get( loggerName );
					lc.setLevel( oldLevel );
				}
			}
			previousLoggerLevels.clear();
		}
		ctx.updateLoggers();
	}

	@Override
	public void toggleRendering()
	{
		try
		{
			if( currentlyRendering )
			{
				stopDisplayTick();
				appRenderingSession.deactivateApplicationGraph();
			}
			else
			{
				appRenderingSession.activateApplicationGraph();
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
				if( appRenderingSession != null )
				{
					appRenderingSession.unsetApplicationGraph( oldGraph );
				}
			}
			userVisibleRack = rackController.createNewRackDataModel( "Empty Application Rack",
					"",
					RackService.DEFAULT_RACK_COLS,
					RackService.DEFAULT_RACK_ROWS,
					true );
			guiRack.setRackDataModel( userVisibleRack );

			final MadGraphInstance<?,?> graphToRender = rackController.getRackGraphInstance( userVisibleRack );
			if( appRenderingSession != null )
			{
				appRenderingSession.setApplicationGraph( graphToRender );
			}

			if( previousRack != null )
			{
				log.trace("Beginning clean up of previous rack");
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

		final RackDataModel newRack = rackController.loadBaseRackFromFile(filename);
		if( newRack != null )
		{
			final RackDataModel oldModel = userVisibleRack;
			final MadGraphInstance<?,?> oldGraph = rackController.getRackGraphInstance( oldModel );
			if( appRenderingSession != null )
			{
				appRenderingSession.unsetApplicationGraph( oldGraph );
			}
			userVisibleRack = newRack;
			guiRack.setRackDataModel( userVisibleRack );

			final MadGraphInstance<?,?> rackGraph = rackController.getRackGraphInstance( userVisibleRack );
			if( appRenderingSession != null )
			{
				appRenderingSession.setApplicationGraph( rackGraph );
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
		rackController.saveBaseRackToFile( userVisibleRack, filename );
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
			rackController.saveBaseRackToFile( userVisibleRack, absolutePathToFilename );
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

	private void startDisplayTick()
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

	private void stopDisplayTick()
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

	@Override
	public void receiveDisplayTick()
	{
		final MadTimingParameters timingParameters = timingService.getTimingSource().getTimingParameters();

		final long currentGuiFrameTime = appRenderingSession.getCurrentUiFrameTime();
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
			final IMadUiInstance<?, ?> uiInstance = rc.getUiInstance();
			if( doAll || uiInstance instanceof SubRackMadUiInstance )
			{
//				log.debug("Calling rdt on " + uiInstance.getInstance().getInstanceName() );

				rc.receiveDisplayTick( guiTemporaryEventStorage, timingParameters, currentGuiFrameTime );
			}
		}
	}

	private void stopRenderingCleanupGraph()
	{
		log.debug( "Unsetting application graph" );
		try
		{
			if( appRenderingSession != null )
			{
				if( appRenderingSession.isApplicationGraphActive() )
				{
					log.debug( "Will first deactivate the application graph");
					appRenderingSession.deactivateApplicationGraph();
				}

				if( appRenderingSession.isApplicationGraphSet() )
				{
					final MadGraphInstance<?,?> graphToUnset = rackController.getRackGraphInstance( userVisibleRack );
					appRenderingSession.unsetApplicationGraph( graphToUnset );
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
			final boolean hadAudioEngineDifferences = userPreferencesController.checkForAudioEnginePrefsChanges();

			userPreferencesController.applyUserPreferencesChanges();

			if( hadAudioEngineDifferences && isAudioEngineRunning() )
			{
				// And reset the audio IO too
				stopAudioEngine();
				Thread.sleep( AUDIO_ENGINE_RESTART_PAUSE_MILLIS );
			}
			if( !isAudioEngineRunning() )
			{
				startAudioEngine();
			}
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
			final boolean hadAudioEngineDifferences = userPreferencesController.checkForAudioEnginePrefsChanges();

			if( hadAudioEngineDifferences )
			{
				final boolean retVal = callCheckOrStartAudioEngine( false );

				return retVal;
			}
			else
			{
				return true;
			}
		}
		catch(final DatastoreException de)
		{
			final String msg = "DatastoreException caught testing user preferences changes: " + de.toString();
			log.error( msg, de );
			return false;
		}
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
		return (appRenderingSession == null ? false : appRenderingSession.isRendering() );
	}

	@Override
	public void stopAudioEngine()
	{
		if( appRenderingSession != null )
		{
			try
			{
				if( appRenderingSession.isApplicationGraphActive() )
				{
					appRenderingSession.deactivateApplicationGraph();
				}
				final MadGraphInstance<?,?> rgi = rackService.getRackGraphInstance( userVisibleRack );
				appRenderingSession.unsetApplicationGraph( rgi );
				appRenderingSession.stopRendering();
				appRenderingSession.destroy();
				appRenderingSession = null;
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
				if( appRenderingSession != null )
				{
					throw new DatastoreException( "Attempting to replace magical audio IO when one already exists!");
				}
				appRenderingSession = appRenderingController.createAppRenderingSessionForConfiguration( hardwareIOConfiguration, errorCallback );
				final MadGraphInstance<?,?> rgi = rackService.getRackGraphInstance( userVisibleRack );
				appRenderingSession.setApplicationGraph( rgi );

				appRenderingSession.startRendering();
				retVal = true;
			}
			else
			{
				// is a config test
				boolean wasRunningBeforeTest = false;
				if( appRenderingSession != null && appRenderingSession.isRendering())
				{
					wasRunningBeforeTest = true;
					appRenderingSession.stopRendering();
					appRenderingSession.destroy();
					appRenderingSession = null;
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

				final AppRenderingSession testAppRenderingSession = appRenderingController.createAppRenderingSessionForConfiguration( hardwareIOConfiguration, errorCallback );
				retVal = testAppRenderingSession.testRendering( AUDIO_TEST_RUN_MILLIS );

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
			if( appRenderingSession != null )
			{
				appRenderingSession.stopRendering();
				appRenderingSession.destroy();
				appRenderingSession = null;
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
		guiController.registerRackTabbedPane( rackTabbedPane );
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
		guiController.showYesNoQuestionDialog( parentComponent, message, title, messageType,
				options, defaultChoice, callback );
	}

	@Override
	public void showTextInputDialog( final Component parentComponent, final String message,
			final String title, final int messageType, final String initialValue,
			final TextInputDialogCallback callback )
	{
		guiController.showTextInputDialog( parentComponent, message, title,
				messageType, initialValue, callback );
	}

	@Override
	public void showMessageDialog( final Component parentComponent, final String message,
			final String title,
			final int messageType,
			final MessageDialogCallback callback )
	{
		guiController.showMessageDialog( parentComponent, message, title, messageType,
				callback );
	}

	@Override
	public RackDataModel getUserRack()
	{
		return userVisibleRack;
	}
}
